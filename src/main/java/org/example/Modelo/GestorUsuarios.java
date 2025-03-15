package org.example.Modelo;

import java.net.Socket;
import java.util.*;

public class GestorUsuarios {
    private static final int RANGO_MIN_CLIENTE = 8400;
    private static final int RANGO_MAX_CLIENTE = 8800;
    private int contadorCliente = RANGO_MIN_CLIENTE;
    private final Map<Integer, Socket> clientes = new HashMap<>();

    public synchronized int asignarPuertoCliente(Socket socket) {
        if (contadorCliente > RANGO_MAX_CLIENTE) {
            return -1; // No hay más puertos disponibles
        }
        int puertoAsignado = contadorCliente++;
        clientes.put(puertoAsignado, socket);
        return puertoAsignado;
    }
}
