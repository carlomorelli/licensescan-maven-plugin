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
        if (forbiddenLicenses != null && !forbiddenLicenses.isEmpty()) {
            log.info("");
            log.warn("FORBIDDEN LICENSES");
            log.warn("-----------------------");
            log.info(
                    "NOTE: For artifacts with multiple licenses, violation will be marked only when all licenses match the denylist.");
            for (String forbiddenLicense : forbiddenLicenses) {
                List<String> array = forbiddenMap.get(forbiddenLicense);
                // if (!array.isEmpty()) {
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
