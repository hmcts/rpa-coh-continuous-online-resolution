package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingStateHistory;

import java.lang.reflect.Array;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class OnlineHearingMapperTest {

    @Before
    public void setup() {

    }

    @Test
    public void testMappings() {
        OnlineHearingState drafted = new OnlineHearingState();
        drafted.setState("continuous_online_hearing_started");

        OnlineHearingState relisted = new OnlineHearingState();
        relisted.setState("continuous_online_hearing_relisted");

        Date startDate = new Date();
        Calendar yesterday = new GregorianCalendar();
        yesterday.set(Calendar.DAY_OF_YEAR, -1);

        OnlineHearingStateHistory history1 = new OnlineHearingStateHistory();
        history1.setDateOccurred(yesterday.getTime());
        history1.setOnlineHearingState(drafted);

        OnlineHearingStateHistory history2 = new OnlineHearingStateHistory();
        history2.setDateOccurred(startDate);
        history2.setOnlineHearingState(relisted);

        OnlineHearingPanelMember member1 = new OnlineHearingPanelMember();
        member1.setFullName("foo bar");

        UUID uuid = UUID.randomUUID();
        OnlineHearing onlineHearing = new OnlineHearing();
        onlineHearing.setOnlineHearingId(uuid);
        onlineHearing.setCaseId("foo");
        onlineHearing.setStartDate(startDate);
        onlineHearing.setEndDate(startDate);
        onlineHearing.setOnlineHearingStateHistories(Arrays.asList(history1, history2));
        onlineHearing.setPanelMembers(Arrays.asList(member1));

        OnlineHearingResponse response = new OnlineHearingResponse();
        OnlineHearingMapper.map(response, onlineHearing);

        assertEquals(uuid.toString(), response.getOnlineHearingId().toString());
        assertEquals("foo", response.getCaseId());
        assertEquals(startDate, response.getStartDate());
        assertEquals(startDate, response.getEndDate());
        ISO8601DateFormat df = new ISO8601DateFormat();
        assertEquals("continuous_online_hearing_relisted", response.getCurrentState().getState_name());
        assertEquals(df.format(startDate), response.getCurrentState().getDatetime());
        assertEquals(1, response.getPanel().size());
    }
}
