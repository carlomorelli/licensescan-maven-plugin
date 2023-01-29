package com.csoft.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportBuilder {

    private static final String PLUGIN_NAMESPACE = "com.github.carlomorelli:licensescan-maven-plugin";
    private static final String REPORTING_BUILD_SUBDIR = "license-scan-results";
    private static final String JSON_REPORT_FILE_NAME = "license-scan-report.json";
    private static final String HTML_REPORT_TEMPLATE_NAME = "report.template.mustache";
    private static final String HTML_REPORT_FILE_NAME = "index.html";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final Mustache HTML_MAPPER = new DefaultMustacheFactory().compile(HTML_REPORT_TEMPLATE_NAME);

    private final MavenProject project;

    public ReportBuilder(final MavenProject project) {
        this.project = project;
    }

    public String buildReport(final Map<String, List<String>> licenseMap,
                              final Map<String, List<String>> violationsMap) throws IOException {
        ObjectNode root = JSON_MAPPER.createObjectNode();
        String jsonLicenseScan = JSON_MAPPER.writeValueAsString(new Results(licenseMap, violationsMap));
        root.put("licenseScanResults", JSON_MAPPER.readTree(jsonLicenseScan));
        Path reportPath = Files.createDirectories(
                Paths.get(project.getBuild().getDirectory()).resolve(REPORTING_BUILD_SUBDIR));
        File jsonReportFile = reportPath.resolve(JSON_REPORT_FILE_NAME).toFile();
        JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonReportFile, root);
        return jsonReportFile.toString();
    }


    public String buildHtmlReport(final Map<String, List<String>> licenseMap,
                                  final Map<String, List<String>> violationsMap) throws IOException {
        Path reportPath = Files.createDirectories(
                Paths.get(project.getBuild().getDirectory()).resolve(REPORTING_BUILD_SUBDIR));
        File htmlReportFile = reportPath.resolve(HTML_REPORT_FILE_NAME).toFile();
        HTML_MAPPER.execute(new FileWriter(htmlReportFile), new Report(project, licenseMap, violationsMap)).flush();
        return htmlReportFile.toString();
    }

    /**
     * Internal class used by Mustache to back the Report template.
     */
    private static class Report {

        private final MavenProject project;
        private final Map<String, List<String>> licenseMap;
        private final Map<String, List<String>> violationsMap;

        public Report(final MavenProject project,
                      final Map<String, List<String>> licenseMap,
                      final Map<String, List<String>> violationsMap) {
            this.project = project;
            this.licenseMap = licenseMap;
            this.violationsMap = violationsMap;
        }

        /**
         * Returns the Project Name from {@link MavenProject} properties..
         * @return String
         */
        public String projectName() {
            return project.getName();
        }

        /**
         * Returns the Project Version from {@link MavenProject} properties
         * @return String
         */
        public String projectVersion() {
            return project.getVersion();
        }

        /**
         * Returns the Report execution date.
         * @return String
         */
        public String reportDate() {
            return new Date().toString();
        }

        /**
         * Returns the LicenseScan Plugin version used by the Report.
         * @return String
         */
        public String pluginVersion() {
            return project.getPlugin(PLUGIN_NAMESPACE).getVersion();
        }

        /**
         * Returns the artefacts-to-licenses entry-set.
         * @return Set
         */
        public Set<Map.Entry<String, List<String>>> licenses() {
            return licenseMap.entrySet();
        }

        /**
         * Returns the forbiddenLicenses-to-matchedArtefacts entry-set.
         * @return Set
         */
        public Set<Map.Entry<String, List<String>>> violations() {
            return violationsMap.entrySet();
        }

    }


    /**
     * Internal class used to build the auxiliary Json report file.
     */
    private static class Results {
        public Map<String, List<String>> licenseMap;
        public Map<String, List<String>> violationsMap;

        public Results() {
        }

        public Results(final Map<String, List<String>> licenseMap,
                       final Map<String, List<String>> violationsMap) {
            this.licenseMap = licenseMap;
            this.violationsMap = violationsMap;
        }
    }
}
