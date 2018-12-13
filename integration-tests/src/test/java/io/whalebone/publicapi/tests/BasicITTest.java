package io.whalebone.publicapi.tests;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.time.ZonedDateTime;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BasicITTest extends Arquillian {

    ArchiveInitiator archiveInitiator;
    @BeforeSuite
    public void prepare() throws IOException {
        //example usage
        archiveInitiator = new ArchiveInitiator();
        //archiveInitiator.sendLogEventJsonToArchive("logs.json", ZonedDateTime.now());
        archiveInitiator.cleanEventLogs();
    }
    @Deployment(name = "ear", testable = false)
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(ZipImporter.class, "public-api.ear")
                .importFrom(new File("../ear/target/public-api.ear"))
                .as(EnterpriseArchive.class);
    }

    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void eventLogsTest(@ArquillianResource URL context) throws Exception {
        archiveInitiator.sendLogEventJsonToArchive("logs.json", ZonedDateTime.now());
        WebClient webClient = new WebClient();
        //TODO: c&c queries don't work atm
        //WebRequest requestSettings = new WebRequest(new URL(context + "1/events/search?threat_type=c%26c"), HttpMethod.GET);
        WebRequest requestSettings = new WebRequest(new URL(context + "1/events/search"), HttpMethod.GET);
        requestSettings.setAdditionalHeader("whalebone_client_id", "2");
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        String expected = "\"threat_type\":[\"c\\u0026c\"]";
        assertTrue(page.getWebResponse().getContentAsString().contains(expected), "Expected: " + expected + ", got: " + page.getWebResponse().getContentAsString());
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
    }
}
