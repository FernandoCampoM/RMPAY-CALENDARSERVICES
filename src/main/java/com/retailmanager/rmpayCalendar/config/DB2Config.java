package com.retailmanager.rmpayCalendar.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.retailmanager.rmpayCalendar.db2.repository",
    entityManagerFactoryRef = "db2EntityManagerFactory",
    transactionManagerRef = "db2TransactionManager"
)
public class DB2Config {

@Bean
@ConfigurationProperties(prefix = "spring.datasource.db2")
public DataSource db2DataSource() {
    return DataSourceBuilder.create()
            .type(com.zaxxer.hikari.HikariDataSource.class)
            .build();
}

    @Bean
    public LocalContainerEntityManagerFactoryBean db2EntityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        Map<String, Object> properties = new HashMap<>();

        // 🔥 CONFIGURACIÓN CLAVE PARA DB EXTERNA / LEGACY
        properties.put("hibernate.hbm2ddl.auto", "update"); // 🚨 NO TOCAR LA DB
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        properties.put("hibernate.show_sql", false);
        properties.put("hibernate.format_sql", true);

        return builder
                .dataSource(db2DataSource())
                .packages("com.retailmanager.rmpayCalendar.db2.entity")
                .persistenceUnit("db2")
                .properties(properties)
                .build();
    }

    @SuppressWarnings("null")
    @Bean
    public PlatformTransactionManager db2TransactionManager(
            @Qualifier("db2EntityManagerFactory")
            EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}