package com.fyp.AABookingProject.ai.service;

import com.fyp.AABookingProject.ai.model.FreeSlot;
import com.fyp.AABookingProject.ai.model.TimeInterval;
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
        Long lecturerUserId = current.getStudent().getAdvisor().getUser().getId();

        Map<String, List<TimeInterval>> teacherBusy = loadBusyByUserLatestTimetable(lecturerUserId);
        Map<String, List<TimeInterval>> studentBusy = loadBusyByUserLatestTimetable(studentUserId);
        if (teacherBusy.isEmpty() || studentBusy.isEmpty()) return List.of();

        List<FreeSlot> result = new ArrayList<>();
        // Load existing appointments (current week scope heuristic: today +/- 30 days)
        var startDate = java.time.LocalDate.now().minusDays(30);
        var endDate = java.time.LocalDate.now().plusDays(30);
        List<Appointment> advisorAppointments = appointmentRepository.findByAdvisorIdAndDateBetween(lecturerUserId, startDate, endDate);
        for (String day : DAYS) {
            var tBusy = mergeIntervals(teacherBusy.getOrDefault(day, List.of()));
            var sBusy = mergeIntervals(studentBusy.getOrDefault(day, List.of()));
            var mutualIntervals = intersectFree(invertToFree(tBusy), invertToFree(sBusy));
            // 切成 30 分钟槽
            java.time.LocalDate mappedDate = mapDayToNextDate(day);
            for (TimeInterval iv : mutualIntervals) {
                var sliced = sliceIntoSlots(day, mappedDate, iv);
                // Advisor 级别：只要该时间段被任何学生预约，就不推荐给其它学生
                List<FreeSlot> filtered = sliced.stream()
                        .filter(fs -> {
                            java.time.LocalTime slotStart = java.time.LocalTime.parse(fs.getStartTime());
                            java.time.LocalTime slotEnd = java.time.LocalTime.parse(fs.getEndTime());
                            return !isBooked(advisorAppointments, fs.getDate(), slotStart, slotEnd);
                        })
                        .toList();
                result.addAll(filtered);
            }
        }
        return result.stream()
                .sorted(Comparator.comparing((FreeSlot fs) -> dayOrder(fs.getDay()))
                        .thenComparing(FreeSlot::getStartTime))
                .collect(Collectors.toList());
    }

    private boolean isBooked(List<Appointment> list, java.time.LocalDate date, java.time.LocalTime s, java.time.LocalTime e) {
        return list.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> a.getDate().equals(date))
                .anyMatch(a -> a.getStartTime().isBefore(e) && s.isBefore(a.getEndTime()));
    }

    // Map weekday label to the next upcoming actual date (simple heuristic)
    private java.time.LocalDate mapDayToNextDate(String day) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.DayOfWeek target = switch (day) {
            case "Mon" -> java.time.DayOfWeek.MONDAY;
            case "Tue" -> java.time.DayOfWeek.TUESDAY;
            case "Wed" -> java.time.DayOfWeek.WEDNESDAY;
            case "Thu" -> java.time.DayOfWeek.THURSDAY;
            case "Fri" -> java.time.DayOfWeek.FRIDAY;
            default -> today.getDayOfWeek();
        };
        int diff = target.getValue() - today.getDayOfWeek().getValue();
        if (diff < 0) diff += 7;
        return today.plusDays(diff);
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

    private int dayOrder(String d){ return DAYS.indexOf(d); }
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