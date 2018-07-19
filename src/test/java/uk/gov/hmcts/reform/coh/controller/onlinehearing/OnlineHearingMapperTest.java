package uk.gov.hmcts.reform.coh.controller.onlinehearing;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import cucumber.deps.com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.coh.domain.OnlineHearing;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingPanelMember;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingState;
import uk.gov.hmcts.reform.coh.domain.OnlineHearingStateHistory;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class OnlineHearingMapperTest {

    private static final ISO8601DateFormat df = new ISO8601DateFormat();

    UUID uuid = UUID.randomUUID();

    Calendar startDate = new GregorianCalendar();
    Calendar endDate = new GregorianCalendar();
    OnlineHearingState startState = new OnlineHearingState();
    OnlineHearingState relistedState = new OnlineHearingState();
    OnlineHearingResponse response = new OnlineHearingResponse();
    OnlineHearing onlineHearing = new OnlineHearing();

    @Before
    public void setup() {
        startDate.set(Calendar.DAY_OF_YEAR, -2);
        endDate.set(Calendar.DAY_OF_YEAR, -1);
        startState.setState(OnlineHearingStates.STARTED.getStateName());
        relistedState.setState(OnlineHearingStates.RELISTED.getStateName());

        onlineHearing.setOnlineHearingId(uuid);
        onlineHearing.setCaseId("foo");
        onlineHearing.setStartDate(startDate.getTime());
        onlineHearing.setEndDate(endDate.getTime());
        onlineHearing.setOnlineHearingState(startState);

        OnlineHearingStateHistory history1 = new OnlineHearingStateHistory();
        history1.setDateOccurred(startDate.getTime());
        history1.setOnlinehearingstate(startState);
        OnlineHearingStateHistory history2 = new OnlineHearingStateHistory();
        history2.setDateOccurred(endDate.getTime());
        history2.setOnlinehearingstate(relistedState);
        onlineHearing.setOnlineHearingStateHistories(Arrays.asList(history1, history2));

        OnlineHearingPanelMember member = new OnlineHearingPanelMember();
        member.setFullName("foo bar");
        onlineHearing.setPanelMembers(Arrays.asList(member));
    }

    @Test
    public void testMappings() {
        OnlineHearingMapper.map(response, onlineHearing);

        assertEquals(uuid, response.getOnlineHearingId());
        assertEquals("foo", response.getCaseId());
        assertEquals(startDate.getTime(), response.getStartDate());
        assertEquals(endDate.getTime(), response.getEndDate());
        assertEquals(1, response.getPanel().size());
        assertEquals("foo bar", response.getPanel().get(0).getName());
        assertEquals(startState.getState(), response.getCurrentState().getName());
        assertEquals(2, response.getHistories().size());
        assertEquals(relistedState.getState(), response.getHistories().get(1).getName());
    }

    @Test
    public void testEmptyHistories() {
        onlineHearing.setOnlineHearingStateHistories(Arrays.asList());
        OnlineHearingMapper.map(response, onlineHearing);
        assertEquals(0, response.getHistories().size());
    }

    @Test
    public void testNullHistories() {
        onlineHearing.setOnlineHearingStateHistories(null);
        OnlineHearingMapper.map(response, onlineHearing);
        assertEquals(0, response.getHistories().size());
    }
}
