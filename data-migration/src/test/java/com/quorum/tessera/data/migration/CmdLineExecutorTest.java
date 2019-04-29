package com.quorum.tessera.data.migration;

import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import org.apache.commons.cli.MissingOptionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class CmdLineExecutorTest {

    private CmdLineExecutor executor;

    @Rule
    public TestName testName = new TestName();

    private Path outputPath;

    @Before
    public void onSetup() throws Exception {
        this.outputPath = Files.createTempFile(testName.getMethodName(), ".db");
        this.executor = new CmdLineExecutor();
    }

    @Test
    public void help() throws Exception {
        final String[] args = new String[]{"help"};

        assertThat(executor.execute(args)).isEqualTo(0);
    }

    @Test
    public void noOptions() {

        final String[] args = new String[]{};

        final Throwable throwable = catchThrowable(() -> executor.execute(args));

        assertThat(throwable).isInstanceOf(MissingOptionException.class);
        assertThat(((MissingOptionException) throwable).getMissingOptions())
            .containsExactlyInAnyOrder("storetype", "inputpath", "exporttype", "outputfile", "dbpass", "dbuser");

    }

    @Test
    public void missingStoreTypeOption() {

        final String[] args = new String[]{
            "-inputpath", "somefile.txt",
            "-exporttype", "h2",
            "-outputfile", outputPath.toString(),
            "-dbpass", "-dbuser"
        };

        final Throwable throwable = catchThrowable(() -> executor.execute(args));
        assertThat(throwable).isInstanceOf(MissingOptionException.class);
        assertThat(((MissingOptionException) throwable).getMissingOptions()).containsExactlyInAnyOrder("storetype");
    }

    @Test
    public void missingInputFileOption() {
        final String[] args = new String[]{
            "-storetype", "bdb",
            "-exporttype", "h2",
            "-outputfile", outputPath.toString(),
            "-dbpass", "-dbuser"
        };

        final Throwable throwable = catchThrowable(() -> executor.execute(args));
        assertThat(throwable).isInstanceOf(MissingOptionException.class);
        assertThat(((MissingOptionException) throwable).getMissingOptions()).containsExactlyInAnyOrder("inputpath");
    }




    @Test
    public void bdbStoreType() throws Exception {
        final Path inputFile = Paths.get(getClass().getResource("/bdb/single-entry.txt").toURI());

        final String[] args = new String[]{
            "-storetype", "bdb",
            "-inputpath", inputFile.toString(),
            "-exporttype", "h2",
            "-outputfile", outputPath.toString(),
            "-dbpass", "-dbuser"
        };

        executor.execute(args);
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

        executor.execute(args);
    }

    @Test(expected = MissingOptionException.class)
    public void exportTypeJdbcNoDbConfigProvided() throws Exception {
        final Path inputFile = Paths.get(getClass().getResource("/dir/").toURI());

        final String[] args = new String[]{
            "-storetype", "dir",
            "-inputpath", inputFile.toString(),
            "-outputfile", outputPath.toString(),
            "-exporttype", "jdbc",
            "-dbpass", "-dbuser"
        };

        executor.execute(args);
    }

    @Test
    public void exportTypeJdbc() throws Exception {

        final JDBCMockObjectFactory mockObjectFactory = new JDBCMockObjectFactory();

        try {
            mockObjectFactory.registerMockDriver();

            final String dbConfigPath = getClass().getResource("/dbconfig.properties").getFile();

            final Path inputFile = Paths.get(getClass().getResource("/dir/").toURI());

            final String[] args = new String[]{
                "-storetype", "dir",
                "-inputpath", inputFile.toString(),
                "-outputfile", outputPath.toString(),
                "-exporttype", "jdbc",
                "-dbconfig", dbConfigPath,
                "-dbpass", "-dbuser"
            };

            executor.execute(args);
        } finally {
            mockObjectFactory.restoreDrivers();
        }

    }
}
