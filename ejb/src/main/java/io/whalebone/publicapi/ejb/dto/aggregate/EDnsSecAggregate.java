package io.whalebone.publicapi.ejb.dto.aggregate;

/**
 * EDnsSecAggregate is very similar to EDnsAggregate but it contains lesser items
 */
public enum EDnsSecAggregate implements IDnsAggregate {
    TLD(IDnsAggregate.TLD),
    DOMAIN(IDnsAggregate.DOMAIN),
    QUERY(IDnsAggregate.QUERY),
    QUERY_TYPE(IDnsAggregate.QUERY_TYPE)
    ;

    private String elasticField;

    EDnsSecAggregate(String elasticField) {
        this.elasticField = elasticField;
    }

    @Override
    public String getElasticField() {
        return elasticField;
    }
}
