package com.example.pi_maya.core.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public final class DateUtils {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private static final DateTimeFormatter DAY_FULL =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_BR);
    private static final DateTimeFormatter DAY_SHORT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);
    private static final DateTimeFormatter TIME_HM =
            DateTimeFormatter.ofPattern("HH:mm", PT_BR);
    private static final DateTimeFormatter ISO =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private DateUtils() {}

    public static String formatTodayLong() {
        LocalDate today = LocalDate.now();
        String dow = today.getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR);
        return capitalize(dow) + ", " + today.getDayOfMonth() + " de "
                + capitalize(today.getMonth().getDisplayName(TextStyle.FULL, PT_BR));
    }

    public static String formatDateShort(LocalDate date) {
        return date.format(DAY_SHORT);
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(TIME_HM);
    }

    public static String formatDateTimeFriendly(OffsetDateTime dateTime) {
        LocalDate today = LocalDate.now();
        LocalDate target = dateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
        String time = dateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalTime().format(TIME_HM);
        if (target.equals(today)) {
            return "Hoje, " + time;
        }
        if (target.equals(today.plusDays(1))) {
            return "Amanhã, " + time;
        }
        return target.format(DAY_SHORT) + ", " + time;
    }

    public static OffsetDateTime parseIsoOffset(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try {
            return OffsetDateTime.parse(iso, ISO);
        } catch (Exception e) {
            try {
                // Postgres às vezes manda sem offset
                return LocalDateTime.parse(iso).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
