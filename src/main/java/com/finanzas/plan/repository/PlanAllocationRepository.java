package com.finanzas.plan.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.finanzas.plan.model.AllocationRole;
import com.finanzas.plan.model.PlanAllocation;
import com.finanzas.plan.model.PlanStage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PlanAllocationRepository {

    private static final String COLUMNS = """
            id, period_id, stage, concept, percentage, objective, allocation_role, sort_order,
            created_at, updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public PlanAllocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PlanAllocation> findByPeriodId(Long periodId) {
        String sql = "SELECT " + COLUMNS + " FROM plan_allocations WHERE period_id = ? ORDER BY stage, sort_order, id";
        return jdbcTemplate.query(sql, this::mapRow, periodId);
    }

    public Optional<PlanAllocation> findById(Long id) {
        String sql = "SELECT " + COLUMNS + " FROM plan_allocations WHERE id = ?";
        return jdbcTemplate.query(sql, this::mapRow, id).stream().findFirst();
    }

    public PlanAllocation insert(PlanAllocation allocation) {
        String sql = """
                INSERT INTO plan_allocations (
                    period_id, stage, concept, percentage, objective, allocation_role, sort_order,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setLong(1, allocation.periodId());
            statement.setString(2, allocation.stage().name());
            statement.setString(3, allocation.concept());
            statement.setBigDecimal(4, allocation.percentage());
            statement.setString(5, allocation.objective());
            statement.setString(6, allocation.allocationRole().name());
            statement.setInt(7, allocation.sortOrder());
            statement.setTimestamp(8, Timestamp.valueOf(now));
            statement.setTimestamp(9, Timestamp.valueOf(now));
            return statement;
        }, keyHolder);

        Long id = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
        return findById(id).orElseThrow();
    }

    public PlanAllocation update(PlanAllocation allocation) {
        String sql = """
                UPDATE plan_allocations
                SET stage = ?, concept = ?, percentage = ?, objective = ?, allocation_role = ?,
                    sort_order = ?, updated_at = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                allocation.stage().name(),
                allocation.concept(),
                allocation.percentage(),
                allocation.objective(),
                allocation.allocationRole().name(),
                allocation.sortOrder(),
                Timestamp.valueOf(LocalDateTime.now()),
                allocation.id());

        return findById(allocation.id()).orElseThrow();
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM plan_allocations WHERE id = ?", id);
    }

    public int nextSortOrder(Long periodId, PlanStage stage) {
        Integer max = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(sort_order), -1) FROM plan_allocations WHERE period_id = ? AND stage = ?",
                Integer.class, periodId, stage.name());
        return max == null ? 0 : max + 1;
    }

    private PlanAllocation mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PlanAllocation(
                rs.getLong("id"),
                rs.getLong("period_id"),
                PlanStage.valueOf(rs.getString("stage")),
                rs.getString("concept"),
                rs.getBigDecimal("percentage"),
                rs.getString("objective"),
                AllocationRole.valueOf(rs.getString("allocation_role")),
                rs.getInt("sort_order"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
