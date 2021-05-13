package com.quorum.tessera.data.migration;

public enum StoreType {
  BDB(new BdbDumpFile()),
  DIR(new DirectoryStoreFile()),
  SQLITE(new SqliteLoader());

  private final StoreLoader loader;

  StoreType(final StoreLoader loader) {
    this.loader = loader;
  }

  public StoreLoader getLoader() {
    return this.loader;
  }
}
