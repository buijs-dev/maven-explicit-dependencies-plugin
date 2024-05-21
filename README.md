[![](https://img.shields.io/badge/Buijs-Software-blue)](https://pub.dev/publishers/buijs.dev/packages)
[![CodeScene Code Health](https://codescene.io/projects/54076/status-badges/code-health)](https://codescene.io/projects/54076)
[![codecov](https://codecov.io/gh/buijs-dev/maven-explicit-dependencies-plugin/graph/badge.svg?token=Bz9XcQYruX)](https://codecov.io/gh/buijs-dev/maven-explicit-dependencies-plugin)
[![GitHub](https://img.shields.io/github/license/buijs-dev/maven-explicit-dependencies-plugin?color=black)](https://github.com/buijs-dev/maven-explicit-dependencies-plugin/blob/main/LICENSE)

> Unreleased... checkout and build locally to try.

# maven-explicit-dependencies-plugin
Maven plugin to force transitive dependencies to be explicitly declared.

## Usage
Add the buijs-dev maven repository:
```xml
<pluginRepositories>
    <pluginRepository>
        <id>buijs-dev</id>
        <url>https://repsy.io/mvn/buijs-dev/maven</url>
    </pluginRepository>
</pluginRepositories>
```

Add the plugin:
```xml
<plugin>
    <groupId>dev.buijs.maven</groupId>
    <artifactId>explicit-dependencies-maven-plugin</artifactId>
    <version>1.0.0</version>
</plugin>
```

The plugin can be executed after configuration:

```shell
mvn explicit-dependencies:compile
```

Minimal configuration:
```xml
<plugin>
    <groupId>dev.buijs.maven</groupId>
    <artifactId>explicit-dependencies-maven-plugin</artifactId>
    <version>1.0.0</version>
</plugin>
```

Add an execution goal to automatically execute the plugin during
the compilation:

```xml
<plugin>
    <groupId>dev.buijs.maven</groupId>
    <artifactId>explicit-dependencies-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Set the force option false to let compilation continue when the plugin has errors.

```xml
<plugin>
    <groupId>dev.buijs.maven</groupId>
    <artifactId>explicit-dependencies-maven-plugin</artifactId>
    ...
    <configuration>
        <force>false</force>
    </configuration>
</plugin>
```

Log output is available in target/maven-explicit-dependencies directory:
- dependencies.json (all explicitly added dependencies)
- dependenciesMissing.json (all transitive dependencies that are not explicitly added)
- dependencyTree.txt (compiled dependency-tree)
- dependencyTreeFlattened.json (all dependencies which should be explicitly added)