package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertEquals;

public class BasicITTest extends Arquillian {

    private ArchiveInitiator archiveInitiator;
    @BeforeSuite
    public void prepare() throws IOException {
        //example usage
        archiveInitiator = new ArchiveInitiator();
        //archiveInitiator.sendLogEventJsonToArchive("logs.json", ZonedDateTime.now());
        archiveInitiator.cleanEventLogs();
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventLogsTest(@ArquillianResource URL context) throws Exception {
        archiveInitiator.sendLogEvent("logs.json", ZonedDateTime.now()); // 1 log using single file
        archiveInitiator.sendMultipleLogEvents("logs", ZonedDateTime.now()); // two logs using multiple files in dir
        WebClient webClient = new WebClient();
        //WebRequest requestSettings = new WebRequest(new URL(context + "1/events/search?threat_type=c%26c"), HttpMethod.GET);
        WebRequest requestSettings = new WebRequest(new URL(context + "1/events/search"), HttpMethod.GET);
        requestSettings.setAdditionalHeader("whalebone_client_id", "2");
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(page.getWebResponse().getContentAsString());
        // three logs have been sent to archive
        assertThat(element.getAsJsonArray().size(), is(greaterThanOrEqualTo(0)));
    }
}
