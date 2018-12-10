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

public class BasicITTest extends Arquillian {

    //data should be put into database before test suite starts, as connecting to elastic in arquillian is problematic (it struggles to create new indices)
    @BeforeSuite
    public void prepare() throws IOException {
        //example usage
        ArchiveInitiator.sendLogEventJsonToArchive("logs.json", ZonedDateTime.now());
        ArchiveInitiator.cleanLogs("logs*");
        ArchiveInitiator.sendLogEventJsonToArchive("logs.json", ZonedDateTime.now());
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
    public void trivialTest2(@ArquillianResource URL context) throws Exception {
        WebClient webClient = new WebClient();
        WebRequest requestSettings = new WebRequest(new URL(context + "1/events/search"), HttpMethod.GET);
        requestSettings.setAdditionalHeader("whalebone_client_id", "2");
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
    }
}
