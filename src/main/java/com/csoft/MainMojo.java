package com.csoft;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo(name = "sayshi")
public class MainMojo extends AbstractMojo {

    @Parameter(property = "person", defaultValue = "world")
    private String person;

    public void execute() {

        getLog().info("Hello " + person);
    }
}
