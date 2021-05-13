package com.quorum.tessera.data;

public enum TestConfig {
  H2(
      "jdbc:h2:mem:test",
      "NULL not allowed for column \"%s\"",
      "Unique index or primary key violation"),
  HSQL(
      "jdbc:hsqldb:mem:test",
      "integrity constraint violation: NOT NULL check constraint",
      "unique constraint or index violation"),
  SQLITE(
      "jdbc:sqlite:file::memory:?cache=shared",
      "NOT NULL constraint failed",
      "UNIQUE constraint failed");

  private String url;

  private String requiredFieldColumnTemplate;

  private String uniqueConstraintViolationMessage;

  TestConfig(
      String url, String requiredFieldColumnTemplate, String uniqueConstraintViolationMessage) {
    this.url = url;
    this.requiredFieldColumnTemplate = requiredFieldColumnTemplate;
    this.uniqueConstraintViolationMessage = uniqueConstraintViolationMessage;
  }

  public String getUrl() {
    return url;
  }

  public String getRequiredFieldColumnTemplate() {
    return requiredFieldColumnTemplate;
  }

  public String getUniqueConstraintViolationMessage() {
    return uniqueConstraintViolationMessage;
  }
}
