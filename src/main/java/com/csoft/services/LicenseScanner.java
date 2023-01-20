package com.csoft.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;

import com.csoft.utils.TextUtils;

/**
 * Class that implements the scanning of licesesToMatch agains the set of found
 * licenses in a Maven project.
 */
public class LicenseScanner {

    private final DependencyAnalyzer dependencyAnalyzer;
    private final List<String> licensesToMatch;

    public LicenseScanner(final DependencyAnalyzer dependencyAnalyzer,
            final List<String> licensesToMatch) {
        this.dependencyAnalyzer = dependencyAnalyzer;
        this.licensesToMatch = licensesToMatch;
    }

    /**
     * Compares input set of {@link Artifact} objects derived from a Maven project
     * agains the list of licenses to match.
     * 
     * Matching can be done either literally at string equality or at regex
     * verification, if the input string begins with 'regex:' label.
     * 
     * @param artifacts Set of {@link Artifact} objects
     * @return Map of type {key: licenseToMatch, value: listOf[artifact GAV label]}
     */
    public Map<String, List<String>> scan(final Set<Artifact> artifacts) {

        Map<String, List<String>> licenseMap = dependencyAnalyzer.analyze(artifacts);

        // initialise return map as empty map with empty lists as values
        Map<String, List<String>> returnMap = new HashMap<>();
        for (String licenseToMatch : licensesToMatch) {
            returnMap.put(licenseToMatch, new ArrayList<String>());
        }

        // scan input map for matches
        for (Map.Entry<String, List<String>> entry : licenseMap.entrySet()) {
            String gavLabel = entry.getKey();
            List<String> licenseList = entry.getValue();
            for (String license : licenseList) {
                Match forbiddenMatch = matchOf(license);
                if (forbiddenMatch.isMatch) {
                    List<String> array = returnMap.get(forbiddenMatch.licenseEntry);
                    array.add(gavLabel);
                    returnMap.put(forbiddenMatch.licenseEntry, array);
                }
            }
        }
        return returnMap;
    }

    private Match matchOf(final String license) {
        for (String entry : licensesToMatch) {
            if (entry.startsWith("regex:")) {
                Pattern p = Pattern.compile(TextUtils.parseAsRegex(entry), Pattern.CASE_INSENSITIVE);
                if (p.matcher(license).find()) {
                    return new Match(entry);
                }
            } else if (entry.equalsIgnoreCase(license)) {
                return new Match(entry);
            }
        }
        return new Match();
    }

    private static class Match {
        final boolean isMatch;
        final String licenseEntry;

        Match() {
            this.isMatch = false;
            this.licenseEntry = null;
        }

        Match(String licenseEntry) {
            this.isMatch = true;
            this.licenseEntry = licenseEntry;
        }
    }

}