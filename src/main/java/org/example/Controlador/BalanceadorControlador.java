package org.example.Controlador;

import org.example.Modelo.GestorServidores;
import org.example.Modelo.GestorUsuarios;
import org.example.Modelo.Servidor;
import java.net.ServerSocket;

import java.io.*;
import java.net.*;

public class BalanceadorControlador {
    private static final int PUERTO_BALANCEADOR_SERVIDORES = 5000;
    private static final int PUERTO_BALANCEADOR_CLIENTES = 12345;
    private static final int PUERTO_MONITOREO_BALANCEADOR = 2000;
    private static final String IP_MONITOREO = "localhost";
    private static final int PUERTO_MONITOREO = 54321;

    private ServerSocket serverSocket;

    private final GestorServidores gestorServidores;
    private final GestorUsuarios gestorUsuarios;

    public BalanceadorControlador() {
        this.gestorServidores = new GestorServidores();
        this.gestorUsuarios = new GestorUsuarios();
    }


    public void iniciarBalanceador() {


        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PUERTO_BALANCEADOR_SERVIDORES)) {
                System.out.println("Balanceador escuchando servidores en el puerto " + PUERTO_BALANCEADOR_SERVIDORES);
                while (true) {
                    Socket socket = serverSocket.accept();
                    manejarConexionServidor(socket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();




        new Thread(() -> {
            try (ServerSocket clientSocket = new ServerSocket(PUERTO_MONITOREO_BALANCEADOR)) {
                System.out.println("Balanceador escuchando monitoreo de balanceador en el puerto  " + PUERTO_MONITOREO_BALANCEADOR);
                while (true) {
                    Socket socket = clientSocket.accept();
                    /*manejarConexionCliente(socket);*/
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

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


    /*Cambio de conexiones a cliente*/

    public void iniciarEscuchaCliente (){
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PUERTO_BALANCEADOR_CLIENTES)) {
                System.out.println("[Balanceador] Escuchando mensajes del cliente en el puerto " + PUERTO_BALANCEADOR_CLIENTES);

                while (true) {
                    System.out.println("Entro al while") ;
                    Socket socket = serverSocket.accept();
                    System.out.println("paso el server socket acept");
                    manejarMensajeCliente(socket);

                }
            } catch (IOException e) {
                System.out.println("[Balanceador] Error en la escucha del cliente: " + e.getMessage());
            }
        }).start();
    }

    private void manejarMensajeCliente(Socket socketCliente) {
        System.out.println("Entro a manejar mensajes con el mensaje "+ socketCliente );
        try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true)
        ) {
            String mensaje = entrada.readLine();
            System.out.println("[Balanceador] Mensaje recibido del cliente: " + mensaje);

            Servidor servidorDisponible = (Servidor) gestorServidores.obtenerServidoresActivos();
            if (servidorDisponible == null) {
                salida.println("ERROR: No hay servidores disponibles.");
                return;
            }

            try (Socket socketServidor = new Socket("localhost", servidorDisponible.getPuertoBalanceador());
                 PrintWriter salidaServidor = new PrintWriter(socketServidor.getOutputStream(), true);
                 BufferedReader entradaServidor = new BufferedReader(new InputStreamReader(socketServidor.getInputStream()))) {

                salidaServidor.println(mensaje);
                String respuestaServidor = entradaServidor.readLine();
                salida.println(respuestaServidor);

                System.out.println("[Balanceador] Respuesta reenviada al cliente: " + respuestaServidor);

            } catch (IOException e) {
                salida.println("ERROR: No se pudo conectar con el servidor.");
                System.out.println("[Balanceador] Error al comunicarse con el servidor: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("[Balanceador] Error en la conexión con el cliente: " + e.getMessage());
        }
    }


}
