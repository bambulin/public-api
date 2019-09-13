package io.whalebone.publicapi.ejb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ActiveIoCStatsDTO {
    private long totalCount;
    private List<ThreatTypeCountDTO> threatTypeCounts;
}
