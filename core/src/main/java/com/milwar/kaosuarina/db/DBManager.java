package com.milwar.kaosuarina.db;

import com.badlogic.gdx.Gdx;
import com.milwar.kaosuarina.db.dao.RunDAO;
import com.milwar.kaosuarina.db.dao.impl.RunDAOImpl;
import com.milwar.kaosuarina.db.vo.RunVO;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private static boolean initialized = false;

    private static void ensureInit() {
        if (initialized) return;
        try {
            initDB();
            initialized = true;
        } catch (Exception e) {
            Gdx.app.error("DBManager", "Error en initDB: " + e.getMessage());
        }
    }

    private static void initDB() throws SQLException {
        String[] ddl = {
            // catalog: tipo_enemigo
            "CREATE TABLE IF NOT EXISTS tipo_enemigo (" +
                "  id     INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  nombre TEXT NOT NULL UNIQUE" +
                ")",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('BASICO')",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('RAPIDO')",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('TANQUE')",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('SHOOTER')",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('MALDITO')",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('ESPECTRAL')",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('GUARDIAN')",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('ARQUERO')",
            "INSERT OR IGNORE INTO tipo_enemigo (nombre) VALUES ('DEVASTADOR')",

            // main run table
            "CREATE TABLE IF NOT EXISTS run (" +
                "  id                 INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  personaje_id       INTEGER NOT NULL," +
                "  score              INTEGER NOT NULL DEFAULT 0," +
                "  tiempo_segundos    INTEGER NOT NULL DEFAULT 0," +
                "  nivel_alcanzado    INTEGER NOT NULL DEFAULT 1," +
                "  mana_total_gastado INTEGER NOT NULL DEFAULT 0," +
                "  completada         INTEGER NOT NULL DEFAULT 0," +
                "  fecha_fin          TEXT    DEFAULT CURRENT_TIMESTAMP" +
                ")",
            "CREATE INDEX IF NOT EXISTS idx_run_score ON run (score DESC)",

            // kills per enemy type
            "CREATE TABLE IF NOT EXISTS run_kill (" +
                "  id              INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  run_id          INTEGER NOT NULL," +
                "  tipo_enemigo_id INTEGER NOT NULL," +
                "  cantidad        INTEGER NOT NULL DEFAULT 0," +
                "  UNIQUE (run_id, tipo_enemigo_id)" +
                ")",
            "CREATE INDEX IF NOT EXISTS idx_run_kill ON run_kill (run_id)",

            // equipped weapons
            "CREATE TABLE IF NOT EXISTS run_arma (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  run_id      INTEGER NOT NULL," +
                "  slot        INTEGER NOT NULL," +
                "  arma_tipo   TEXT    NOT NULL," +
                "  inscripcion TEXT" +
                ")",
            "CREATE INDEX IF NOT EXISTS idx_run_arma ON run_arma (run_id)",

            // meta-progresión
            "CREATE TABLE IF NOT EXISTS meta_tokens (" +
                "  id    INTEGER PRIMARY KEY DEFAULT 1," +
                "  total INTEGER NOT NULL DEFAULT 0" +
                ")",
            "INSERT OR IGNORE INTO meta_tokens (id, total) VALUES (1, 0)"
        };

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            for (String sql : ddl) {
                st.execute(sql);
            }
        }
    }

    /**
     * Guarda run + kills + armas en una transacción.
     * @return ID de la run insertada, o -1 si falló.
     */
    public static int guardarRun(RunData data) {
        ensureInit();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            RunDAO dao = new RunDAOImpl(conn);

            RunVO vo = new RunVO(data.personajeId, data.score,
                data.tiempoSegundos, data.nivelAlcanzado, data.manaTotal);
            vo.completada = data.completada;
            int runId = dao.guardar(vo);
            if (runId < 0) {
                conn.rollback();
                return -1;
            }

            String[] tiposEnemigo = {
                "BASICO", "RAPIDO", "TANQUE", "SHOOTER",
                "MALDITO", "ESPECTRAL", "GUARDIAN", "ARQUERO", "DEVASTADOR"
            };
            if (data.killsPorTipo != null) {
                dao.guardarKills(runId, tiposEnemigo, data.killsPorTipo);
            }

            if (data.armasEquipadas != null && data.armasEquipadas.length > 0) {
                dao.guardarArmas(runId, data.armasEquipadas, data.inscripcionesEquipadas);
            }

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

    // ── Meta-progresión tokens ────────────────────────────────────────────────

    public static int addTokens(int amount) {
        if (amount <= 0) return getTokensTotal();
        ensureInit();
        try (Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "UPDATE meta_tokens SET total = total + ? WHERE id = 1")) {
            ps.setInt(1, amount);
            ps.executeUpdate();
            return getTokensTotal();
        } catch (Exception e) {
            Gdx.app.error("DBManager", "Error al sumar tokens: " + e.getMessage());
            return -1;
        }
    }

    public static int getTokensTotal() {
        ensureInit();
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT total FROM meta_tokens WHERE id=1")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            Gdx.app.error("DBManager", "Error al leer tokens: " + e.getMessage());
            return 0;
        }
    }

    public static int calcularTokens(int score, int level, int waves) {
        return Math.max(1, score / 200 + level * 2 + waves);
    }

    /**
     * Top 10 runs por score (todas las runs, no solo completadas).
     */
    public static List<RunVO> getTop10() {
        ensureInit();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            RunDAO dao = new RunDAOImpl(conn);
            return dao.obtenerTop10();
        } catch (Exception e) {
            Gdx.app.error("DBManager", "Error al obtener top10: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // ── DTO ──────────────────────────────────────────────────────────────────

    public static class RunData {
        public int personajeId;
        public int score;
        public int tiempoSegundos;
        public int nivelAlcanzado;
        public int manaTotal;
        public boolean completada;
        public int[] killsPorTipo;
        public String[] armasEquipadas;
        public String[] inscripcionesEquipadas;
    }
}
