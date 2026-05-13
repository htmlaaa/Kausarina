package com.milwar.kaosuarina.db.dao;

import com.milwar.kaosuarina.db.vo.RunVO;

import java.sql.SQLException;
import java.util.List;

public interface RunDAO {

    /** Inserta la run y devuelve el ID generado, o -1 si falla. */
    int guardar(RunVO run) throws SQLException;

    /**
     * Inserta kills por tipo de enemigo.
     * tiposEnemigo: nombres del enum Enemy.Tipo ("BASICO", "MALDITO", etc.)
     * cantidades:   kills de cada tipo, mismo índice que tiposEnemigo.
     */
    void guardarKills(int runId, String[] tiposEnemigo, int[] cantidades) throws SQLException;

    /**
     * Inserta upgrades elegidos durante la run.
     * tiposUpgrade: nombres del enum Upgrade.Tipo ("DANIO_UP", "FILO_IGNEO", etc.)
     * niveles:      nivel tomado de cada upgrade, mismo índice.
     */
    void guardarUpgrades(int runId, String[] tiposUpgrade, int[] niveles) throws SQLException;

    /** Registra la reliquia activa en la run. */
    void guardarReliquia(int runId, int reliquiaId) throws SQLException;

    /** Top 10 runs por score. */
    List<RunVO> obtenerTop10() throws SQLException;
}
