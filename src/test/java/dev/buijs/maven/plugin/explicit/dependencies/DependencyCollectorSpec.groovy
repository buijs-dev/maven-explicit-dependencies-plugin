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

import org.apache.maven.model.Dependency
import org.apache.maven.model.DependencyManagement
import org.apache.maven.project.MavenProject
import spock.lang.Specification

class DependencyCollectorSpec extends Specification {

    def version1 = "1.2.3"
    def groupId1 = "my.favorite.food"
    def artifactId1 = "pizza"
    def artifactId2 = "burger"
    def artifactId3 = "fries"

    def dependency1 = Mock(Dependency.class) {
        it.version >> version1
        it.groupId >> groupId1
        it.artifactId >> artifactId1
    }

    def dependency2 = Mock(Dependency.class) {
        it.version >> version1
        it.groupId >> groupId1
        it.artifactId >> artifactId2
    }

    def dependency3 = Mock(Dependency.class) {
        it.version >> version1
        it.groupId >> groupId1
        it.artifactId >> artifactId3
    }

    def dependencyRecord1 =
            new DependencyRecord(groupId1, artifactId1, version1)

    def dependencyRecord2 =
            new DependencyRecord(groupId1, artifactId2, version1)

    def dependencyRecord3 =
            new DependencyRecord(groupId1, artifactId3, version1)

    def project = Mock(MavenProject.class) {
        it.dependencies >> [dependency2]
        it.dependencyManagement >> Mock(DependencyManagement.class) {
            it.dependencies >> [dependency1, dependency3]
        }
    }

    def writer = Mock(DependencyWriter.class)

    def converter = new DependencyRecordConverter()

    def sut = new DependencyCollector(project, converter, writer)

    def "Verify dependencies from dependencies and management section are collected"() {
        given:
        def written = new HashSet()

        when:
        def dependencies = sut.getDependencies()

        then:
        dependencies.containsAll(dependencyRecord1, dependencyRecord2, dependencyRecord3)

        and:
        1 * writer.writeNewFile("dependencies.json", _) >> {
            arguments -> //noinspection GroovyAssignabilityCheck
                written.addAll(arguments[1])
        }

        and:
        Objects.equals(written, dependencies)
    }

}