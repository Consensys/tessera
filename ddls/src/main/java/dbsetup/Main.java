package dbsetup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String... args) throws Exception {

        String user = args[0];
        String password = args[1];
        String url = args[2];

        String createTableSql = Stream.of(Main.class.getResourceAsStream("/h2-ddl.sql"))
                .map(InputStreamReader::new)
                .map(BufferedReader::new)
                .flatMap(BufferedReader::lines)
                .collect(Collectors.joining(System.lineSeparator()));

        try (Connection conn = DriverManager.getConnection(url, user, password)) {

            try (Statement statement = conn.createStatement()) {
                statement.execute(createTableSql);
            }

        }

    }
}
