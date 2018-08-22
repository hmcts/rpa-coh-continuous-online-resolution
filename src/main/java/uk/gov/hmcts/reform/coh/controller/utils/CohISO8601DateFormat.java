package uk.gov.hmcts.reform.coh.controller.utils;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.text.ParseException;
import java.util.Date;

public class CohISO8601DateFormat {

    private CohISO8601DateFormat() {}

    private static final ISO8601DateFormat df = new ISO8601DateFormat();

    public static String format(Date date) {
        return df.format(date);
    }

    public static Date parse(String date) throws ParseException {
        return df.parse(date);
    }
}
