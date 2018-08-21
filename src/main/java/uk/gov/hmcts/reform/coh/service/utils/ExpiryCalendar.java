package uk.gov.hmcts.reform.coh.service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.annotation.PostConstruct;

@Component
public class ExpiryCalendar {

    public static ExpiryCalendar INSTANCE;

    @Value("${deadline.extension-days}")
    private int deadlineExtensionDays;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    public int getDeadlineExtensionDays() {
        return deadlineExtensionDays;
    }

    public Date getDeadlineExpiryDate() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.DAY_OF_YEAR, this.deadlineExtensionDays);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }
}