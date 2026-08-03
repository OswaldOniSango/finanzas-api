package com.finanzas.tools;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.flywaydb.core.Flyway;

public final class SupabaseToH2Refresh {
    private static final List<String> TABLES = List.of(
            "users", "financial_periods", "expense_items", "credit_cards",
            "plan_allocations", "monthly_actuals");

    private SupabaseToH2Refresh() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException(
                    "Uso interno: <postgres-url> <postgres-user> <postgres-password> <h2-url>");
        }

        migrateLocalSchema(args[3]);
        try (Connection source = DriverManager.getConnection(args[0], args[1], args[2]);
             Connection target = DriverManager.getConnection(args[3], "sa", "")) {
            target.setAutoCommit(false);
            try {
                setReferentialIntegrity(target, false);
                clearTarget(target);
                for (String table : TABLES) {
                    if (!tableExists(source, table) || !tableExists(target, table)) {
                        System.out.printf("%-20s omitida (no existe en ambas bases)%n", table);
                        continue;
                    }
                    System.out.printf("%-20s %d fila(s)%n", table, copyTable(source, target, table));
                }
                resetIdentities(target);
                setReferentialIntegrity(target, true);
                target.commit();
                System.out.println("Refresh completado correctamente.");
            } catch (Exception exception) {
                target.rollback();
                throw exception;
            }
        }
    }

    private static void migrateLocalSchema(String h2Url) {
        Flyway.configure()
                .dataSource(h2Url, "sa", "")
                .locations("classpath:db/migration", "classpath:db/vendor/h2")
                .load()
                .migrate();
    }

    private static void clearTarget(Connection target) throws SQLException {
        List<String> reverse = new ArrayList<>(TABLES);
        Collections.reverse(reverse);
        try (Statement statement = target.createStatement()) {
            for (String table : reverse) {
                if (tableExists(target, table)) statement.executeUpdate("DELETE FROM " + table);
            }
        }
    }

    private static int copyTable(Connection source, Connection target, String table) throws SQLException {
        Set<String> targetColumns = columns(target, table);
        try (Statement query = source.createStatement();
             ResultSet rows = query.executeQuery("SELECT * FROM " + table)) {
            ResultSetMetaData metadata = rows.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int index = 1; index <= metadata.getColumnCount(); index++) {
                String name = metadata.getColumnLabel(index).toLowerCase();
                if (targetColumns.contains(name)) columns.add(name);
            }
            String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
            String sql = "INSERT INTO " + table + " (" + String.join(", ", columns)
                    + ") VALUES (" + placeholders + ")";
            int count = 0;
            try (PreparedStatement insert = target.prepareStatement(sql)) {
                while (rows.next()) {
                    for (int index = 0; index < columns.size(); index++) {
                        insert.setObject(index + 1, rows.getObject(columns.get(index)));
                    }
                    insert.executeUpdate();
                    count++;
                }
            }
            return count;
        }
    }

    private static Set<String> columns(Connection connection, String table) throws SQLException {
        Set<String> result = new HashSet<>();
        try (ResultSet rows = connection.getMetaData().getColumns(null, null, table, null)) {
            while (rows.next()) result.add(rows.getString("COLUMN_NAME").toLowerCase());
        }
        return result;
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet rows = metadata.getTables(null, null, table, new String[]{"TABLE"})) {
            return rows.next();
        }
    }

    private static void resetIdentities(Connection target) throws SQLException {
        try (Statement statement = target.createStatement()) {
            for (String table : TABLES) {
                if (!tableExists(target, table)) continue;
                long next;
                try (ResultSet row = statement.executeQuery(
                        "SELECT COALESCE(MAX(id), 0) + 1 FROM " + table)) {
                    row.next();
                    next = row.getLong(1);
                }
                statement.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + next);
            }
        }
    }

    private static void setReferentialIntegrity(Connection target, boolean enabled) throws SQLException {
        try (Statement statement = target.createStatement()) {
            statement.execute("SET REFERENTIAL_INTEGRITY " + (enabled ? "TRUE" : "FALSE"));
        }
    }
}
