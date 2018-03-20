package com.csoft;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


@Mojo(name = "sayshi")
public class MainMojo extends AbstractMojo {

    @Parameter(defaultValue="${project}", readonly=true, required=true)
    private MavenProject mavenProject;

    @Parameter(property = "person", defaultValue = "world")
    private String person;


    public void setPerson(String person) {
        this.person = person;
    }

    public void execute() {
        getLog().info("Hello " + person);

        for (Artifact artifact : mavenProject.getArtifacts()) {
            getLog().info("Found artifact: " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getScope());
        }
    }

}
