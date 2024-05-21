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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.BuildingDependencyNodeVisitor;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor;

class DependencyTreeCollector {
  private final MavenProject project;
  private final MavenSession session;
  private final DependencyGraphBuilder graphBuilder;
  private final DependencyRecordConverter recordFactory;
  private final DependencyWriter writer;
  DependencyTreeCollector(
      final MavenProject project,
      final MavenSession session,
      final DependencyGraphBuilder graphBuilder,
      final DependencyRecordConverter recordFactory,
      final DependencyWriter writer) {
    this.project = project;
    this.session = session;
    this.graphBuilder = graphBuilder;
    this.recordFactory = recordFactory;
    this.writer = writer;
  }

  Set<DependencyRecord> getDependencies() throws PluginException {
    var rootNode = getRootNode();
    writer.writeNewFile("dependencyTree.txt", serializeDependencyTree(rootNode));
    var records = collectDependencyTree(rootNode);
    records.removeIf(
        record -> {
          var artifact = rootNode.getArtifact();
          if (!Objects.equals(artifact.getGroupId(), record.groupId())) {
            return false;
          }

          if (!Objects.equals(artifact.getArtifactId(), record.artifactId())) {
            return false;
          }

          return Objects.equals(artifact.getVersion(), record.version());
        });
    writer.writeNewFile("dependencyTreeFlattened.json", records);
    return records;
  }

  private String serializeDependencyTree(DependencyNode theRootNode) {
    var writer = new StringWriter();
    DependencyNodeVisitor visitor =
        new SerializingDependencyNodeVisitor(
            writer, SerializingDependencyNodeVisitor.STANDARD_TOKENS);
    visitor = new BuildingDependencyNodeVisitor(visitor);
    theRootNode.accept(visitor);
    return writer.toString();
  }

  private Set<DependencyRecord> collectDependencyTree(DependencyNode theRootNode) {
    var collection = new ArrayList<DependencyRecord>();
    DependencyNodeVisitor visitor = new DependencyTreeNodeVisitor(collection, recordFactory);
    visitor = new BuildingDependencyNodeVisitor(visitor);
    theRootNode.accept(visitor);
    return new LinkedHashSet<>(collection)
        .stream().sorted().collect(LinkedHashSet::new, Set::add, Set::addAll);
  }

  private DependencyNode getRootNode() {
    var request = session.getProjectBuildingRequest();
    var buildingRequest = new DefaultProjectBuildingRequest(request);
    buildingRequest.setProject(project);

    try {
      return graphBuilder.buildDependencyGraph(buildingRequest, null);
    } catch (DependencyGraphBuilderException e) {
      throw new RuntimeException(e);
    }
  }
}
