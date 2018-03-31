package com.csoft;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.*;

import java.util.*;


@Mojo(
        name = "sayshi",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class MainMojo extends AbstractMojo {

    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;

    @Parameter(defaultValue="${session}", readonly=true, required=true)
    private MavenSession session;

    @Component
    private ProjectBuilder projectBuilder;

    @Parameter(property = "printLicenses", defaultValue = "false")
    private boolean printLicenses;

    @Parameter(property = "blacklistedLicenses")
    private List<String> blacklistedLicenses;

    @Parameter(property = "failBuildOnBlacklisted", defaultValue = "false")
    private boolean failBuildOnBlacklisted;

    private Log log = getLog();

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

        Map<String, List<String>> blacklistedMap = new HashMap<String, List<String>>();
        if (!blacklistedLicenses.isEmpty()) {
            for (String blacklistedLicense : blacklistedLicenses) {
                blacklistedMap.put(blacklistedLicense, new ArrayList<String>());
            }
        }


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

        log.info("BASE DEPENDENCIES");
        log.info("-----------------------");
        for (Dependency dependency : project.getDependencies()) {
            log.info(" - " + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion() + ":" + dependency.getScope());
        }

        log.info("TRANSITIVE DEPENDENCIES");
        log.info("-----------------------");
        Set<Artifact> transitiveDependencies = project.getArtifacts();
        transitiveDependencies.removeAll(project.getDependencyArtifacts());
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());

        try {
            for (Artifact artifact : transitiveDependencies) {
                String artifactLabel = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getScope();
                log.info(" - artifact " + artifactLabel);
                if (printLicenses) {
                    buildingRequest.setProject(null);
                    MavenProject mavenProject = projectBuilder.build(artifact, buildingRequest).getProject();
                    if (mavenProject.getLicenses().isEmpty()) {
                        log.info("   with license: n/a");
                    } else {
                        for (License license : mavenProject.getLicenses()) {
                            log.info("   with license: " + license.getName());
                            if (blacklistedMap.keySet().contains(license.getName())) {
                                List<String> array = blacklistedMap.get(license.getName());
                                array.add(artifactLabel);
                                blacklistedMap.put(license.getName(), array);
                                log.warn("WARNING: found blacklisted license");
                            }
                        }
                    }
                }
            }
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException("Error while building project", e);
        }

        boolean potentiallyFailBuild = false;
        if (blacklistedLicenses != null && !blacklistedLicenses.isEmpty()) {
            log.warn("BLACKLIST");
            log.warn("-----------------------");
            for (String blacklistedLicense : blacklistedLicenses) {
                List<String> array = blacklistedMap.get(blacklistedLicense);
                if (!array.isEmpty()) {
                    log.warn("Found " + array.size() + " violations for license '" + blacklistedLicense +"':");
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
