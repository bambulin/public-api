package io.whalebone.publicapi.ejb.dto;

public enum EAggregate {
    CLIENT_IP("client"),
    TLD("domain_l1"),
    DOMAIN("domain_l2"),
    QUERY("query"),
    TYPE("query_type"),
    ANSWER("answer");

    private String elasticField;

    EAggregate(String elasticField) {
        this.elasticField = elasticField;
    }

    public String getElasticField() {
        return elasticField;
    }
}
