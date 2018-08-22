package uk.gov.hmcts.reform.coh.service.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static junit.framework.TestCase.assertEquals;

@RunWith(SpringRunner.class)
public class ExpiryCalendarTest {

    @InjectMocks
    private ExpiryCalendar expiryCalendar;

    @Before
    public void setUp() {
        expiryCalendar.init();
    }

    @Test
    public void testGetDeadlineReturnsDateOf7DaysAway() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.DAY_OF_YEAR, expiryCalendar.getDeadlineExtensionDays());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        Date deadlineExpiryDate = expiryCalendar.getDeadlineExpiryDate();
        assertEquals(calendar.getTime().toString(), deadlineExpiryDate.toString());
    }
}
