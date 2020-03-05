package Chat;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.net.Socket;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Rafa Aguiar
 */
public class ServidorChat extends Thread {

    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;
    private DataInputStream entradaCli = null;
    private DataOutputStream salidaCli = null;
    private static ArrayList<Socket> lClientes = new ArrayList<Socket>();

    public ServidorChat() {

    }

    public ServidorChat(Socket socket, ArrayList<Socket> lClientes) {
        clientSocket = socket;
        this.lClientes = lClientes;
        lClientes.add(clientSocket);
    }

    public void run() {

        try {
            //Marcamos que el cliente está conectado
            boolean conectado = true;
            System.out.println("Arrancando hilo");

            //Abrimos la entrada para recibir los mensajes del cliente
            entradaCli = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            salidaCli = new DataOutputStream(clientSocket.getOutputStream());
            //Recogemos el nombre de usuario y indicamos que se ha conectado
            String usuario = entradaCli.readUTF();
            System.out.println(usuario + " se ha conectado.");

            String conecta = entradaCli.readUTF();
            broadcastStatus(conecta);

            while (conectado) {
                //Mientras esté el cliente el servidor recibirá el texto y lo devolverá a todos los usuarios
                String mensaje = entradaCli.readUTF();
                broadcast(mensaje, usuario);
                if (!mensaje.equals("/bye")) {
                    //Si el texto no es /bye se muestra el mensaje junto con el nombre de usuario
                    System.out.println(usuario + ":" + mensaje);
                } else {
                    //si es /bye el boolean se vuelve false y desconectamos
                    conectado = false;
                }
            }

            //Broadcast de desconexion
            conecta = entradaCli.readUTF();
            broadcastStatus(conecta);
            //Indicamos que el usuario se desconecta y cerramos la entrada.
            System.out.println(usuario + " se ha desconectado");
            entradaCli.close();

        } catch (IOException ex) {
            Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        try {
            //realizamos la iniciación del serviodr
            System.out.println("Creando socket servidor");

            ServerSocket serverSocket = new ServerSocket();

            System.out.println("Realizando el bind");

            //pedimos por teclado el puerto a establecer
            int port = selecPuerto();

            InetSocketAddress addr = new InetSocketAddress("localhost", port);
            serverSocket.bind(addr);
            System.out.println("Inicializando servicio en: localhost");
            System.out.println("Inicializando servicio en: " + port);

            System.out.println("Aceptando conexiones");

            while (serverSocket != null) {

                //empezamos la conexión con el cliente y empezamos un hilo
                Socket newSocket = serverSocket.accept();
                System.out.println("Conexión recibida");
                System.out.println("cliente: " + newSocket);

                ServidorChat hilo = new ServidorChat(newSocket, lClientes);
                hilo.start();
            }
            System.out.println("Conexion recibida");
        } catch (IOException ex) {
            Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Método para escribir el puerto sin que sea posible escribir letras y con una longitud mayor a 2
    public static int selecPuerto() {
        String puerto = JOptionPane.showInputDialog("Indique puerto donde alojar su servidor");
        int port = 0;
        if (puerto.length() >= 2 && puerto.matches("[0-9]+")) {
            port = Integer.parseInt(puerto);
        } else {
            port = selecPuerto();
        }
        return port;

    }

    //Método para reenviar el mensaje recibido de un cliente a todos.
    //Iteramos el ArrayList de Sockets para enviar a cada uno de los clientes el mensaje (Socket)
    public void broadcast(String mensaje, String usuario) {

        try {
            for (Socket cliente : lClientes) {
                String mensaje2 = usuario + ":" + mensaje;
                salidaCli = new DataOutputStream(cliente.getOutputStream());
                salidaCli.writeUTF(mensaje2);
            }
            salidaCli.flush();

        } catch (IOException ex) {
            Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Método para reenviar la conexión/desconexión de los clientes.
    public void broadcastStatus(String mensaje) {

        try {
            for (Socket cliente : lClientes) {
                salidaCli = new DataOutputStream(cliente.getOutputStream());
                salidaCli.writeUTF(mensaje);
            }
            salidaCli.flush();

        } catch (IOException ex) {
            Logger.getLogger(ServidorChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
