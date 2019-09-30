package io.whalebone.publicapi.ejb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResolverMetricsDTO {
    private int resolverId;
    private String hostname;
    List<ResolverMetricsTimeBucketDTO> timeline;
}
