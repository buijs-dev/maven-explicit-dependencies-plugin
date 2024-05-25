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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PluginContext {

  /** The name of the directory where logging output is stored. */
  private static final String LOG_DIRECTORY = "maven-explicit-dependencies";

  /** The error message when creating the output directory has failed. */
  private static final String LOG_DIRECTORY_CREATION_ERROR_MESSAGE =
      "failed to create log directory";

  /** The error message when deleting the output directory has failed. */
  private static final String LOG_DIRECTORY_DELETION_ERROR_MESSAGE =
      "failed to delete log directory";

  @NotNull private final DependencyCollector dependenciesCollector;
  @NotNull private final DependencyTreeCollector dependencyTreeCollector;
  @NotNull private final DependencyAnalyzer dependencyAnalyzer;

  PluginContext(
      @NotNull MavenProject project,
      @NotNull MavenSession session,
      @NotNull DependencyGraphBuilder graphBuilder)
      throws PluginException {
    @NotNull DependencyRecordConverter factory = new DependencyRecordConverter();
    @NotNull DependencyWriter writer = new DependencyWriter(getLogDirectory(project, new IOUtil()));
    this.dependencyAnalyzer = new DependencyAnalyzer(writer);
    this.dependenciesCollector = new DependencyCollector(project, factory, writer);
    this.dependencyTreeCollector =
        new DependencyTreeCollector(project, session, graphBuilder, factory, writer);
  }

  /**
   * Create log directory in the maven project build directory.
   *
   * @param project which is being analyzed and where the log should be stored.
   * @return path to the log directory.
   * @throws PluginException when deleting the existing log directory or creating the new directory
   *     failed.
   * @see PluginContext#LOG_DIRECTORY_CREATION_ERROR_MESSAGE
   * @see PluginContext#LOG_DIRECTORY_DELETION_ERROR_MESSAGE
   */
  @NotNull
  private static Path getLogDirectory(@NotNull MavenProject project, @Nullable IOUtil ioUtilOrNull)
      throws PluginException {
    var ioUtil = Optional.ofNullable(ioUtilOrNull).orElse(new IOUtil());
    var buildDirectory = project.getBuild().getDirectory();
    var path = ioUtil.toPath(buildDirectory).resolve(LOG_DIRECTORY);
    if (ioUtil.exists(path)) {
      try {
        ioUtil.delete(path.toFile());
      } catch (IOException e) {
        throw new PluginException(e, LOG_DIRECTORY_DELETION_ERROR_MESSAGE);
      }
    }

    try {
      return ioUtil.create(path);
    } catch (IOException e) {
      throw new PluginException(e, LOG_DIRECTORY_CREATION_ERROR_MESSAGE);
    }
  }

  @NotNull
  Set<DependencyRecord> getDependencies() throws PluginException {
    return dependenciesCollector.getDependencies();
  }

  @NotNull
  Set<DependencyRecord> getDependenciesFromTree() throws PluginException {
    return dependencyTreeCollector.getDependencies();
  }

  @NotNull
  Set<DependencyRecord> getMissingExplicitDependencies() throws PluginException {
    return dependencyAnalyzer.getMissingExplicitDependencies(
        getDependencies(), getDependenciesFromTree());
  }
}
