package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.fail;

public class IoCsCountITTest extends Arquillian {
    private static final String CLIENT_ID = "2";

    private ArchiveInitiator archiveInitiator;

    public IoCsCountITTest() {
        archiveInitiator = new ArchiveInitiator();
    }

    @BeforeMethod
    public void setUp() throws IOException {
        archiveInitiator.cleanIoCs();
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void iocsCountNoIoCsTest(@ArquillianResource URL context) throws IOException {
        // create the index manually otherwise the old elastic would throw MissingIndexException
        archiveInitiator.createIoCsIndex();
        JsonObject iocsStats = iocsStats(context);
        // only total count is expected to be in the resopse jsone
        assertThat(iocsStats.size(), is(1));
        long totalCount = iocsStats.get("total_count").getAsLong();
        assertThat(totalCount, is(0L));
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void iocsCountTest(@ArquillianResource URL context) throws IOException {
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "malware");
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "malware");
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "c&c");
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "blacklist");
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "blacklist");
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "blacklist");
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "phishing");
        // this one should not be involved in the result since it is inactive
        archiveInitiator.sendIoCOfThreatType("iocs/ioc_inactive.json", "phishing");
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "exploit");
        // this one should not be involved in the result since threat type is unknown
        archiveInitiator.sendIoCOfThreatType("iocs/ioc.json", "blabla");
        JsonObject iocsStats = iocsStats(context);
        assertThat(iocsStats.size(), is(2));
        long totalCount = iocsStats.get("total_count").getAsLong();
        assertThat(totalCount, is(8L));
        JsonArray threatTypeCounts = iocsStats.getAsJsonArray("threat_type_counts");
        assertThat(threatTypeCounts.size(), is(5));
        for (JsonElement e : threatTypeCounts) {
            JsonObject countObject = e.getAsJsonObject();
            assertThat(countObject.size(), is(2));
            String threatType = countObject.getAsJsonPrimitive("threat_type").getAsString();
            long count = countObject.getAsJsonPrimitive("count").getAsLong();
            switch (threatType){
                case "malware":
                    assertThat(count, is(2L));
                    break;
                case "c&c":
                    assertThat(count, is(1L));
                    break;
                case "blacklist":
                    assertThat(count, is(3L));
                    break;
                case "phishing":
                    assertThat(count, is(1L));
                    break;
                case "exploit":
                    assertThat(count, is(1L));
                    break;
                default:
                    fail("unexpected threat type " + threatType);
            }
        }
    }

    private static JsonObject iocsStats(URL context) throws IOException {
        WebClient webClient = new WebClient();
        WebRequest requestSettings = new WebRequest(new URL(context + "1/ioc/count"), HttpMethod.GET);
        requestSettings.setAdditionalHeader("WB-Client-Id", CLIENT_ID);
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertThat(page.getWebResponse().getStatusCode(), is(HttpURLConnection.HTTP_OK));
        JsonParser parser = new JsonParser();
        return parser.parse(page.getWebResponse().getContentAsString()).getAsJsonObject();
    }
}
