package org.example.Modelo;

import java.net.Socket;

public class Servidor {
    private int puertoBalanceador;
    private int puertoMonitoreo;
    private boolean activo;
    private Socket socket;

    public Servidor(int puertoBalanceador, int puertoMonitoreo, Socket socket) {
        this.puertoBalanceador = puertoBalanceador;
        this.puertoMonitoreo = puertoMonitoreo;
        this.socket = socket;
        this.activo = true;
    }

    public int getPuertoBalanceador() {
        return puertoBalanceador;
    }

    public int getPuertoMonitoreo() {
        return puertoMonitoreo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Socket getSocket() {
        return socket;
    }
}
