package org.example.Modelo;

import java.net.Socket;
import java.util.*;

public class GestorServidores {
    private static final int RANGO_MIN_IP = 1;
    private static final int RANGO_MAX_IP = 10000;
    private int contadorIP = RANGO_MIN_IP;
    private final List<Servidor> servidores;

    public GestorServidores() {
        this.servidores = new ArrayList<>();
    }

    public synchronized Servidor agregarServidor(Socket socket) {
        if (contadorIP > RANGO_MAX_IP) {
            System.out.println("No hay más IPs disponibles para asignar.");
            return null;
        }

        Servidor servidor = new Servidor(contadorIP++, socket);
        servidores.add(servidor);
        return servidor;
    }

    public synchronized List<Servidor> obtenerServidoresActivos() {
        List<Servidor> activos = new ArrayList<>();
        for (Servidor servidor : servidores) {
            if (servidor.isActivo()) {
                activos.add(servidor);
            }
        }
        return activos;
    }

    public synchronized void marcarServidorInactivo(int ip) {
        for (Servidor servidor : servidores) {
            if (servidor.getIpAsignada() == ip) {
                servidor.setActivo(false);
                System.out.println("Servidor con IP " + ip + " marcado como inactivo.");
                break;
            }
        }
    }
}
