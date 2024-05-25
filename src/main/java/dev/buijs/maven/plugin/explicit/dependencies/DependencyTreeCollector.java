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
import java.util.*;
import java.util.stream.Collectors;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.BuildingDependencyNodeVisitor;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Utility to find all dependencies which are used in the maven project. The dependency-tree output
 * is stored as plain text. The flattened dependency-tree (basically just the list of all used
 * dependencies) is stored as JSON.
 *
 * @see DependencyTreeCollector#TXT_TREE_FILENAME
 * @see DependencyTreeCollector#JSON_TREE_FLAT_FILENAME
 * @see DependencyTreeCollector#getDependencies()
 */
class DependencyTreeCollector {
  /**
   * The name of the text file which will be created after creating the dependency-tree.
   *
   * @see DependencyTreeCollector#getDependencies()
   * @see DependencyWriter
   */
  @NotNull private static final String TXT_TREE_FILENAME = "dependencyTree.txt";

  /**
   * The name of the JSON file which will be created after creating the dependency-tree.
   *
   * @see DependencyTreeCollector#getDependencies()
   * @see DependencyWriter
   */
  @NotNull private static final String JSON_TREE_FLAT_FILENAME = "dependencyTreeFlattened.json";

  @NotNull private final MavenProject project;

  @NotNull private final MavenSession session;

  @NotNull private final DependencyGraphBuilder graphBuilder;

  @NotNull private final DependencyRecordConverter converter;

  @NotNull private final DependencyWriter writer;

  DependencyTreeCollector(
      @NotNull final MavenProject project,
      @NotNull final MavenSession session,
      @NotNull final DependencyGraphBuilder graphBuilder,
      @NotNull final DependencyRecordConverter converter,
      @NotNull final DependencyWriter writer) {
    this.project = project;
    this.session = session;
    this.graphBuilder = graphBuilder;
    this.converter = converter;
    this.writer = writer;
  }

  /**
   * Collect all dependencies which are used in this maven project.
   *
   * @return Set of DependencyRecord all dependencies.
   * @throws PluginException when storing the output failed.
   * @see DependencyTreeCollector#TXT_TREE_FILENAME
   * @see DependencyTreeCollector#JSON_TREE_FLAT_FILENAME
   */
  @NotNull
  Set<DependencyRecord> getDependencies() throws PluginException {
    var rootNode = getRootNode();
    var records = collectDependencyTree(rootNode);
    removeRootNodeFromCollection(records, rootNode);
    writer.writeNewFile(TXT_TREE_FILENAME, serializeDependencyTree(rootNode));
    writer.writeNewFile(JSON_TREE_FLAT_FILENAME, records);
    return records;
  }

  /**
   * Get the maven project root node, which is used to find all dependencies.
   *
   * @return DependencyNode the root node of this maven project.
   * @throws PluginException when finding the root node failed.
   */
  @NotNull
  private DependencyNode getRootNode() throws PluginException {
    var request = session.getProjectBuildingRequest();
    var buildingRequest = new DefaultProjectBuildingRequest(request);
    buildingRequest.setProject(project);

    try {
      return graphBuilder.buildDependencyGraph(buildingRequest, null);
    } catch (DependencyGraphBuilderException e) {
      throw new PluginException(e, "failed to build dependency-tree graph", e.getMessage());
    }
  }

  /**
   * Collect all dependencies for this maven project.
   *
   * @param rootNode DependencyNode the main artifact which itself should not be reported.
   * @return Set of DependencyRecord containing all dependencies of this maven project.
   */
  @NotNull
  private Set<DependencyRecord> collectDependencyTree(@NotNull DependencyNode rootNode) {
    var collection = new ArrayList<DependencyRecord>();
    DependencyNodeVisitor visitor = new DependencyTreeNodeVisitor(collection, converter);
    visitor = new BuildingDependencyNodeVisitor(visitor);
    rootNode.accept(visitor);
    return collection.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Remove the root node from the collection because only the dependencies of this node should be
   * reported on.
   *
   * @param records Set of DependencyRecord containing all dependencies of this maven project.
   * @param rootNode DependencyNode the main artifact which itself should not be reported.
   */
  private void removeRootNodeFromCollection(
      @NotNull Set<DependencyRecord> records, @NotNull DependencyNode rootNode) {
    var artifact = rootNode.getArtifact();
    var record = converter.convert(artifact);
    records.remove(record);
  }

  /**
   * Serialize the dependency-tree exactly as invoking mvn dependency:tree from the command line
   * would.
   *
   * @param theRootNode DependencyNode the main artifact which itself should not be reported.
   * @return String serialized tree.
   */
  @NotNull
  private String serializeDependencyTree(@NotNull DependencyNode theRootNode) {
    var writer = new StringWriter();
    DependencyNodeVisitor visitor =
        new SerializingDependencyNodeVisitor(
            writer, SerializingDependencyNodeVisitor.STANDARD_TOKENS);
    visitor = new BuildingDependencyNodeVisitor(visitor);
    theRootNode.accept(visitor);
    return writer.toString();
  }
}
