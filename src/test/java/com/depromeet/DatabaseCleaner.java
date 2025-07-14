package com.depromeet;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class DatabaseCleaner {

    private DatabaseCleaner() {
        throw new IllegalStateException("Utility class");
    }

    public static void clear(ApplicationContext applicationContext) {
        var entityManager = applicationContext.getBean(EntityManager.class);
        var jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
        var transactionTemplate = applicationContext.getBean(TransactionTemplate.class);

        transactionTemplate.execute(
                status -> {
                    entityManager.clear();
                    deleteAll(jdbcTemplate, entityManager);
                    return null;
                });
    }

    private static void deleteAll(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        for (String tableName : findDatabaseTableNames(jdbcTemplate)) {
            deleteDataFromTable(entityManager, tableName);
            resetAutoIncrementColumn(jdbcTemplate, entityManager, tableName);
        }
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    private static void deleteDataFromTable(EntityManager entityManager, String tableName) {
        String deleteQuery = "DELETE FROM %s".formatted(tableName);
        entityManager.createNativeQuery(deleteQuery).executeUpdate();
    }

    private static void resetAutoIncrementColumn(
            JdbcTemplate jdbcTemplate, EntityManager entityManager, String tableName) {
        String autoIncrementColumn = findAutoIncrementColumn(jdbcTemplate, tableName);
        String resetQuery =
                "ALTER TABLE %s ALTER COLUMN %s RESTART WITH 1"
                        .formatted(tableName, autoIncrementColumn);
        entityManager.createNativeQuery(resetQuery).executeUpdate();
    }

    private static String findAutoIncrementColumn(JdbcTemplate jdbcTemplate, String tableName) {
        String query =
                """
			SELECT column_name FROM information_schema.columns
			WHERE table_schema = ? AND table_name = ? AND is_identity = 'YES'
			""";

        List<String> columns =
                jdbcTemplate.query(
                        query, (rs, rowNum) -> rs.getString("column_name"), "PUBLIC", tableName);

        return columns.getFirst();
    }

    private static List<String> findDatabaseTableNames(JdbcTemplate jdbcTemplate) {
        String query =
                """
			SELECT table_name FROM information_schema.tables
			WHERE table_schema = ? AND table_type = 'BASE TABLE'
			ORDER BY table_name
			""";
        return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("table_name"), "PUBLIC");
    }
}
