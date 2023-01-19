package com.csoft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.License;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;

import com.csoft.utils.TextUtils;

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

    private final Map<String, List<String>> blacklistedMap = new HashMap<>();

    public MainMojo() {
    }

    public MainMojo(MavenProject proj, MavenSession session, ProjectBuilder builder, Log log) {
        setLog(log);
        this.project = proj;
        this.session = session;
        this.projectBuilder = builder;
        this.blacklistedLicenses = new ArrayList<String>();
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

        if (!blacklistedLicenses.isEmpty()) {
            for (String blacklistedLicense : blacklistedLicenses) {
                blacklistedMap.put(blacklistedLicense, new ArrayList<String>());
            }
        }

        logHeadAnalysis(project);
        logBaseDeps(project);
        logTransitiveDeps(project);

        failBuild();
    }

    private void analyze(Set<Artifact> transitiveDependencies) throws MojoExecutionException {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

        try {
            for (Artifact artifact : transitiveDependencies) {
                String artifactLabel = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
                        + artifact.getVersion() + ":" + artifact.getScope();
                getLog().info(" - artifact " + artifactLabel);
                buildingRequest.setProject(null);
                MavenProject mavenProject = projectBuilder.build(artifact, buildingRequest).getProject();
                if (printLicenses && mavenProject.getLicenses().isEmpty()) {
                    getLog().info("   with license: n/a");
                } else {
                    for (License license : mavenProject.getLicenses()) {
                        if (printLicenses) {
                            getLog().info("   with license: " + license.getName());
                        }
                        Match forbiddenMatch = isForbiddenLicense(license);
                        if (forbiddenMatch.isMatch) {
                            List<String> array = blacklistedMap.get(forbiddenMatch.licenseEntry);
                            array.add(artifactLabel);
                            blacklistedMap.put(forbiddenMatch.licenseEntry, array);
                            getLog().warn("WARNING: found blacklisted license");
                        }
                    }
                }
            }
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException("Error while building project", e);
        }
    }

    private Match isForbiddenLicense(License license) {
        for (String entry : blacklistedMap.keySet()) {
            if (entry.startsWith("regex:")) {
                Pattern p = Pattern.compile(TextUtils.parseAsRegex(entry), Pattern.CASE_INSENSITIVE);
                if (p.matcher(license.getName()).find()) {
                    return new Match(entry);
                }
            } else if (entry.equalsIgnoreCase(license.getName())) {
                return new Match(entry);
            }
        }
        return new Match();
    }

    private class Match {
        final boolean isMatch;
        final String licenseEntry;

        Match() {
            this.isMatch = false;
            this.licenseEntry = null;
        }

        Match(String licenseEntry) {
            this.isMatch = true;
            this.licenseEntry = licenseEntry;
        }
    }

    private void logHeadAnalysis(final MavenProject project) {
        Log log = getLog();
        log.info("Found project: " + project);
        log.info(" - artifactId          : " + project.getArtifactId());
        log.info(" - groupId             : " + project.getGroupId());
        log.info(" - description         : " + project.getDescription());
        log.info(" - version             : " + project.getVersion());
        log.info(" - getArtifact.activeP : " + project.getActiveProfiles());
        log.info(" - getArtifact.artId   : " + project.getArtifact().getArtifactId());
        log.info(" - getArtifact.groupId : " + project.getArtifact().getGroupId());
        log.info(" - getArtifact.version : " + project.getArtifact().getVersion());
        log.info(" - getArtifacts.isEmpty: " + project.getArtifacts().isEmpty());
    }

    private void logBaseDeps(final MavenProject project) throws MojoExecutionException {
        Log log = getLog();
        log.info("");
        log.info("BASE DEPENDENCIES");
        log.info("-----------------------");
        analyze(project.getDependencyArtifacts());
    }

    private void logTransitiveDeps(final MavenProject project) throws MojoExecutionException {
        Log log = getLog();
        log.info("");
        log.info("TRANSITIVE DEPENDENCIES");
        log.info("-----------------------");
        Set<Artifact> transitiveDependencies = project.getArtifacts();
        transitiveDependencies.removeAll(project.getDependencyArtifacts());
        analyze(transitiveDependencies);
    }

    private void failBuild() throws MojoFailureException {
        Log log = getLog();
        boolean potentiallyFailBuild = false;
        if (blacklistedLicenses != null && !blacklistedLicenses.isEmpty()) {
            log.warn("BLACKLIST");
            log.warn("-----------------------");
            for (String blacklistedLicense : blacklistedLicenses) {
                List<String> array = blacklistedMap.get(blacklistedLicense);
                if (!array.isEmpty()) {
                    log.warn("Found " + array.size() + " violations for license '" + blacklistedLicense + "':");
                    for (String artifact : array) {
                        log.warn(" - " + artifact);
                    }
                    potentiallyFailBuild = true;
                }
            }
            if (failBuildOnBlacklisted && potentiallyFailBuild) {
                throw new MojoFailureException("Failing build");
            }
        }
    }
}
