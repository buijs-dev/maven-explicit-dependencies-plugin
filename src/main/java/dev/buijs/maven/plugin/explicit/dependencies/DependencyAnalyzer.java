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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Utility to find all transitive dependencies which are not explicitly added to the maven project.
 * The output is stored as JSON in file.
 *
 * @see DependencyAnalyzer#JSON_FILENAME
 * @see DependencyAnalyzer#getMissingExplicitDependencies(Set, Set)
 */
public class DependencyAnalyzer {

  /**
   * The name of the JSON file which will be created after collecting the dependencies.
   *
   * @see DependencyCollector#getDependencies()
   * @see DependencyWriter
   */
  private static final String JSON_FILENAME = "dependenciesMissing.json";

  /**
   * The writer to store dependencies information in JSON file.
   *
   * @see DependencyWriter
   * @see DependencyAnalyzer#JSON_FILENAME
   */
  private final DependencyWriter writer;

  DependencyAnalyzer(@NotNull DependencyWriter writer) {
    this.writer = writer;
  }

  /**
   * Compare the explicitly configured dependencies with the dependency-tree and return all
   * transitive dependencies that are not explicitly added.
   *
   * @param explicitDependencies Set of DependencyRecord containing all dependencies from maven pom
   *     dependencies and dependencyManagement.
   * @param implicitDependencies Set of DependencyRecord containing all dependencies from the
   *     dependency-tree which includes all transitive dependencies.
   * @return Set of DependencyRecord containing all transitive dependencies that are not explicitly
   *     added to the maven pom.
   */
  @NotNull
  Set<DependencyRecord> getMissingExplicitDependencies(
      @NotNull Set<DependencyRecord> explicitDependencies,
      @NotNull Set<DependencyRecord> implicitDependencies)
      throws PluginException {
    var dependencies =
        implicitDependencies.stream()
            .filter(dependency -> !explicitDependencies.contains(dependency))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    writer.writeNewFile(JSON_FILENAME, dependencies);
    return dependencies;
  }
}
