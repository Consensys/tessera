package com.quorum.tessera.data.migration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreTypeTest {

    @Test
    public void bdbType() {
        assertThat(StoreType.BDB.getLoader()).isInstanceOf(BdbDumpFile.class);
    }

    @Test
    public void dirType() {
        assertThat(StoreType.DIR.getLoader()).isInstanceOf(DirectoryStoreFile.class);
    }

    @Test
    public void sqliteType() {
        assertThat(StoreType.SQLITE.getLoader()).isInstanceOf(SqliteLoader.class);
    }
}
