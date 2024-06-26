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

import java.util.List;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.jetbrains.annotations.NotNull;

class DependencyTreeNodeVisitor implements DependencyNodeVisitor {

  @NotNull private final List<DependencyRecord> collection;

  @NotNull private final DependencyRecordConverter converter;

  DependencyTreeNodeVisitor(
      @NotNull List<DependencyRecord> collection, @NotNull DependencyRecordConverter converter) {
    this.collection = collection;
    this.converter = converter;
  }

  public boolean visit(@NotNull DependencyNode node) {
    this.collection.add(converter.convert(node.getArtifact()));
    return true;
  }

  @Override
  public boolean endVisit(@NotNull DependencyNode dependencyNode) {
    return true;
  }
}
