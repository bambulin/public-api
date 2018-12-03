package io.whalebone.publicapi.ejb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class DnsTimeBucketDTO {
    private ZonedDateTime timestamp;
    private Long count;
    private List<DnsAggregateBucketDTO> buckets;
}
