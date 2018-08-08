package com.quorum.tessera.data.migration;

import org.apache.commons.cli.MissingOptionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class CmdLineExecutorTest {

    @Rule
    public TestName testName = new TestName();

    private Path outputPath;

    @Before
    public void onSetup() throws Exception {
        this.outputPath = Files.createTempFile(testName.getMethodName(), ".db");
    }

    @After
    public void onTearDown() throws IOException {
        if (Files.exists(outputPath)) {
            Files.walk(outputPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    public void help() throws Exception {

        final String[] args = new String[]{"help"};

        assertThat(CmdLineExecutor.execute(args)).isEqualTo(0);

    }

    @Test
    public void noOptions() {

        final String[] args = new String[]{};

        final Throwable throwable = catchThrowable(() -> CmdLineExecutor.execute(args));

        assertThat(throwable).isInstanceOf(MissingOptionException.class);
        assertThat(((MissingOptionException) throwable).getMissingOptions())
            .containsExactlyInAnyOrder("storetype", "inputpath", "exporttype", "outputfile", "dbpass", "dbuser");

    }

    @Test
    public void missingStoreTypeOption() throws Exception {

        String[] args = new String[]{
            "-inputpath", "somefile.txt",
            "-exporttype", "h2",
            "-outputfile", outputPath.toString(),
            "-dbpass", "-dbuser"
        };

        try {
            CmdLineExecutor.execute(args);
            failBecauseExceptionWasNotThrown(MissingOptionException.class);
        } catch (MissingOptionException ex) {
            assertThat(ex.getMissingOptions()).hasSize(1);
            assertThat(ex.getMissingOptions()).containsExactly("storetype");
        }

    }

    @Test
    public void missingInputFileOption() throws Exception {

        String[] args = new String[]{
            "-storetype", "bdb",
            "-exporttype", "h2",
            "-outputfile", outputPath.toString(),
            "-dbpass", "-dbuser"
        };

        try {
            CmdLineExecutor.execute(args);
            failBecauseExceptionWasNotThrown(MissingOptionException.class);
        } catch (MissingOptionException ex) {
            assertThat(ex.getMissingOptions()).hasSize(1);
            assertThat(ex.getMissingOptions()).containsExactly("inputpath");
        }

    }

    @Test
    public void bdbStoreType() throws Exception {

        Path inputFile = Paths.get(getClass().getResource("/bdb/single-entry.txt").toURI());

        Files.deleteIfExists(outputPath);

        String[] args = new String[]{
            "-storetype", "bdb",
            "-inputpath", inputFile.toString(),
            "-exporttype", "h2",
            "-outputfile", outputPath.toString(),
            "-dbpass", "-dbuser"
        };

        CmdLineExecutor.execute(args);

    }

    @Test
    public void dirStoreType() throws Exception {

        final Path inputFile = Paths.get(getClass().getResource("/dir/").toURI());

        final String[] args = new String[]{
            "-storetype", "dir",
            "-inputpath", inputFile.toString(),
            "-outputfile", outputPath.toString(),
            "-exporttype", "sqlite",
            "-dbpass", "-dbuser"
        };

        CmdLineExecutor.execute(args);

        assertThat(outputPath).isNotNull();

    }

    @Test
    public void cannotBeConstructed() throws Exception {

        final Constructor constructor = CmdLineExecutor.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        final Throwable throwable = catchThrowable(constructor::newInstance);
        assertThat(throwable)
            .isInstanceOf(InvocationTargetException.class)
            .hasCauseExactlyInstanceOf(UnsupportedOperationException.class);
    }

}
