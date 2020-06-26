package com.quorum.tessera.data.migration;

import com.mockrunner.mock.jdbc.JDBCMockObjectFactory;
import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.CliType;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.Config;
import com.sun.management.UnixOperatingSystemMXBean;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TestName;
import picocli.CommandLine;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CmdLineExecutorTest {

    @Rule public TestName testName = new TestName();

    @Rule public SystemErrRule systemErrRule = new SystemErrRule().enableLog().muteForSuccessfulTests();

    @Rule public SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    private Path outputPath;

    private CommandLine commandLine;

    @Before
    public void onSetup() throws Exception {
        System.setProperty(CliType.CLI_TYPE_KEY, CliType.DATA_MIGRATION.name());
        systemErrRule.clearLog();
        systemOutRule.clearLog();
        this.outputPath = Files.createTempFile(testName.getMethodName(), ".db");

        commandLine = new CommandLine(new CmdLineExecutor());
        commandLine
                .registerConverter(Config.class, new ConfigConverter())
                .setSeparator(" ")
                .setCaseInsensitiveEnumValuesAllowed(true);
    }

    @Test
    public void correctCliType() {
        assertThat(new CmdLineExecutor().getType()).isEqualTo(CliType.DATA_MIGRATION);
    }

    @Test
    public void help() {
        final String[] args = new String[] {"help"};

        commandLine.execute(args);
        final CliResult result = commandLine.getExecutionResult();

        assertThat(result).isNull();
        //        assertThat(result).isEqualToComparingFieldByField(new CliResult(0, true, null));
        assertThat(systemOutRule.getLog())
                .contains(
                        "Usage:",
                        "Database migration tool from older systems to Tessera",
                        "<main class> [help] [-dbconfig <dbconfig>] [-dbpass <password>] [-dbuser",
                        "<username>] -exporttype <exportType> -inputpath <inputpath>",
                        "-outputfile <outputFile> -storetype <storeType>");
    }

    @Test
    public void noOptions() {
        commandLine.execute();
        final CliResult result = commandLine.getExecutionResult();

        final String expectedLog =
                "Missing required options [-storetype <storeType>, -inputpath <inputpath>, -exporttype <exportType>, -outputfile <outputFile>]";

        //        assertThat(result).isEqualToComparingFieldByField(new CliResult(1, true, null));
        assertThat(result).isNull();
        assertThat(systemErrRule.getLog()).contains(expectedLog);
    }

    @Test
    public void missingStoreTypeOption() {
        final String[] args =
                new String[] {
                    "-inputpath", "somefile.txt",
                    "-exporttype", "h2",
                    "-outputfile", outputPath.toString(),
                    "-dbpass", "-dbuser"
                };

        commandLine.execute(args);
        final CliResult result = commandLine.getExecutionResult();

        //        assertThat(result).isEqualToComparingFieldByField(new CliResult(1, true, null));
        assertThat(result).isNull();
        assertThat(systemErrRule.getLog()).contains("Missing required option '-storetype <storeType>'");
    }

    @Test
    public void missingInputFileOption() throws Exception {
        final String[] args =
                new String[] {
                    "-storetype", "bdb",
                    "-exporttype", "h2",
                    "-outputfile", outputPath.toString(),
                    "-dbpass", "-dbuser"
                };

        commandLine.execute(args);
        final CliResult result = commandLine.getExecutionResult();

        //        assertThat(result).isEqualToComparingFieldByField(new CliResult(1, true, null));
        assertThat(result).isNull();
        assertThat(systemErrRule.getLog()).contains("Missing required option '-inputpath <inputpath>'");
    }

    @Test
    public void bdbStoreType() throws Exception {
        final Path inputFile = Paths.get(getClass().getResource("/bdb/single-entry.txt").toURI());

        final String[] args =
                new String[] {
                    "-storetype", "bdb",
                    "-inputpath", inputFile.toString(),
                    "-exporttype", "h2",
                    "-outputfile", outputPath.toString(),
                    "-dbpass", "-dbuser"
                };

        commandLine.execute(args);
        final CliResult result = commandLine.getExecutionResult();
    }

    @Test
    public void dirStoreType() throws Exception {
        final Path inputFile = Paths.get(getClass().getResource("/dir/").toURI());

        final String[] args =
                new String[] {
                    "-storetype", "dir",
                    "-inputpath", inputFile.toString(),
                    "-outputfile", outputPath.toString(),
                    "-exporttype", "sqlite",
                    "-dbpass", "-dbuser"
                };

        commandLine.execute(args);
        final CliResult result = commandLine.getExecutionResult();
    }

    @Test()
    public void exportTypeJdbcNoDbConfigProvided() throws Exception {
        final Path inputFile = Paths.get(getClass().getResource("/dir/").toURI());

        final String[] args =
                new String[] {
                    "-storetype", "dir",
                    "-inputpath", inputFile.toString(),
                    "-outputfile", outputPath.toString(),
                    "-exporttype", "jdbc",
                    "-dbpass", "-dbuser"
                };

        commandLine.execute(args);
        final CliResult result = commandLine.getExecutionResult();

        String output = systemErrRule.getLog();
        assertThat(output)
                .contains(
                        "java.lang.IllegalArgumentException: dbconfig file path is required when no export type is defined.");
    }

    @Test
    public void exportTypeJdbc() throws Exception {

        final JDBCMockObjectFactory mockObjectFactory = new JDBCMockObjectFactory();

        try {
            mockObjectFactory.registerMockDriver();

            final String dbConfigPath = getClass().getResource("/dbconfig.properties").getFile();

            final Path inputFile = Paths.get(getClass().getResource("/dir/").toURI());

            final String[] args =
                    new String[] {
                        "-storetype",
                        "dir",
                        "-inputpath",
                        inputFile.toString(),
                        "-outputfile",
                        outputPath.toString(),
                        "-exporttype",
                        "jdbc",
                        "-dbconfig",
                        dbConfigPath,
                        "-dbpass",
                        "-dbuser"
                    };

            commandLine.execute(args);
            final CliResult result = commandLine.getExecutionResult();
        } finally {
            mockObjectFactory.restoreDrivers();
        }
    }

    // This tests that even with a lot of files, the file descriptor limit isn't hit
    @Test
    public void directoryStoreAndSqliteWithLotsOfFilesWorks() throws Exception {
        final Path descriptorTestFolder = Files.createTempDirectory("descriptorTest");
        final InputStream dataStream =
                getClass()
                        .getResourceAsStream(
                                "/dir/2JRLWGXFSDJUYUKADO7VFO3INL27WUXB2YDR5FCI3REQDTJGX6FULIDCIMYDV4H23PFUECWFYBMTIUTNY2ESAFMQADFCFUYBHBBJT4I=");
        final byte[] data = IOUtils.toByteArray(dataStream);

        // this code snippet fetches the number of file descriptors we can use
        // some will already be used, but opening more doesn't hurt since that is what we are testing
        if (!(ManagementFactory.getOperatingSystemMXBean() instanceof UnixOperatingSystemMXBean)) {
            // we skip this test on Windows and other unsupported OS's
            // the point of the test is to show we don't keep file descriptors open when not needed
            // which is independent of the OS we are running on
            return;
        }

        final UnixOperatingSystemMXBean osMxBean =
                (UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        final long descriptorCount = osMxBean.getMaxFileDescriptorCount();

        // if the descriptor count is too high, this test tries to do too much
        // even at 1,000,000 files, it took a long time to run (20 minutes in a vagrant environment)
        //
        // On MacOS the limit is 10240, and on Travis the limit is 30000.

        if (descriptorCount > 35000) {
            return;
        }

        for (int i = 0; i < descriptorCount; i++) {
            final String filename = new Base32().encodeToString(String.valueOf(i).getBytes());
            final Path newFile = descriptorTestFolder.resolve(filename);
            Files.write(newFile, data);
        }

        final String[] args =
                new String[] {
                    "-storetype", "dir",
                    "-inputpath", descriptorTestFolder.toString(),
                    "-outputfile", outputPath.toString(),
                    "-exporttype", "sqlite",
                    "-dbpass", "-dbuser"
                };

        commandLine.execute(args);
        final CliResult result = commandLine.getExecutionResult();
    }
}
