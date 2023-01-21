package com.csoft;

import java.io.File;

import org.junit.jupiter.api.extension.RegisterExtension;

import io.takari.maven.testing.TestResources5;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenPluginTest;

@MavenVersions({"3.8.7"})
public class IntegrationTest {
    @RegisterExtension
    public final TestResources5 resources = new TestResources5();

    public final MavenRuntime maven;

    public IntegrationTest(MavenRuntime.MavenRuntimeBuilder mavenBuilder) throws Exception {
        this.maven = mavenBuilder.withCliOptions("-B", "-U").build();
    }

    @MavenPluginTest
    public void test_Success() throws Exception {
        File basedir = resources.getBasedir("integration_pass");
        System.out.println(basedir.getAbsolutePath());
        maven.forProject(basedir)
                .execute("licensescan:audit")
                .assertErrorFreeLog();
    }

    @MavenPluginTest
    public void test_Fail() throws Exception {
        File basedir = resources.getBasedir("integration_fail");
        System.out.println(basedir.getAbsolutePath());
        maven.forProject(basedir)
                .execute("licensescan:audit")
                .assertLogText("[ERROR]");
    }
}