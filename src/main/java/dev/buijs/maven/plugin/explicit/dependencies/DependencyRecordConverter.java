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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.jetbrains.annotations.NotNull;

/**
 * Utility to convert org.apache.maven.artifact.Artifact and org.apache.maven.model.Dependency to
 * DependencyRecord.
 *
 * @see DependencyRecord
 */
public class DependencyRecordConverter {

  /**
   * Convert org.apache.maven.artifact.Artifact to DependencyRecord.
   *
   * @param artifact org.apache.maven.artifact.Artifact.
   * @return DependencyRecord.
   */
  @NotNull
  DependencyRecord convert(@NotNull Artifact artifact) {
    return new DependencyRecord(
        artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
  }

  /**
   * Convert org.apache.maven.model.Dependency to DependencyRecord.
   *
   * @param dependency org.apache.maven.model.Dependency.
   * @return DependencyRecord.
   */
  @NotNull
  DependencyRecord convert(@NotNull Dependency dependency) {
    return new DependencyRecord(
        dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
  }
}
