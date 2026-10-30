package com.devsuj.crmapi.model;

/** PROGRAMADA = actividad agendada; se cierra como REALIZADA o CANCELADA (estados finales). */
public enum EstadoInteraccion {
    PROGRAMADA, REALIZADA, CANCELADA;

    public boolean esFinal() {
        return this != PROGRAMADA;
    }
}
