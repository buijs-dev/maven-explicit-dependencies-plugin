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
package dev.buijs.maven.plugin.explicit.dependencies

import org.apache.maven.execution.MavenSession
import org.apache.maven.model.Build
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder
import org.apache.maven.shared.dependency.graph.DependencyNode
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor
import spock.lang.Specification

import java.nio.file.Files

class PluginMojoSpec extends Specification {

    def "Verify a PluginException is thrown when dependencies are missing and force is true"() {
        given:
        def project = projectStub
        def session = Stub(MavenSession)
        def graphBuilder = Stub(DependencyGraphBuilder) {
            it.buildDependencyGraph(_ as ProjectBuildingRequest, null) >> Stub(DependencyNode) {
                it.accept(_ as DependencyNodeVisitor) >> {
                    //multiple visitor passes, so need to check if this is the correct one
                    //noinspection GroovyAssignabilityCheck
                    def visitor = arguments[0].getDependencyNodeVisitor()
                    if (visitor instanceof DependencyTreeNodeVisitor) {
                        def record = new DependencyRecord("foo.groupie", "bar", "1.2.3")
                        //noinspection GroovyAccessibility
                        visitor.collection.add(record)
                    }
                }
            }
        }

        and:
        def mojo = new PluginMojo(project: project,
                session: session,
                dependencyGraphBuilder: graphBuilder,
                force: true)

        when:
        mojo.execute()

        then:
        PluginException e = thrown()
        e.message == "fix this error by adding all missing dependencies to your pom explicitly"
    }

    def "Verify plugin execution is successful when there is no missing dependency"() {
        given:
        def project = projectStub
        def session = Stub(MavenSession)
        def graphBuilder = Stub(DependencyGraphBuilder)
        def force = true
        def log = Mock(Log)
        def mojo = new TestPluginMojo(log, project, session, graphBuilder, force)

        when:
        mojo.execute()

        then:
        1 * log.info("dependency-tree is fully explicit")
    }

    def "Verify plugin execution is successful when dependencies are missing but force is false"() {
        given:
        def log = Mock(Log)
        def project = projectStub
        def session = Stub(MavenSession)
        def graphBuilder = Stub(DependencyGraphBuilder) {
            it.buildDependencyGraph(_ as ProjectBuildingRequest, null) >> Stub(DependencyNode) {
                it.accept(_ as DependencyNodeVisitor) >> {
                    //multiple visitor passes, so need to check if this is the correct one
                    //noinspection GroovyAssignabilityCheck
                    def visitor = arguments[0].getDependencyNodeVisitor()
                    if (visitor instanceof DependencyTreeNodeVisitor) {
                        def record = new DependencyRecord("foo.groupie", "bar", "1.2.3")
                        //noinspection GroovyAccessibility
                        visitor.collection.add(record)
                    }
                }
            }
        }

        and:
        def mojo = new PluginMojo(project: project,
                session: session,
                dependencyGraphBuilder: graphBuilder,
                force: false)

        and:
        mojo.log = log

        when:
        mojo.execute()

        then:
        1 * log.warn("missing explicit dependencies: \n -  foo.groupie.bar:1.2.3")
    }

    // Test Class for easier mocking/stubbing.
    static class TestPluginMojo extends PluginMojo {

        Log log

        TestPluginMojo(Log log,
                       MavenProject project,
                       MavenSession session,
                       DependencyGraphBuilder dependencyGraphBuilder,
                       Boolean force) {
            super.dependencyGraphBuilder = dependencyGraphBuilder
            super.project = project
            super.session = session
            super.force = force
            this.log = log
        }

        @Override
        Log getLog() {
            return log
        }
    }

    def getProjectStub() {
        def buildDirectory = Files.createTempDirectory("")

        Stub(MavenProject) {
            it.getBuild() >> Stub(Build) {
                it.getDirectory() >> buildDirectory.toFile().path
            }
        }
    }

}