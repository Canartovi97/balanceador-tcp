package org.example.Modelo;

import java.net.Socket;

public class Servidor {
    private String nombre;
    private int ipAsignada;
    private boolean activo;
    private Socket socket;

    public Servidor(int ipAsignada, Socket socket) {
        this.nombre = nombre;
        this.ipAsignada = ipAsignada;
        this.socket = socket;
        this.activo = true;
    }

    public String getNombre() {
        return nombre;
    }

    public int getIpAsignada() {
        return ipAsignada;
    }

    public boolean isActivo() {
        return activo;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
