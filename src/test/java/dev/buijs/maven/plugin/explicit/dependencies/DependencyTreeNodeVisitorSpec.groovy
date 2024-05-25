package dev.buijs.maven.plugin.explicit.dependencies

import org.apache.maven.artifact.Artifact
import org.apache.maven.shared.dependency.graph.DependencyNode
import spock.lang.Specification

class DependencyTreeNodeVisitorSpec extends Specification {

    def version1 = "1.2.3"
    def groupId1 = "my.favorite.food"
    def artifactId1 = "pizza"
    def dependency1 = Mock(DependencyNode.class) {
        it.artifact >> Stub(Artifact.class) {
            it.version >> version1
            it.groupId >> groupId1
            it.artifactId >> artifactId1
        }
    }

    def static converter = new DependencyRecordConverter()

    def "Verify visitor converts node to record"() {
        given:
        def visited = []

        when:
        new DependencyTreeNodeVisitor(visited, converter).visit(dependency1)

        then:
        visited.size() == 1
        with(visited[0] as DependencyRecord) {
            it.version() == version1
            it.groupId() == groupId1
            it.artifactId() == artifactId1
        }
    }

    def "Verify endVisit always returns true"() {
        expect:
        new DependencyTreeNodeVisitor([], converter).endVisit(dependency1)
    }
}
