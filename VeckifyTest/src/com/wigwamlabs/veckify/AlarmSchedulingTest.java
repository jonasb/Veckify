package com.wigwamlabs.veckify;

import android.text.format.DateFormat;

import com.wigwamlabs.veckify.alarms.Alarm;
import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmSchedulingTest extends TestCase {
    public void testAllDaysRepeat() throws Exception {
        final Alarm alarm = new Alarm();
        setAlarm(alarm, 9, 0, Alarm.DAYS_ALL);

        assertNextAlarmEquals("2013-04-22 09:00", alarm, "2013-04-22 08:30");
        assertNextAlarmEquals("2013-04-23 09:00", alarm, "2013-04-22 09:30");

        assertNextAlarmEquals("2013-04-23 09:00", alarm, "2013-04-23 08:30");
        assertNextAlarmEquals("2013-04-24 09:00", alarm, "2013-04-23 09:30");

        assertNextAlarmEquals("2013-04-24 09:00", alarm, "2013-04-24 08:30");
        assertNextAlarmEquals("2013-04-25 09:00", alarm, "2013-04-24 09:30");

        assertNextAlarmEquals("2013-04-25 09:00", alarm, "2013-04-25 08:30");
        assertNextAlarmEquals("2013-04-26 09:00", alarm, "2013-04-25 09:30");

        assertNextAlarmEquals("2013-04-26 09:00", alarm, "2013-04-26 08:30");
        assertNextAlarmEquals("2013-04-27 09:00", alarm, "2013-04-26 09:30");

        assertNextAlarmEquals("2013-04-27 09:00", alarm, "2013-04-27 08:30");
        assertNextAlarmEquals("2013-04-28 09:00", alarm, "2013-04-27 09:30");

        assertNextAlarmEquals("2013-04-28 09:00", alarm, "2013-04-28 08:30");
        assertNextAlarmEquals("2013-04-29 09:00", alarm, "2013-04-28 09:30");
    }

    public void testOneDayRepeat() throws Exception {
        final Alarm alarm = new Alarm();
        setAlarm(alarm, 9, 0, Alarm.DAY_WEDNESDAY);

        assertNextAlarmEquals("2013-04-24 09:00", alarm, "2013-04-22 08:30");

        assertNextAlarmEquals("2013-04-24 09:00", alarm, "2013-04-23 08:30");

        assertNextAlarmEquals("2013-04-24 09:00", alarm, "2013-04-24 08:30");
        assertNextAlarmEquals("2013-05-01 09:00", alarm, "2013-04-24 09:30");

        assertNextAlarmEquals("2013-05-01 09:00", alarm, "2013-04-25 08:30");

        assertNextAlarmEquals("2013-05-01 09:00", alarm, "2013-04-26 08:30");

        assertNextAlarmEquals("2013-05-01 09:00", alarm, "2013-04-27 08:30");

        assertNextAlarmEquals("2013-05-01 09:00", alarm, "2013-04-28 08:30");
    }

    private void setAlarm(Alarm alarm, int hour, int minute, int repeatDays) {
        alarm.setTime(hour, minute);
        alarm.setRepeatDays(repeatDays);
    }

    private void assertNextAlarmEquals(String expectedTime, Alarm alarm, String nowTime) throws Exception {
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd kk:mm");
        final Date date = formatter.parse(nowTime);
        final Calendar cal = alarm.getNextAlarmTime(date.getTime());
        assertEquals(expectedTime, DateFormat.format("yyyy-MM-dd kk:mm", cal));
    }
}
