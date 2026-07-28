package com.finanzas.expenses.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.finanzas.common.Currency;
import com.finanzas.expenses.model.ExpenseItem;
import com.finanzas.expenses.model.ExpenseType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ExpenseItemRepository {

    private static final String COLUMNS = """
            id, period_id, category, detail, amount, currency, expense_type, expense_group, note,
            sort_order, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public ExpenseItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ExpenseItem> findByPeriodId(Long periodId) {
        String sql = "SELECT " + COLUMNS + " FROM expense_items WHERE period_id = ? ORDER BY sort_order, id";
        return jdbcTemplate.query(sql, this::mapRow, periodId);
    }

    public Optional<ExpenseItem> findById(Long id) {
        String sql = "SELECT " + COLUMNS + " FROM expense_items WHERE id = ?";
        return jdbcTemplate.query(sql, this::mapRow, id).stream().findFirst();
    }

    public ExpenseItem insert(ExpenseItem item) {
        String sql = """
                INSERT INTO expense_items (
                    period_id, category, detail, amount, currency, expense_type, expense_group, note,
                    sort_order, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setLong(1, item.periodId());
            statement.setString(2, item.category());
            statement.setString(3, item.detail());
            statement.setBigDecimal(4, item.amount());
            statement.setString(5, item.currency().name());
            statement.setString(6, item.expenseType().name());
            statement.setString(7, item.expenseGroup());
            statement.setString(8, item.note());
            statement.setInt(9, item.sortOrder());
            statement.setTimestamp(10, Timestamp.valueOf(now));
            statement.setTimestamp(11, Timestamp.valueOf(now));
            return statement;
        }, keyHolder);

        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        return findById(id).orElseThrow();
    }

    public ExpenseItem update(ExpenseItem item) {
        String sql = """
                UPDATE expense_items
                SET category = ?, detail = ?, amount = ?, currency = ?, expense_type = ?, expense_group = ?,
                    note = ?, sort_order = ?, updated_at = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                item.category(),
                item.detail(),
                item.amount(),
                item.currency().name(),
                item.expenseType().name(),
                item.expenseGroup(),
                item.note(),
                item.sortOrder(),
                Timestamp.valueOf(LocalDateTime.now()),
                item.id());

        return findById(item.id()).orElseThrow();
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM expense_items WHERE id = ?", id);
    }

    public int nextSortOrder(Long periodId) {
        Integer max = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(sort_order), -1) FROM expense_items WHERE period_id = ?",
                Integer.class, periodId);
        return max == null ? 0 : max + 1;
    }

    private ExpenseItem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ExpenseItem(
                rs.getLong("id"),
                rs.getLong("period_id"),
                rs.getString("category"),
                rs.getString("detail"),
                rs.getBigDecimal("amount"),
                Currency.valueOf(rs.getString("currency")),
                ExpenseType.valueOf(rs.getString("expense_type")),
                rs.getString("expense_group"),
                rs.getString("note"),
                rs.getInt("sort_order"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
