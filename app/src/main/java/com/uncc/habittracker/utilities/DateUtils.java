package com.uncc.habittracker.utilities;
import android.util.Log;

import com.uncc.habittracker.data.model.HabitProgress;
import com.uncc.habittracker.data.model.UserHabitDoc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class DateUtils {
    static String TAG = "DateUtils";

    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static HashSet<Date> getHabitsMatchingCriteria(ArrayList<HabitProgress> habitProgress,
                                                   UserHabitDoc habit, Date startDate,
                                                   Date endDate) {
        HashSet<Date> dates = new HashSet<>();

        Log.d(TAG, startDate.toString() + "   " + endDate.toString());
        Log.d(TAG, habitProgress.toString());

        List<HabitProgress> habitProgressMatches = habitProgress.stream()
                .filter(progress -> progress.getUserHabitDocId().equals(habit.getDocId()))
                .filter(progress -> progress.getProgressDate().toDate().compareTo(startDate) > 0)
                .filter(progress -> progress.getProgressDate().toDate().compareTo(endDate) < 0)
                .collect(Collectors.toList());

        if (habitProgressMatches.size() > 0) {
            for (HabitProgress habitProgressMatch : habitProgressMatches) {
                Log.d(TAG, habitProgressMatch.toString());
                dates.add(DateUtils.removeTime(habitProgressMatch.getProgressDate().toDate()));
            }
        }
        else {
            Log.d(TAG, "Not found");
        }

        Log.d(TAG, String.valueOf(dates.size()));
        return dates;
    }

    public static HashSet<Date> uniqueDatesCurrentDay(ArrayList<HabitProgress> habitProgress,
                                                  UserHabitDoc habit) {
        LocalDate endLocalDate = LocalDate.now();
        LocalDate startLocalDate = LocalDate.now();

        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant());
        Date endDate = Date.from(LocalTime.MAX.atDate(endLocalDate).atZone(ZoneOffset.systemDefault()).toInstant());

        return getHabitsMatchingCriteria(habitProgress, habit, startDate, endDate);
    }

    public static HashSet<Date> uniqueDatesInWeek(ArrayList<HabitProgress> habitProgress,
                                           UserHabitDoc habit) {
        LocalDate endLocalDate = LocalDate.now();
        LocalDate startLocalDate = endLocalDate.with(WeekFields.of(Locale.US).dayOfWeek(), 1L);

        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant());
        Date endDate = Date.from(LocalTime.MAX.atDate(endLocalDate).atZone(ZoneOffset.systemDefault()).toInstant());

        return getHabitsMatchingCriteria(habitProgress, habit, startDate, endDate);
    }

    public static HashSet<Date> uniqueDatesInMonth(ArrayList<HabitProgress> habitProgress,
                                            UserHabitDoc habit) {
        LocalDate endLocalDate = LocalDate.now();
        LocalDate startLocalDate = endLocalDate.withDayOfMonth(1);

        Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneOffset.systemDefault()).toInstant());
        Date endDate = Date.from(LocalTime.MAX.atDate(endLocalDate).atZone(ZoneOffset.systemDefault()).toInstant());

        return getHabitsMatchingCriteria(habitProgress, habit, startDate, endDate);
    }

    public static int getDailyProgress(ArrayList<HabitProgress> habitProgress, UserHabitDoc habit) {
        HashSet<Date> dates = uniqueDatesCurrentDay(habitProgress, habit);

        return dates.size();
    }

    public static int getDailyProgressForWeek(ArrayList<HabitProgress> habitProgress, UserHabitDoc habit) {
        HashSet<Date> dates = uniqueDatesInWeek(habitProgress, habit);

        return dates.size();
    }

    public static int getWeeklyProgressForWeek(ArrayList<HabitProgress> habitProgress, UserHabitDoc habit) {
        HashSet<Date> dates = uniqueDatesInWeek(habitProgress, habit);

        return (dates.size() > 0) ? 1 : 0;
    }

    public static int getMonthlyProgress(ArrayList<HabitProgress> habitProgress, UserHabitDoc habit) {
        HashSet<Date> dates = uniqueDatesInMonth(habitProgress, habit);

        return (dates.size() > 0) ? 1 : 0;
    }
}