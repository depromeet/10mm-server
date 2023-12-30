package com.depromeet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;
import org.hibernate.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleaner implements InitializingBean {

    @PersistenceContext private EntityManager entityManager;
    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        entityManager.unwrap(Session.class).doWork(this::extractTableNames);
    }

    private void extractTableNames(Connection conn) {
        tableNames =
                entityManager.getMetamodel().getEntities().stream()
                        .map(e -> e.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase())
                        .collect(Collectors.toList());
    }

    public void execute() {
        entityManager.unwrap(Session.class).doWork(this::cleanUpDatabase);
    }

    private void cleanUpDatabase(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate("SET REFERENTIAL_INTEGRITY FALSE");

        for (String tableName : tableNames) {
            statement.executeUpdate("TRUNCATE TABLE " + tableName);
            statement.executeUpdate(
                    "ALTER TABLE "
                            + tableName
                            + " ALTER COLUMN "
                            + tableName
                            + "_id RESTART WITH 1");
        }

        statement.executeUpdate("SET REFERENTIAL_INTEGRITY TRUE");
    }
}
