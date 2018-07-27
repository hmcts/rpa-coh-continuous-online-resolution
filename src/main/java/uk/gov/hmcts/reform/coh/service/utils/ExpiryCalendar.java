package uk.gov.hmcts.reform.coh.service.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ExpiryCalendar {
    /**
     * Hard-coded to 7 days.
     * @return
     */
    public static Date getDeadlineExpiryDate() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}
