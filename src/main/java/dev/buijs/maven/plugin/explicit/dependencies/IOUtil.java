package dev.buijs.maven.plugin.explicit.dependencies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.jetbrains.annotations.NotNull;

public class IOUtil {

  @NotNull
  public Path toPath(@NotNull String path) {
    return Path.of(path);
  }

  public boolean exists(@NotNull Path path) {
    return Files.exists(path);
  }

  public boolean delete(@NotNull File file) throws IOException {
    if (file.isDirectory()) {
      var files = Optional.of(file).map(File::listFiles).stream().flatMap(Arrays::stream).toList();

      for (var child : files) {
        delete(child);
      }
    }
    return file.delete();
  }

  public Path create(@NotNull Path path) throws IOException {
    return Files.createDirectory(path);
  }
}
