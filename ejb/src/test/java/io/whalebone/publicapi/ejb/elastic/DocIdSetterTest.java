package io.whalebone.publicapi.ejb.elastic;

import io.whalebone.publicapi.ejb.dto.DnsTimeBucketDTO;
import io.whalebone.publicapi.ejb.dto.EventDTO;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.fail;

public class DocIdSetterTest {

    @Test
    public void setDocIdIfApplicableTest_applicable() {
        EventDTO event = new EventDTO();
        DocIdSetter.setDocIdIfApplicable(event, "document-id");

        assertThat(event.getEventId(), is("document-id"));
    }

    @Test
    public void setDocIdIfApplicableTest_notApplicable() {
        DnsTimeBucketDTO bucket = DnsTimeBucketDTO.builder().build();
        try {
            DocIdSetter.setDocIdIfApplicable(bucket, "document-id");
        } catch (Throwable t) {
            fail("No exception expected but a Throwable has been thrown", t);
        }
    }
}
