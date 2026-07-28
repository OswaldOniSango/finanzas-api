package com.finanzas.cards.repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.finanzas.cards.model.CardStatus;
import com.finanzas.cards.model.CreditCard;
import com.finanzas.common.Currency;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CreditCardRepository {

    private static final String COLUMNS = """
            id, period_id, name, balance, currency, minimum_payment, annual_rate_percent, due_date,
            monthly_payment, status, sort_order, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public CreditCardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CreditCard> findByPeriodId(Long periodId) {
        String sql = "SELECT " + COLUMNS + " FROM credit_cards WHERE period_id = ? ORDER BY sort_order, id";
        return jdbcTemplate.query(sql, this::mapRow, periodId);
    }

    public Optional<CreditCard> findById(Long id) {
        String sql = "SELECT " + COLUMNS + " FROM credit_cards WHERE id = ?";
        return jdbcTemplate.query(sql, this::mapRow, id).stream().findFirst();
    }

    public CreditCard insert(CreditCard card) {
        String sql = """
                INSERT INTO credit_cards (
                    period_id, name, balance, currency, minimum_payment, annual_rate_percent, due_date,
                    monthly_payment, status, sort_order, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setLong(1, card.periodId());
            statement.setString(2, card.name());
            statement.setBigDecimal(3, card.balance());
            statement.setString(4, card.currency().name());
            statement.setBigDecimal(5, card.minimumPayment());
            statement.setBigDecimal(6, card.annualRatePercent());
            if (card.dueDate() == null) {
                statement.setNull(7, Types.DATE);
            } else {
                statement.setDate(7, Date.valueOf(card.dueDate()));
            }
            statement.setBigDecimal(8, card.monthlyPayment());
            statement.setString(9, card.status().name());
            statement.setInt(10, card.sortOrder());
            statement.setTimestamp(11, Timestamp.valueOf(now));
            statement.setTimestamp(12, Timestamp.valueOf(now));
            return statement;
        }, keyHolder);

        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        return findById(id).orElseThrow();
    }

    public CreditCard update(CreditCard card) {
        String sql = """
                UPDATE credit_cards
                SET name = ?, balance = ?, currency = ?, minimum_payment = ?, annual_rate_percent = ?,
                    due_date = ?, monthly_payment = ?, status = ?, sort_order = ?, updated_at = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                card.name(),
                card.balance(),
                card.currency().name(),
                card.minimumPayment(),
                card.annualRatePercent(),
                card.dueDate() == null ? null : Date.valueOf(card.dueDate()),
                card.monthlyPayment(),
                card.status().name(),
                card.sortOrder(),
                Timestamp.valueOf(LocalDateTime.now()),
                card.id());

        return findById(card.id()).orElseThrow();
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM credit_cards WHERE id = ?", id);
    }

    public int nextSortOrder(Long periodId) {
        Integer max = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(sort_order), -1) FROM credit_cards WHERE period_id = ?",
                Integer.class, periodId);
        return max == null ? 0 : max + 1;
    }

    private CreditCard mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date dueDate = rs.getDate("due_date");
        return new CreditCard(
                rs.getLong("id"),
                rs.getLong("period_id"),
                rs.getString("name"),
                rs.getBigDecimal("balance"),
                Currency.valueOf(rs.getString("currency")),
                rs.getBigDecimal("minimum_payment"),
                rs.getBigDecimal("annual_rate_percent"),
                dueDate == null ? null : dueDate.toLocalDate(),
                rs.getBigDecimal("monthly_payment"),
                CardStatus.valueOf(rs.getString("status")),
                rs.getInt("sort_order"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
