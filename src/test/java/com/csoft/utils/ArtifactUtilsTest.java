package com.csoft.utils;

import mocks.TestArtifact;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.hamcrest.Matchers;
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
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ArtifactUtilsTest {

    private static final Artifact dep1 = new TestArtifact("group1", "artifact1", "1.0", "compile");
    private static final Artifact dep2 = new TestArtifact("group2", "artifact2", "2.0", "compile");
    private static final Artifact testDep = new TestArtifact("group3", "artifact3", "3.0", "test");
    private static final Artifact providedDep = new TestArtifact("group4", "artifact4", "4.0", "provided");

    @Mock
    private MavenProject mavenProject;


    @Test
    public void testGetDirectDependencies_WHEN_inputHasMixedScopeDeps_THEN_returnsSetWithOnlyRuntimeOrCompile() {
        //given
        Set<Artifact> deps = new HashSet() {{
            add(dep1);
            add(dep2);
            add(testDep);
            add(providedDep);
        }};
        when(mavenProject.getDependencyArtifacts()).thenReturn(deps);

        //when
        Set<Artifact> artifacts = ArtifactUtils.getDirectDependencies(mavenProject);

        //then
        assertThat(artifacts, containsInAnyOrder(dep1, dep2));
        assertThat(artifacts, not(containsInAnyOrder(testDep, providedDep)));
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetDirectDependencies_WHEN_inputNoRuntimeOrCompileScopeDeps_THEN_returnsEmptySet() {
        //given
        Set<Artifact> deps = new HashSet() {{
            add(testDep);
            add(providedDep);
        }};
        when(mavenProject.getDependencyArtifacts()).thenReturn(deps);

        //when
        Set<Artifact> artifacts = ArtifactUtils.getDirectDependencies(mavenProject);

        //then
        assertThat(artifacts, empty());
        assertThat(artifacts, not(containsInAnyOrder(testDep, providedDep)));
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }

    @Test
    public void testGetDirectDependencies_WHEN_inputHasNoDeps_THEN_returnsEmptySet() {
        //given
        when(mavenProject.getDependencyArtifacts()).thenReturn(new HashSet<>());

        //when
        Set<Artifact> artifacts = ArtifactUtils.getDirectDependencies(mavenProject);

        //then
        assertThat(artifacts, empty());
        verify(mavenProject, times(1)).getDependencyArtifacts();
    }



    @Test
    public void testGetTransitiveDependencies_WHEN_inputHasNoDeps_THEN_returnsEmptySet() {
        //given
        when(mavenProject.getArtifacts()).thenReturn(new HashSet<>());
        when(mavenProject.getDependencyArtifacts()).thenReturn(new HashSet<>());

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
        when(mavenProject.getArtifacts()).thenReturn(new HashSet<>());
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
        when(mavenProject.getDependencyArtifacts()).thenReturn(new HashSet<>());

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
