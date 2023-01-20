# LicenseScan Maven Plugin

[![licensescan-maven-plugin](https://jitpack.io/v/carlomorelli/licensescan-maven-plugin.svg)](https://jitpack.io/#carlomorelli/licensescan-maven-plugin)

![CI pipeline](https://github.com/carlomorelli/licensescan-maven-plugin/actions/workflows/maven.yml/badge.svg)

[![Coverage Status](https://coveralls.io/repos/github/carlomorelli/licensescan-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/carlomorelli/licensescan-maven-plugin?branch=master)



LicenseScan Maven plugin audits the dependencies and the transitive dependencies for the Runtime and Compile scopes,
and allow to fail the build if a license is detected belonging to the configured blacklist.

The plugin is exclusively composed of the `audit` goal. The goal can be linked at any stage of the Maven lifecycle with the appropriate `<executions/>` configuration.

The following configuration parameters are offered:
* `printLicenses`: prints the scanned licenses during the build (default `false`)
* `blacklistedLicenses`: list of licenses that will make the build fail if detected
* `failBuildOnBlacklisted`: if `blacklistedLicenses` are configured and at least a violation is found, makes the build fail (default `false`)

Parameter list `blacklistedLicenses` is tricky to configure as some Maven artifacts use different names (e.g. Apache 2.0, Apache Apache License, Version 2.0, Apache Version 2.0, etc...) for the same license.
For this reason the plugin supports Regex expressions. You can define a regex for a license by prefixing the string with "regex:" like this:
```<license>regex:Apache.*</license>```. Literal string names and regex strings are also case insensitive to make them a little easier.

Plugin configuration example in a project:
```xml
 <plugin>
    <groupId>com.github.carlomorelli</groupId>
    <artifactId>licensescan-maven-plugin</artifactId>
    <version>2.1</version>
    <configuration>
      <printLicenses>true</printLicenses>
      <blacklistedLicenses>
        <license>GNU General Public License, v2.0</license>
        <license>GNU General Public License, v3.0</license>
        <license>regex:.*Affero.*</license> <!-- to enable use of wildcards, use string prefix 'regex:' -->
      </blacklistedLicenses>
      <failBuildOnBlacklisted>true</failBuildOnBlacklisted>
    </configuration>
    <executions>
      <execution>
        <phase>compile</phase> <!-- choose the most relevant goal for your pipeline, e.g. 'compile', 'test' or 'deploy' -->
        <goals>
          <goal>audit</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
```
The plugin is released through [Jitpack](https://jitpack.io), thus the following block needs also to be enabled in your `pom.xml`:
```xml
  <pluginRepositories>
    <pluginRepository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </pluginRepository>
  </pluginRepositories>
```

If the `<executions/>` block is configured, the plugin will run during your selected lifecycle. Otherwise, you can launch the execution independently with:
```sh
mvn licensescan:audit
```

Let me know if you find this plugin useful!

--Carlo
