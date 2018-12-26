package com.csoft;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
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
import org.apache.maven.project.*;

import java.util.*;


@Mojo(
        name = "audit",
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

    public MainMojo(){

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

        Map<String, List<String>> blacklistedMap = new HashMap<String, List<String>>();
        if (!blacklistedLicenses.isEmpty()) {
            for (String blacklistedLicense : blacklistedLicenses) {
                blacklistedMap.put(blacklistedLicense, new ArrayList<String>());
            }
        }


        getLog().info("Found project: " + project);
        getLog().info(" - artifactId          : " + project.getArtifactId());
        getLog().info(" - groupId             : " + project.getGroupId());
        getLog().info(" - description         : " + project.getDescription());
        getLog().info(" - version             : " + project.getVersion());
        getLog().info(" - getArtifact.activeP : " + project.getActiveProfiles());
        getLog().info(" - getArtifact.artId   : " + project.getArtifact().getArtifactId());
        getLog().info(" - getArtifact.groupId : " + project.getArtifact().getGroupId());
        getLog().info(" - getArtifact.version : " + project.getArtifact().getVersion());
        getLog().info(" - getArtifacts.isEmpty: " + project.getArtifacts().isEmpty());

        getLog().info("BASE DEPENDENCIES");
        getLog().info("-----------------------");
        for (Dependency dependency : project.getDependencies()) {
            getLog().info(" - " + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion() + ":" + dependency.getScope());
        }

        getLog().info("TRANSITIVE DEPENDENCIES");
        getLog().info("-----------------------");
        Set<Artifact> transitiveDependencies = project.getArtifacts();
        transitiveDependencies.removeAll(project.getDependencyArtifacts());
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

        try {
            for (Artifact artifact : transitiveDependencies) {
                String artifactLabel = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getScope();
                getLog().info(" - artifact " + artifactLabel);
                if (printLicenses) {
                    buildingRequest.setProject(null);
                    MavenProject mavenProject = projectBuilder.build(artifact, buildingRequest).getProject();
                    if (mavenProject.getLicenses().isEmpty()) {
                        getLog().info("   with license: n/a");
                    } else {
                        for (License license : mavenProject.getLicenses()) {
                            getLog().info("   with license: " + license.getName());
                            if (blacklistedMap.keySet().contains(license.getName())) {
                                List<String> array = blacklistedMap.get(license.getName());
                                array.add(artifactLabel);
                                blacklistedMap.put(license.getName(), array);
                                getLog().warn("WARNING: found blacklisted license");
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
            getLog().warn("BLACKLIST");
            getLog().warn("-----------------------");
            for (String blacklistedLicense : blacklistedLicenses) {
                List<String> array = blacklistedMap.get(blacklistedLicense);
                if (!array.isEmpty()) {
                    getLog().warn("Found " + array.size() + " violations for license '" + blacklistedLicense +"':");
                    for (String artifact : array) {
                        getLog().warn(" - " + artifact);
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
