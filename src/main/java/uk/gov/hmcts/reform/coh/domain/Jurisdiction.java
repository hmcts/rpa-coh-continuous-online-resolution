package uk.gov.hmcts.reform.coh.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "jurisdiction")
public class Jurisdiction {

    @Id
    @Column(name = "jurisdiction_id")
    private Long jurisdictionId;

    @Column(name = "jurisdiction_name")
    private String jurisdictionName;

    @Column(name = "url")
    private String url;

    @Column(name = "max_question_rounds")
    private Integer maxQuestionRounds;

    public Long getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(Long jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public String getJurisdictionName() {
        return jurisdictionName;
    }

    public void setJurisdictionName(String jurisdictionName) {
        this.jurisdictionName = jurisdictionName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMaxQuestionRounds(int maxQuestionRounds){
        this.maxQuestionRounds = maxQuestionRounds;
    }

    public int getMaxQuestionRounds() {
        return maxQuestionRounds;
    }

    @Override
    public String toString() {
        return "Jurisdiction{" +
                "jurisdictionId=" + jurisdictionId +
                ", jurisdictionName='" + jurisdictionName + '\'' +
                ", url='" + url + '\'' +
                ", maxQuestionRounds=" + maxQuestionRounds +
                '}';
    }
}
