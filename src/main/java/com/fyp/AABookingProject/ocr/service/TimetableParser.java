//package com.fyp.AABookingProject.ocr.service;
//
//import com.fyp.AABookingProject.core.entity.ScheduleBlock;
//import com.fyp.AABookingProject.core.enumClass.BlockType;
//import org.springframework.stereotype.Component;
//
//import java.time.DayOfWeek;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//public class TimetableParser {
//
//    // Sample parser - customize based on your timetable format
//    public List<ScheduleBlock> parseTimetable(String ocrText) {
//        List<ScheduleBlock> blocks = new ArrayList<>();
//        String[] lines = ocrText.split("\\r?\\n");
//
//        // Example pattern: "Monday 09:00-11:00 MATH101 Lecture Hall A"
//        Pattern pattern = Pattern.compile("(\\w+)\\s+(\\d{2}:\\d{2})-(\\d{2}:\\d{2})\\s+(\\w+)\\s+(.*)");
//
//        for (String line : lines) {
//            Matcher matcher = pattern.matcher(line);
//            if (matcher.find()) {
//                ScheduleBlock block = new ScheduleBlock();
//                block.setDayOfWeek(parseDay(matcher.group(1)));
//                block.setStartTime(LocalTime.parse(matcher.group(2), DateTimeFormatter.ofPattern("HH:mm")));
//                block.setEndTime(LocalTime.parse(matcher.group(3), DateTimeFormatter.ofPattern("HH:mm")));
//                block.setBlockType(BlockType.CLASS);
//                block.setDescription(matcher.group(4) + " - " + matcher.group(5));
//                blocks.add(block);
//            }
//        }
//        return blocks;
//    }
//
//    private DayOfWeek parseDay(String dayString) {
//        return switch (dayString.toUpperCase()) {
//            case "MON" -> DayOfWeek.MONDAY;
//            case "TUE" -> DayOfWeek.TUESDAY;
//            case "WED" -> DayOfWeek.WEDNESDAY;
//            case "THU" -> DayOfWeek.THURSDAY;
//            case "FRI" -> DayOfWeek.FRIDAY;
//            case "SAT" -> DayOfWeek.SATURDAY;
//            case "SUN" -> DayOfWeek.SUNDAY;
//            default -> throw new IllegalArgumentException("Invalid day: " + dayString);
//        };
//    }
//}