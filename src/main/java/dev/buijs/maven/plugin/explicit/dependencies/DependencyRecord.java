/* Copyright (c) 2021 - 2024 Buijs Software
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.buijs.maven.plugin.explicit.dependencies;

import java.util.regex.Pattern;

/**
 * Data class for storing dependency information.
 * @param groupId the dependency groupId (com.example).
 * @param artifactId the dependency artifactId (library-foo).
 * @param version the dependency version (format major.minor.patch 1.0.0 or with suffix 1.0.0-SNAPSHOT etc.)
 */
public record DependencyRecord(String groupId,
                               String artifactId,
                               String version)
        implements Comparable<DependencyRecord> {

  /**
   * Serialization template to store the information as JSON.
   * @see DependencyRecord#toString()
   */
  private static final String JSON_TEMPLATE =
      """
        {
        "groupId": "%s",
        "artifactId": "%s",
        "version": "%s"
        }""";

  /**
   * Regex pattern to check if a dependency uses a semantic version.
   * @see DependencyRecord#compareTo
   */
  private static final Pattern SEMANTIC_VERSION_PATTERN =
          Pattern.compile("^(?<version>\\d+\\.\\d+.\\d+)(?<suffix>.*)$");

  /**
   * Compare logic to order a collection of records
   * alphabetically and then from newest to oldest version.
   * <br/>
   * Versions without suffix are preferred.
   * E.g. when comparing 1.0.2 and 1.0.2-SNAPSHOT,
   * 1.0.2 is given a higher priority than 1.0.2-SNAPSHOT.
   * @param other the object to be compared.
   * <br/>
   * @return int (negative) number of priority.
   */
  @Override
  public int compareTo(DependencyRecord other) {
    int groupCompare = this.groupId.compareTo(other.groupId);
    if (groupCompare != 0) {
      return groupCompare;
    }

    int artifactCompare = this.artifactId.compareTo(other.artifactId);
    if (artifactCompare != 0) {
      return artifactCompare;
    }

    var thisVersionMatcher = SEMANTIC_VERSION_PATTERN.matcher(this.version);
    var thisIsSemanticallyVersioned = thisVersionMatcher.find();
    if(!thisIsSemanticallyVersioned) {
      return other.version.compareTo(this.version);
    }

    var otherVersionMatcher = SEMANTIC_VERSION_PATTERN.matcher(other.version);
    var otherIsSemanticallyVersioned = otherVersionMatcher.find();
    if(!otherIsSemanticallyVersioned) {
      return other.version.compareTo(this.version);
    }

    var thisVersionWithoutSuffix = thisVersionMatcher.group("version");
    var otherVersionWithoutSuffix = otherVersionMatcher.group("version");
    var versionComparison = otherVersionWithoutSuffix.compareTo(thisVersionWithoutSuffix);
    if(versionComparison != 0) {
      return versionComparison;
    }

    var thisSuffixOrNull = thisVersionMatcher.group("suffix");
    if(thisSuffixOrNull.isEmpty()) {
      return other.version.compareTo(thisVersionWithoutSuffix);
    }

    var otherSuffixOrNull = otherVersionMatcher.group("suffix");
    if(otherSuffixOrNull.isEmpty()) {
      return this.version.compareTo(otherVersionWithoutSuffix);
    }

    return otherSuffixOrNull.compareTo(thisSuffixOrNull);
  }

  @Override
  public String toString() {
    return JSON_TEMPLATE.formatted(groupId, artifactId, version);
  }

}