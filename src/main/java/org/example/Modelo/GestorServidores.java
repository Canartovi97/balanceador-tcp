package org.example.Modelo;

import java.net.Socket;
import java.util.*;

public class GestorServidores {
    private static final int RANGO_MIN_BALANCEADOR = 5400;
    private static final int RANGO_MAX_BALANCEADOR = 5800;
    private static final int RANGO_MIN_MONITOREO = 6400;
    private static final int RANGO_MAX_MONITOREO = 6800;

    private int contadorBalanceador = RANGO_MIN_BALANCEADOR;
    private int contadorMonitoreo = RANGO_MIN_MONITOREO;
    private final List<Servidor> servidores;

    public GestorServidores() {
        this.servidores = new ArrayList<>();
    }

    public synchronized Servidor agregarServidor(Socket socket) {
        if (contadorMonitoreo > RANGO_MAX_MONITOREO || contadorBalanceador > RANGO_MAX_BALANCEADOR) {
            System.out.println("No hay más IPs disponibles para asignar.");
            return null;
        }

        int puertoBalanceador = contadorBalanceador++; // Asigna el siguiente puerto en el rango 5400-5800
        int puertoMonitoreo = contadorMonitoreo++; // Asigna el siguiente puerto en el rango 6400-6800

        Servidor servidor = new Servidor(puertoBalanceador, puertoMonitoreo, socket);
        servidores.add(servidor);
        return servidor;
    }

    public synchronized void removerServidor(int puertoMonitoreo) {
        servidores.removeIf(servidor -> servidor.getPuertoMonitoreo() == puertoMonitoreo);
        System.out.println("[GestorServidores] Eliminado servidor con puerto de monitoreo " + puertoMonitoreo);
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

    public synchronized void marcarServidorInactivo(int puertoMonitoreo) {
        for (Servidor servidor : servidores) {
            if (servidor.getPuertoMonitoreo() == puertoMonitoreo) {
                servidor.setActivo(false);
                System.out.println("Servidor con puerto de monitoreo " + puertoMonitoreo + " marcado como inactivo.");
                break;
            }
        }
    }
}
