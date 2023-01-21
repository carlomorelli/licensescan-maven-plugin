package com.csoft.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mocks.TestArtifact;

@ExtendWith(MockitoExtension.class)
public class ArtifactUtilsTest {

    @Mock
    private MavenProject mavenProject;

    @Test
    public void testGetTransitiveDependencies_WHEN_inputHasNoDeps_THEN_returnsEmptySet() {
        when(mavenProject.getArtifacts()).thenReturn(new HashSet<Artifact>());
        when(mavenProject.getDependencyArtifacts()).thenReturn(new HashSet<Artifact>());
        Set<Artifact> artifacts = ArtifactUtils.getTransitiveDependencies(mavenProject);
        assertTrue(artifacts.isEmpty());
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetTransitiveDependencies_WHEN_inputHasOnlyDirectDeps_THEN_returnsEmptySet() {
        Set<Artifact> directDeps = new HashSet<Artifact>();
        directDeps.add(new TestArtifact("group1", "artifact1", "1.0"));
        directDeps.add(new TestArtifact("group2", "artifact2", "2.0"));
        when(mavenProject.getArtifacts()).thenReturn(new HashSet<Artifact>());
        when(mavenProject.getDependencyArtifacts()).thenReturn(directDeps);
        Set<Artifact> artifacts = ArtifactUtils.getTransitiveDependencies(mavenProject);
        assertTrue(artifacts.isEmpty());
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetTransitiveDependencies_WHEN_inputHasOnlyDepsInGlobalSet_THEN_returnsNonEmptySet() {
        Set<Artifact> allDeps = new HashSet<Artifact>();
        allDeps.add(new TestArtifact("group1", "artifact1", "1.0"));
        allDeps.add(new TestArtifact("group2", "artifact2", "2.0"));
        when(mavenProject.getArtifacts()).thenReturn(allDeps);
        when(mavenProject.getDependencyArtifacts()).thenReturn(new HashSet<Artifact>());
        Set<Artifact> artifacts = ArtifactUtils.getTransitiveDependencies(mavenProject);
        assertFalse(artifacts.isEmpty());
        assertEquals(2, artifacts.size());
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetCumulativeDependencies_WHEN_inputHasDeps_THEN_returnsNonEmptySet() {
        Set<Artifact> allDeps = new HashSet<Artifact>();
        allDeps.add(new TestArtifact("group1", "artifact1", "1.0"));
        Set<Artifact> directDeps = new HashSet<Artifact>();
        allDeps.add(new TestArtifact("group2", "artifact2", "2.0"));
        when(mavenProject.getArtifacts()).thenReturn(allDeps);
        when(mavenProject.getDependencyArtifacts()).thenReturn(directDeps);
        Set<Artifact> artifacts = ArtifactUtils.getCumulativeDependencies(mavenProject);
        assertFalse(artifacts.isEmpty());
        assertEquals(2, artifacts.size());
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

}
