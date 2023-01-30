package com.csoft.services;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.Map;

/**
 * Class that implements the output shown during the build by the LicenseScan
 * Maven Plugin.
 */
public class BuildLogger {

    private final boolean printLicenses;
    private final Log log;


    public BuildLogger(final boolean printLicenses,
                       final Log log) {
        this.printLicenses = printLicenses;
        this.log = log;
    }

    /**
     * Prints the Header of a LicenseScan log output, showing all details
     * of the {@link MavenProject} in input.
     */
    public void logHeadAnalysis(final MavenProject project) {
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
    }

    /**
     * Prints the Base Dependencies section of a LicenseScan.
     *
     * @param licensesMap Map between artifact GAV label (key) and associated list of licenses (value).
     */
    public void logBaseDeps(final Map<String, List<String>> licensesMap) {
        logDeps(licensesMap, "BASE DEPENDENCIES", printLicenses);
    }

    /**
     * Prints the Transitive Dependencies section of a LicenseScan.
     *
     * @param licensesMap Map between artifact GAV label (key) and associated list of licenses (value).
     */
    public void logTransitiveDeps(final Map<String, List<String>> licensesMap) {
        logDeps(licensesMap, "TRANSITIVE DEPENDENCIES", printLicenses);
    }

    private void logDeps(final Map<String, List<String>> licensesMap,
                         final String titleLabel,
                         final boolean printLicenses) {
        log.info("");
        log.info(titleLabel);
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
