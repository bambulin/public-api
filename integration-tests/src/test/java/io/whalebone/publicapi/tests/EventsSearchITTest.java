package io.whalebone.publicapi.tests;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

public class EventsSearchITTest {

//    private ArchiveInitiator archiveInitiator;
//
//    public EventsSearchITTest() {
//        archiveInitiator = new ArchiveInitiator();
//    }
//
//    @Deployment(name = "ear", testable = false)
//    public static Archive<?> createTestArchive() {
//        return ShrinkWrap.create(ZipImporter.class, "public-api.ear")
//                .importFrom(new File("../ear/target/public-api.ear"))
//                .as(EnterpriseArchive.class);
//    }
//
//    @BeforeSuite
//    public void prepare() throws IOException {
//        archiveInitiator.cleanEventLogs();
//        archiveInitiator.sendLogEventJsonToArchive("logs/", ZonedDateTime.now());
//    }
//
//    public void eventsSearchByTestClientIpTest() {
//
//    }
}
