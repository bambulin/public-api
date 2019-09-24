package io.whalebone.publicapi.ejb.dto.aggregate;

public enum EDnsAggregate implements IDnsAggregate {
    CLIENT_IP(IDnsAggregate.CLIENT_IP),
    TLD(IDnsAggregate.TLD),
    DOMAIN(IDnsAggregate.DOMAIN),
    QUERY(IDnsAggregate.QUERY),
    QUERY_TYPE(IDnsAggregate.QUERY_TYPE),
    ANSWER(IDnsAggregate.ANSWER),
    DEVICE_ID(IDnsAggregate.DEVICE_ID)
    ;

    private String elasticField;

    EDnsAggregate(String elasticField) {
        this.elasticField = elasticField;
    }

    public String getElasticField() {
        return elasticField;
    }
}
