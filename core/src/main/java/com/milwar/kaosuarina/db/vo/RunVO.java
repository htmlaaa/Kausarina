package com.milwar.kaosuarina.db.vo;

public class RunVO {

    public int id;
    public int personajeId;
    public int score;
    public int tiempoSegundos;
    public int nivelAlcanzado;
    public int manaTotal;       // mana_total_gastado
    public boolean completada;

    public RunVO() {
    }

    public RunVO(int personajeId, int score, int tiempoSegundos,
                 int nivelAlcanzado, int manaTotal) {
        this.personajeId = personajeId;
        this.score = score;
        this.tiempoSegundos = tiempoSegundos;
        this.nivelAlcanzado = nivelAlcanzado;
        this.manaTotal = manaTotal;
        this.completada = true;
    }
}
