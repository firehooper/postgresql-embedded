package ru.yandex.qatools.embed.postgresql;

import de.flapdoodle.embed.process.distribution.BitSize;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.distribution.Platform;
import de.flapdoodle.embed.process.store.IArtifactStore;
import de.flapdoodle.embed.process.store.ImmutableArtifactStore;
import de.flapdoodle.embed.process.store.PostgresArtifactStoreBuilder;
import org.junit.Test;
import ru.yandex.qatools.embed.postgresql.distribution.PostgreSQLVersion;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestDownloads {

    /** Version 11 binary downloads are available for OS X and Windows 64 bit only */
    private boolean supported(Distribution distribution) {
        if (distribution.version().asInDownloadPath().startsWith("9.")
            || distribution.version().asInDownloadPath().startsWith("10.")) {
            return true;
        }
        switch (distribution.platform()) {
        case OS_X:
            return true;
        case Windows:
            return distribution.bitsize() == BitSize.B64;
        default:
            return false;
        }
    }

    @Test
    public void testDownloads() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IArtifactStore artifactStore = new PostgresArtifactStoreBuilder().defaults(Command.Postgres).build(ImmutableArtifactStore.Builder::build);

        for (Platform p : asList(Platform.OS_X, Platform.Linux, Platform.Windows)) {
            for (BitSize b : BitSize.values()) {
                for (de.flapdoodle.embed.process.distribution.Version version : PostgreSQLVersion.Main.values()) {
                    Distribution distribution = Distribution.of(version, p, b);
                    if (! supported(distribution)) {
                        continue;
                    }
                    final Method method = artifactStore.getClass ().getDeclaredMethod ("checkDistribution", Distribution.class);
                    method.setAccessible (true);
                    assertThat("Distribution: " + distribution + " should be accessible", method.invoke (artifactStore, distribution), is(true));
                }
            }
        }
    }
}
