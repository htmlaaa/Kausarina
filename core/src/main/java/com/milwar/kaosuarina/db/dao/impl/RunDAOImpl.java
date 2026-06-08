package com.milwar.kaosuarina.db.dao.impl;

import com.milwar.kaosuarina.db.dao.RunDAO;
import com.milwar.kaosuarina.db.vo.RunVO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RunDAOImpl implements RunDAO {

    private final Connection connection;

    public RunDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int guardar(RunVO run) throws SQLException {
        String sql = "INSERT INTO run (personaje_id, score, tiempo_segundos, " +
            "nivel_alcanzado, mana_total_gastado, completada, fecha_fin) " +
            "VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, run.personajeId);
            ps.setInt(2, run.score);
            ps.setInt(3, run.tiempoSegundos);
            ps.setInt(4, run.nivelAlcanzado);
            ps.setInt(5, run.manaTotal);
            ps.setBoolean(6, run.completada);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    @Override
    public void guardarKills(int runId, String[] tiposEnemigo, int[] cantidades) throws SQLException {
        String sql = "INSERT INTO run_kill (run_id, tipo_enemigo_id, cantidad) VALUES (?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < tiposEnemigo.length; i++) {
                if (cantidades[i] <= 0) continue;
                int tipoId = resolverIdCatalogo("tipo_enemigo", tiposEnemigo[i]);
                if (tipoId < 0) continue;
                ps.setInt(1, runId);
                ps.setInt(2, tipoId);
                ps.setInt(3, cantidades[i]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public void guardarUpgrades(int runId, String[] tiposUpgrade, int[] niveles) throws SQLException {
        String sql = "INSERT INTO run_upgrade (run_id, tipo_upgrade_id, nivel_tomado, orden) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < tiposUpgrade.length; i++) {
                int tipoId = resolverIdCatalogo("tipo_upgrade", tiposUpgrade[i]);
                if (tipoId < 0) continue;
                ps.setInt(1, runId);
                ps.setInt(2, tipoId);
                ps.setInt(3, niveles[i]);
                ps.setInt(4, i + 1);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public void guardarReliquia(int runId, int reliquiaId) throws SQLException {
        String sql = "INSERT INTO run_reliquia (run_id, reliquia_id) VALUES (?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, runId);
            ps.setInt(2, reliquiaId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<RunVO> obtenerTop10() throws SQLException {
        String sql = "SELECT id, personaje_id, score, tiempo_segundos, nivel_alcanzado, " +
            "mana_total_gastado FROM run WHERE completada = 1 " +
            "ORDER BY score DESC LIMIT 10";
        List<RunVO> lista = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RunVO vo = new RunVO();
                vo.id = rs.getInt("id");
                vo.personajeId = rs.getInt("personaje_id");
                vo.score = rs.getInt("score");
                vo.tiempoSegundos = rs.getInt("tiempo_segundos");
                vo.nivelAlcanzado = rs.getInt("nivel_alcanzado");
                vo.manaTotal = rs.getInt("mana_total_gastado");
                vo.completada = true;
                lista.add(vo);
            }
        }
        return lista;
    }

    @Override
    public void guardarArmas(int runId, String[] armas, String[] inscripciones) throws SQLException {
        String sql = "INSERT INTO run_arma (run_id, slot, arma_tipo, inscripcion) VALUES (?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < armas.length; i++) {
                if (armas[i] == null) continue;
                ps.setInt(1, runId);
                ps.setInt(2, i);
                ps.setString(3, armas[i]);
                String ins = (inscripciones != null && i < inscripciones.length) ? inscripciones[i] : null;
                if (ins != null) ps.setString(4, ins);
                else ps.setNull(4, Types.VARCHAR);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Resuelve el ID de un catálogo (tipo_enemigo o tipo_upgrade) por nombre.
     */
    private int resolverIdCatalogo(String tabla, String nombre) throws SQLException {
        String sql = "SELECT id FROM " + tabla + " WHERE nombre = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }
}
