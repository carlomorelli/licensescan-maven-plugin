package com.csoft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;
import mocks.TestLog;
import mocks.TestProjectBuilder;
import mocks.TestUtils;

public class MainMojoTest {
    private final String goodLicense = "Happy Freedom License v1";
    private final String badLicense = "Bad Banned License v2";
    private final String regex = "regex:Apache.*Version 1.*";

    private final Set<Artifact> empty = Collections.emptySet();
    @Rule
    public TestResources testResources = new TestResources();
    @Rule
    public final TestMavenRuntime maven = new TestMavenRuntime();
    private TestLog log;
    private TestProjectBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new TestProjectBuilder();
    }

    @Test
    public void basicTestWithNoDependencies() throws Exception {
        MainMojo mojo = configure(empty, empty);

        mojo.execute();

        log.assertInfo(" - artifactId          : test-project");
        log.assertInfo(" - groupId             : com.acme.test.co");
        log.assertInfo(" - description         : A nice test pom");
        log.assertInfo(" - version             : 1");
        log.assertInfo(" - getArtifact.activeP : ");
        log.assertInfo(" - getArtifact.artId   : test-project");
        log.assertInfo(" - getArtifact.groupId : com.acme.test.co");
        log.assertInfo(" - getArtifact.version : 1");
        log.assertInfo(" - getArtifacts.isEmpty: true");
    }

    @Test
    public void passesWithAllGoodLicenses() throws Exception {
        MainMojo mojo = configure(builder.createArtifact("acme", "main", "2", goodLicense),
                builder.createArtifact("acme", "artifact", "1", goodLicense));

        mojo.execute();

        log.assertNoWarning("Found 1 violations for license 'Bad Banned License v2':");
    }

    @Test
    public void willFailWhenBannedLicenseShowsUpInTransient() throws Exception {
        MainMojo mojo = configure(empty, builder.createArtifact("acme", "artifact", "1", badLicense));

        try {
            mojo.execute();
            fail("should have thrown error");
        } catch (MojoFailureException e) {
            assertEquals("Failing build", e.getMessage());
            log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
            log.assertWarning(" - acme:artifact:1:null");
        }
    }

    @Test
    public void doesFailOnDirectDependencies() throws Exception {
        MainMojo mojo = configure(builder.createArtifact("acme", "artifact", "1", badLicense), empty);

        try {
            mojo.execute();
            fail("should have thrown error");
        } catch (MojoFailureException e) {
            assertEquals("Failing build", e.getMessage());
            log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
            log.assertWarning(" - acme:artifact:1:null");
        }
    }

    @Test
    public void canUseRegex() throws Exception {
        MainMojo mojo = configure(builder.createArtifact("acme", "artifact", "1", "Apache License, Version 1.0"),
                builder.createArtifact("acme", "else", "1", "Apache License, Version 2.0"));

        try {
            mojo.execute();
            fail("should have thrown error");
        } catch (MojoFailureException e) {
            assertEquals("Failing build", e.getMessage());
            log.assertWarning("Found 1 violations for license 'regex:Apache.*Version 1.*':");
            log.assertWarning(" - acme:artifact:1:null");
        }
    }

    @Test
    public void regexisNotCaseSensitive() throws Exception {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "artifact", "1", "Apache License, Version 1.0".toUpperCase()), empty);

        try {
            mojo.execute();
            fail("should have thrown error");
        } catch (MojoFailureException e) {
            assertEquals("Failing build", e.getMessage());
            log.assertWarning("Found 1 violations for license 'regex:Apache.*Version 1.*':");
            log.assertWarning(" - acme:artifact:1:null");
        }
    }

    @Test
    public void doesNotAnalyzePrimaryDependenciesAsTransient() throws Exception {
        MainMojo mojo = configure(builder.createArtifact("acme", "artifact", "1", badLicense),
                builder.createArtifact("acme", "artifact", "1", badLicense));

        try {
            mojo.execute();
            fail("should have thrown error");
        } catch (MojoFailureException e) {
            assertEquals("Failing build", e.getMessage());
            log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
            log.assertWarning(" - acme:artifact:1:null");
        }
    }

    @Test
    public void doesNotFailUnlessPrintLicensesIsOn() throws Exception {
        MainMojo mojo = configure(empty, builder.createArtifact("acme", "artifact", "1", badLicense));

        mojo.setPrintLicenses(false);

        try {
            mojo.execute();
            fail("should have thrown error");
        } catch (MojoFailureException e) {
            assertEquals("Failing build", e.getMessage());
            log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
            log.assertWarning(" - acme:artifact:1:null");
        }
    }

    @Test
    public void doesFailWithoutOverride() throws Exception {
        MainMojo mojo = configure(builder.createArtifact("acme", "artifact", "1", goodLicense, badLicense), empty);

        mojo.setOverruleOnNotBlacklisted(false);

        try {
            mojo.execute();
            fail("should have thrown error");
        } catch (MojoFailureException e) {
            assertEquals("Failing build", e.getMessage());
            log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
            log.assertWarning(" - acme:artifact:1:null");
        }
    }

    @Test
    public void doesNotFailWithOverride() throws Exception {
        MainMojo mojo = configure(builder.createArtifact("acme", "artifact", "1", goodLicense, badLicense), empty);

        mojo.setOverruleOnNotBlacklisted(true);

        mojo.execute();
        log.assertNoWarning("Found 1 violations for license 'Bad Banned License v2':");
    }

    private MainMojo configure(Set<Artifact> primaryArtifacts, Set<Artifact> transientArtifacts) {
        try {
            MavenProject proj = maven.readMavenProject(testResources.getBasedir("basic"));
            proj.setArtifacts(TestUtils.union(primaryArtifacts, transientArtifacts));
            proj.setDependencyArtifacts(primaryArtifacts);
            MavenSession session = maven.newMavenSession(proj);
            log = new TestLog();
            MainMojo mainMojo = new MainMojo(proj, session, builder, log);
            mainMojo.setPrintLicenses(true);
            mainMojo.setFailBuildOnBlacklisted(true);
            mainMojo.setBlacklistedLicenses(Arrays.asList(badLicense, regex));
            return mainMojo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
