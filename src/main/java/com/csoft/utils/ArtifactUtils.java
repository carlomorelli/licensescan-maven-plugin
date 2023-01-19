package com.csoft.utils;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class ArtifactUtils {

    public static Set<Artifact> getTransitiveDependencies(final MavenProject mavenProject) {
        Set<Artifact> transitiveDependencies = mavenProject.getArtifacts();
        transitiveDependencies.removeAll(mavenProject.getDependencyArtifacts());
        return transitiveDependencies;
    }

    public static Set<Artifact> getCumulativeDependencies(final MavenProject mavenProject) {
        Set<Artifact> cumulativeDependencies = mavenProject.getArtifacts();
        cumulativeDependencies.addAll(mavenProject.getDependencyArtifacts());
        return cumulativeDependencies;
    }

}
