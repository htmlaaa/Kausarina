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

            // catalog: tipo_upgrade
            "CREATE TABLE IF NOT EXISTS tipo_upgrade (" +
                "  id     INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  nombre TEXT NOT NULL UNIQUE" +
                ")",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('DANIO_UP')",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('CADENCIA_UP')",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('VELOCIDAD_UP')",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('VIDA_MAXIMA_UP')",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('PERFORACION')",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('BALA_EXTRA')",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('FILO_IGNEO')",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('CUCHILLA_VENENO')",
            "INSERT OR IGNORE INTO tipo_upgrade (nombre) VALUES ('VAMPIRISMO')",

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

            // upgrades chosen
            "CREATE TABLE IF NOT EXISTS run_upgrade (" +
                "  id              INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  run_id          INTEGER NOT NULL," +
                "  tipo_upgrade_id INTEGER NOT NULL," +
                "  nivel_tomado    INTEGER NOT NULL DEFAULT 1," +
                "  orden           INTEGER NOT NULL" +
                ")",
            "CREATE INDEX IF NOT EXISTS idx_run_upg ON run_upgrade (run_id)",

            // active relic
            "CREATE TABLE IF NOT EXISTS run_reliquia (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  run_id      INTEGER NOT NULL," +
                "  reliquia_id INTEGER NOT NULL" +
                ")",
            "CREATE INDEX IF NOT EXISTS idx_run_rel ON run_reliquia (run_id)",

            // equipped weapons + inscriptions
            "CREATE TABLE IF NOT EXISTS run_arma (" +
                "  id          INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  run_id      INTEGER NOT NULL," +
                "  slot        INTEGER NOT NULL," +
                "  arma_tipo   TEXT    NOT NULL," +
                "  inscripcion TEXT" +
                ")",
            "CREATE INDEX IF NOT EXISTS idx_run_arma ON run_arma (run_id)",

            // meta-progresión: tokens acumulados entre runs (MEC-01 Plan A)
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
     * Guarda todos los datos de la run en la BD usando el patrón DAO.
     * Envuelve la operación en una transacción: si algo falla, hace rollback.
     * Si la BD no está disponible, el juego continúa — solo se loguea el error.
     *
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
            int runId = dao.guardar(vo);
            if (runId < 0) {
                conn.rollback();
                return -1;
            }

            // kills por tipo (mismo orden que Enemy.Tipo.values())
            String[] tiposEnemigo = {
                "BASICO", "RAPIDO", "TANQUE", "SHOOTER",
                "MALDITO", "ESPECTRAL", "GUARDIAN", "ARQUERO", "DEVASTADOR"
            };
            dao.guardarKills(runId, tiposEnemigo, data.killsPorTipo);

            // upgrades elegidos
            if (data.upgradesTipos != null && data.upgradesTipos.length > 0) {
                dao.guardarUpgrades(runId, data.upgradesTipos, data.upgradesNiveles);
            }

            // reliquia activa
            dao.guardarReliquia(runId, data.reliquiaId);

            // armas equipadas + inscripciones
            if (data.armasEquipadas != null && data.armasEquipadas.length > 0) {
                dao.guardarArmas(runId, data.armasEquipadas, data.inscripcionesEquipadas);
            }

            conn.commit();
            return runId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
            Gdx.app.error("DBManager", "Error al guardar run: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            Gdx.app.error("DBManager", "Error inesperado al guardar run: " + e.getMessage());
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // ── MEC-01 — Meta-progresión tokens ──────────────────────────────────────

    /**
     * Adds tokens to the persistent total. Formula: score/200 + level*2 + waves.
     *
     * @return the new total, or -1 on error.
     */
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

    /**
     * Returns the current accumulated token total, or 0 on error.
     */
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

    /**
     * Calculates tokens earned for a run.
     */
    public static int calcularTokens(int score, int level, int waves) {
        return Math.max(1, score / 200 + level * 2 + waves);
    }

    /**
     * Top 10 runs por score. Devuelve lista vacía si la BD no está disponible.
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
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // ── DTO (transfer object entre el juego y la BD) ──────────────────────────

    public static class RunData {
        public int personajeId;            // 1=Caballero, 2=Mago, 3=Tirador
        public int score;
        public int tiempoSegundos;
        public int nivelAlcanzado;
        public int manaTotal;
        public int[] killsPorTipo;            // indexado por Enemy.Tipo.ordinal()
        public String[] upgradesTipos;          // nombres del enum Upgrade.Tipo
        public int[] upgradesNiveles;         // nivel_tomado de cada upgrade
        public int reliquiaId;             // 1=Caballero, 2=Mago, 3=Tirador
        public String[] armasEquipadas;         // WeaponType.name() por slot; null = slot vacío
        public String[] inscripcionesEquipadas; // Inscription.getName() por slot; null = sin inscripción
    }
}
