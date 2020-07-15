package com.quorum.tessera.data;

public enum TestConfig {
    H2("jdbc:h2:mem:test", "NULL not allowed for column \"%s\"", "Unique index or primary key violation"),
    HSQL(
            "jdbc:hsqldb:mem:test",
            "integrity constraint violation: NOT NULL check constraint",
            "unique constraint or index violation"),
    SQLITE("jdbc:sqlite:", "NOT NULL constraint failed", "UNIQUE constraint failed");

    private String url;

    private String requiredFieldColumTemplate;

    private String uniqueContraintViolationMessage;

    TestConfig(String url, String requiredFieldColumTemplate, String uniqueContraintViolationMessage) {
        this.url = url;
        this.requiredFieldColumTemplate = requiredFieldColumTemplate;
        this.uniqueContraintViolationMessage = uniqueContraintViolationMessage;
    }

    public String getUrl() {
        return url;
    }

    public String getRequiredFieldColumTemplate() {
        return requiredFieldColumTemplate;
    }

    public String getUniqueContraintViolationMessage() {
        return uniqueContraintViolationMessage;
    }
}
