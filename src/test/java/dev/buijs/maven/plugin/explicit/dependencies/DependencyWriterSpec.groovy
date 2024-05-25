package dev.buijs.maven.plugin.explicit.dependencies

import spock.lang.Specification

import java.nio.file.Path

class DependencyWriterSpec extends Specification {

    def "Verify an exception is thrown when writing to log directory fails"() {
        given:
        def writer = new DependencyWriter(Path.of("doesNotExist"))

        when:
        writer.writeNewFile("foo", "boo")

        then:
        PluginException e = thrown()
        e.message == "failed to write log files"
    }

}
