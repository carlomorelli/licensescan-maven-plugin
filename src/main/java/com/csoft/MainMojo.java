package com.csoft;

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
import org.apache.maven.project.*;

import java.util.*;
import java.util.regex.Pattern;


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

    private final Map<String, List<String>> blacklistedMap = new HashMap<String, List<String>>();

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
        analyze(project.getDependencyArtifacts());

        getLog().info("");
        getLog().info("TRANSITIVE DEPENDENCIES");
        getLog().info("-----------------------");
        Set<Artifact> transitiveDependencies = project.getArtifacts();
        transitiveDependencies.removeAll(project.getDependencyArtifacts());

        analyze(transitiveDependencies);

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

    private void analyze(Set<Artifact> transitiveDependencies) throws MojoExecutionException {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

        try {
            for (Artifact artifact : transitiveDependencies) {
                String artifactLabel = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getScope();
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
        for(String entry : blacklistedMap.keySet()){
            if(entry.startsWith("regex:")){
                Pattern p = Pattern.compile(entry.replace("regex:",""));
                if(p.matcher(license.getName()).find()){
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

        Match(){
            this.isMatch = false;
            this.licenseEntry = null;
        }

        Match(String licenseEntry){
            this.isMatch = true;
            this.licenseEntry = licenseEntry;
        }
    }

}
