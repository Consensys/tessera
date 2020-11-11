package com.quorum.tessera.data;

import com.quorum.tessera.config.JdbcConfig;

import javax.sql.DataSource;
import java.util.ServiceLoader;

public interface DataSourceFactory {

    DataSource create(JdbcConfig config);

    static DataSourceFactory create() {
        return ServiceLoader.load(DataSourceFactory.class).findFirst().get();
    }

}
