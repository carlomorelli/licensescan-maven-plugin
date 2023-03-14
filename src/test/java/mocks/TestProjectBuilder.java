package mocks;

import com.google.common.collect.ComparisonChain;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.License;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TestProjectBuilder implements ProjectBuilder {
    private Map<Artifact, TreeSet<License>> licenses = new HashMap<>();

    public Set<Artifact> createArtifact(String group, String artifact, String version, Set<String> licenseStrings) {
        // Note: setting the scope as 'compile' as it is the default scope in Maven when not specified
        //(see https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope)
        return createArtifact(group, artifact, version, "compile", licenseStrings);
    }

    public Set<Artifact> createArtifact(String group, String artifact, String version, String scope, Set<String> licenseStrings) {
        Artifact a = new TestArtifact(group, artifact, version, scope);
        if (!licenses.containsKey(a)) {
            licenses.put(a, new TreeSet<>((o1, o2) -> ComparisonChain.start()
                    .compare(o1.getName(), o2.getName())
                    .result()));
        }
        licenses.get(a).addAll(toLicenses(licenseStrings));
        return Collections.singleton(a);
    }

    private static Collection<License> toLicenses(Set<String> licenseStrings) {
        return licenseStrings.stream()
                .map(licenseString -> {
                    License license = new License();
                    license.setName(licenseString);
                    return license;
                })
                .collect(Collectors.toSet());
    }

    public ProjectBuildingResult build(File projectFile, ProjectBuildingRequest request) throws ProjectBuildingException {
        return null;
    }

    public ProjectBuildingResult build(Artifact projectArtifact, ProjectBuildingRequest request) throws ProjectBuildingException {
        return new TestProjectResult(projectArtifact, licenses.get(projectArtifact));
    }

    public ProjectBuildingResult build(Artifact projectArtifact, boolean allowStubModel, ProjectBuildingRequest request) throws ProjectBuildingException {
        return null;
    }

    public ProjectBuildingResult build(ModelSource modelSource, ProjectBuildingRequest request) throws ProjectBuildingException {
        return null;
    }

    public List<ProjectBuildingResult> build(List<File> pomFiles, boolean recursive, ProjectBuildingRequest request) throws ProjectBuildingException {
        return null;
    }

    public static class TestProjectResult implements ProjectBuildingResult {
        private final MavenProject project;

        public TestProjectResult(Artifact projectArtifact, Set<License> licenses) {
            project = new MavenProject();
            project.setArtifact(projectArtifact);
            if (licenses != null) {
                project.setLicenses(new ArrayList<>(licenses));
            } else {
                project.setLicenses(new ArrayList<License>());
            }
        }

        public String getProjectId() {
            return null;
        }

        public File getPomFile() {
            return null;
        }

        public MavenProject getProject() {
            return project;
        }

        public List<ModelProblem> getProblems() {
            return null;
        }

        public DependencyResolutionResult getDependencyResolutionResult() {
            return null;
        }
    }


}
