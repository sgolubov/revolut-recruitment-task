package ua.com.golubov.revolut.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import ua.com.golubov.revolut.Application;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;

public class GuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Application.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    @Provides
    @Singleton
    private DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:h2:mem:revolut");
        ds.setUsername("sa");
        ds.setPassword("");
        return ds;
    }

    @Provides
    @Singleton
    @Inject
    private Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }

    @Provides
    @Singleton
    private Validator validator() {
        return Validation
                .buildDefaultValidatorFactory()
                .getValidator();
    }
}
