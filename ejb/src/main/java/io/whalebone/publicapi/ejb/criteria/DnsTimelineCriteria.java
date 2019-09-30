package io.whalebone.publicapi.ejb.criteria;

import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import io.whalebone.publicapi.ejb.dto.ETimeInterval;
import io.whalebone.publicapi.ejb.dto.aggregate.IDnsAggregate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DnsTimelineCriteria extends ClientCriteria {
    private EDnsQueryType queryType;
    private IDnsAggregate aggregate;
    private ETimeInterval interval;
    private String query;
    private String answer;
    private String tld;
    private boolean dga;

    @Builder
    public DnsTimelineCriteria(final String clientId,
                               final int days,
                               final String domain,
                               final Integer resolverId,
                               final String clientIp,
                               final String deviceId,
                               final EDnsQueryType queryType,
                               final IDnsAggregate aggregate,
                               final ETimeInterval interval,
                               final String query,
                               final String answer,
                               final String tld,
                               final boolean dga) {
        super(clientId, days, domain, resolverId, clientIp, deviceId);
        this.queryType = queryType;
        this.aggregate = aggregate;
        this.interval = interval;
        this.query = query;
        this.answer = answer;
        this.tld = tld;
        this.dga = dga;
    }
}
