package com.csoft;

import io.takari.maven.testing.TestResources5;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenPluginTest;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;

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

    //To be removed in next major version, when non-inclusive terms are removed
    @MavenPluginTest
    public void test_Success_With_Noninclusive_Term() throws Exception {
        File basedir = resources.getBasedir("integration_pass_old");
        System.out.println(basedir.getAbsolutePath());
        maven.forProject(basedir)
                .execute("licensescan:audit")
                .assertErrorFreeLog();
    }

    //To be removed in next major version, when non-inclusive terms are removed
    @MavenPluginTest
    public void test_Fail_With_Noninclusive_Term() throws Exception {
        File basedir = resources.getBasedir("integration_fail_old");
        System.out.println(basedir.getAbsolutePath());
        maven.forProject(basedir)
                .execute("licensescan:audit")
                .assertLogText("[ERROR]");
    }
}