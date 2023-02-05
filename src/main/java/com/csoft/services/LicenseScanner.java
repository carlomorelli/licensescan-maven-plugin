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

    // special forbidden license string used for artifacts with no license at all
    public static final String NONE_LICENSE = "NONE";

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
     * <p>
     * Matching can be done either literally at string equality or at regex
     * verification, if the input string begins with 'regex:' label.
     *
     * @param artifacts Set of {@link Artifact} objects
     * @return Map of type {key: licenseToMatch, value: listOf[artifact GAV label]}
     */
    public Map<String, List<String>> scan(final Set<Artifact> artifacts) {

        Map<String, List<String>> licenseMap = dependencyAnalyzer.analyze(artifacts);

        // initialise return map as empty map with empty lists as values;
        // also add "absent licenses" forbidden condition entry in license map
        Map<String, List<String>> returnMap = new HashMap<>();
        for (String licenseToMatch : licensesToMatch) {
            returnMap.put(licenseToMatch, new ArrayList<>());
        }
        returnMap.put(NONE_LICENSE, new ArrayList<>());

        // scan input map for matches
        licenseMap.forEach((artifactGavLabel, artifactLicenses) -> {

            // collect all Match results for all the licenses attached to the artifact,
            // including "absent licenses" condition
            boolean cumulativeIsMatch = true;
            List<Match> forbiddenMatches = new ArrayList<>();
            for (String artifactLicense : artifactLicenses) {
                Match forbiddenMatch = matchOf(artifactLicense);
                forbiddenMatches.add(forbiddenMatch);
                cumulativeIsMatch = cumulativeIsMatch && forbiddenMatch.isMatch;
            }
            if (artifactLicenses.isEmpty()) {
                forbiddenMatches.add(new Match(NONE_LICENSE));
            }

            // only if all licenses of the artifact are a match, record the license stats in
            // the return map; otherwise, it means that at least one license attached to the
            // artifact is not forbidden, and we shouldn't record it in the return map.
            if (cumulativeIsMatch) {
                forbiddenMatches.stream()
                        .map(forbiddenMatch -> forbiddenMatch.licenseEntry)
                        .forEach(forbiddenLicense -> addLicenseToMapEntry(returnMap, forbiddenLicense, artifactGavLabel));
            }
        });
        return returnMap;
    }

    private static void addLicenseToMapEntry(final Map<String, List<String>> map,
                                             final String key,
                                             final String newItemForValue) {
        List<String> value = map.get(key);
        value.add(newItemForValue);
        map.put(key, value);
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
