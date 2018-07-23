
package com.github.tessera.dao;

import com.github.tessera.transaction.EncryptedTransactionDAO;
import com.github.tessera.transaction.EncryptedTransactionDAOImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@ComponentScan(basePackages = "com.github.tessera.dao")
public abstract class JpaConfig {

    @Bean
    public EncryptedTransactionDAO someDAO() {
        return new EncryptedTransactionDAOImpl();
    }

    @Bean
    public JpaTransactionManager jpaTransactionManager(final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public abstract DataSource dataSource();

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(final DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource);
        localContainerEntityManagerFactoryBean.setJpaDialect(new EclipseLinkJpaDialect());
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        localContainerEntityManagerFactoryBean.setJpaPropertyMap(new HashMap<String, String>() {{
            put("eclipselink.weaving", "false");
            put("javax.persistence.schema-generation.database.action", "create");
        }});
        
        /*
            <property name="jpaVendorAdapter">
                <bean class="org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter">
                    <property name="showSql" value="true"/>
                    <property name="generateDdl" value="true"/>
                    <property name="databasePlatform" value="org.eclipse.persistence.platform.database.H2Platform"/>
                </bean>
            </property>
        */

        return localContainerEntityManagerFactoryBean;

    }

}
