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

import org.apache.maven.project.MavenProject;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DependencyCollector {
    DependencyCollector(final MavenProject project,
                        final DependencyRecordFactory recordFactory,
                        final DependencyWriter writer) {
        this.project = project;
        this.recordFactory = recordFactory;
        this.writer = writer;
    }

    private final MavenProject project;
    private final DependencyRecordFactory recordFactory;
    private final DependencyWriter writer;

    protected Set<DependencyRecord> getDependencies() throws PluginException {
        var dependenciesDirect =
                project.getDependencies();

        var dependenciesManaged =
                project.getDependencyManagement().getDependencies();

        var dependencies = Stream.of(dependenciesDirect, dependenciesManaged)
                .flatMap(Collection::stream)
                .map(recordFactory::create)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        writer.writeNewFile("dependencies.json", dependencies);
        return dependencies;
    }

}