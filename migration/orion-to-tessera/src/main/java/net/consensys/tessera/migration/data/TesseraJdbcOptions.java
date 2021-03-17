package net.consensys.tessera.migration.data;

import picocli.CommandLine;

public class TesseraJdbcOptions {

    @CommandLine.Option(
            names = {"tessera.jdbc.user"},
            required = true,
            description = "Target Tessera DB username")
    private String username;

    @CommandLine.Option(
            names = {"tessera.jdbc.password"},
            required = true,
            description = "Target Tessera DB password")
    private String password;

    @CommandLine.Option(
            names = "tessera.jdbc.url",
            required = true,
            description = "Target Tessera DB JDBC connection string")
    private String url;

    @CommandLine.Option(names = "tessera.db.action", hidden = true, fallbackValue = "create",defaultValue = "create")
    private String action;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getAction() {
        return action;
    }
}
