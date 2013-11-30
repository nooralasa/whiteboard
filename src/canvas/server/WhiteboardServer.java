package canvas.server;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import canvas.ButtonPanel;
import canvas.Canvas;
import canvas.whiteboardclient.*;

public class WhiteboardServer {
    private final ServerSocket serverSocket;
    private WhiteboardClient canvas;
    private AtomicInteger numOfClients = new AtomicInteger(0);
    private HashMap<String,String> clientWhiteboards = new HashMap<String,String>();
    private HashMap<String,ArrayList<String>> clientCommands = new HashMap<String,ArrayList<String>>();
    private BlockingQueue<String> commandsQueue = new ArrayBlockingQueue<String>(100000); //queue wouldn't take MAX_VALUE as argument
    //TODO: should be storing a hashmap of the canvas name to the list of strings not the actual canvas
    
    public WhiteboardServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        canvas = new WhiteboardClient(800,600,1);        
        canvas.makeCanvas(800, 600, canvas);
    }

    /**
     * Run the server, listening for client connections and handling them. Never
     * returns unless an exception is thrown.
     * 
     * @throws IOException
     *             if the main server socket is broken (IOExceptions from
     *             individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        final String welcome = "Welcome to this Whiteboard. ";
        final String hello = " people are collaborating including you. Type 'help' for help.";
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            numOfClients.getAndIncrement();

            // start a new thread to handle the connection
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    PrintWriter out;
                    try {
                        out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(welcome + numOfClients + hello + "\n");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    // the client socket object is now owned by this thread,
                    // and mustn't be touched again in the main thread
                    try {
                        handleConnection(socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket
     *            socket where the client is connected
     * @throws IOException
     *             if connection has an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        try {
            for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                String output = handleRequest(line);
                if (output != null) {
                    out.println(output);
                    if (output.equals("Thank you!")) {
                        numOfClients.getAndDecrement();
                        break;
                    }
                }
            }
        } finally {
            out.close();
            in.close();
            socket.close();
        }
    }


    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client
     */
    private String handleRequest(String input) {
        String regex = "(selectBoard -?\\d+)|(-?\\d+ draw -?\\d+ -?\\d+ -?\\d+ -?\\d+)|(-?\\d+ erase -?\\d+ -?\\d+ -?\\d+ -?\\d+)|(username -?\\d+)"
                + "(help)|(bye)";
        if ( ! input.matches(regex)) {
            // invalid input
            System.err.println("Invalid Input");
            return null;
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("selectBoard")) {
            if (!tokens[1].equals("")) {
                int whiteBoardNumber = Integer.parseInt(tokens[1]);                
            }
            

        } else if (tokens[0].equals("username")) {
            
        } else if (tokens[0].equals("help")) {
            // 'help' request
            return canvas.helpMessage();
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            //terminate connection
            return "Thank you!";
        } else {
            int x1 = Integer.parseInt(tokens[2]);
            int y1 = Integer.parseInt(tokens[3]);
            int x2 = Integer.parseInt(tokens[4]);
            int y2 = Integer.parseInt(tokens[5]);
            if (tokens[1].equals("draw")) {
                // 'draw x1 y1 x2 y2' request
                System.out.println("Draw");
                return canvas.drawLineSegment(x1,y1,x2,y2);
            } else if (tokens[1].equals("erase")) {
                // 'draw x1 y1 x2 y2' request
                return canvas.eraseLineSegment(x1,y1,x2,y2);
            }
        }
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }
    
    /**
     * Start a WhiteboardServer using the given arguments.
     * 
     * Usage: WhiteboardServer [--port PORT]
     * 
     * PORT is an optional integer in the range 0 to 65535 inclusive, specifying
     * the port the server should be listening on for incoming connections. E.g.
     * "WhiteboardServer --port 1234" starts the server listening on port 1234.
     * @throws IOException 
     * 
     */
    public static void main(String[] args) throws IOException {
        // Command-line argument parsing is provided. Do not change this method.
        int port = 4444; // default port


        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while (!arguments.isEmpty()) {
                String flag = arguments.remove();
                try {                
                    if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > 65535) {
                            throw new IllegalArgumentException("port " + port
                                    + " out of range");
                        }
                    
                    } else {
                        throw new IllegalArgumentException("unknown option: \""
                                + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for "
                            + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(
                            "unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err
                    .println("usage: WhiteBoardServer [--port PORT]");
            return;
        }

        try {
            runWhiteboardServer(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Start a WhiteboardServer running on the specified po rt.
     * 
     * @param port
     *            The network port on which the server should listen.
     */
    public static void runWhiteboardServer(final int port) throws IOException {
        WhiteboardServer server = new WhiteboardServer(port);
        server.serve();
    }
}

