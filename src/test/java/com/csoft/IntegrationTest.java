package com.csoft;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.2.3", "3.2.5", "3.6.0"})
public class IntegrationTest {
    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime maven;

    public IntegrationTest(MavenRuntime.MavenRuntimeBuilder mavenBuilder) throws Exception {
        this.maven = mavenBuilder.withCliOptions("-B", "-U").build();
    }

    @Test
    public void test_Success() throws Exception {
        File basedir = resources.getBasedir("integration_pass");
        System.out.println(basedir.getAbsolutePath());
        maven.forProject(basedir).execute("licensescan:audit").assertErrorFreeLog();
    }

    @Test
    public void test_Fail() throws Exception {
        File basedir = resources.getBasedir("integration_fail");
        System.out.println(basedir.getAbsolutePath());
        maven.forProject(basedir).execute("licensescan:audit").assertLogText("[ERROR]");
    }

    @Test
    public void test_Bundle_Success() throws Exception {
        File basedir = resources.getBasedir("integration_bundle_pass");
        System.out.println(basedir.getAbsolutePath());
        maven.forProject(basedir).execute("licensescan:audit").assertErrorFreeLog();
    }

    @Test
    public void test_Bundle_Fail() throws Exception {
        File basedir = resources.getBasedir("integration_bundle_fail");
        System.out.println(basedir.getAbsolutePath());
        maven.forProject(basedir).execute("licensescan:audit").assertLogText("[ERROR]");
    }
}