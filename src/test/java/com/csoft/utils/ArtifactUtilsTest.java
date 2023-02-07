package com.csoft.utils;

import mocks.TestArtifact;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArtifactUtilsTest {

    private static final Artifact dep1 = new TestArtifact("group1", "artifact1", "1.0");
    private static final Artifact dep2 = new TestArtifact("group2", "artifact2", "2.0");
    @Mock
    private MavenProject mavenProject;

    @Test
    public void testGetTransitiveDependencies_WHEN_inputHasNoDeps_THEN_returnsEmptySet() {
        //given
        when(mavenProject.getArtifacts()).thenReturn(new HashSet<Artifact>());
        when(mavenProject.getDependencyArtifacts()).thenReturn(new HashSet<Artifact>());

        //when
        Set<Artifact> artifacts = ArtifactUtils.getTransitiveDependencies(mavenProject);

        //then
        assertThat(artifacts, empty());
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetTransitiveDependencies_WHEN_inputHasOnlyDirectDeps_THEN_returnsEmptySet() {
        //given
        Set<Artifact> directDeps = new HashSet<>();
        directDeps.add(dep1);
        directDeps.add(dep2);
        when(mavenProject.getArtifacts()).thenReturn(new HashSet<Artifact>());
        when(mavenProject.getDependencyArtifacts()).thenReturn(directDeps);

        //when
        Set<Artifact> artifacts = ArtifactUtils.getTransitiveDependencies(mavenProject);

        //then
        assertThat(artifacts, empty());
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetTransitiveDependencies_WHEN_inputHasOnlyDepsInGlobalSet_THEN_returnsNonEmptySet() {
        //given
        Set<Artifact> allDeps = new HashSet<>();
        allDeps.add(dep1);
        allDeps.add(dep2);
        when(mavenProject.getArtifacts()).thenReturn(allDeps);
        when(mavenProject.getDependencyArtifacts()).thenReturn(new HashSet<Artifact>());

        //when
        Set<Artifact> artifacts = ArtifactUtils.getTransitiveDependencies(mavenProject);

        //then
        assertThat(artifacts.size(), is(2));
        assertThat(artifacts, containsInAnyOrder(dep1, dep2));
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetTransitiveDependencies_WHEN_inputHasDepsInBothSets_THEN_returnsNonEmptySetWithItemNotInCommon() {
        //given
        Set<Artifact> allDeps = new HashSet<>();
        allDeps.add(dep1);
        allDeps.add(dep2);
        Set<Artifact> directDeps = new HashSet<>();
        directDeps.add(dep2);
        when(mavenProject.getArtifacts()).thenReturn(allDeps);
        when(mavenProject.getDependencyArtifacts()).thenReturn(directDeps);

        //when
        Set<Artifact> artifacts = ArtifactUtils.getTransitiveDependencies(mavenProject);

        //then
        assertThat(artifacts.size(), is(1));
        assertThat(artifacts, containsInAnyOrder(dep1));
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetCumulativeDependencies_WHEN_inputHasDeps_THEN_returnsNonEmptySet() {
        //given
        Set<Artifact> allDeps = new HashSet<>();
        allDeps.add(dep1);
        Set<Artifact> directDeps = new HashSet<>();
        allDeps.add(dep2);
        when(mavenProject.getArtifacts()).thenReturn(allDeps);
        when(mavenProject.getDependencyArtifacts()).thenReturn(directDeps);

        //when
        Set<Artifact> artifacts = ArtifactUtils.getCumulativeDependencies(mavenProject);

        //then
        assertThat(artifacts.size(), is(2));
        assertThat(artifacts, containsInAnyOrder(dep1, dep2));
        verify(mavenProject, times(1)).getArtifacts();
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

}
