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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;

/**
 * Utility to collect all dependencies currently used in this project. The output is stored as JSON
 * in file.
 *
 * @see DependencyCollector#JSON_FILENAME
 * @see DependencyCollector#getDependencies()
 */
class DependencyCollector {

  /**
   * The name of the JSON file which will be created after collecting the dependencies.
   *
   * @see DependencyCollector#getDependencies()
   * @see DependencyWriter
   */
  private static final String JSON_FILENAME = "dependencies.json";

  /**
   * The project (pom.xml) file to analyze.
   *
   * <p>The collector will include everything from both sections {@code <dependencyManagement/>} and
   * {@code <dependencies/>}.
   */
  @NotNull private final MavenProject project;

  /**
   * The converter to create DependencyRecord's from org.apache.maven.model.Dependency. <br>
   *
   * @see DependencyRecord
   * @see DependencyRecordConverter
   */
  @NotNull private final DependencyRecordConverter recordFactory;

  /**
   * The writer to store dependencies information in JSON file. <br>
   *
   * @see DependencyWriter
   * @see DependencyCollector#JSON_FILENAME
   */
  @NotNull private final DependencyWriter writer;

  DependencyCollector(
      @NotNull final MavenProject project,
      @NotNull final DependencyRecordConverter converter,
      @NotNull final DependencyWriter writer) {
    this.project = project;
    this.recordFactory = converter;
    this.writer = writer;
  }

  /**
   * Collect all dependencies currently used in this project.
   *
   * @return Set of DependencyRecord.
   * @throws PluginException when collecting the dependencies has failed.
   * @see DependencyRecord
   * @see DependencyRecordConverter
   */
  @NotNull
  protected Set<DependencyRecord> getDependencies() throws PluginException {
    var dependenciesDirect = project.getDependencies();

    var dependenciesManaged =
        Optional.of(project)
            .map(MavenProject::getDependencyManagement)
            .map(DependencyManagement::getDependencies)
            .orElse(List.of());

    var dependencies =
        Stream.of(dependenciesDirect, dependenciesManaged)
            .flatMap(Collection::stream)
            .map(recordFactory::convert)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    writer.writeNewFile(JSON_FILENAME, dependencies);
    return dependencies;
  }
}
