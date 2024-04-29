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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

@Mojo(name="compile", defaultPhase = LifecyclePhase.COMPILE)
public class PluginMojo extends AbstractMojo {

    @Component
    DependencyGraphBuilder dependencyGraphBuilder;

    @Parameter(defaultValue = "${session}", readonly = true)
    MavenSession session;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "force", defaultValue = "true" )
    Boolean force;

    @Override
    public void execute() throws PluginException {
        var context = new PluginContext(project, session, dependencyGraphBuilder);

        var dependencies = context.dependenciesCollector.getDependencies();

        var dependencyTree = context.dependencyTreeCollector.getDependencies();

        var dependenciesMissing = context.dependencyAnalyzer
                .getMissingExplicitDependencies(dependencies, dependencyTree);

        if(dependenciesMissing.isEmpty()) {
            getLog().info("dependency-tree is fully explicit");
        } else {
            getLog().warn("missing explicit dependencies: " + dependenciesMissing);
            if(force) {
                var msg =  "missing explicit dependencies";
                var description = "fix this error by adding all missing dependencies to your pom explicitly";
                throw new PluginException(dependenciesMissing, msg, description);
            }
        }
    }

}