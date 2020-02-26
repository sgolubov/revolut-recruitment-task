package ua.com.golubov.revolut.db;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

@Singleton
public class FlywayMigrator {

    private final DataSource dataSource;

    @Inject
    public FlywayMigrator(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void migrate() {
        Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();
    }

}
