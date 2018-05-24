package com.vng.datasync.util;

/**
 * Author  : duyng
 * since   : 10/20/2016
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.PluralsRes;
import android.text.TextUtils;

import com.vng.datasync.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateUtil {

    private static final String TAG = DateUtil.class.getSimpleName();

    private static final SimpleDateFormat DAY_MONTH_YEAR_FMT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private static final SimpleDateFormat HOUR_MINUTE_SECOND_FMT = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private static final SimpleDateFormat HOUR_MINUTE_FMT = new SimpleDateFormat("HH:mm", Locale.US);

    private static final SimpleDateFormat YEAR_MONTH_DAY_HOUR_MINUTE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    private static final SimpleDateFormat YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private static final SimpleDateFormat HOUR_MINUTE_SECOND_YEAR_MONTH_DAY_FMT = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd", Locale.US);

    private static final SimpleDateFormat DATE_MONTH_YEAR_WITH_SLASH_FMT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    private static final SimpleDateFormat DAY_MONTH_YEAR_TIME_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);

    private static final SimpleDateFormat HOUR_MINUTE_WITH_MARKER = new SimpleDateFormat("h:mm a", Locale.US);

    private static final SimpleDateFormat DAY_MONTH_YEAR_TIME_UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    private static final int SECOND = 1000;

    private static final int MINUTE = 60 * SECOND;

    private static final int HOUR = 60 * MINUTE;

    private static final int DAY = 24 * HOUR;

    private static final long DAY_TO_MILLIS = TimeUnit.DAYS.toMillis(1);

    private static final long HOUR_TO_MILLIS = TimeUnit.HOURS.toMillis(1);

    private static final long MINUTE_TO_MILLIS = TimeUnit.MINUTES.toMillis(1);

    public static final long SECOND_TO_MILLIS = TimeUnit.SECONDS.toMillis(1);

    public static String toDate(long time) {
        return formatTime(time, DATE_MONTH_YEAR_WITH_SLASH_FMT, "GMT+7");
    }

    // second
    public static String toTime(long time) {
        return formatTime(time, HOUR_MINUTE_WITH_MARKER, "GMT+7");
    }

    //example: GMT+7
    public static String formatTime(long time, SimpleDateFormat dateFormat, String timeZone) {
        Date date = new Date(time * 1000);

        if (!TextUtils.isEmpty(timeZone)) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        }

        return dateFormat.format(date);
    }

    public static String getTimeAgo(long time, Context ctx) {
        return getTimeAgo(time, ctx, ctx.getString(R.string.text_time_streaming));
    }

    public static String getTimeAgo(long time, Context ctx, String moment) {
        // TODO: use DateUtils methods instead
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return moment;
        }

        final long diff = now - time;

        if (diff < MINUTE) {
            return moment;
        } else if (diff < 2 * MINUTE) {
            return ctx.getString(R.string.text_time_one_minute_ago);
        } else if (diff < 50 * MINUTE) {
            return String.format(Locale.US, "%d %s", diff / MINUTE, ctx.getString(R.string.text_time_minute_ago));
        } else if (diff < 90 * MINUTE) {
            return ctx.getString(R.string.text_time_one_hour_ago);
        } else if (diff < 24 * HOUR) {
            return String.format(Locale.US, "%d %s", diff / HOUR, ctx.getString(R.string.text_time_hour_ago));
        } else if (diff < 48 * HOUR) {
            return ctx.getString(R.string.text_time_one_day_ago);
        } else {
            return String.format(Locale.US, "%d %s", diff / DAY, ctx.getString(R.string.text_time_day_ago));
        }
    }

    public static String getChatDisplayTime(long time, Context ctx) {
        String moment = ctx.getString(R.string.text_just_now);
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if (time <= 0) {
            return moment;
        }

        if (time > now) {
            if (time - now < MINUTE) {
                return moment;
            }
            Calendar nowCalendar = Calendar.getInstance();
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(time);
            if (nowCalendar.get(Calendar.DAY_OF_MONTH) == lastModified.get(Calendar.DAY_OF_MONTH)) {
                return toTime(time / 1000L);
            } else {
                return toDate(time / 1000L);
            }
        }

        final long diff = now - time;

        if (diff < MINUTE) {
            return moment;
        } else if (diff < 2 * MINUTE) {
            return ctx.getString(R.string.text_time_one_minute_ago);
        } else if (diff < 59 * MINUTE) {
            return String.format(Locale.US, "%d %s", diff / MINUTE, ctx.getString(R.string.text_time_minute_ago));
        } else if (diff < 2 * HOUR) {
            return ctx.getString(R.string.text_time_one_hour_ago);
        } else {
            Calendar nowCalendar = Calendar.getInstance();
            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(time);
            if (nowCalendar.get(Calendar.DAY_OF_MONTH) == lastModified.get(Calendar.DAY_OF_MONTH)) {
                return toTime(time / 1000L);
            } else {
                return toDate(time / 1000L);
            }
        }
    }

    public static boolean isInSameDay(long lastCheckInTimestamp) {
        Calendar lastTimestamp = Calendar.getInstance();
        lastTimestamp.setTimeInMillis(lastCheckInTimestamp);

        Calendar now = Calendar.getInstance();

        return !(now.get(Calendar.YEAR) == lastTimestamp.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == lastTimestamp.get(Calendar.MONTH) &&
                now.get(Calendar.DAY_OF_MONTH) == lastTimestamp.get(Calendar.DAY_OF_MONTH));
    }

    public static String getTimeLiveEnded(long milliseconds) {
        String time = "";
        String hoursStr = "";
        String minutesStr = "";
        String secondsStr = "";
        try {
            if (milliseconds < 0)
                return time;

            int seconds = (int) (milliseconds / 1000) % 60;
            int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
            int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

            if (seconds <= 9)
                secondsStr = "0" + seconds;
            else secondsStr = "" + seconds;

            if (minutes <= 9)
                minutesStr = "0" + minutes;
            else minutesStr = "" + minutes;

            if (hours <= 9)
                hoursStr = "0" + hours;
            else hoursStr = "" + hours;

        } catch (Exception e) {
        }

        return hoursStr + " : " + minutesStr + " : " + secondsStr;
    }

    public static String getWatermarkDateString(Date date) {
        if (null == date) {
            date = new Date();
        }
        return DATE_MONTH_YEAR_WITH_SLASH_FMT.format(date);
    }

    // expired day auth key
    public static String toExpiredDay(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, day);

        DAY_MONTH_YEAR_TIME_UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        return DAY_MONTH_YEAR_TIME_UTC.format(calendar.getTime());
    }

    public static int getTimestampFromDateString(String date) {
        try {
            Date formattedDate = DATE_MONTH_YEAR_WITH_SLASH_FMT.parse(date);
            return (int) (formattedDate.getTime() / 1000);
        } catch (ParseException e) {
            return 0;
        }
    }

    public static boolean isVipExpired(long expireDateMilliseconds) {
        long durations = expireDateMilliseconds - System.currentTimeMillis();
        if (durations <= 0) {
            return true;
        }

        long hour = (durations / HOUR) % 24L;
        long minute = (durations / MINUTE) % 60L;
        if (hour == 0 && minute == 0) {
            return true;
        }
        return false;
    }

    private static String getDuration(@NonNull Context context, @PluralsRes int stringDayId, @PluralsRes int stringHourId,
                                      @PluralsRes int stringMinuteId, @PluralsRes int stringSecondId,
                                      long timeLeft, int maxLevel) {
        long timeUnitValue;
        int timeResId;
        int currentLevel = 0;
        long newTimeLeft = timeLeft;
        final StringBuilder resultDisplayString = new StringBuilder();
        while (newTimeLeft > 0 && currentLevel++ < maxLevel) {

            if (newTimeLeft >= DAY_TO_MILLIS) {
                timeUnitValue = DAY_TO_MILLIS;
                timeResId = stringDayId;
            } else if (newTimeLeft >= HOUR_TO_MILLIS) {
                timeUnitValue = HOUR_TO_MILLIS;
                timeResId = stringHourId;
            } else if (newTimeLeft >= MINUTE_TO_MILLIS) {
                timeUnitValue = MINUTE_TO_MILLIS;
                timeResId = stringMinuteId;
            } else {
                timeUnitValue = SECOND_TO_MILLIS;
                timeResId = stringSecondId;
            }

            long modValue = (timeUnitValue == SECOND_TO_MILLIS) ? 0 : newTimeLeft % timeUnitValue;

            int displayTime = (int)(newTimeLeft / timeUnitValue);
            newTimeLeft = modValue;

            final String newTimeDisplay = context.getResources().getQuantityString(timeResId, displayTime, displayTime);
            resultDisplayString.append(newTimeDisplay);
            if (modValue > 0) {
                resultDisplayString.append(" ");
            }
        }

        return resultDisplayString.toString();
    }

    public static String getEquipmentProfileRemainingTime(@NonNull Context context, long expireDateMilliseconds) {
        String timeDisplay = "";
        long durations = expireDateMilliseconds - System.currentTimeMillis();
        if (durations <= 0) {
            return timeDisplay;
        }
        long daysLeft = (durations / DAY);
        if (daysLeft > 0) {
            long timeLeft = expireDateMilliseconds - ((daysLeft * DAY) + System.currentTimeMillis());
            timeDisplay = context.getString(R.string.profile_menu_equipment_day_expire, daysLeft, timeLeft / HOUR);
        } else if (daysLeft == 0) {
            long hour = durations / HOUR;
            long minute = durations / MINUTE;
            minute = minute == 0 ? 1 : minute;
            timeDisplay = context.getString(R.string.profile_menu_equipment_hour_expire, hour, minute);
        }
        return timeDisplay;
    }

    public static String getVipRemainingTime(@NonNull Context context, long expireDateMilliseconds) {
        String timeDisplay = "";
        long durations = expireDateMilliseconds - System.currentTimeMillis();
        if (durations <= 0) {
            return timeDisplay;
        }
        long daysLeft = (durations / DAY);
        if (daysLeft > 0) {
            long timeLeft = expireDateMilliseconds - ((daysLeft * DAY) + System.currentTimeMillis());
            timeDisplay = context.getString(R.string.profile_menu_vip_day_expire, daysLeft, timeLeft / HOUR);
        } else if (daysLeft == 0) {
            long hour = durations / HOUR;
            long minute = durations / MINUTE;
            minute = minute == 0 ? 1 : minute;
            timeDisplay = context.getString(R.string.profile_menu_vip_hour_expire, hour, minute);
        }
        return timeDisplay;
    }

    public static String getExpireDateBuyVipSuccess(@NonNull Context context, long expireDate) {
        Date date = new Date(expireDate);
        String hourMinute = HOUR_MINUTE_SECOND_FMT.format(date);
        String yearMonthDay = DAY_MONTH_YEAR_FMT.format(date);

        return context.getString(R.string.buy_vip_dialog_expiredate, hourMinute, yearMonthDay);
    }

    public static String getVipExpiredDate(@NonNull Context context, long expiredDate) {
        final Date date = new Date(expiredDate);
        final String yearMonthDay = DAY_MONTH_YEAR_FMT.format(date);

        return yearMonthDay;
    }

    public static String getRemainTimeDailyTopFan(@NonNull Context context, long seconds) {
        long time = seconds * 1000;
        return context.getString(R.string.remain_time_daily_top_fan, (time / HOUR) % 24L, (time / MINUTE) % 60L);
    }

    public static String getRemainTimeMonthlyTopFan(@NonNull Context context, long seconds) {
        long time = (seconds * 1000);
        int hour = (int) ((time / HOUR) % 24L);
        int minute = (int) ((time / MINUTE) % 60L);
        Calendar calendar = Calendar.getInstance();
        int numDaysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int month = (int) ((time / DAY) % numDaysOfMonth);

        return context.getString(R.string.remain_time_monthy_top_fan, month, hour, minute);
    }
}
