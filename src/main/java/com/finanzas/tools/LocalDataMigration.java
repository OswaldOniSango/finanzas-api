package com.finanzas.tools;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Copies the personal data from the local H2 database to the production
 * PostgreSQL database. The migration is transactional and can safely be run
 * again: rows are matched by their primary key or natural unique key.
 */
public final class LocalDataMigration {

    private LocalDataMigration() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            throw new IllegalArgumentException(
                    "Expected: <h2-url> <postgres-url> <postgres-user> <postgres-password> <--dry-run|--execute>");
        }

        boolean execute = switch (args[4]) {
            case "--execute" -> true;
            case "--dry-run" -> false;
            default -> throw new IllegalArgumentException("Last argument must be --dry-run or --execute");
        };

        try (Connection source = DriverManager.getConnection(args[0], "sa", "");
             Connection target = DriverManager.getConnection(args[1], args[2], args[3])) {
            target.setAutoCommit(false);
            try {
                MigrationResult result = migrate(source, target);
                if (execute) {
                    target.commit();
                } else {
                    target.rollback();
                }
                System.out.printf(
                        "%s: %d usuario(s), %d mes(es), %d gasto(s), %d tarjeta(s), %d asignación(es).%n",
                        execute ? "Migración completada" : "Simulación correcta (sin cambios)",
                        result.users, result.periods, result.expenses, result.cards, result.allocations);
            } catch (Exception exception) {
                target.rollback();
                throw exception;
            }
        }
    }

    private static MigrationResult migrate(Connection source, Connection target) throws SQLException {
        Map<Long, Long> userIds = migrateUsers(source, target);
        Map<Long, Long> periodIds = migratePeriods(source, target, userIds);
        int expenses = migrateExpenses(source, target, periodIds);
        int cards = migrateCards(source, target, periodIds);
        int allocations = migrateAllocations(source, target, periodIds);
        resetSequences(target);
        return new MigrationResult(userIds.size(), periodIds.size(), expenses, cards, allocations);
    }

    private static Map<Long, Long> migrateUsers(Connection source, Connection target) throws SQLException {
        Map<Long, Long> ids = new HashMap<>();
        String select = "SELECT id, username, password_hash, role, enabled, created_at, updated_at FROM users";
        String upsert = """
                INSERT INTO users (username, password_hash, role, enabled, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (username) DO UPDATE SET
                    role = EXCLUDED.role,
                    enabled = EXCLUDED.enabled,
                    updated_at = EXCLUDED.updated_at
                RETURNING id
                """;
        try (Statement query = source.createStatement();
             ResultSet rows = query.executeQuery(select);
             PreparedStatement write = target.prepareStatement(upsert)) {
            while (rows.next()) {
                write.setString(1, rows.getString("username"));
                write.setString(2, rows.getString("password_hash"));
                write.setString(3, rows.getString("role"));
                write.setBoolean(4, rows.getBoolean("enabled"));
                write.setTimestamp(5, rows.getTimestamp("created_at"));
                write.setTimestamp(6, rows.getTimestamp("updated_at"));
                try (ResultSet inserted = write.executeQuery()) {
                    inserted.next();
                    ids.put(rows.getLong("id"), inserted.getLong(1));
                }
            }
        }
        return ids;
    }

    private static Map<Long, Long> migratePeriods(
            Connection source, Connection target, Map<Long, Long> userIds) throws SQLException {
        Map<Long, Long> ids = new HashMap<>();
        String select = """
                SELECT id, owner_user_id, period_year, period_month, salary_ars, salary_usd,
                       reference_rate, card_dollar_rate, payoneer_dollar_rate,
                       conservative_base_usd, apartment_target_price_usd,
                       apartment_down_payment_percent, apartment_current_savings_usd,
                       notes, created_at, updated_at
                FROM financial_periods
                """;
        String upsert = """
                INSERT INTO financial_periods (
                    owner_user_id, period_year, period_month, salary_ars, salary_usd,
                    reference_rate, card_dollar_rate, payoneer_dollar_rate,
                    conservative_base_usd, apartment_target_price_usd,
                    apartment_down_payment_percent, apartment_current_savings_usd,
                    notes, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (owner_user_id, period_year, period_month) DO UPDATE SET
                    salary_ars = EXCLUDED.salary_ars,
                    salary_usd = EXCLUDED.salary_usd,
                    reference_rate = EXCLUDED.reference_rate,
                    card_dollar_rate = EXCLUDED.card_dollar_rate,
                    payoneer_dollar_rate = EXCLUDED.payoneer_dollar_rate,
                    conservative_base_usd = EXCLUDED.conservative_base_usd,
                    apartment_target_price_usd = EXCLUDED.apartment_target_price_usd,
                    apartment_down_payment_percent = EXCLUDED.apartment_down_payment_percent,
                    apartment_current_savings_usd = EXCLUDED.apartment_current_savings_usd,
                    notes = EXCLUDED.notes,
                    updated_at = EXCLUDED.updated_at
                RETURNING id
                """;
        try (Statement query = source.createStatement();
             ResultSet rows = query.executeQuery(select);
             PreparedStatement write = target.prepareStatement(upsert)) {
            while (rows.next()) {
                Long ownerId = userIds.get(rows.getLong("owner_user_id"));
                if (ownerId == null) {
                    throw new SQLException("No target user for local user " + rows.getLong("owner_user_id"));
                }
                write.setLong(1, ownerId);
                write.setInt(2, rows.getInt("period_year"));
                write.setInt(3, rows.getInt("period_month"));
                setDecimal(write, 4, rows, "salary_ars");
                setDecimal(write, 5, rows, "salary_usd");
                setDecimal(write, 6, rows, "reference_rate");
                setDecimal(write, 7, rows, "card_dollar_rate");
                setDecimal(write, 8, rows, "payoneer_dollar_rate");
                setDecimal(write, 9, rows, "conservative_base_usd");
                setDecimal(write, 10, rows, "apartment_target_price_usd");
                setDecimal(write, 11, rows, "apartment_down_payment_percent");
                setDecimal(write, 12, rows, "apartment_current_savings_usd");
                write.setString(13, rows.getString("notes"));
                write.setTimestamp(14, rows.getTimestamp("created_at"));
                write.setTimestamp(15, rows.getTimestamp("updated_at"));
                try (ResultSet inserted = write.executeQuery()) {
                    inserted.next();
                    ids.put(rows.getLong("id"), inserted.getLong(1));
                }
            }
        }
        return ids;
    }

    private static int migrateExpenses(
            Connection source, Connection target, Map<Long, Long> periodIds) throws SQLException {
        String select = """
                SELECT id, period_id, category, detail, amount, currency, expense_type,
                       payment_method, expense_group, note, sort_order, created_at, updated_at
                FROM expense_items
                """;
        String upsert = """
                INSERT INTO expense_items (
                    id, period_id, category, detail, amount, currency, expense_type,
                    payment_method, expense_group, note, sort_order, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    period_id = EXCLUDED.period_id, category = EXCLUDED.category,
                    detail = EXCLUDED.detail, amount = EXCLUDED.amount,
                    currency = EXCLUDED.currency, expense_type = EXCLUDED.expense_type,
                    payment_method = EXCLUDED.payment_method, expense_group = EXCLUDED.expense_group,
                    note = EXCLUDED.note, sort_order = EXCLUDED.sort_order,
                    updated_at = EXCLUDED.updated_at
                """;
        return copyChildren(source, target, periodIds, select, upsert, (rows, write, periodId) -> {
            write.setLong(1, rows.getLong("id"));
            write.setLong(2, periodId);
            write.setString(3, rows.getString("category"));
            write.setString(4, rows.getString("detail"));
            write.setBigDecimal(5, rows.getBigDecimal("amount"));
            write.setString(6, rows.getString("currency"));
            write.setString(7, rows.getString("expense_type"));
            write.setString(8, rows.getString("payment_method"));
            write.setString(9, rows.getString("expense_group"));
            write.setString(10, rows.getString("note"));
            write.setInt(11, rows.getInt("sort_order"));
            write.setTimestamp(12, rows.getTimestamp("created_at"));
            write.setTimestamp(13, rows.getTimestamp("updated_at"));
        });
    }

    private static int migrateCards(
            Connection source, Connection target, Map<Long, Long> periodIds) throws SQLException {
        String select = """
                SELECT id, period_id, name, balance, currency, minimum_payment,
                       annual_rate_percent, due_date, monthly_payment, status,
                       sort_order, created_at, updated_at
                FROM credit_cards
                """;
        String upsert = """
                INSERT INTO credit_cards (
                    id, period_id, name, balance, currency, minimum_payment,
                    annual_rate_percent, due_date, monthly_payment, status,
                    sort_order, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    period_id = EXCLUDED.period_id, name = EXCLUDED.name,
                    balance = EXCLUDED.balance, currency = EXCLUDED.currency,
                    minimum_payment = EXCLUDED.minimum_payment,
                    annual_rate_percent = EXCLUDED.annual_rate_percent,
                    due_date = EXCLUDED.due_date, monthly_payment = EXCLUDED.monthly_payment,
                    status = EXCLUDED.status, sort_order = EXCLUDED.sort_order,
                    updated_at = EXCLUDED.updated_at
                """;
        return copyChildren(source, target, periodIds, select, upsert, (rows, write, periodId) -> {
            write.setLong(1, rows.getLong("id"));
            write.setLong(2, periodId);
            write.setString(3, rows.getString("name"));
            write.setBigDecimal(4, rows.getBigDecimal("balance"));
            write.setString(5, rows.getString("currency"));
            write.setBigDecimal(6, rows.getBigDecimal("minimum_payment"));
            write.setBigDecimal(7, rows.getBigDecimal("annual_rate_percent"));
            LocalDate dueDate = rows.getObject("due_date", LocalDate.class);
            write.setObject(8, dueDate);
            write.setBigDecimal(9, rows.getBigDecimal("monthly_payment"));
            write.setString(10, rows.getString("status"));
            write.setInt(11, rows.getInt("sort_order"));
            write.setTimestamp(12, rows.getTimestamp("created_at"));
            write.setTimestamp(13, rows.getTimestamp("updated_at"));
        });
    }

    private static int migrateAllocations(
            Connection source, Connection target, Map<Long, Long> periodIds) throws SQLException {
        String select = """
                SELECT id, period_id, stage, concept, percentage, objective,
                       allocation_role, sort_order, created_at, updated_at
                FROM plan_allocations
                """;
        String upsert = """
                INSERT INTO plan_allocations (
                    id, period_id, stage, concept, percentage, objective,
                    allocation_role, sort_order, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    period_id = EXCLUDED.period_id, stage = EXCLUDED.stage,
                    concept = EXCLUDED.concept, percentage = EXCLUDED.percentage,
                    objective = EXCLUDED.objective, allocation_role = EXCLUDED.allocation_role,
                    sort_order = EXCLUDED.sort_order, updated_at = EXCLUDED.updated_at
                """;
        return copyChildren(source, target, periodIds, select, upsert, (rows, write, periodId) -> {
            write.setLong(1, rows.getLong("id"));
            write.setLong(2, periodId);
            write.setString(3, rows.getString("stage"));
            write.setString(4, rows.getString("concept"));
            write.setBigDecimal(5, rows.getBigDecimal("percentage"));
            write.setString(6, rows.getString("objective"));
            write.setString(7, rows.getString("allocation_role"));
            write.setInt(8, rows.getInt("sort_order"));
            write.setTimestamp(9, rows.getTimestamp("created_at"));
            write.setTimestamp(10, rows.getTimestamp("updated_at"));
        });
    }

    private static int copyChildren(
            Connection source,
            Connection target,
            Map<Long, Long> periodIds,
            String select,
            String upsert,
            RowBinder binder) throws SQLException {
        int count = 0;
        try (Statement query = source.createStatement();
             ResultSet rows = query.executeQuery(select);
             PreparedStatement write = target.prepareStatement(upsert)) {
            while (rows.next()) {
                Long periodId = periodIds.get(rows.getLong("period_id"));
                if (periodId == null) {
                    throw new SQLException("No target period for local period " + rows.getLong("period_id"));
                }
                binder.bind(rows, write, periodId);
                write.executeUpdate();
                count++;
            }
        }
        return count;
    }

    private static void resetSequences(Connection target) throws SQLException {
        for (String table : new String[]{"users", "financial_periods", "expense_items", "credit_cards", "plan_allocations"}) {
            try (Statement statement = target.createStatement()) {
                statement.execute("""
                        SELECT setval(
                            pg_get_serial_sequence('%s', 'id'),
                            COALESCE((SELECT MAX(id) FROM %s), 1),
                            (SELECT COUNT(*) > 0 FROM %s))
                        """.formatted(table, table, table));
            }
        }
    }

    private static void setDecimal(
            PreparedStatement statement, int parameter, ResultSet rows, String column) throws SQLException {
        BigDecimal value = rows.getBigDecimal(column);
        statement.setBigDecimal(parameter, value);
    }

    @FunctionalInterface
    private interface RowBinder {
        void bind(ResultSet rows, PreparedStatement write, long periodId) throws SQLException;
    }

    private record MigrationResult(int users, int periods, int expenses, int cards, int allocations) {
    }
}
