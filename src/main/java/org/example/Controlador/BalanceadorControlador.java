package org.example.Controlador;

import org.example.Modelo.GestorServidores;
import org.example.Modelo.Servidor;

import java.io.*;
import java.net.*;

public class BalanceadorControlador {
    private static final int PUERTO_BALANCEADOR = 5000;
    private static final String IP_MONITOREO = "localhost";
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

            int puertoBalanceador = nuevoServidor.getPuertoBalanceador();
            int puertoMonitoreo = nuevoServidor.getPuertoMonitoreo();

            salida.println(puertoBalanceador + " " + puertoMonitoreo);
            System.out.println("Asignando puertos - Balanceador: " + puertoBalanceador + ", Monitoreo: " + puertoMonitoreo);

            // Notificar al Monitoreo sobre el nuevo servidor
            enviarServidorAlMonitoreo(puertoMonitoreo);

        } catch (IOException e) {
            System.out.println(" Error manejando servidor: " + e.getMessage());
        }
    }

    private void enviarServidorAlMonitoreo(int puertoMonitoreo) {
        try (Socket socket = new Socket("localhost", PUERTO_MONITOREO);
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            String mensaje = "NEW_SERVER " + puertoMonitoreo;
            salida.println(mensaje);
            System.out.println("[Balanceador] Notificación enviada al monitoreo: " + mensaje);

        } catch (IOException e) {
            System.out.println("[Balanceador] Error al enviar mensaje al monitoreo: " + e.getMessage());
        }
    }

    public void iniciarEscuchaMonitoreo() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PUERTO_MONITOREO)) {
                System.out.println("[Balanceador] Escuchando mensajes del monitoreo en el puerto " + PUERTO_MONITOREO);

                while (true) {
                    Socket socket = serverSocket.accept();
                    manejarMensajeMonitoreo(socket);
                }
            } catch (IOException e) {
                System.out.println("[Balanceador] Error en la escucha del monitoreo: " + e.getMessage());
            }
        }).start();
    }

    private void manejarMensajeMonitoreo(Socket socket) {
        try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            String mensaje = entrada.readLine();
            System.out.println("[Balanceador] Mensaje recibido del monitoreo: " + mensaje);

            if (mensaje.startsWith("SERVER_DOWN")) {
                String[] partes = mensaje.split(" ");
                if (partes.length == 2) {
                    int puertoInactivo = Integer.parseInt(partes[1]);
                    gestorServidores.removerServidor(puertoInactivo);
                    System.out.println("[Balanceador] Servidor en puerto " + puertoInactivo + " eliminado de la lista.");
                }
            }
        } catch (IOException e) {
            System.out.println("[Balanceador] Error procesando mensaje del monitoreo: " + e.getMessage());
        }
    }
}
