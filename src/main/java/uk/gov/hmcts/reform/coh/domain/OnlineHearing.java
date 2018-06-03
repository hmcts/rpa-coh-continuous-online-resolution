package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ONLINE_HEARING")
public class OnlineHearing {

    @Id
    private int id;

    @Column(name = "EXTERNAL_REF")
    private String externalRef;
}
