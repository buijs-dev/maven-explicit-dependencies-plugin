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

import org.apache.maven.model.Build
import org.apache.maven.project.MavenProject
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class PluginContextSpec extends Specification {

    def project = Stub(MavenProject)
    def ioUtil = Stub(IOUtil)

    def "Verify log directory is returned successfully"() {
        given:
        def buildDirectory = Files.createTempDirectory("pcs")
        project.getBuild() >> Stub(Build) {
            it.getDirectory() >> buildDirectory.toAbsolutePath().toString()
        }

        expect:
        //noinspection GroovyAccessibility
        PluginContext.getLogDirectory(project, null).toFile().exists()
    }

    def "Verify log directory is deleted when it exists"() {
        given:
        def buildDirectory = Files.createTempDirectory("pcs")
        def logDirectory = buildDirectory.resolve("maven-explicit-dependencies")
        logDirectory.toFile().mkdir()
        def file = logDirectory.resolve("foo.txt").toFile()
        file.createNewFile()
        project.getBuild() >> Stub(Build) {
            it.getDirectory() >> buildDirectory.toAbsolutePath().toString()
        }

        expect:
        //noinspection GroovyAccessibility
        def output = PluginContext.getLogDirectory(project, null)
        output.toFile().exists()
        !file.exists()
    }

    def "Verify an exception is thrown when creating a new log directory fails"() {
        given:
        ioUtil.exists(_ as Path) >> exists
        ioUtil.create(_ as Path) >> { throw new IOException("BOOM!") }

        when:
        //noinspection GroovyAccessibility
        PluginContext.getLogDirectory(project, ioUtil)

        then:
        PluginException e = thrown()
        e.message == "failed to create log directory"

        where:
        exists << [true, false]
    }

    def "Verify an exception is thrown when the existing log directory can't be deleted"() {
        given:
        ioUtil.exists(_ as Path) >> true
        ioUtil.delete(_ as File) >> { throw new IOException("BOOM!") }

        when:
        //noinspection GroovyAccessibility
        PluginContext.getLogDirectory(project, ioUtil)

        then:
        PluginException e = thrown()
        e.message == "failed to delete log directory"
    }
}
