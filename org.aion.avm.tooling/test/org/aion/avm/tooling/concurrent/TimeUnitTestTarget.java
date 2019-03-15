package org.aion.avm.tooling.concurrent;

import java.util.concurrent.TimeUnit;
import org.aion.avm.tooling.abi.Callable;

public class TimeUnitTestTarget {

    @Callable
    public static boolean testavm_convert() {
        return 2 ==  TimeUnit.DAYS.convert(48, TimeUnit.HOURS) &&
                2 == TimeUnit.DAYS.convert(2880 , TimeUnit.MINUTES) &&
                2 == TimeUnit.DAYS.convert(172800, TimeUnit.SECONDS) &&
                2 == TimeUnit.DAYS.convert(172800000, TimeUnit.MILLISECONDS) &&
                2 == TimeUnit.DAYS.convert(172800000000L, TimeUnit.MICROSECONDS) &&
                2 == TimeUnit.DAYS.convert(172800000000000L, TimeUnit.NANOSECONDS) &&

                48 == TimeUnit.HOURS.convert(2, TimeUnit.DAYS) &&
                2 ==  TimeUnit.HOURS.convert(120, TimeUnit.MINUTES) &&
                2 ==  TimeUnit.HOURS.convert(7200, TimeUnit.SECONDS) &&
                2 ==  TimeUnit.HOURS.convert(7200000, TimeUnit.MILLISECONDS) &&
                2 ==  TimeUnit.HOURS.convert(7200000000L, TimeUnit.MICROSECONDS) &&
                2 ==  TimeUnit.HOURS.convert(7200000000000L, TimeUnit.NANOSECONDS) &&

                2880 == TimeUnit.MINUTES.convert(2, TimeUnit.DAYS) &&
                120 ==  TimeUnit.MINUTES.convert(2, TimeUnit.HOURS) &&
                2 ==    TimeUnit.MINUTES.convert(120, TimeUnit.SECONDS) &&
                2 ==    TimeUnit.MINUTES.convert(120000, TimeUnit.MILLISECONDS) &&
                2 ==    TimeUnit.MINUTES.convert(120000000, TimeUnit.MICROSECONDS) &&
                2 ==    TimeUnit.MINUTES.convert(120000000000L, TimeUnit.NANOSECONDS) &&

                172800 == TimeUnit.SECONDS.convert(2, TimeUnit.DAYS) &&
                7200 ==   TimeUnit.SECONDS.convert(2, TimeUnit.HOURS) &&
                120 ==    TimeUnit.SECONDS.convert(2, TimeUnit.MINUTES) &&
                2 ==      TimeUnit.SECONDS.convert(2000, TimeUnit.MILLISECONDS) &&
                2 ==      TimeUnit.SECONDS.convert(2000000, TimeUnit.MICROSECONDS) &&
                2 ==      TimeUnit.SECONDS.convert(2000000000, TimeUnit.NANOSECONDS) &&

                172800000== TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS) &&
                7200000 ==  TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS) &&
                120000 ==   TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES) &&
                2000 ==     TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS) &&
                2000 ==     TimeUnit.MILLISECONDS.convert(2000000, TimeUnit.MICROSECONDS) &&
                2000 ==     TimeUnit.MILLISECONDS.convert(2000000000, TimeUnit.NANOSECONDS) &&

                172800000000L == TimeUnit.MICROSECONDS.convert(2, TimeUnit.DAYS) &&
                7200000000L ==   TimeUnit.MICROSECONDS.convert(2, TimeUnit.HOURS) &&
                120000000==      TimeUnit.MICROSECONDS.convert(2, TimeUnit.MINUTES) &&
                2000000 ==       TimeUnit.MICROSECONDS.convert(2, TimeUnit.SECONDS) &&
                2000 ==                 TimeUnit.MICROSECONDS.convert(2, TimeUnit.MILLISECONDS) &&
                2000 ==                 TimeUnit.MICROSECONDS.convert(2000000, TimeUnit.NANOSECONDS) &&

                172800000000000L == TimeUnit.NANOSECONDS.convert(2, TimeUnit.DAYS) &&
                7200000000000L ==   TimeUnit.NANOSECONDS.convert(2, TimeUnit.HOURS) &&
                120000000000L ==    TimeUnit.NANOSECONDS.convert(2, TimeUnit.MINUTES) &&
                2000000000 ==       TimeUnit.NANOSECONDS.convert(2, TimeUnit.SECONDS) &&
                2000000 ==                 TimeUnit.NANOSECONDS.convert(2, TimeUnit.MILLISECONDS) &&
                2000 ==                    TimeUnit.NANOSECONDS.convert(2, TimeUnit.MICROSECONDS) &&

                1 == TimeUnit.DAYS.convert(47, TimeUnit.HOURS) &&
                2 == TimeUnit.DAYS.convert(49, TimeUnit.HOURS) &&

                1 == TimeUnit.HOURS.convert(119, TimeUnit.MINUTES) &&
                2 == TimeUnit.HOURS.convert(121, TimeUnit.MINUTES) &&

                1 == TimeUnit.MINUTES.convert(119, TimeUnit.SECONDS) &&
                2 == TimeUnit.MINUTES.convert(121, TimeUnit.SECONDS) &&

                1 == TimeUnit.SECONDS.convert(1999, TimeUnit.MILLISECONDS) &&
                2 == TimeUnit.SECONDS.convert(2001, TimeUnit.MILLISECONDS) &&

                1 == TimeUnit.MILLISECONDS.convert(1999, TimeUnit.MICROSECONDS) &&
                2 == TimeUnit.MILLISECONDS.convert(2001, TimeUnit.MICROSECONDS) &&

                1 == TimeUnit.MILLISECONDS.convert(1999, TimeUnit.MICROSECONDS) &&
                2 == TimeUnit.MILLISECONDS.convert(2001, TimeUnit.MICROSECONDS) &&

                1 == TimeUnit.MICROSECONDS.convert(1999, TimeUnit.NANOSECONDS) &&
                2 == TimeUnit.MICROSECONDS.convert(2001, TimeUnit.NANOSECONDS);
    }

    @Callable
    public static boolean testavm_toX() {
         return 48 ==           TimeUnit.DAYS.toHours(2) &&
                 120 == TimeUnit.HOURS.toMinutes(2) &&
                 7200 ==        TimeUnit.HOURS.toSeconds(2) &&
                 2000 ==        TimeUnit.SECONDS.toMillis(2) &&
                 2000000 ==     TimeUnit.SECONDS.toMicros(2) &&
                 5000000000L == TimeUnit.SECONDS.toNanos(5) &&
                 2 ==           TimeUnit.HOURS.toDays(48) &&
                 1 ==           TimeUnit.SECONDS.toHours(3603);
    }

    @Callable
    public static boolean testavm_values() {
        TimeUnit[] values =  (TimeUnit[]) TimeUnit.values();
        return TimeUnit.DAYS == values[0] &&
                TimeUnit.HOURS == values[1] &&
                TimeUnit.MINUTES == values[2] &&
                TimeUnit.SECONDS == values[3] &&
                TimeUnit.MILLISECONDS == values[4] &&
                TimeUnit.MICROSECONDS == values[5] &&
                TimeUnit.NANOSECONDS == values[6];
    }

    @Callable
    public static boolean testavm_valueOf() {
        return TimeUnit.DAYS == TimeUnit.valueOf(new String("DAYS")) &&
                TimeUnit.HOURS == TimeUnit.valueOf(new String("HOURS")) &&
                TimeUnit.MINUTES == TimeUnit.valueOf(new String("MINUTES")) &&
                TimeUnit.SECONDS == TimeUnit.valueOf(new String("SECONDS")) &&
                TimeUnit.MILLISECONDS == TimeUnit.valueOf(new String("MILLISECONDS")) &&
                TimeUnit.MICROSECONDS == TimeUnit.valueOf(new String("MICROSECONDS")) &&
                TimeUnit.NANOSECONDS == TimeUnit.valueOf(new String("NANOSECONDS"));
    }
}
