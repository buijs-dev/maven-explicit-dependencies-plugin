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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.project.MavenProject;

public class DependencyWriter {

  private final MavenProject project;

  DependencyWriter(MavenProject project) {
    this.project = project;
  }

  void writeNewFile(final String filename, final Object content) throws PluginException {
    var json = getLogDirectory().resolve(filename).toAbsolutePath();

    try {
      //noinspection ResultOfMethodCallIgnored
      json.toFile().createNewFile();
      Files.writeString(json, content.toString());
    } catch (IOException e) {
      throw new PluginException(e, "failed to write log files", "");
    }
  }

  private Path getLogDirectory() throws PluginException {
    var dir = getBuildDirectory().resolve("maven-explicit-dependencies");
    if (Files.exists(dir)) {
      return dir;
    }

    try {
      return Files.createDirectory(dir);
    } catch (IOException e) {
      throw new PluginException(e, "failed to create log directory", "");
    }
  }

  private Path getBuildDirectory() {
    return Path.of(project.getBuild().getDirectory());
  }
}
