package org.example.Controlador;

import org.example.Modelo.GestorServidores;
import org.example.Modelo.Servidor;

import java.io.*;
import java.net.*;

public class BalanceadorControlador {
    private static final int PUERTO_BALANCEADOR = 5000;
    private static final String IP_MONITOREO = "127.0.0.1";
    private static final int PUERTO_MONITOREO = 54321;
    private final GestorServidores gestorServidores;

    public BalanceadorControlador() {
        this.gestorServidores = new GestorServidores();
    }

    public void iniciarBalanceador() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_BALANCEADOR)) {
            System.out.println("Balanceador escuchando en el puerto " + PUERTO_BALANCEADOR);

            while (true) {
                Socket socket = serverSocket.accept();
                manejarConexionServidor(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manejarConexionServidor(Socket socket) {
        try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println(" Servidor conectado desde: " + socket.getInetAddress());

            Servidor nuevoServidor = gestorServidores.agregarServidor(socket);
            if (nuevoServidor == null) {
                System.out.println(" No hay más IPs disponibles. Cerrando conexión.");
                salida.println("ERROR: No hay más IPs disponibles");
                socket.close();
                return;
            }

            int puertoAsignado = nuevoServidor.getIpAsignada();
            System.out.println(" Asignando IP: " + puertoAsignado);
            salida.println(String.format("%05d", puertoAsignado));

            System.out.println(" Servidor registrado con IP " + puertoAsignado);
            enviarAlMonitoreo(puertoAsignado);

        } catch (IOException e) {
            System.out.println(" Error manejando servidor: " + e.getMessage());
        }
    }


    private void enviarAlMonitoreo(int puertoAsignado) {
        try (Socket socket = new Socket(IP_MONITOREO, PUERTO_MONITOREO);
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            String mensaje = "NEW_SERVER " + puertoAsignado;
            salida.println(mensaje);
            System.out.println("Notificación enviada al monitoreo: " + mensaje);

        } catch (IOException e) {
            System.out.println("Error al enviar mensaje al monitoreo: " + e.getMessage());
        }
    }
}
