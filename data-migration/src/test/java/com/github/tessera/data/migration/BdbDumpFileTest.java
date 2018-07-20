package com.github.tessera.data.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BdbDumpFileTest {

    public BdbDumpFileTest() {
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws IOException {

    }

    @Test
    public void execute() throws Exception {

        Path inputFile = Paths.get(getClass().getResource("/bdb-sample.txt").toURI());
        BdbDumpFile instance = new BdbDumpFile(inputFile);
        Path outputFile = Files.createTempFile("execute", ".db");

        instance.execute(outputFile);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + outputFile.toString());
                PreparedStatement stmt = connection.prepareStatement("select count(*) from exported_txn");
                ResultSet rs = stmt.executeQuery()) {

            assertThat(rs.next()).isTrue();
            assertThat(rs.getLong(1)).isEqualTo(1);

        }

        Files.delete(outputFile);

    }

    @Test
    public void executeSingleEntry() throws Exception {

        Path inputFile = Paths.get(getClass().getResource("/minus-p-sample.txt").toURI());
        BdbDumpFile instance = new BdbDumpFile(inputFile);
        Path outputFile = Paths.get("target", "mark.db");

        instance.execute(outputFile);

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + outputFile.toString());
                PreparedStatement stmt = connection.prepareStatement("select * from ENCRYPTED_TRANSACTION");
                ResultSet rs = stmt.executeQuery()) {

            assertThat(rs.next()).isTrue();

            byte[] data = rs.getBytes("HASH");
            String hash = Base64.getEncoder().encodeToString(data);
            assertThat(hash).isEqualTo("U0Wo/9fX8YJrM8azM00+0CX6aUAlx8f8HgTccnmn1grmd1IK0fgVCApZe9dMc/gOu1f+hPZTqMFzfgC2RgHNUw==");

        }
        // Files.delete(outputFile);

    }
}
