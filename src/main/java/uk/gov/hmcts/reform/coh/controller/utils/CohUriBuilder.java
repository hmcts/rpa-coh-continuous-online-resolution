package uk.gov.hmcts.reform.coh.controller.utils;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

public class CohUriBuilder {

    private static final UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance();

    public static UriComponents buildOnlineHearingGet(UUID onlineHearingId) {

        return uriBuilder.path("/continuous-online-hearings/{onlineHearingId}").buildAndExpand(onlineHearingId);
    }
}
