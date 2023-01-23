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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TestProjectBuilder implements ProjectBuilder {
    private Map<Artifact, TreeSet<License>> licenses = new HashMap<Artifact, TreeSet<License>>();

    public Set<Artifact> createArtifact(String group, String artifact, String version, String... licenseStrings) {
        Artifact a = new TestArtifact(group, artifact, version);
        if (!licenses.containsKey(a)) {
            licenses.put(a, new TreeSet<License>(new Comparator<License>() {
                public int compare(License o1, License o2) {
                    return ComparisonChain.start()
                            .compare(o1.getName(), o2.getName())
                            .result();
                }
            }));
        }
        licenses.get(a).addAll(toLicenses(licenseStrings));
        return Collections.singleton(a);
    }

    private Collection<License> toLicenses(String[] licenseStrings) {
        Set<License> set = new HashSet<License>();
        for (String s : licenseStrings) {
            License l = new License();
            l.setName(s);
            set.add(l);
        }
        return set;
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
