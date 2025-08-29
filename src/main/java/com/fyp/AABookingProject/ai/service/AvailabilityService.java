package com.fyp.AABookingProject.ai.service;

import com.fyp.AABookingProject.ai.model.FreeSlot;
import com.fyp.AABookingProject.ai.model.TimeInterval;
import com.fyp.AABookingProject.appointment.model.EditAppointmentRequest;
import com.fyp.AABookingProject.core.entity.*;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.appointment.repository.AppointmentRepository;
import com.fyp.AABookingProject.core.entity.Appointment;
import com.fyp.AABookingProject.core.enumClass.AppointmentStatus;
import com.fyp.AABookingProject.ocr.repository.TimetableEntryRepository;
import com.fyp.AABookingProject.ocr.repository.TimetableRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {
    private static final int SLOT_MINUTES = 30;
    private static final LocalTime WORK_START = LocalTime.of(8, 0);
    private static final LocalTime WORK_END   = LocalTime.of(18, 0);
    private static final List<String> DAYS = List.of("Mon","Tue","Wed","Thu","Fri"); // 添加周末则扩展

    private final UserRepository userRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(TimetableRepository timetableRepository,
                               TimetableEntryRepository timetableEntryRepository,
                               UserRepository userRepository,
                               AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.timetableRepository = timetableRepository;
        this.timetableEntryRepository = timetableEntryRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * 调用入口：
     * 1) 显式给 studentUserId & lecturerUserId
     * 2) 为空时，如果当前登录用户是学生，则用其 advisor
     * topN <=0 返回全部。
     */
    public List<FreeSlot> recommend() {
        UserDetails ud = getUserDetails();
        User current = userRepository.findByUsername(ud.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (current.getStudent() == null || current.getStudent().getAdvisor() == null) {
            return List.of();
        }
        Long studentUserId = current.getId();
        Long advisorUserId = current.getStudent().getAdvisor().getUser().getId(); // 用于课表(基于 user)
        Long advisorId = current.getStudent().getAdvisor().getId();               // 用于预约(基于 advisor)

        Map<String, List<TimeInterval>> teacherBusy = loadBusyByUserLatestTimetable(advisorUserId);
        Map<String, List<TimeInterval>> studentBusy = loadBusyByUserLatestTimetable(studentUserId);
        if (teacherBusy.isEmpty() || studentBusy.isEmpty()) return List.of();

        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.minusDays((today.getDayOfWeek().getValue()+6)%7);
        LocalDate endDate = thisMonday.plusWeeks(2).plusDays(4); // 第二周 Friday
        List<Appointment> advisorAppointments =
                appointmentRepository.findByAdvisorIdAndDateBetween(advisorId, thisMonday, endDate);

        List<FreeSlot> result = new ArrayList<>();
        for (int week = 0; week < 2; week++) {
            for (int dayIdx = 0; dayIdx < DAYS.size(); dayIdx++) {
                String day = DAYS.get(dayIdx);        // Mon..Fri
                LocalDate concreteDate = thisMonday.plusWeeks(week).plusDays(dayIdx);
                var tBusy = mergeIntervals(teacherBusy.getOrDefault(day, List.of()));
                var sBusy = mergeIntervals(studentBusy.getOrDefault(day, List.of()));
                var mutual = intersectFree(invertToFree(tBusy), invertToFree(sBusy));
                for (TimeInterval iv : mutual) {
                    for (FreeSlot fs : sliceIntoSlots(day, concreteDate, iv)) {
                        LocalTime slotStart = LocalTime.parse(fs.getStartTime());
                        LocalTime slotEnd   = LocalTime.parse(fs.getEndTime());
                        if (!isBooked(advisorAppointments, fs.getDate(), slotStart, slotEnd)) {
                            result.add(fs);
                        }
                    }
                }
            }
        }
        return result.stream()
                .sorted(Comparator.comparing((FreeSlot fs)->fs.getDate())
                        .thenComparing(FreeSlot::getStartTime))
                .toList();
    }

    // 导师查看未来两周(工作日)可用时段：
    // 1) 不带参数 -> 仅基于导师课表 & 预约
    // 2) 携带 EditAppointmentRequest(appointmentId) -> 针对某预约重排：
    //    - 若找到该预约且属于当前导师：
    //       a) 加入该学生课表交集（只推荐双方都空闲的槽）
    //       b) 允许当前已占用的原预约时间槽继续显示（即便已被占）
    public List<FreeSlot> recommendForAdvisor(EditAppointmentRequest editAppointmentRequest) {
        UserDetails ud = getUserDetails();
        User current = userRepository.findByUsername(ud.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (current.getAdvisor() == null) {
            return List.of();
        }
        Long advisorUserId = current.getId();
        Long advisorId = current.getAdvisor().getId();

        Appointment editingAppointment = null;
        if (editAppointmentRequest != null && editAppointmentRequest.getAppointmentId() != null) {
            editingAppointment = appointmentRepository.findById(editAppointmentRequest.getAppointmentId())
                    .filter(a -> Objects.equals(a.getAdvisorId(), advisorId))
                    .orElse(null);
            if (editingAppointment != null) {
                // 由 studentId -> student.user.id (需要简单查找：此处为了避免额外 repository，可在后续优化)
                // 简化：只做双方交集需要学生课表；如果没有学生信息则退化为单导师空闲
                // 这里 studentId 存的是 Student 实体主键，需要通过用户反向查。为了减少复杂度，这里不再做转换，除非后续需要显示学生交集。
            }
        }

        Map<String, List<TimeInterval>> advisorBusy = loadBusyByUserLatestTimetable(advisorUserId);
        if (advisorBusy.isEmpty()) return List.of();
        Map<String, List<TimeInterval>> studentBusy = Collections.emptyMap();
        boolean doStudentIntersect = false;
        if (editingAppointment != null) {
            // 查询预约对应学生的 User（需遍历? 为简单起见，此处仅在当前用户缓存中无法直接取到，暂忽略交集，可扩展：注入 StudentRepository）
            // 如果你希望真正做交集，需要提供 StudentRepository 以 studentId -> student.user.id.
            // 先保留逻辑开关 false，方便后续扩展。
            doStudentIntersect = false; // 设置为 true 后需要真正拿到 studentBusy
        }

        LocalDate today = LocalDate.now();
        LocalDate thisMonday = today.minusDays((today.getDayOfWeek().getValue()+6)%7);
        LocalDate endDate = thisMonday.plusWeeks(2).plusDays(4);
        List<Appointment> advisorAppointments =
                appointmentRepository.findByAdvisorIdAndDateBetween(advisorId, thisMonday, endDate);

        // 如果是编辑模式，允许原 slot 被视为“可用”
        LocalDate keepDate = null; LocalTime keepStart = null; LocalTime keepEnd = null;
        if (editingAppointment != null) {
            keepDate = editingAppointment.getDate();
            keepStart = editingAppointment.getStartTime();
            keepEnd = editingAppointment.getEndTime();
        }

        List<FreeSlot> result = new ArrayList<>();
        for (int week = 0; week < 2; week++) {
            for (int dayIdx = 0; dayIdx < DAYS.size(); dayIdx++) {
                String day = DAYS.get(dayIdx);
                LocalDate concreteDate = thisMonday.plusWeeks(week).plusDays(dayIdx);
                var aBusy = mergeIntervals(advisorBusy.getOrDefault(day, List.of()));
                List<TimeInterval> freeIntervals;
                if (doStudentIntersect) {
                    var sBusy = mergeIntervals(studentBusy.getOrDefault(day, List.of()));
                    var mutual = intersectFree(invertToFree(aBusy), invertToFree(sBusy));
                    freeIntervals = mutual;
                } else {
                    freeIntervals = invertToFree(aBusy);
                }
                for (TimeInterval iv : freeIntervals) {
                    for (FreeSlot fs : sliceIntoSlots(day, concreteDate, iv)) {
                        LocalTime slotStart = LocalTime.parse(fs.getStartTime());
                        LocalTime slotEnd   = LocalTime.parse(fs.getEndTime());
                        boolean isOriginal = keepDate != null && fs.getDate().equals(keepDate)
                                && !slotStart.isBefore(keepStart) && !slotEnd.isAfter(keepEnd);
                        if (isOriginal || !isBooked(advisorAppointments, fs.getDate(), slotStart, slotEnd)) {
                            result.add(fs);
                        }
                    }
                }
            }
        }
        return result.stream()
                .sorted(Comparator.comparing((FreeSlot fs)->fs.getDate())
                        .thenComparing(FreeSlot::getStartTime))
                .toList();
    }

    private boolean isBooked(List<Appointment> list, java.time.LocalDate date, java.time.LocalTime s, java.time.LocalTime e) {
    return list.stream()
        .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.REJECTED)
                .filter(a -> a.getDate().equals(date))
                .anyMatch(a -> a.getStartTime().isBefore(e) && s.isBefore(a.getEndTime()));
    }


    private Map<String, List<TimeInterval>> loadBusyByUserLatestTimetable(Long userId) {
        Map<String, List<TimeInterval>> map = new HashMap<>();
        var latestOpt = timetableRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
        if (latestOpt.isEmpty()) return map;
        Long timetableId = latestOpt.get().getId();
        var entries = timetableEntryRepository.findByTimetableId(timetableId);

        for (TimetableEntry e : entries) {
            String day = normalizeDay(e.getDay());
            if (day == null) continue;
            LocalTime start = e.getStartTime();
            LocalTime end = e.getEndTime();
            if (end.isBefore(WORK_START) || start.isAfter(WORK_END)) continue;
            if (start.isBefore(WORK_START)) start = WORK_START;
            if (end.isAfter(WORK_END)) end = WORK_END;
            if (start.isBefore(end)) {
                map.computeIfAbsent(day, k -> new ArrayList<>())
                        .add(new TimeInterval(start, end));
            }
        }
        return map;
    }

    private String normalizeDay(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String d = raw.trim();
        if (d.length() > 3) d = d.substring(0,3);
        d = d.substring(0,1).toUpperCase() + d.substring(1).toLowerCase();
        return DAYS.contains(d) ? d : null;
    }

    private List<TimeInterval> mergeIntervals(List<TimeInterval> list) {
        if (list.isEmpty()) return List.of();
        var sorted = list.stream()
                .sorted(Comparator.comparing(TimeInterval::getStart))
                .collect(Collectors.toList());
        LinkedList<TimeInterval> merged = new LinkedList<>();
        for (TimeInterval iv : sorted) {
            if (merged.isEmpty() || iv.getStart().isAfter(merged.getLast().getEnd())) {
                merged.add(new TimeInterval(iv.getStart(), iv.getEnd()));
            } else if (iv.getEnd().isAfter(merged.getLast().getEnd())) {
                merged.getLast().setEnd(iv.getEnd());
            }
        }
        return merged;
    }

    private List<TimeInterval> invertToFree(List<TimeInterval> busyMerged) {
        List<TimeInterval> free = new ArrayList<>();
        LocalTime cursor = WORK_START;
        for (TimeInterval b : busyMerged) {
            if (b.getStart().isAfter(cursor)) {
                free.add(new TimeInterval(cursor, min(b.getStart(), WORK_END)));
            }
            if (b.getEnd().isAfter(cursor)) cursor = b.getEnd();
            if (!cursor.isBefore(WORK_END)) break;
        }
        if (cursor.isBefore(WORK_END)) free.add(new TimeInterval(cursor, WORK_END));
        return free.stream().filter(iv -> iv.getStart().isBefore(iv.getEnd())).collect(Collectors.toList());
    }

    private List<TimeInterval> intersectFree(List<TimeInterval> a, List<TimeInterval> b) {
        List<TimeInterval> out = new ArrayList<>();
        int i=0,j=0;
        while (i<a.size() && j<b.size()) {
            LocalTime start = max(a.get(i).getStart(), b.get(j).getStart());
            LocalTime end = min(a.get(i).getEnd(), b.get(j).getEnd());
            if (start.isBefore(end)) out.add(new TimeInterval(start,end));
            if (a.get(i).getEnd().isBefore(b.get(j).getEnd())) i++; else j++;
        }
        return out;
    }

    private LocalTime min(LocalTime a, LocalTime b){ return a.isBefore(b)?a:b; }
    private LocalTime max(LocalTime a, LocalTime b){ return a.isAfter(b)?a:b; }

    private UserDetails getUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken || auth == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (UserDetails) auth.getPrincipal();
    }

    private List<FreeSlot> sliceIntoSlots(String day, java.time.LocalDate date, TimeInterval iv) {
        List<FreeSlot> slots = new ArrayList<>();
        LocalTime alignedStart = alignUp(iv.getStart());
        LocalTime alignedEnd = alignDown(iv.getEnd());
        while (alignedStart.isBefore(alignedEnd)) {
            LocalTime next = alignedStart.plusMinutes(SLOT_MINUTES);
            if (next.isAfter(alignedEnd)) break;
            slots.add(new FreeSlot(day, alignedStart.toString(), next.toString(), date));
            alignedStart = next;
        }
        return slots;
    }

    // 向上取整到最近的 :00 / :30
    private LocalTime alignUp(LocalTime t) {
        int minute = t.getMinute();
        int mod = minute % SLOT_MINUTES;
        if (mod == 0) return t;
        int add = SLOT_MINUTES - mod;
        LocalTime r = t.plusMinutes(add);
        return r.isAfter(WORK_END) ? WORK_END : r;
    }

    // 向下取整到最近的 :00 / :30
    private LocalTime alignDown(LocalTime t) {
        int minute = t.getMinute();
        int mod = minute % SLOT_MINUTES;
        LocalTime r = (mod == 0) ? t : t.minusMinutes(mod);
        return r.isBefore(WORK_START) ? WORK_START : r;
    }
}