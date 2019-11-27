package com.quorum.tessera.cli;

public enum CliType {
    CONFIG,
    ADMIN,
    ENCLAVE,
    CONFIG_MIGRATION,
    DATA_MIGRATION;

    public static final String CLI_TYPE_KEY = "tessera.cli.type";
}
