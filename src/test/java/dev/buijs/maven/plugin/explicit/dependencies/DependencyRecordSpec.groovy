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

import groovy.json.JsonSlurper
import spock.lang.Specification

class DependencyRecordSpec extends Specification {

    def "Verify DependencyRecord toString returns valid JSON"() {
        given:
        def record = new DependencyRecord("com.example", "foo-library", "1.0.3-SNAPSHOT")

        when:
        def JSON = record.toString()

        then:
        with(JSON.readLines()) {
            it[0] == "{"
            it[1] == '"groupId": "com.example",'
            it[2] == '"artifactId": "foo-library",'
            it[3] == '"version": "1.0.3-SNAPSHOT"'
            it[4] == "}"
        }

        and:
        with(new JsonSlurper().parseText(JSON)) {
            it["groupId"] == "com.example"
            it["artifactId"] == "foo-library"
            it["version"] == "1.0.3-SNAPSHOT"
        }
    }

    def "Verify a collection of DependencyRecord is sorted alphabetically and from newest to oldest version"() {
        given:
        def record1 = new DependencyRecord("com.example", "foo-library", "2.0.0")
        def record2 = new DependencyRecord("com.example", "foo-library", "1.0.3")
        def record3 = new DependencyRecord("com.example", "foo-library", "1.0.3-SNAPSHOT")
        def record4 = new DependencyRecord("nl.example", "bar-library", "1.0.3")
        def record5 = new DependencyRecord("nl.example", "foo-library", "1.0.3")
        def record6 = new DependencyRecord("xyz.comp", "component", "0.0.89")
        def record7 = new DependencyRecord("xyz.comp", "component", "abcd")
        def record8 = new DependencyRecord("xyz.comp", "component", "bcde")
        def records = [record1, record4, record6, record2, record5, record8, record7, record3]

        expect:
        with(records.stream().sorted().toList()) {
            it[0] == record1
            it[1] == record2
            it[2] == record3
            it[3] == record4
            it[4] == record5
            it[5] == record6
            it[6] == record7
            it[7] == record8
        }
    }

    @SuppressWarnings('GroovyAccessibility')
    def "Verify versions are compared correctly"() {
        expect:
        DependencyRecord.INNER.compareByVersion("abcde", "abcd") > 0
        DependencyRecord.INNER.compareByVersion("1.0.1", "abcd") < 0
        DependencyRecord.INNER.compareByVersion("1.0.1", "1.0.1") == 0
        DependencyRecord.INNER.compareByVersion("1.0.1", "1.0.0") == -1
        DependencyRecord.INNER.compareByVersion("1.0.1-SNAPSHOT", "1.0.1") > 0
        DependencyRecord.INNER.compareByVersion("1.0.1-SNAPSHOT", "1.0.1-RC") < 0
    }
}