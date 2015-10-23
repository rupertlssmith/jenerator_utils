package com.thesett.util.time;

import org.joda.time.LocalDate;

/**
 * TimeUtils provides some helper methods for working with dates and times.
 */
public class TimeUtils {
    /** Private constructor to prevent instantiation of utility class. */
    private TimeUtils() {
    }

    /**
     * Provides the date that is the start of the quarter that the specified date lies within.
     *
     * @param  someDate A date to get the quarter start for.
     *
     * @return The date that is the start of the quarter that the specified date lies within.
     */
    public static LocalDate startDateOfQuarter(LocalDate someDate) {
        int month = someDate.monthOfYear().get();
        int quarter = (month - 1) / 3;
        int qStartMonth = (quarter * 3) + 1;

        return new LocalDate(someDate.year().get(), qStartMonth, 1);
    }

    /**
     * Provides the date that is the end of the quarter that the specified date lies within.
     *
     * @param  someDate A date to get the quarter end for.
     *
     * @return The date that is the end of the quarter that the specified date lies within.
     */
    public static LocalDate endDateOfQuarter(LocalDate someDate) {
        LocalDate startOfQuarter = startDateOfQuarter(someDate);
        LocalDate startOfNextQuarter = startOfQuarter.plusMonths(3);

        return startOfNextQuarter.minusDays(1);
    }
}
