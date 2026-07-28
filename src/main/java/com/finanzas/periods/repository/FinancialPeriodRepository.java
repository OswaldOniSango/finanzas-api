package com.finanzas.periods.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.finanzas.periods.model.ApartmentGoal;
import com.finanzas.periods.model.FinancialPeriod;
import com.finanzas.periods.model.Income;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class FinancialPeriodRepository {

    private static final String COLUMNS = """
            id, period_year, period_month, salary_ars, salary_usd, reference_rate, conservative_base_usd,
            apartment_target_price_usd, apartment_down_payment_percent, apartment_current_savings_usd,
            notes, created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public FinancialPeriodRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FinancialPeriod> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM financial_periods ORDER BY period_year DESC, period_month DESC";
        return jdbcTemplate.query(sql, this::mapRow);
    }

    public Optional<FinancialPeriod> findById(Long id) {
        String sql = "SELECT " + COLUMNS + " FROM financial_periods WHERE id = ?";
        return jdbcTemplate.query(sql, this::mapRow, id).stream().findFirst();
    }

    public Optional<FinancialPeriod> findByYearMonth(int year, int month) {
        String sql = "SELECT " + COLUMNS + " FROM financial_periods WHERE period_year = ? AND period_month = ?";
        return jdbcTemplate.query(sql, this::mapRow, year, month).stream().findFirst();
    }

    public Optional<FinancialPeriod> findLatest() {
        String sql = "SELECT " + COLUMNS + """
                 FROM financial_periods
                 ORDER BY period_year DESC, period_month DESC
                 LIMIT 1
                """;
        return jdbcTemplate.query(sql, this::mapRow).stream().findFirst();
    }

    public boolean existsAny() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM financial_periods", Integer.class);
        return count != null && count > 0;
    }

    public FinancialPeriod insert(FinancialPeriod period) {
        String sql = """
                INSERT INTO financial_periods (
                    period_year, period_month, salary_ars, salary_usd, reference_rate, conservative_base_usd,
                    apartment_target_price_usd, apartment_down_payment_percent, apartment_current_savings_usd,
                    notes, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setInt(1, period.periodYear());
            statement.setInt(2, period.periodMonth());
            statement.setBigDecimal(3, period.income().salaryArs());
            statement.setBigDecimal(4, period.income().salaryUsd());
            statement.setBigDecimal(5, period.income().referenceRate());
            statement.setBigDecimal(6, period.income().conservativeBaseUsd());
            statement.setBigDecimal(7, period.apartmentGoal().targetPriceUsd());
            statement.setBigDecimal(8, period.apartmentGoal().downPaymentPercent());
            statement.setBigDecimal(9, period.apartmentGoal().currentSavingsUsd());
            statement.setString(10, period.notes());
            statement.setTimestamp(11, Timestamp.valueOf(now));
            statement.setTimestamp(12, Timestamp.valueOf(now));
            return statement;
        }, keyHolder);

        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        return findById(id).orElseThrow();
    }

    public FinancialPeriod updateIncome(Long id, Income income) {
        String sql = """
                UPDATE financial_periods
                SET salary_ars = ?, salary_usd = ?, reference_rate = ?, conservative_base_usd = ?, updated_at = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                income.salaryArs(),
                income.salaryUsd(),
                income.referenceRate(),
                income.conservativeBaseUsd(),
                Timestamp.valueOf(LocalDateTime.now()),
                id);

        return findById(id).orElseThrow();
    }

    public FinancialPeriod updateApartmentGoal(Long id, ApartmentGoal goal) {
        String sql = """
                UPDATE financial_periods
                SET apartment_target_price_usd = ?, apartment_down_payment_percent = ?,
                    apartment_current_savings_usd = ?, updated_at = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                goal.targetPriceUsd(),
                goal.downPaymentPercent(),
                goal.currentSavingsUsd(),
                Timestamp.valueOf(LocalDateTime.now()),
                id);

        return findById(id).orElseThrow();
    }

    public FinancialPeriod updateNotes(Long id, String notes) {
        jdbcTemplate.update(
                "UPDATE financial_periods SET notes = ?, updated_at = ? WHERE id = ?",
                notes, Timestamp.valueOf(LocalDateTime.now()), id);
        return findById(id).orElseThrow();
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM financial_periods WHERE id = ?", id);
    }

    private FinancialPeriod mapRow(ResultSet rs, int rowNum) throws SQLException {
        Income income = new Income(
                rs.getBigDecimal("salary_ars"),
                rs.getBigDecimal("salary_usd"),
                rs.getBigDecimal("reference_rate"),
                rs.getBigDecimal("conservative_base_usd"));

        ApartmentGoal goal = new ApartmentGoal(
                rs.getBigDecimal("apartment_target_price_usd"),
                rs.getBigDecimal("apartment_down_payment_percent"),
                rs.getBigDecimal("apartment_current_savings_usd"));

        return new FinancialPeriod(
                rs.getLong("id"),
                rs.getInt("period_year"),
                rs.getInt("period_month"),
                income,
                goal,
                rs.getString("notes"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
