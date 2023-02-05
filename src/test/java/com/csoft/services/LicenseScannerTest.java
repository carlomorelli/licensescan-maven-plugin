package com.csoft.services;

import org.apache.maven.artifact.Artifact;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LicenseScannerTest {

    @Mock
    private DependencyAnalyzer dependencyAnalyzer;

    private LicenseScanner licenseScanner;

    @Test
    public void testScan_WHEN_noLicensesToMatch_returnsMapWithNoneLicenseEntryOnly() {
        List<String> licensesToMatch = Collections.emptyList();
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertThat(result, aMapWithSize(1));
        assertThat(result, hasEntry(is("NONE"), empty()));
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    @Test
    public void testScan_WHEN_licensesToMatchExistButNoneMatch_THEN_returnsMapWithSameNumberOfKeysAndEmptyValueList() {
        List<String> licensesToMatch = Arrays.asList("licenseA", "licenseB", "regex:.*\tlicC.*");
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertThat(result, aMapWithSize(4));
        assertThat(result, hasEntry(is("licenseA"), empty()));
        assertThat(result, hasEntry(is("licenseB"), empty()));
        assertThat(result, hasEntry(is("regex:.*\tlicC.*"), empty()));
        assertThat(result, hasEntry(is("NONE"), empty()));
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    @Test
    public void testScan_WHEN_licensesToMatchExistAndSomeMatchesArtifactsWithSingleLicense_THEN_returnsMapWithArtifactGavLabel() {
        List<String> licensesToMatch = Arrays.asList("licenseA", "license1", "licenseB", "license2");
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertThat(result, aMapWithSize(5));
        assertThat(result, hasEntry(is("licenseA"), empty()));
        assertThat(result, hasEntry(is("licenseB"), empty()));
        assertThat(result, hasEntry(is("license1"), containsInAnyOrder("group1:artifact1:1.0:null")));
        assertThat(result, hasEntry(is("license2"), containsInAnyOrder("group2:artifact2:2.0:null")));
        assertThat(result, hasEntry(is("NONE"), empty()));
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    @Test
    public void testScan_WHEN_licensesToMatchExistAndSomeMatchesPartiallyArtifactsWithMultipleLicense_returnsMapWithEmptyValueList() {
        List<String> licensesToMatch = Arrays.asList("licenseA", "license31");
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertThat(result, aMapWithSize(3));
        assertThat(result, hasEntry(is("licenseA"), empty()));
        assertThat(result, hasEntry(is("license31"), empty()));
        assertThat(result, hasEntry(is("NONE"), empty()));
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    @Test
    public void testScan_WHEN_licensesToMatchExistAndSomeMatchCompletelyArtifactsWithMultipleLicense_returnsMapWithArtifactGavLabel() {
        List<String> licensesToMatch = Arrays.asList("licenseA", "license31", "license32");
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMap());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertThat(result, aMapWithSize(4));
        assertThat(result, hasEntry(is("licenseA"), empty()));
        assertThat(result, hasEntry(is("license31"), containsInAnyOrder("group3:artifact3:3.0:null")));
        assertThat(result, hasEntry(is("license32"), containsInAnyOrder("group3:artifact3:3.0:null")));
        assertThat(result, hasEntry(is("NONE"), empty()));
        verify(dependencyAnalyzer, times(1)).analyze(ArgumentMatchers.<Artifact>anySet());
    }

    @Test
    public void testScan_WHEN_licensesToMatchExistAndArtifactHasNoLicenses_returnsMapWithNoneLicenseEntryFilled() {
        List<String> licensesToMatch = Collections.emptyList();
        when(dependencyAnalyzer.analyze(ArgumentMatchers.<Artifact>anySet())).thenReturn(analyzeMapWithArtifactWithNoLicense());
        licenseScanner = new LicenseScanner(dependencyAnalyzer, licensesToMatch);
        Map<String, List<String>> result = licenseScanner.scan(new HashSet<Artifact>());
        assertThat(result, aMapWithSize(1));
        assertThat(result, hasEntry(is("NONE"), containsInAnyOrder("group4:artifact4:4.0:null")));
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

    private static Map<String, List<String>> analyzeMapWithArtifactWithNoLicense() {
        Map<String, List<String>> analyzeMap = new HashMap<>();
        analyzeMap.put("group4:artifact4:4.0:null", Collections.<String>emptyList());
        return analyzeMap;
    }

}
