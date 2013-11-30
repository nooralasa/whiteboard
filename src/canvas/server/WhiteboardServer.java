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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import canvas.ButtonPanel;
import canvas.Canvas;
import canvas.whiteboardclient.*;

public class WhiteboardServer {
    private final ServerSocket serverSocket;
    private AtomicInteger numOfClients = new AtomicInteger(0);
    private final HashMap<String, String> clientToWhiteboardsMap;
    private final HashMap<String, ArrayList<String>> whiteboardToCommandsMap;
    private final BlockingQueue<String> commandsQueue;
    private final ArrayList<String> userNames;
    private final ArrayList<String> whiteboardNames;

    /**
     * Creates a Whiteboard Server.
     * @param port
     * @throws IOException
     */
    public WhiteboardServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientToWhiteboardsMap = new HashMap<String,String>(); // maps each clients to the whiteboard it is working on
        whiteboardToCommandsMap = new HashMap<String,ArrayList<String>>(); // maps each whiteboard to its commands
        commandsQueue = new ArrayBlockingQueue<String>(100000); // queue wouldn't take MAX_VALUE as argument
        userNames = new ArrayList<String>(); // may not need
        whiteboardNames = new ArrayList<String>(); // may not need
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
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                if (output != null) {
                    out.println(output);
                    if (output.equals("Thank you!")) { // this is if thank you is the disconnect message
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
        String regex = "([^=]* selectBoard [^=]*)|([^=]* draw -?\\d+ -?\\d+ -?\\d+ -?\\d+)|([^=]* erase -?\\d+ -?\\d+ -?\\d+ -?\\d+)|"
                + "(help)|(bye)|(new username [^=]*)|(addBoard [^=]*)";
        if (!input.matches(regex)) {
            // invalid input
            System.err.println("Invalid Input");
            return null; // disconnects user
        }
        String[] tokens = input.split(" ");
        // Adding username to the list of usernames
        if ((tokens[0].equals("new")) && (tokens[1].equals("username"))){
            if (!clientToWhiteboardsMap.containsKey(tokens[2])){
                clientToWhiteboardsMap.put(tokens[2],"");
                return "Please choose a whiteboard to work on."; // TODO: need method here that will send the strings to the user
            } else{ // case in which the username is already in the map
                return "Username already taken. Please select a new username.";
            }
        } else if (tokens[0].equals("addBoard")){
            if (whiteboardToCommandsMap.containsKey(tokens[1])){
                return "Whiteboard already exists.";
            } else{
                ArrayList<String> commandList = new ArrayList<String>();
                whiteboardToCommandsMap.put(tokens[1], commandList);
                return "Board " + tokens[1] + "added";
            }
        } else if (tokens[0].equals("help")) {
            // 'help' request
            return "Instructions: username yourUsername, selectBoard board#, help, bye, board# draw x1 y1 c2 y2, board# erase x1 y1 x2 y2";
        } else if (tokens[0].equals("bye")) {
            //terminate connection
            System.err.println("Connection terminated");
            return "Thank you!";
        } else if ((tokens[1].equals("draw")) || (tokens[1].equals("erase"))){
            if (!whiteboardToCommandsMap.containsKey(tokens[0])){
                return "Whiteboard doesn't exist.";
            } else{ //TODO: eventually should have something checking if the parameters are out of the boundaries
                ArrayList<String> commands = whiteboardToCommandsMap.get(tokens[0]);
                commands.add(input);
                whiteboardToCommandsMap.put(tokens[0], commands);
                return input;
            }
        } else if (tokens[1].equals("selectBoard")) { // Selecting board
            if (!clientToWhiteboardsMap.containsKey(tokens[0])){
                return "Username does not exist.";
            } else if (!whiteboardToCommandsMap.containsKey(tokens[2])){
                return "Whiteboard does not exist. Select a different board or make a board.";
            } else{
                clientToWhiteboardsMap.put(tokens[0], tokens[2]);
                return "You are currently on board "+ tokens[2];
            }
        } else { // draw or erase condition
            System.err.println("Invalid Input");
            return "Invalid input.";
        }
        // Should never get here--make sure to return in each of the valid cases above.
        //        throw new UnsupportedOperationException();
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