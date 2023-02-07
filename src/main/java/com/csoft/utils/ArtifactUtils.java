package com.csoft.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class ArtifactUtils {

    private ArtifactUtils() {}

    /**
     * Extracts the set of Transitive {@link Artifact} dependencies from a input
     * {@link MavenProject}.
     *
     * @param mavenProject The input project.
     * @return Set of dependencies.
     */
    public static Set<Artifact> getTransitiveDependencies(final MavenProject mavenProject) {
        Set<Artifact> transitiveDependencies = new HashSet<>(mavenProject.getArtifacts());
        transitiveDependencies.removeAll(new HashSet<>(mavenProject.getDependencyArtifacts()));
        return transitiveDependencies;
    }

    /**
     * Extracts the set of All {@link Artifact} dependencies (Direct + Transitive)
     * from a input {@link MavenProject}.
     *
     * @param mavenProject The input project.
     * @return Set of dependencies.
     */
    public static Set<Artifact> getCumulativeDependencies(final MavenProject mavenProject) {
        Set<Artifact> cumulativeDependencies = new HashSet<>(mavenProject.getArtifacts());
        cumulativeDependencies.addAll(new HashSet<>(mavenProject.getDependencyArtifacts()));
        return cumulativeDependencies;
    }

}
