package io.whalebone.publicapi.ejb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class DnsAggregateBucketDTO {
    // one of these fields is only supposed to be instantiated
    private EDnsQueryType type;
    private String clientIp;
    private String answer;
    private String query;
    private String domain;
    private String tld;

    private Long count;
}
