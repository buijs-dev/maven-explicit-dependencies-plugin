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

public record DependencyRecord(
        String groupId,
        String artifactId,
        String version
) implements Comparable<DependencyRecord> {

    @Override
    public int compareTo(DependencyRecord other) {
        int groupCompare = this.groupId.compareTo(other.groupId);
        if (groupCompare != 0) {
            return groupCompare;
        }

        int artifactCompare = this.artifactId.compareTo(other.artifactId);
        if (artifactCompare != 0) {
            return artifactCompare;
        }

        return this.version.compareTo(other.version);
    }

    @Override
    public String toString() {
        return JSON_TEMPLATE.formatted(groupId, artifactId, version);
    }

    private static final String JSON_TEMPLATE = """
        {
        "groupId": "%s",
        "artifactId": "%s",
        "version": "%s"
        }""";

}