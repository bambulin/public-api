package io.whalebone.publicapi.ejb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DnsAggregateBucketDTO {
    // one of these fields is only supposed to be instantiated
    private EDnsQueryType queryType;
    private String clientIp;
    private String answer;
    private String query;
    private String domain;
    private String tld;

    private Long count;
}
