package com.csoft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csoft.services.ReportBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.artifact.Artifact;
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
@Mojo(
        name = "audit",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class MainMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    // NOTE: this injection is needed when running integration tests
    //@Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    //private String buildDirectory;

    @Component
    private ProjectBuilder projectBuilder;

    @Parameter(property = "printLicenses", defaultValue = "false")
    private boolean printLicenses;

    @Parameter(property = "forbiddenLicenses", alias = "blacklistedLicenses")
    private List<String> forbiddenLicenses;

    @Parameter(property = "failBuildOnViolation", alias = "failBuildOnBlacklisted", defaultValue = "false")
    private boolean failBuildOnViolation;

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
        this.forbiddenLicenses = new ArrayList<>();
    }

    public void setPrintLicenses(boolean printLicenses) {
        this.printLicenses = printLicenses;
    }

    public void setForbiddenLicenses(List<String> forbiddenLicenses) {
        this.forbiddenLicenses = forbiddenLicenses;
    }

    public void setFailBuildOnViolation(boolean failBuildOnViolation) {
        this.failBuildOnViolation = failBuildOnViolation;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        printWarning();
        DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(session, projectBuilder);
        LicenseScanner licenseScanner = new LicenseScanner(dependencyAnalyzer, forbiddenLicenses);
        BuildLogger buildLogger = new BuildLogger(dependencyAnalyzer, project, getLog());
        ReportBuilder reportBuilder = new ReportBuilder(project);

        buildLogger.logHeadAnalysis();
        buildLogger.logBaseDeps(printLicenses);
        buildLogger.logTransitiveDeps(printLicenses);

        //Set<Artifact> directDeps = ArtifactUtils.getCumulativeDependencies(project);
        //Set<Artifact> transitiveDeps = ArtifactUtils.getCumulativeDependencies(project);
        Set<Artifact> allDeps = ArtifactUtils.getCumulativeDependencies(project);
        Map<String, List<String>> licensesMap = dependencyAnalyzer.analyze(allDeps);
        Map<String, List<String>> violationsMap = licenseScanner.scan(allDeps);
        try {
            String jsonFile = reportBuilder.buildReport(licensesMap, violationsMap);
            getLog().info("JSON report generated: " + jsonFile);
            String htmlFile = reportBuilder.buildHtmlReport(licensesMap, violationsMap);
            getLog().info("HTML report generated: " + htmlFile);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
        failBuild(violationsMap);
    }

    private void failBuild(final Map<String, List<String>> violationsMap) throws MojoFailureException {
        Log log = getLog();
        boolean potentiallyFailBuild = false;
        if (forbiddenLicenses != null && !forbiddenLicenses.isEmpty()) {
            log.info("");
            log.warn("FORBIDDEN LICENSES");
            log.warn("-----------------------");
            log.info(
                    "NOTE: For artifacts with multiple licenses, violation will be marked only when all licenses match the denylist.");
            for (String forbiddenLicense : forbiddenLicenses) {
                List<String> array = violationsMap.get(forbiddenLicense);
                log.warn("Found " + array.size() + " violations for license '" + forbiddenLicense + "':");
                for (String artifact : array) {
                    log.warn(" - " + artifact);
                }
                if (!array.isEmpty()) {
                    potentiallyFailBuild = true;
                }
            }
            if (failBuildOnViolation && potentiallyFailBuild) {
                throw new MojoFailureException("Failing build");
            }
        }
    }

    private void printWarning() {
        Log log = getLog();
        log.warn("+--------------------------------------------------------------------------------------------------------+");
        log.warn("| Usage of configuration terms 'blacklistedLicenses' and 'failBuildOnBlacklisted' is deprecated, and it  |");
        log.warn("| will be removed in the next major release. Use 'forbiddenLicenses' and 'failBuildOnViolation' instead. |");
        log.warn("+--------------------------------------------------------------------------------------------------------+");
    }

}
