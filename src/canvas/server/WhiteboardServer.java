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
    private AtomicInteger threadID = new AtomicInteger(-1);
    private final HashMap<String, String> clientToWhiteboardMap;
    private final HashMap<String, ArrayList<String>> whiteboardToCommandsMap;
    private final HashMap<String, Integer> clientToThreadNumMap;
    private final ArrayList<BlockingQueue<String>> commandQueues;
    private final HashMap<Integer, String> threadIDclient;
    private final ArrayList<String> userNames;
    private final ArrayList<String> whiteboardNames;
    private final ArrayList<Socket> socketList;

    /**
     * Creates a Whiteboard Server.
     * @param port
     * @throws IOException
     */
    public WhiteboardServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientToWhiteboardMap = new HashMap<String,String>(); // maps each client to the whiteboard it is working on
        whiteboardToCommandsMap = new HashMap<String,ArrayList<String>>(); // maps each whiteboard to its commands
        userNames = new ArrayList<String>(); // may not need
        whiteboardNames = new ArrayList<String>(); // may not need
        socketList = new ArrayList<Socket>();
        commandQueues = new ArrayList<BlockingQueue<String>>();
        clientToThreadNumMap = new HashMap<String, Integer>();
        threadIDclient = new HashMap<Integer, String>();
    }
    // have a hashmap of each client to its command blocking queue
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
            //            socketList.add(socket); // Adds each socket into the socketList
            numOfClients.getAndIncrement();
            threadID.getAndIncrement();
            BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(100000);
            commandQueues.add(blockingQueue);

            final Integer threadNum = threadID.get();
            // start a new thread to handle the connection
            Thread inputThread = new Thread(new Runnable() {
                public void run() {
                    System.out.println("New Thread Created");
                    PrintWriter out;
                    try {
                        out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(welcome + numOfClients + hello);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    // the client socket object is now owned by this thread,
                    // and mustn't be touched again in the main thread
                    try {
                        handleConnection(socket, threadNum);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            inputThread.start();   

            // start a new thread to handle outputs
            Thread outputThread = new Thread(new Runnable() {
                public void run() {
                    System.out.println("Starting Output Thread");
                    try {
                        handleOutputs(socket, threadNum);
                    } catch (IOException | InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            outputThread.start();    
        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket
     *            socket where the client is connected
     * @throws IOException
     *             if connection has an error or terminates unexpectedly
     * @throws InterruptedException 
     */
    private void handleOutputs(Socket socket, Integer threadNum) throws IOException, InterruptedException {
        //        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try {
            //            for (String line = in.readLine(); line != null; line = in.readLine()) {
            //                handleRequest(line, threadNum);
            while (true){ // constantly poll the commands queue
                BlockingQueue<String> commandsQueue = commandQueues.get(threadNum);
                while (commandsQueue.peek() != null){
                    String output = (String) commandsQueue.take();
                    System.out.println(output); // so server can see what is being output DELETE later
                    out.println(output);
                    if (output.equals("Thank you!")) { // this is if thank you is the disconnect message
                        numOfClients.getAndDecrement();
                        // unassociated 
                        out.close();
                        socket.close();
                        break;
                    }
                }
            }
        } finally {
            System.out.println("Socket for Thread " + threadNum.toString() + " closed");
        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket
     *            socket where the client is connected
     * @throws IOException
     *             if connection has an error or terminates unexpectedly
     * @throws InterruptedException 
     */
    private void handleConnection(Socket socket, Integer threadNum) throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                handleRequest(line, threadNum);
                //                BlockingQueue<String> commandsQueue = commandQueues.get(threadNum);
                //                System.out.println("Looking in" + threadNum.toString());
                //                while (commandsQueue.peek() != null){
                //                    String output = (String) commandsQueue.take();
                //                    System.out.println(output);
                //                    out.println(output);
                //                    String[] tokens = output.split(" ");
                //                    if (output.equals("Thank you!")) { // this is if thank you is the disconnect message
                //                        numOfClients.getAndDecrement();
                //                        // unassociated 
                //                        break;
                //                    }
                //                    if (tokens.length > 1){
                //                        if ((tokens[1].equals("draw")) || (tokens[1].equals("erase"))) {
                //                            // get the board name and for all clients associated witht he board name, use the socket list to 
                //                            System.out.println("Should put commands into all queues!");
                //                        }
                //                    }
                //                }
            }
        } finally {
            //            out.close();
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
    private void handleRequest(String input, Integer threadNum) {
        String regex = "([^=]* selectBoard [^=]*)|([^=]* draw -?\\d+ -?\\d+ -?\\d+ -?\\d+)|([^=]* erase -?\\d+ -?\\d+ -?\\d+ -?\\d+)|"
                + "(help)|(bye)|(new username [^=]*)|(addBoard [^=]*)";
        if (!input.matches(regex)) {
            // invalid input
            System.err.println("Invalid Input");
            //            commandQueues.get(threadNum).add("Invalid Input");
            //            return null; // disconnects user
        }
        String[] tokens = input.split(" ");
        // Adding username to the list of usernames
        if ((tokens[0].equals("new")) && (tokens[1].equals("username"))){
            if (!clientToWhiteboardMap.containsKey(tokens[2])){
                clientToWhiteboardMap.put(tokens[2],"");
                clientToThreadNumMap.put(tokens[2], threadNum);
                System.out.println("Client " + tokens[2] + "assigned thread" + threadNum.toString());
                threadIDclient.put(threadNum, tokens[2]);
                commandQueues.get(threadNum).add("Please choose a whiteboard to work on.");
                //                return "Please choose a whiteboard to work on."; // TODO: need method here that will send the strings to the user
            } else{ // case in which the username is already in the map
                commandQueues.get(threadNum).add("Username already taken. Please select a new username.");
                //                return "Username already taken. Please select a new username.";
            }
        } else if (tokens[0].equals("addBoard")){
            if (whiteboardToCommandsMap.containsKey(tokens[1])){
                commandQueues.get(threadNum).add("Whiteboard already exists.");

                //                return "Whiteboard already exists.";
            } else{
                ArrayList<String> commandList = new ArrayList<String>();
                whiteboardToCommandsMap.put(tokens[1], commandList);
                commandQueues.get(threadNum).add("Board " + tokens[1] + "added");

                //                return "Board " + tokens[1] + "added";
            }
        } else if (tokens[0].equals("help")) {
            // 'help' request
            commandQueues.get(threadNum).add("Help");

            //            return "Instructions: username yourUsername, selectBoard board#, help, bye, board# draw x1 y1 c2 y2, board# erase x1 y1 x2 y2";
        } else if (tokens[0].equals("bye")) {
            //terminate connection
            System.err.println("Connection terminated");
            commandQueues.get(threadNum).add("Thank you!");

            //            return "Thank you!";
        } else if (tokens.length > 1) {
            if ((tokens[1].equals("draw")) || (tokens[1].equals("erase"))){
                if (!whiteboardToCommandsMap.containsKey(tokens[0])){
                    commandQueues.get(threadNum).add("Whiteboard doesn't exist.");
                    //                return "Whiteboard doesn't exist.";
                } else{ //TODO: eventually should have something checking if the parameters are out of the boundaries
                    // put the input in the whiteboard commandlist
                    ArrayList<String> commands = whiteboardToCommandsMap.get(tokens[0]);
                    commands.add(input);
                    whiteboardToCommandsMap.put(tokens[0], commands);
                    System.out.println("Got here");
                    // putting commands into all relevant queues
                    for (String keys : clientToWhiteboardMap.keySet()){
                        System.out.println(keys);
                        System.out.println(clientToWhiteboardMap.get(keys));
                        System.out.println(tokens[0]);
                        if (clientToWhiteboardMap.get(keys).equals(tokens[0])){
                            System.out.println("Here");
                            commandQueues.get(clientToThreadNumMap.get(keys)).add(input);
                            System.out.println(keys);
                            System.out.println(clientToThreadNumMap.get(keys).toString());
                            // put a print statement here saying what is placed where to debug
                        }
                    }
                    commandQueues.get(threadNum).add(input);
                    //                return input; // probably shouldn't be returning this, b/c this should be sent by the commands queue
                }
            } else if (tokens[1].equals("selectBoard")) { // Selecting board
                if (!clientToWhiteboardMap.containsKey(tokens[0])){
                    commandQueues.get(threadNum).add("Whiteboard doesn't exist.");
                    commandQueues.get(threadNum).add("Username does not exist.");

                    //                return "Username does not exist.";
                } else if (!whiteboardToCommandsMap.containsKey(tokens[2])){
                    commandQueues.get(threadNum).add("Whiteboard does not exist. Select a different board or make a board.");

                    //                return "Whiteboard does not exist. Select a different board or make a board.";
                } else{
                    clientToWhiteboardMap.put(tokens[0], tokens[2]);
                    commandQueues.get(threadNum).add("You are currently on board "+ tokens[2]);
                    //                return "You are currently on board "+ tokens[2];
                }
            }
        } else { // Invalid Input
            //            System.err.println("Invalid Input");
            //            commandQueues.get(threadNum).add("Invalid input.");

            //            return "Invalid input.";
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