package com.csoft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;

import com.csoft.services.BuildLogger;
import com.csoft.services.DependencyAnalyzer;
import com.csoft.services.LicenseScanner;
import com.csoft.utils.ArtifactUtils;

/**
 * Main Mojo for the LicenseScan Maven Plugin.
 * <p>
 * (C) 2018-2023 Carlo Morelli
 * Released with MIT License
 */
@Mojo(name = "audit", requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MainMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Component
    private ProjectBuilder projectBuilder;

    @Parameter(property = "printLicenses", defaultValue = "false")
    private boolean printLicenses;

    @Parameter(property = "blacklistedLicenses")
    private List<String> blacklistedLicenses;

    @Parameter(property = "failBuildOnBlacklisted", defaultValue = "false")
    private boolean failBuildOnBlacklisted;

    public MainMojo() {
    }

    public MainMojo(final MavenProject proj,
                    final MavenSession session,
                    final ProjectBuilder builder,
                    final Log log) {
        setLog(log);
        this.project = proj;
        this.session = session;
        this.projectBuilder = builder;
        this.blacklistedLicenses = new ArrayList<>();
    }

    public void setPrintLicenses(boolean printLicenses) {
        this.printLicenses = printLicenses;
    }

    public void setBlacklistedLicenses(List<String> blacklistedLicenses) {
        this.blacklistedLicenses = blacklistedLicenses;
    }

    public void setFailBuildOnBlacklisted(boolean failBuildOnBlacklisted) {
        this.failBuildOnBlacklisted = failBuildOnBlacklisted;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        DependencyAnalyzer dependencyAnalizer = new DependencyAnalyzer(session, projectBuilder);
        LicenseScanner licenseScanner = new LicenseScanner(dependencyAnalizer, blacklistedLicenses);
        BuildLogger buildLogger = new BuildLogger(dependencyAnalizer, project, getLog());

        buildLogger.logHeadAnalysis();
        buildLogger.logBaseDeps(printLicenses);
        buildLogger.logTransitiveDeps(printLicenses);

        Map<String, List<String>> cumulativeForbiddenMap = licenseScanner
                .scan(ArtifactUtils.getCumulativeDependencies(project));
        failBuild(cumulativeForbiddenMap);
    }

    private void failBuild(final Map<String, List<String>> forbiddenMap) throws MojoFailureException {
        Log log = getLog();
        boolean potentiallyFailBuild = false;
        if (blacklistedLicenses != null && !blacklistedLicenses.isEmpty()) {
            log.info("");
            log.warn("BLACKLIST");
            log.warn("-----------------------");
            for (String blacklistedLicense : blacklistedLicenses) {
                List<String> array = forbiddenMap.get(blacklistedLicense);
                // if (!array.isEmpty()) {
                log.warn("Found " + array.size() + " violations for license '" + blacklistedLicense + "':");
                for (String artifact : array) {
                    log.warn(" - " + artifact);
                }
                if (!array.isEmpty()) {
                    potentiallyFailBuild = true;
                }
            }
            if (failBuildOnBlacklisted && potentiallyFailBuild) {
                throw new MojoFailureException("Failing build");
            }
        }
    }
}
