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
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException
import spock.lang.Specification

import java.nio.file.Files

class DependencyTreeCollectorSpec extends Specification {

    def buildDirectory = Files.createTempDirectory("")
    def project = Stub(MavenProject.class)
    def session = Stub(MavenSession.class)
    def builder = Stub(DependencyGraphBuilder.class)
    def converter = new DependencyRecordConverter()
    def writer = new DependencyWriter(buildDirectory)
    def sut = new DependencyTreeCollector(project, session, builder, converter, writer)

    def "Verify an exeption is thrown when the dependency-tree can not be created"() {
        given:
        builder.buildDependencyGraph(_ as ProjectBuildingRequest, null) >> {
            throw new DependencyGraphBuilderException("BOOM!")
        }

        when:
        sut.dependencies

        then:
        PluginException e = thrown()
        e.message == "failed to build dependency-tree graph"
        e.source.toString() == "org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException: BOOM!"
        0 * writer.writeNewFile(_, _)
    }

}
