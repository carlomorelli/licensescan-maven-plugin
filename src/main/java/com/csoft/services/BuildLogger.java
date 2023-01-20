package com.csoft.services;

import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.csoft.utils.ArtifactUtils;

/**
 * Class that implements the output shown during the build by the LicenseScan
 * Maven Plugin.
 */
public class BuildLogger {

    private final MavenProject mavenProject;
    private final Log log;
    private final Map<String, List<String>> dependencyLicensesMap;
    private final Map<String, List<String>> transitiveDependencyLicensesMap;

    public BuildLogger(final DependencyAnalyzer dependencyAnalyzer, final MavenProject mavenProject, final Log log) {
        this.mavenProject = mavenProject;
        this.log = log;
        dependencyLicensesMap = dependencyAnalyzer.analyze(mavenProject.getDependencyArtifacts());
        transitiveDependencyLicensesMap = dependencyAnalyzer
                .analyze(ArtifactUtils.getTransitiveDependencies(mavenProject));
    }

    /**
     * Prints the Header of a LicenseScan, showing all details of the project under
     * analysis.
     */
    public void logHeadAnalysis() {
        log.info("Found project: " + mavenProject);
        log.info(" - artifactId          : " + mavenProject.getArtifactId());
        log.info(" - groupId             : " + mavenProject.getGroupId());
        log.info(" - description         : " + mavenProject.getDescription());
        log.info(" - version             : " + mavenProject.getVersion());
        log.info(" - getArtifact.activeP : " + mavenProject.getActiveProfiles());
        log.info(" - getArtifact.artId   : " + mavenProject.getArtifact().getArtifactId());
        log.info(" - getArtifact.groupId : " + mavenProject.getArtifact().getGroupId());
        log.info(" - getArtifact.version : " + mavenProject.getArtifact().getVersion());
        log.info(" - getArtifacts.isEmpty: " + mavenProject.getArtifacts().isEmpty());
    }

    /**
     * Prints the Base Dependencies section of a LicenseScan.
     * 
     * @param printLicenses Boolean indicating wheter to print in the output the
     *                      licenses of each artifact.
     */
    public void logBaseDeps(final boolean printLicenses) {
        logDeps(dependencyLicensesMap, "BASE DEPENDENCIES", printLicenses);
    }

    /**
     * Prints the Transitive Dependencies section of a LicenseScan.
     * 
     * @param printLicenses Boolean indicating wheter to print in the output the
     *                      licenses of each artifact.
     */
    public void logTransitiveDeps(final boolean printLicenses) {
        logDeps(transitiveDependencyLicensesMap, "TRANSITIVE DEPENDENCIES", printLicenses);
    }

    private void logDeps(final Map<String, List<String>> licensesMap,
            final String titleLabel,
            final boolean printLicenses) {
        log.info("");
        log.info("BASE DEPENDENCIES");
        log.info("-----------------------");
        for (String gavLabel : licensesMap.keySet()) {
            log.info(" - artifact " + gavLabel);
            if (printLicenses) {
                if (licensesMap.get(gavLabel) == null) {
                    log.info("   with license: n/a");
                } else {
                    for (String license : licensesMap.get(gavLabel)) {
                        log.info("   with license: " + license);
                    }
                }
            }
        }
    }
}
