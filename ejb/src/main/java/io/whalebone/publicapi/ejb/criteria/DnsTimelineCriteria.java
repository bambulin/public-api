package io.whalebone.publicapi.ejb.criteria;

import io.whalebone.publicapi.ejb.dto.EAggregate;
import io.whalebone.publicapi.ejb.dto.EDnsQueryType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DnsTimelineCriteria extends ClientCriteria {
    private EDnsQueryType queryType;
    private EAggregate aggregate;
    private String query;
    private String answer;
    private String tld;
    private boolean dga;

    @Builder
    public DnsTimelineCriteria(final String clientId,
                               final String domain,
                               final int days,
                               final Integer resolverId,
                               final String clientIp,
                               final EDnsQueryType queryType,
                               final EAggregate aggregate,
                               final String query,
                               final String answer,
                               final String tld,
                               final boolean dga) {
        super(clientId, domain, days, resolverId, clientIp);
        this.queryType = queryType;
        this.aggregate = aggregate;
        this.query = query;
        this.answer = answer;
        this.tld = tld;
        this.dga = dga;
    }
}
