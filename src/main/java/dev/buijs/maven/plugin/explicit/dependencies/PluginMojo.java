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

import java.util.Set;
import java.util.function.Function;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.jetbrains.annotations.NotNull;

@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
public class PluginMojo extends AbstractMojo {

  /** The message logged when all dependencies are explicit. */
  private static final String INFO_SUCCESS_MESSAGE = "dependency-tree is fully explicit";

  /** The message logged when not all dependencies are explicit. */
  private static final String WARN_MISSING_EXPLICITS_MESSAGE = "missing explicit dependencies: %s";

  /** The exception message when not all dependencies are explicit. */
  private static final String EXCEPTION_MISSING_EXPLICITS_MESSAGE =
      "fix this error by adding all missing dependencies to your pom explicitly";

  /** Serialization template to pretty print dependency records. */
  private static final String PRETTY_PRINT_TEMPLATE = "%s.%s:%s";

  private static final Function<DependencyRecord, String> prettyPrint =
      dependency ->
          String.format(
              PRETTY_PRINT_TEMPLATE,
              dependency.groupId(),
              dependency.artifactId(),
              dependency.version());

  @Component DependencyGraphBuilder dependencyGraphBuilder;

  @Parameter(defaultValue = "${session}", readonly = true)
  MavenSession session;

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

  @Parameter(property = "force", defaultValue = "true")
  Boolean force;

  @Override
  public void execute() throws PluginException {
    var context = new PluginContext(project, session, dependencyGraphBuilder);
    var dependencies = context.getMissingExplicitDependencies();
    if (dependencies.isEmpty()) {
      onSuccess();
    } else {
      onFailure(dependencies);
    }
  }

  /**
   * Log success message when all dependencies are explicit.
   *
   * @see PluginMojo#INFO_SUCCESS_MESSAGE
   */
  private void onSuccess() {
    getLog().info(INFO_SUCCESS_MESSAGE);
  }

  /**
   * Log warning message when not all dependencies are explicit.
   *
   * @see PluginMojo#WARN_MISSING_EXPLICITS_MESSAGE
   * @param dependencies the implicit dependencies that should be added explicitly.
   * @throws PluginException when {@link PluginMojo#force} is set to true.
   */
  private void onFailure(@NotNull Set<DependencyRecord> dependencies) throws PluginException {
    var prettyPrinted = prettyPrint(dependencies);
    var warning = String.format(WARN_MISSING_EXPLICITS_MESSAGE, prettyPrinted);
    getLog().warn(warning);
    if (force) {
      throw new PluginException(dependencies, EXCEPTION_MISSING_EXPLICITS_MESSAGE);
    }
  }

  private String prettyPrint(@NotNull Set<DependencyRecord> dependencies) {
    var builder = new StringBuilder();
    dependencies.stream()
        .map(prettyPrint)
        .forEach(
            (str) -> {
              builder.append("\n -  ");
              builder.append(str);
            });
    return builder.toString();
  }
}
