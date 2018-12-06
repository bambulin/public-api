package io.whalebone.publicapi.tests;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;

import java.io.File;
import java.net.URL;
import java.net.HttpURLConnection;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class BasicITTest extends Arquillian {

    @Deployment(name = "ear", testable = false)
    public static Archive<?> createTestArchive() {
        EnterpriseArchive ear = ShrinkWrap.create(ZipImporter.class, "public-api.ear")
                .importFrom(new File("../ear/target/public-api.ear"))
                .as(EnterpriseArchive.class);
        ear.getAsType(JavaArchive.class, "public-api-ejb.jar").addClass(BasicITTest.class);
        return ear;
    }

    @Test(enabled = false, dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    @OperateOnDeployment("ear")
    @RunAsClient
    public void trivialTest2(@ArquillianResource URL context) throws Exception {
        WebClient webClient = new WebClient();
        WebRequest requestSettings = new WebRequest(new URL(context + "1/events/search?days=1"), HttpMethod.GET);
        requestSettings.setAdditionalHeader("accept", "application/json");
        Page page = webClient.getPage(requestSettings);
        assertEquals(HttpURLConnection.HTTP_OK, page.getWebResponse().getStatusCode());
    }
}
