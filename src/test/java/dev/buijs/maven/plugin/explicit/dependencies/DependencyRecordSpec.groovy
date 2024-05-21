package dev.buijs.maven.plugin.explicit.dependencies

import groovy.json.JsonSlurper
import spock.lang.Specification

import java.util.stream.Stream

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
        def records = [record1, record4, record2, record5, record3]

        expect:
        with(records.stream().sorted().toList()) {
            it[0] == record1
            it[1] == record2
            it[2] == record3
            it[3] == record4
            it[4] == record5
        }

    }

}