package com.quorum.tessera.config.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcDriverClassNameTest {

    @Test
    public void testDefaultH2() {
        assertThat(JdbcDriverClassName.fromUrl("No driver"))
            .isEqualTo("org.h2.Driver");
    }

    @Test
    public void testSqlite() {
        assertThat(JdbcDriverClassName.fromUrl("jdbc:sqlite:memory:myDb"))
            .isEqualTo("org.sqlite.JDBC");
    }

    @Test
    public void testHsql() {
        assertThat(JdbcDriverClassName.fromUrl("jdbc:hsqldb:mem:myDb"))
            .isEqualTo("org.hsqldb.jdbc.JDBCDriver");
    }
}
