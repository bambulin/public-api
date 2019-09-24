package io.whalebone.publicapi.ejb.dto.aggregate;

public interface IDnsAggregate {
    // known elastic fields that can be used for aggregations in Dns and DnsSec timeline requests
    String CLIENT_IP = "client";
    String TLD = "domain_l1";
    String DOMAIN = "domain_l2";
    String QUERY = "query";
    String QUERY_TYPE = "query_type";
    String ANSWER = "answer";
    String DEVICE_ID = "device_id";

    String getElasticField();
}
