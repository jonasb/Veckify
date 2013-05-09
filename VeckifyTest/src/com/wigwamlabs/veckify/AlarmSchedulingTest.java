package com.wigwamlabs.veckify;

import android.text.format.DateFormat;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.wigwamlabs.veckify.AlarmUtils.DAYS_ALL;
import static com.wigwamlabs.veckify.AlarmUtils.DAY_WEDNESDAY;

public class AlarmSchedulingTest extends TestCase {
    public void testAllDaysRepeat() throws Exception {
        assertNextAlarmEquals("2013-04-22 09:00", 9, 0, DAYS_ALL, 0, "2013-04-22 08:30");
        assertNextAlarmEquals("2013-04-23 09:00", 9, 0, DAYS_ALL, 0, "2013-04-22 09:30");

        assertNextAlarmEquals("2013-04-23 09:00", 9, 0, DAYS_ALL, 0, "2013-04-23 08:30");
        assertNextAlarmEquals("2013-04-24 09:00", 9, 0, DAYS_ALL, 0, "2013-04-23 09:30");

        assertNextAlarmEquals("2013-04-24 09:00", 9, 0, DAYS_ALL, 0, "2013-04-24 08:30");
        assertNextAlarmEquals("2013-04-25 09:00", 9, 0, DAYS_ALL, 0, "2013-04-24 09:30");

        assertNextAlarmEquals("2013-04-25 09:00", 9, 0, DAYS_ALL, 0, "2013-04-25 08:30");
        assertNextAlarmEquals("2013-04-26 09:00", 9, 0, DAYS_ALL, 0, "2013-04-25 09:30");

        assertNextAlarmEquals("2013-04-26 09:00", 9, 0, DAYS_ALL, 0, "2013-04-26 08:30");
        assertNextAlarmEquals("2013-04-27 09:00", 9, 0, DAYS_ALL, 0, "2013-04-26 09:30");

        assertNextAlarmEquals("2013-04-27 09:00", 9, 0, DAYS_ALL, 0, "2013-04-27 08:30");
        assertNextAlarmEquals("2013-04-28 09:00", 9, 0, DAYS_ALL, 0, "2013-04-27 09:30");

        assertNextAlarmEquals("2013-04-28 09:00", 9, 0, DAYS_ALL, 0, "2013-04-28 08:30");
        assertNextAlarmEquals("2013-04-29 09:00", 9, 0, DAYS_ALL, 0, "2013-04-28 09:30");
    }

    public void testOneDayRepeat() throws Exception {
        assertNextAlarmEquals("2013-04-24 09:00", 9, 0, DAY_WEDNESDAY, 0, "2013-04-22 08:30");

        assertNextAlarmEquals("2013-04-24 09:00", 9, 0, DAY_WEDNESDAY, 0, "2013-04-23 08:30");

        assertNextAlarmEquals("2013-04-24 09:00", 9, 0, DAY_WEDNESDAY, 0, "2013-04-24 08:30");
        assertNextAlarmEquals("2013-05-01 09:00", 9, 0, DAY_WEDNESDAY, 0, "2013-04-24 09:30");

        assertNextAlarmEquals("2013-05-01 09:00", 9, 0, DAY_WEDNESDAY, 0, "2013-04-25 08:30");

        assertNextAlarmEquals("2013-05-01 09:00", 9, 0, DAY_WEDNESDAY, 0, "2013-04-26 08:30");

        assertNextAlarmEquals("2013-05-01 09:00", 9, 0, DAY_WEDNESDAY, 0, "2013-04-27 08:30");

        assertNextAlarmEquals("2013-05-01 09:00", 9, 0, DAY_WEDNESDAY, 0, "2013-04-28 08:30");
    }

    private void assertNextAlarmEquals(String expectedTime, int hour, int minute, int repeatDays, long oneoffTimeMs, String nowTime) throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        final Date date = formatter.parse(nowTime);
        final Calendar cal = AlarmUtils.getNextAlarmTime(true, hour, minute, repeatDays, oneoffTimeMs, date.getTime());
        assertEquals(expectedTime, DateFormat.format("yyyy-MM-dd kk:mm", cal));
    }
}
