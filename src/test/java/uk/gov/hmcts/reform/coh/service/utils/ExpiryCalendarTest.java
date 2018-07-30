package uk.gov.hmcts.reform.coh.service.utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static junit.framework.TestCase.assertEquals;

public class ExpiryCalendarTest {

    @Test
    public void testGetDeadlineReturnsDateOf7DaysAway() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        Date deadlineExpiryDate = ExpiryCalendar.getDeadlineExpiryDate();
        assertEquals(calendar.getTime().toString(), deadlineExpiryDate.toString());
    }
}
