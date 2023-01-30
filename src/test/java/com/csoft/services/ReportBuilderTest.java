package com.csoft.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReportBuilderTest {

    @Mock
    private MavenProject project;

    @Mock
    private Build build;
    @InjectMocks
    private ReportBuilder reportBuilder;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testBuildJsonReport() throws IOException {
        Path tempPath = Files.createTempDirectory(null);
        System.out.println("temp path = " + tempPath);
        when(build.getDirectory()).thenReturn(tempPath.toString());
        when(project.getBuild()).thenReturn(build);
        reportBuilder.buildJsonReport(LICENSES_MAP, VIOLATIONS_MAP);
        JsonNode root = objectMapper.readTree(tempPath
                .resolve("license-scan-results")
                .resolve("license-scan-report.json")
                .toFile());
        JsonNode licensesMap = root.get("licenseScanResults").get("licenseMap");
        JsonNode violationsMap = root.get("licenseScanResults").get("violationsMap");
        assertNotNull(licensesMap);
        assertThat(licensesMap.size(), is(LICENSES_MAP.size()));
        assertNotNull(violationsMap);
        assertThat(violationsMap.size(), is(VIOLATIONS_MAP.size()));
    }

    private static final Map<String, List<String>> LICENSES_MAP = new HashMap() {{
        put("artifact1", Arrays.asList("license11", "license21"));
        put("artifact2", Arrays.asList("license21"));
    }};

    private static final Map<String, List<String>> VIOLATIONS_MAP = new HashMap() {{
        put("forbiddenString1", Arrays.asList("artifact1", "artifact2"));
        put("forbiddenString2", Arrays.asList("artifact2"));
    }};

}