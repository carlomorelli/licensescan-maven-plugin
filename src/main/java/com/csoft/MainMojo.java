package com.csoft;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.Set;


@Mojo(
        name = "sayshi",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class MainMojo extends AbstractMojo {

    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject project;

    @Parameter(property = "person", defaultValue = "world")
    private String person;

    private Log log = getLog();

    public void setPerson(String person) {
        this.person = person;
    }

    public void execute() {
        log.info("Hello " + person);
        log.info("Found project: " + project);
        log.info(" - artifactId          : " + project.getArtifactId());
        log.info(" - groupId             : " + project.getGroupId());
        log.info(" - description         : " + project.getDescription());
        log.info(" - version             : " + project.getVersion());
        log.info(" - getArtifact.activeP : " + project.getActiveProfiles());
        log.info(" - getArtifact.artId   : " + project.getArtifact().getArtifactId());
        log.info(" - getArtifact.groupId : " + project.getArtifact().getGroupId());
        log.info(" - getArtifact.version : " + project.getArtifact().getVersion());
        log.info(" - getArtifacts.isEmpty: " + project.getArtifacts().isEmpty());

        log.info("BASE DEPENDENCIES");
        log.info("-----------------------");
        for (Dependency dependency : project.getDependencies()) {
            log.info(" - " + dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion() + ":" + dependency.getScope());
        }

        log.info("TRANSITIVE DEPENDENCIES");
        log.info("-----------------------");
        Set<Artifact> transitiveDependencies = project.getArtifacts();
        transitiveDependencies.removeAll(project.getDependencyArtifacts());
        for (Artifact artifact : transitiveDependencies) {
            log.info(" - " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getScope());
        }

    }

}
