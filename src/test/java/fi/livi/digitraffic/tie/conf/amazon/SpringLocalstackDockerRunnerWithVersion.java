package fi.livi.digitraffic.tie.conf.amazon;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import xyz.fabiano.spring.localstack.LocalstackDockerBuilder;
import xyz.fabiano.spring.localstack.annotation.SpringLocalstackProperties;
import xyz.fabiano.spring.localstack.legacy.LocalstackDocker;

/**
 * Copy from xyz.fabiano.spring.localstack.junit.SpringLocalstackDockerRunner
 * to get build with builder.withVersion("0.9.1") as there is bug in newer version.
 * Fixex in 0.0.10 version but it's not yet in maven repo.
 */
public class SpringLocalstackDockerRunnerWithVersion extends SpringJUnit4ClassRunner {
    private Optional<SpringLocalstackProperties> properties;

    public SpringLocalstackDockerRunnerWithVersion(Class<?> clazz) throws InitializationError {
        super(clazz);
        this.properties = findAnnotation(clazz.getAnnotations());
    }

    @Override
    public void run(final RunNotifier notifier) {
        LocalstackDockerBuilder builder = new LocalstackDockerBuilder();

        this.properties.ifPresent(p -> {
            builder.withExternalHost(p.externalHost());
            builder.withServices(p.services());
            builder.withRandomPorts(p.randomPorts());
            builder.pullingNewImages(p.pullNewImage());
            builder.withOptions(options(p));
            builder.withRegion(p.region());
        });
        builder.withVersion("0.9.1");

        LocalstackDocker docker = builder.build();
        // Stop Docker also if runtime is killed softly
        Runtime.getRuntime().addShutdownHook(new DockerStopper(docker));
        try {
            docker.startup();
            super.run(notifier);
        } finally {
            try {
                docker.stop();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private Optional<SpringLocalstackProperties> findAnnotation(Annotation[] annotations) {
        return Stream.of(annotations)
            .filter(a -> a instanceof SpringLocalstackProperties)
            .findFirst()
            .map(a -> (SpringLocalstackProperties) a);
    }

    private List<String> options(SpringLocalstackProperties springLocalstackProperties) {
        List<String> options = new ArrayList<>();
        if (springLocalstackProperties.autoRemove()) {
            options.add("--rm");
        }

        if (springLocalstackProperties.extraOptions() != null && springLocalstackProperties.extraOptions().length > 0) {
            options.addAll(Arrays.asList(springLocalstackProperties.extraOptions()));
        }

        return options;
    }

    private class DockerStopper extends Thread {

        private final LocalstackDocker docker;

        private DockerStopper(LocalstackDocker docker) {
            this.docker = docker;
        }

        @Override
        public void run() {
            try {
                docker.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
