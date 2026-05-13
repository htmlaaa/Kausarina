package com.milwar.kaosuarina.db;

import com.badlogic.gdx.Gdx;
import com.milwar.kaosuarina.db.dao.RunDAO;
import com.milwar.kaosuarina.db.dao.impl.RunDAOImpl;
import com.milwar.kaosuarina.db.vo.RunVO;

import java.sql.Connection;
import java.sql.SQLException;

public class DBManager {

    /**
     * Guarda todos los datos de la run en la BD usando el patrón DAO.
     * Envuelve la operación en una transacción: si algo falla, hace rollback.
     * Si MySQL no está disponible, el juego continúa — solo se loguea el error.
     *
     * @return ID de la run insertada, o -1 si falló.
     */
    public static int guardarRun(RunData data) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            RunDAO dao = new RunDAOImpl(conn);

            RunVO vo = new RunVO(data.personajeId, data.score,
                data.tiempoSegundos, data.nivelAlcanzado, data.manaTotal);
            int runId = dao.guardar(vo);
            if (runId < 0) {
                conn.rollback();
                return -1;
            }

            // kills por tipo (Enemy.Tipo.values() en orden)
            String[] tiposEnemigo = {"BASICO", "RAPIDO", "TANQUE", "SHOOTER", "MALDITO", "ESPECTRAL"};
            dao.guardarKills(runId, tiposEnemigo, data.killsPorTipo);

            // upgrades elegidos
            if (data.upgradesTipos != null && data.upgradesTipos.length > 0) {
                dao.guardarUpgrades(runId, data.upgradesTipos, data.upgradesNiveles);
            }

            // reliquia activa
            dao.guardarReliquia(runId, data.reliquiaId);

            conn.commit();
            return runId;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            Gdx.app.error("DBManager", "Error al guardar run: " + e.getMessage());
            return -1;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // ── DTO (transfer object entre el juego y la BD) ──────────────────────────

    public static class RunData {
        public int      personajeId;       // 1=Caballero, 2=Mago, 3=Tirador
        public int      score;
        public int      tiempoSegundos;
        public int      nivelAlcanzado;
        public int      manaTotal;
        public int[]    killsPorTipo;       // indexado por Enemy.Tipo.ordinal()
        public String[] upgradesTipos;     // nombres del enum Upgrade.Tipo
        public int[]    upgradesNiveles;   // nivel_tomado de cada upgrade
        public int      reliquiaId;        // FK a tabla reliquia (1=Caballero,2=Mago,3=Tirador)
    }
}
