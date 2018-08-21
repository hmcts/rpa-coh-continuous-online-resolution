package uk.gov.hmcts.reform.coh.service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@Component
public class ExpiryCalendar {

    public static int deadlineExtensionDays;

    @Autowired
    private ExpiryCalendar(@Value( "${deadline.extension-days}" ) int deadlineExtensionDays) {
        ExpiryCalendar.deadlineExtensionDays = deadlineExtensionDays;
    }

    public static Date getDeadlineExpiryDate() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.DAY_OF_YEAR, ExpiryCalendar.deadlineExtensionDays);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}