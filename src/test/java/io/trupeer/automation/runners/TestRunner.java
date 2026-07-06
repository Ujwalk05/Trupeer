package io.trupeer.automation.runners;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/** `mvn test` runs this suite over every .feature. Glue/plugins in junit-platform.properties. */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class TestRunner {
}
