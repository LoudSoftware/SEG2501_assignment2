// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.net.InetAddress;
import java.util.Random;

import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer implements Runnable {
    //Class variables *************************************************

    /**
     * The default port to listen on.
     */
    final public static int DEFAULT_PORT = 5555;

    //Constructors ****************************************************

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {
        super(port);
    }


    //Instance methods ************************************************

    /**
     * This method handles any messages received from the client.
     *
     * @param msg    The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient
    (Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);
        this.sendToAllClients(msg);
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server starts listening for connections.
     */
    protected void serverStarted() {
        System.out.println
                ("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass.  Called
     * when the server stops listening for connections.
     */
    protected void serverStopped() {
        System.out.println
                ("Server has stopped listening for connections.");

    }

    /**
     * **** Changed for E49 ****
     * Will get run every time a Client connects
     * Runs a new watchdog thread for each client that connects
     *
     * @param client the connection connected to the client.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        // Printing out the client InetAddress to console
        System.out.println("Connected: Client " + client.getInetAddress());

        // Creating a new Thread to watchdog the connection to the client
        Thread connectionWatchdog = new Thread(client, "Client" +
                client.getInetAddress() + "_" + new Random().nextInt(10000)) {
            // Naming the thread for debugging purposes.

            // Saving the address as if the client is dead, it will be null
            InetAddress savedClient = client.getInetAddress();

            @Override
            public void run() {
                System.out.println("Starting watchdog for" + savedClient);
                while (true) {
                    if (!client.isAlive()) { // Tests if client Thread is still alive.
                        clientDisconnected(savedClient); // Call function with the saved address to be able to print it.
                        break; // Break from the loop, essentially killing the Thread
                    }
                    try { // Sleep for 1 second
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        connectionWatchdog.start(); // Start the thread
    }

    // Prints out a message on server console with client InetAddress that just disconnected.
    protected synchronized void clientDisconnected(InetAddress client) {
        System.out.println("Disconnected: Client " + client);
    }

    // Overriding this function just in case we make a something that triggers it.
    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        System.out.println("Client disconnected");
    }

    //Class methods ***************************************************

    /**
     * This method is responsible for the creation of
     * the server instance (there is no UI in this phase).
     *
     * @param args [0] The port number to listen on.  Defaults to 5555
     *             if no argument is entered.
     */
    public static void main(String[] args) {
        int port = 0; //Port to listen on

        try {
            port = Integer.parseInt(args[0]); //Get port from command line
        } catch (Throwable t) {
            port = DEFAULT_PORT; //Set port to 5555
        }

        EchoServer sv = new EchoServer(port);

        try {
            sv.listen(); //Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }
}
//End of EchoServer class
