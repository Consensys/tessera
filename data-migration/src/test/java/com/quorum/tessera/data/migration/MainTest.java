package com.quorum.tessera.data.migration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainTest {

    @Rule public final ExpectedSystemExit expectedSystemExit = ExpectedSystemExit.none();

    @Test
    public void performValidMigration() throws IOException, URISyntaxException {
        expectedSystemExit.expectSystemExitWithStatus(0);

        final Path inputFile = Paths.get(getClass().getResource("/bdb/single-entry.txt").toURI());
        final Path outputPath = Files.createTempFile("db", ".db");

        Main.main(
                "-storetype",
                "bdb",
                "-inputpath",
                inputFile.toString(),
                "-exporttype",
                "h2",
                "-outputfile",
                outputPath.toString(),
                "-dbpass", "",
                "-dbuser", "");
    }

    @Test
    public void outputDebugInformationWithNullCause() throws IOException {
        expectedSystemExit.expectSystemExitWithStatus(1);

        final Path outputPath = Files.createTempFile("db", ".db");

        Main.main(
                "-storetype",
                "bdb",
                "-inputpath",
                "non-existent",
                "-exporttype",
                "h2",
                "-outputfile",
                outputPath.toString(),
                "-dbpass", "",
                "-dbuser", "",
                "debug");
    }
}
