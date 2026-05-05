package com.milwar.kaosuarina.Systems;

public class Upgrade {

    public enum Tipo {
        DANIO_UP,
        CADENCIA_UP,
        VELOCIDAD_UP,
        VIDA_MAXIMA_UP,
        PERFORACION,
        BALA_EXTRA
    }

    public Tipo   tipo;
    public String nombre;
    public String descripcion;
    public int    nivel;
    public int    nivelMax;

    public Upgrade(Tipo tipo, String nombre, String descripcion, int nivelMax) {
        this.tipo        = tipo;
        this.nombre      = nombre;
        this.descripcion = descripcion;
        this.nivel       = 0;
        this.nivelMax    = nivelMax;
    }

    public boolean puedeMejorar()  { return nivel < nivelMax; }
    public void    mejorar()       { if (puedeMejorar()) nivel++; }
}
