package com.csoft;

import io.takari.maven.testing.TestMavenRuntime5;
import io.takari.maven.testing.TestResources5;
import mocks.TestLog;
import mocks.TestProjectBuilder;
import mocks.TestUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MainMojoTest {
    private final String goodLicense = "Happy Freedom License v1";
    private final String badLicense = "Bad Banned License v2";
    private final String regex = "regex:Apache.*Version 1.*";

    private final Set<Artifact> empty = Collections.emptySet();

    @RegisterExtension
    public TestResources5 testResources = new TestResources5();

    @RegisterExtension
    public final TestMavenRuntime5 maven = new TestMavenRuntime5();

    private TestLog log;
    private TestProjectBuilder builder;

    @BeforeEach
    public void beforeEach() {
        builder = new TestProjectBuilder();
    }

    @Test
    public void test_WHEN_noForbiddenConfigured_THEN_buildPasses() throws Exception {
        MainMojo mojo = configure(empty, empty);
        mojo.execute();
        log.assertInfo(" - artifactId  : test-project");
        log.assertInfo(" - groupId     : com.acme.test.co");
        log.assertInfo(" - description : A nice test pom");
        log.assertInfo(" - version     : 1");
    }

    @Test
    public void test_WHEN_noArtifactForbidden_THEN_buildPasses() throws Exception {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "main", "2", goodLicense),
                builder.createArtifact("acme", "artifact", "1", goodLicense));
        mojo.execute();
        log.assertNoWarning("Found 1 violations for license 'Bad Banned License v2':");
    }

    @Test
    public void test_WHEN_artifactForbiddenInDirectDeps_THEN_buildFails() {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "artifact", "1", badLicense),
                empty);
        Exception e = assertThrows(
                MojoFailureException.class,
                mojo::execute
        );
        assertEquals("Failing build", e.getMessage());
        log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
        log.assertWarning(" - acme:artifact:1:null");
    }

    @Test
    public void test_WHEN_artifactForbiddenInTransitiveDeps_THEN_buildFails() {
        MainMojo mojo = configure(
                empty,
                builder.createArtifact("acme", "artifact", "1", badLicense));
        Exception e = assertThrows(
                MojoFailureException.class,
                mojo::execute
        );
        assertEquals("Failing build", e.getMessage());
        log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
        log.assertWarning(" - acme:artifact:1:null");
    }

    @Test
    public void test_WHEN_artifactForbiddenInBothDeps_THEN_buildFails() {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "artifact", "1", badLicense),
                builder.createArtifact("acme", "artifact", "1", badLicense));
        Exception e = assertThrows(
                MojoFailureException.class,
                mojo::execute
        );
        assertEquals("Failing build", e.getMessage());
        log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
        log.assertWarning(" - acme:artifact:1:null");
    }

    @Test
    public void test_WHEN_artifactForbiddenWithRegex_THEN_buildFails() {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "artifact", "1", "Apache License, Version 1.0"),
                builder.createArtifact("acme", "else", "1", "Apache License, Version 2.0"));
        Exception e = assertThrows(
                MojoFailureException.class,
                mojo::execute
        );
        assertEquals("Failing build", e.getMessage());
        log.assertWarning("Found 1 violations for license 'regex:Apache.*Version 1.*':");
        log.assertWarning(" - acme:artifact:1:null");
    }

    @Test
    public void test_WHEN_artifactForbiddenWithRegexWithAllCaps_THEN_buildFails() {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "artifact", "1", "Apache License, Version 1.0".toUpperCase()),
                empty);
        Exception e = assertThrows(
                MojoFailureException.class,
                mojo::execute
        );
        assertEquals("Failing build", e.getMessage());
        log.assertWarning("Found 1 violations for license 'regex:Apache.*Version 1.*':");
        log.assertWarning(" - acme:artifact:1:null");
    }

    @Test
    public void test_WHEN_artifactDualLicensedAndOnlyOneForbidden_THEN_buildPasses() throws Exception {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "artifact", "1", badLicense, goodLicense),
                empty);
        mojo.execute();
        log.assertNoWarning("Found 1 violations for license 'Bad Banned License v2':");
    }

    @Test
    public void test_WHEN_artifactDualLicensedAndBothForbidden_THEN_buildFails() {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "artifact", "1", badLicense, "Apache, Version 1.0"),
                empty);
        Exception e = assertThrows(
                MojoFailureException.class,
                mojo::execute
        );
        assertEquals("Failing build", e.getMessage());
        log.assertWarning("Found 1 violations for license 'regex:Apache.*Version 1.*':");
        log.assertWarning(" - acme:artifact:1:null");
    }

    @Test
    public void test_WHEN_artifactHasNoLicenseAtAll_THEN_buildFails() {
        MainMojo mojo = configure(
                builder.createArtifact("acme", "artifact", "1"),
                builder.createArtifact("acme", "depArtifact", "1"));
        Exception e = assertThrows(
                MojoFailureException.class,
                mojo::execute
        );
        assertEquals("Failing build", e.getMessage());
        log.assertWarning("Found 2 violations for license 'NONE':");
        log.assertWarning(" - acme:artifact:1:null");
        log.assertWarning(" - acme:depArtifact:1:null");
    }


    @Test
    public void test_WHEN_printLicensesIsOff_THEN_buildFails() {
        MainMojo mojo = configure(
                empty,
                builder.createArtifact("acme", "artifact", "1", badLicense));
        mojo.setPrintLicenses(false);
        Exception e = assertThrows(
                MojoFailureException.class,
                mojo::execute
        );
        assertEquals("Failing build", e.getMessage());
        log.assertWarning("Found 1 violations for license 'Bad Banned License v2':");
        log.assertWarning(" - acme:artifact:1:null");
    }

    private MainMojo configure(Set<Artifact> primaryArtifacts,
                               Set<Artifact> transientArtifacts) {
        try {
            MavenProject proj = maven.readMavenProject(testResources.getBasedir("basic"));
            proj.setArtifacts(TestUtils.union(primaryArtifacts, transientArtifacts));
            proj.setDependencyArtifacts(primaryArtifacts);
            MavenSession session = maven.newMavenSession(proj);
            log = new TestLog();
            MainMojo mainMojo = new MainMojo(proj, session, builder, log);
            mainMojo.setPrintLicenses(true);
            mainMojo.setFailBuildOnViolation(true);
            mainMojo.setForbiddenLicenses(Arrays.asList(badLicense, regex));
            return mainMojo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
