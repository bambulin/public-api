package io.whalebone.publicapi.tests;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

import java.io.File;

@ArquillianSuiteDeployment
public class Deployment {
    @org.jboss.arquillian.container.test.api.Deployment(name = "ear", testable = false)
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(ZipImporter.class, "public-api.ear")
                .importFrom(new File("../ear/target/public-api.ear"))
                .as(EnterpriseArchive.class);
    }
}
