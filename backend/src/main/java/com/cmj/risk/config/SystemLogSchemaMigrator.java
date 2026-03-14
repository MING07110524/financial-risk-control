package com.cmj.risk.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SystemLogSchemaMigrator implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SystemLogSchemaMigrator.class);
    private static final String TABLE_NAME = "sys_log";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_OPERATOR = "operator";
    private static final String INDEX_OPERATOR = "idx_sys_log_operator";
    private static final String INDEX_OPERATION_TIME = "idx_sys_log_operation_time";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public SystemLogSchemaMigrator(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String productName = metaData.getDatabaseProductName();

            if (!columnExists(metaData, catalog, TABLE_NAME, COLUMN_OPERATOR)) {
                jdbcTemplate.execute(isMySql(productName)
                        ? "ALTER TABLE sys_log ADD COLUMN operator VARCHAR(50) DEFAULT NULL COMMENT 'Operator username' AFTER user_id"
                        : "ALTER TABLE sys_log ADD COLUMN operator VARCHAR(50)");
                log.info("Added column {}.{}", TABLE_NAME, COLUMN_OPERATOR);
            }

            if (!isNullable(metaData, catalog, TABLE_NAME, COLUMN_USER_ID)) {
                jdbcTemplate.execute(isMySql(productName)
                        ? "ALTER TABLE sys_log MODIFY COLUMN user_id BIGINT NULL COMMENT 'Operator user ID'"
                        : "ALTER TABLE sys_log ALTER COLUMN user_id BIGINT NULL");
                log.info("Updated column {}.{} to nullable", TABLE_NAME, COLUMN_USER_ID);
            }

            if (!indexExists(metaData, catalog, TABLE_NAME, INDEX_OPERATOR)) {
                jdbcTemplate.execute("CREATE INDEX idx_sys_log_operator ON sys_log (operator)");
                log.info("Created index {}", INDEX_OPERATOR);
            }

            if (!indexExists(metaData, catalog, TABLE_NAME, INDEX_OPERATION_TIME)) {
                jdbcTemplate.execute("CREATE INDEX idx_sys_log_operation_time ON sys_log (operation_time)");
                log.info("Created index {}", INDEX_OPERATION_TIME);
            }

            jdbcTemplate.execute("""
                    UPDATE sys_log
                    SET operator = (
                        SELECT u.username
                        FROM sys_user u
                        WHERE u.id = sys_log.user_id
                    )
                    WHERE operator IS NULL
                      AND user_id IS NOT NULL
                    """);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to migrate sys_log schema", ex);
        }
    }

    private boolean isMySql(String productName) {
        return productName != null && productName.toLowerCase().contains("mysql");
    }

    private boolean columnExists(DatabaseMetaData metaData, String catalog, String tableName, String columnName) throws SQLException {
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, columnName)) {
            return resultSet.next();
        }
    }

    private boolean isNullable(DatabaseMetaData metaData, String catalog, String tableName, String columnName) throws SQLException {
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, columnName)) {
            if (!resultSet.next()) {
                return true;
            }
            return resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
        }
    }

    private boolean indexExists(DatabaseMetaData metaData, String catalog, String tableName, String indexName) throws SQLException {
        try (ResultSet resultSet = metaData.getIndexInfo(catalog, null, tableName, false, false)) {
            while (resultSet.next()) {
                String currentIndex = resultSet.getString("INDEX_NAME");
                if (currentIndex != null && currentIndex.equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
