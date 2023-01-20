package com.csoft.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LicenseScannerTest {

    @Mock
    private DependencyAnalyzer dependencyAnalyzer;

    private LicenseScanner licenseScanner;

    @Test
    public void testScan_WHEN_noLicensesToMatch_returnsEmptyMap() {
        List<String> licensesToMatch = Collections.emptyList();
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertTrue(result.isEmpty());
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    @Test
    public void testScan_WHEN_licensesToMatchExistButNoneMatch_THEN_returnsMapWithSameNumberOfKeysAndEmptyValueList() {
        List<String> licensesToMatch = Arrays.asList("licenseA", "licenseB", "regex:.*\tlicC.*");
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertEquals(3, result.keySet().size());
        assertTrue(result.keySet().contains("licenseA"));
        assertTrue(result.keySet().contains("licenseB"));
        assertTrue(result.keySet().contains("regex:.*\tlicC.*"));
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            assertTrue(entry.getValue().isEmpty());
        }
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    @Test
    public void testScan_WHEN_licensesToMatchExistAndSomeMatchesArtifactsWithSingleLicense_THEN_returnsMapWithNonEmptyValueList() {
        List<String> licensesToMatch = Arrays.asList("licenseA", "license1", "licenseB", "license2");
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertEquals(4, result.keySet().size());
        assertTrue(result.keySet().contains("license1"));
        assertTrue(result.keySet().contains("license2"));
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            if (entry.getKey().equals("license1")) {
                assertEquals(1, entry.getValue().size());
                assertEquals("group1:artifact1:1.0:null", entry.getValue().get(0));
            } else if (entry.getKey().equals("license2")) {
                assertEquals(1, entry.getValue().size());
                assertEquals("group2:artifact2:2.0:null", entry.getValue().get(0));
            } else {
                assertTrue(entry.getValue().isEmpty());
            }
        }
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    @Test
    public void testScan_WHEN_licensesToMatchExistAndSomeMatchesPartiallyArtifactsWithMultipleLicense_returnsMapWithEmptyValueList() {
        List<String> licensesToMatch = Arrays.asList("licenseA", "license31");
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertEquals(2, result.keySet().size());
        assertTrue(result.keySet().contains("licenseA"));
        assertTrue(result.keySet().contains("license31"));
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            assertTrue(entry.getValue().isEmpty());
        }
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    private static Map<String, List<String>> analyzeMap() {
        Map<String, List<String>> analyzeMap = new HashMap<>();
        analyzeMap.put("group1:artifact1:1.0:null", Arrays.asList("license1"));
        analyzeMap.put("group2:artifact2:2.0:null", Arrays.asList("license2"));
        // this is used to test partial match; if an artifact has multiple licenses but
        // only one forbidden, don't mark it as forbidden
        analyzeMap.put("group3:artifact3:3.0:null", Arrays.asList("license31", "license32"));

        return analyzeMap;
    }
}
