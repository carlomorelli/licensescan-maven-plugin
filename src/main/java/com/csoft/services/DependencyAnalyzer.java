package com.csoft.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.License;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;

/**
 * Service that allows to process the dependency space metadata and extract the
 * licenses
 * associated to each artifact.
 */
public class DependencyAnalyzer {

    private final MavenSession mavenSession;
    private final ProjectBuilder projectBuilder;

    public DependencyAnalyzer(final MavenSession mavenSession, final ProjectBuilder projectBuilder) {
        this.mavenSession = mavenSession;
        this.projectBuilder = projectBuilder;
    }

    /**
     * Scans dependencies in input of type {@link Artifact} and return
     * all extracted licenses per artifact name in GAV notation.
     * 
     * @param dependencies
     * @return
     */
    public Map<String, List<String>> analyze(Set<Artifact> dependencies) {
        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(
                mavenSession.getProjectBuildingRequest());
        buildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);

        Map<String, List<String>> licenseMap = new HashMap<>();
        try {
            for (Artifact artifact : dependencies) {
                String gavLabel = String.format("%s:%s:%s:%s",
                        artifact.getGroupId(),
                        artifact.getArtifactId(),
                        artifact.getVersion(),
                        artifact.getScope());
                buildingRequest.setProject(null);
                MavenProject mavenProject = projectBuilder.build(artifact, buildingRequest).getProject();
                List<String> licenseStringList = new ArrayList<>();
                for (License license : mavenProject.getLicenses()) {
                    licenseStringList.add(license.getName());
                }
                licenseMap.put(gavLabel, licenseStringList);
            }
            return licenseMap;
        } catch (ProjectBuildingException e) {
            String projectId = e.getProjectId();
            throw new RuntimeException("Error while building project " + projectId + ". Giving up...", e);
        }
    }

}
