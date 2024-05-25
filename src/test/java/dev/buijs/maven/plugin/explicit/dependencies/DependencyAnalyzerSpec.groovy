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

import spock.lang.Specification

class DependencyAnalyzerSpec extends Specification {

    def static dependency1 =
            new DependencyRecord("com.example", "foo", "1.2.3")

    def static dependency2 =
            new DependencyRecord("com.example", "bar", "1.2.3")

    def writer = Mock(DependencyWriter.class)

    def sut = new DependencyAnalyzer(writer)

    def "Verify an empty list is returned when all dependencies are explicit"() {
        given:
        Set<DependencyRecord> explicit = Set.of(dependency1)
        Set<DependencyRecord> implicit = Set.of(dependency1)

        when:
        def missing = sut
                .getMissingExplicitDependencies(explicit, implicit)

        then:
        missing.isEmpty()

        and:
        1 * writer.writeNewFile("dependenciesMissing.json", _)
    }

    def "Verify al missing dependencies are returned when dependencies are missing"() {
        given:
        Set<DependencyRecord> explicit = Set.of()
        Set<DependencyRecord> implicit = Set.of(dependency1, dependency2)

        when:
        def missing = sut
                .getMissingExplicitDependencies(explicit, implicit)

        then:
        missing.size() == 2
        missing.containsAll(dependency1, dependency2)

        and:
        1 * writer.writeNewFile("dependenciesMissing.json", _)
    }
}
