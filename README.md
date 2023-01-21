# LicenseScan Maven Plugin

[![licensescan-maven-plugin](https://jitpack.io/v/carlomorelli/licensescan-maven-plugin.svg)](https://jitpack.io/#carlomorelli/licensescan-maven-plugin)

![CI pipeline](https://github.com/carlomorelli/licensescan-maven-plugin/actions/workflows/maven.yml/badge.svg)

[![Coverage Status](https://coveralls.io/repos/github/carlomorelli/licensescan-maven-plugin/badge.svg?branch=master)](https://coveralls.io/github/carlomorelli/licensescan-maven-plugin?branch=master)



LicenseScan Maven plugin audits the dependencies and the transitive dependencies for the Runtime and Compile scopes,
and allow to fail the build if a license is detected belonging to the configured blacklist.

The plugin has a single goal called `audit`. The goal can be linked at any stage of the Maven lifecycle with the appropriate `<executions/>` configuration.

## Configuration
To attach the plugin to your Maven project, add the following block in your `pom.xml` in the `build/plugins` section:
```xml
<build>
  ...
  <plugins>
    ...
    <plugin>
      <groupId>com.github.carlomorelli</groupId>
      <artifactId>licensescan-maven-plugin</artifactId>
      <version>3.0</version> <!-- check the latest version -->
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
    ...
  <plugins>
<build>
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

Once properly configured, the plugin will be triggered during a normal build:
```bash
$ mvn clean package
```
Note that the `<executions/>` block is optional. You can omit that part and launch the plugin analysis manually with
```bash
$ mvn clean package licensescan:audit
```

Compatibility of the plugin:
1. supports Java 1.7 onwards.
2. supports Maven 3.x.


Details on the config parameters:
* `printLicenses`: prints the scanned licenses during the build (default `false`);
* `blacklistedLicenses`: the _denylist_ of licenses that the plugin will alert when found;
* `failBuildOnBlacklisted`: if `blacklistedLicenses` are configured and at least an overall violation is found, makes the build fail (default `false`);

## How to use the denylist efficiently
A denylisted license can be either indicated with a flat string (that will then be matched exactly as it is indicated), ot with a regular expression.
* for flat strings, be aware that the same license may be indicated _slightly_ differently in different Maven dependencies. Example: "Apache 2.0", "Apache Apache License, Version 2.0", "Apache Version 2.0", all effectively indicate the same license. However, for the plugin to catch them all, they all need to be added into the denylist.
* for regexes, you can define a regex for a license by prefixing the string with `regex:` like this:
`<license>regex:Apache.*</license>`. Literal string names and regex strings are also case insensitive to make their usage a little easier. To configure properly regexes you also need to be aware of these pitfalls:
  * **regexes must be XML-escaped.** This is the case for all the strings that are used as free strings in a XML configuration such as the POM: if your regex supposedly contain chars such as `>` or `<`, these need to be converted to `&gt;` and `&lt;` when the string is indicated in the denylist. 
   * **regexes must follow Java-style convention.** If the regex supposedly contains chars with slashes, the slash itself needs to be escaped. Example: `\t` needs to be indicated with `\\t` in the denylist; `\\` (escaped slash, indicating the actual slash char) needs to be indicated with `\\\\` in the denylist.

 To make a cumulative example, if we want to match licenses with regex `.*(?<!\+\s?)GNU General Public License.*`, then it will have to be indicated as `<license>regex:.*(?&lt;!\\+\\s?)GNU General Public License.*</license>` in the denylist.

## Development
For code contributions, plase check `CONTRIBUTING.md`. Setting up a developer environment is straightforward:
1. Have any JDK installed with:
  * minimum version 11 (due to Takari platform requirement)
  * maxium version 17 (because newer versions won't have bytecode support for 1.7)
2. Have Maven 3.x series installed
3. Clone the repository
4. Build with `mvn clean package`.

## Foreword

I developed this plugin in the spare time and I don't always have to chance to stay on top of it. However, I appreciate receiving questions or discussing feature request in the Issues GitHub tab.

Although LicenseScan Maven Plugin is pretty safe to use, as it works only in scanning mode, remember: USE AT YOUR OWN RISK.

I'm always interested in voices from the customers.
Let me know if you find this plugin useful! 

--Carlo
