package com.csoft.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
 * Class that implements processing of a Maven project dependency metadata and
 * extracting the licenses associated to each dependency.
 */
public class DependencyAnalyzer {

    private final MavenSession mavenSession;
    private final ProjectBuilder projectBuilder;

    public DependencyAnalyzer(final MavenSession mavenSession, final ProjectBuilder projectBuilder) {
        this.mavenSession = mavenSession;
        this.projectBuilder = projectBuilder;
    }

    /**
     * Scans input set of {@link Artifact} objects derived from a Maven project
     * and extracts a mapping of all license strings per artifact name in GAV
     * notation.
     * <p>
     * In a Maven project, an Artifact can have 0, 1 or more licenses associated.
     * This function allows to extract all the info in a handy Map of Strings.
     *
     * @param dependencies Set of {@link Artifact} objects
     * @return Map of type {key: artifactGAVLabel, value: listOf[artifact licenses]}
     */
    public Map<String, List<String>> analyze(final Set<Artifact> dependencies) {
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
                List<String> licenseStringList = mavenProject.getLicenses().stream()
                        .map(License::getName)
                        .collect(Collectors.toList());
                licenseMap.put(gavLabel, licenseStringList);
            }
            return licenseMap;
        } catch (ProjectBuildingException e) {
            String projectId = e.getProjectId();
            throw new RuntimeException("Error while building project " + projectId + ". Giving up...", e);
        }
    }

}
