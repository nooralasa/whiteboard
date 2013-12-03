package canvas.server;

import java.io.BufferedReader;
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

public class WhiteboardServerTripleThreadNonWorking {
    private final ServerSocket serverSocket;
    private AtomicInteger numOfClients = new AtomicInteger(0);
    private AtomicInteger threadID = new AtomicInteger(-1);
    private final HashMap<String, String> clientToWhiteboardMap;
    private final HashMap<String, ArrayList<String>> whiteboardToCommandsMap;
    private final HashMap<String, Integer> clientToThreadNumMap;
    private final BlockingQueue<String> inputCommandsQueue;
    private final ArrayList<BlockingQueue<String>> outputCommandQueues;

    /**
     * Creates a Whiteboard Server.
     * @param port
     * @throws IOException
     */
    public WhiteboardServerTripleThreadNonWorking(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientToWhiteboardMap = new HashMap<String, String>(); // maps each client to the whiteboard it is working on
        whiteboardToCommandsMap = new HashMap<String, ArrayList<String>>(); // maps each whiteboard to its commands
        outputCommandQueues = new ArrayList<BlockingQueue<String>>();
        inputCommandsQueue = new ArrayBlockingQueue<String>(10000000);
        clientToThreadNumMap = new HashMap<String, Integer>();
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
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            numOfClients.getAndIncrement();
            threadID.getAndIncrement();
            BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(10000000);
            outputCommandQueues.add(blockingQueue);
            final Integer threadNum = threadID.get();
            createThreads(socket, threadNum);
        }
    }

    private synchronized void createThreads(final Socket socket, final Integer threadNum){
        final String welcome = "Welcome to this Whiteboard Server. ";
        final String hello = " people are collaborating including you. Type 'help' for help.";
        // start a new thread to handle the connection
        Thread inputThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Starting Input Thread with Thread " + threadNum.toString());
                PrintWriter out;
                String userlist = "";
                for (String users : clientToWhiteboardMap.keySet()){
                    userlist += users + " ";
                }
                try {
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(welcome + numOfClients + hello);
                    if (numOfClients.get() != 1){
                        out.println("Users currently on the server are");
                        out.println(userlist);
                    } else {
                        out.println("You are the only user");
                    }
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

        // start a new thread to handle outputs
        Thread inputCommandsQueueThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Starting Input Commands Queue Thread with Thread " + threadNum.toString());
                try {
                    handleInputs(socket, threadNum);
                } catch (IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        // start a new thread to handle outputs
        Thread outputThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Starting Output Thread with Thread " + threadNum.toString());
                try {
                    handleOutputs(socket, threadNum);
                } catch (IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        inputThread.start();   
        inputCommandsQueueThread.start();
        outputThread.start();    
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
    private void handleConnection(final Socket socket, final Integer threadNum) throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                inputCommandsQueue.offer(line);
            }
        } finally {
            in.close();
            socket.close();
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
    private void handleInputs(final Socket socket, final Integer threadNum) throws IOException, InterruptedException {
        try {
            while (true){ // constantly poll the commands queue
                while (inputCommandsQueue.peek() != null){
                    String input = (String) inputCommandsQueue.take();
                    //                    System.out.println(output); // so server can see what is being output DELETE later
                    handleRequest(input, threadNum);
//                    if (output.equals("Thank you!")) { // this is if thank you is the disconnect message
//                        numOfClients.getAndDecrement();
//                        socket.close();
//                        break;
//                    }
                }
            }
        } finally {
            System.out.println("Socket for Thread " + threadNum.toString() + " closed");
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client
     */
    private void handleRequest(final String input, final Integer threadNum) {
        String regex = "([^=]* selectBoard [^=]*)|([^=]* draw -?\\d+ -?\\d+ -?\\d+ -?\\d+)|([^=]* erase -?\\d+ -?\\d+ -?\\d+ -?\\d+)|"
                + "(help)|(bye)|(new username [^=]*)|(addBoard [^=]*)";
        if (!input.matches(regex)) { // Invalid Input
            System.err.println("Invalid Input");
            System.out.println("Writing to thread " + threadNum);
            outputCommandQueues.get(threadNum).offer("Invalid Input");
            return ;
        }
        String[] tokens = input.split(" ");
        // Adding username to the list of usernames
        if ((tokens[0].equals("new")) && (tokens[1].equals("username"))){
            if (!clientToWhiteboardMap.containsKey(tokens[2])){
                clientToWhiteboardMap.put(tokens[2],"");
                clientToThreadNumMap.put(tokens[2], threadNum);
                System.out.println("Client " + tokens[2] + "assigned thread " + threadNum.toString());
                outputCommandQueues.get(threadNum).offer("Please choose a whiteboard to work on.");
                // TODO: need method here that will send the strings to the user
            } else{ // case in which the username is already in the map
                outputCommandQueues.get(threadNum).offer("Username already taken. Please select a new username.");
            }
        } else if (tokens[0].equals("addBoard")){
            if (whiteboardToCommandsMap.containsKey(tokens[1])){
                outputCommandQueues.get(threadNum).offer("Whiteboard already exists.");
            } else{
                ArrayList<String> commandList = new ArrayList<String>();
                whiteboardToCommandsMap.put(tokens[1], commandList);
                outputCommandQueues.get(threadNum).offer("Board " + tokens[1] + "added");
            }
        } else if (tokens[0].equals("help")) {
            // 'help' request
            outputCommandQueues.get(threadNum).offer("Help");
        } else if (tokens[0].equals("bye")) {
            //terminate connection
            System.err.println("Connection terminated");
            outputCommandQueues.get(threadNum).offer("Thank you!");
        } else if (tokens.length > 1) {
            if ((tokens[1].equals("draw")) || (tokens[1].equals("erase"))){
                if (!whiteboardToCommandsMap.containsKey(tokens[0])){
                    outputCommandQueues.get(threadNum).offer("Whiteboard doesn't exist.");
                } else{ //TODO: eventually should have something checking if the parameters are out of the boundaries
                    // put the input in the whiteboard commandlist
                    ArrayList<String> commands = whiteboardToCommandsMap.get(tokens[0]);
                    commands.add(input);
                    whiteboardToCommandsMap.put(tokens[0], commands);
                    // putting commands into all relevant queues
                    for (String keys : clientToWhiteboardMap.keySet()){
                        if (clientToWhiteboardMap.get(keys).equals(tokens[0])){
                            outputCommandQueues.get(clientToThreadNumMap.get(keys)).offer(input);
                        }
                    }
                }
            } else if (tokens[1].equals("selectBoard")) { // Selecting board
                if (!clientToWhiteboardMap.containsKey(tokens[0])){
                    outputCommandQueues.get(threadNum).offer("Whiteboard doesn't exist.");
                    outputCommandQueues.get(threadNum).offer("Username does not exist.");
                } else if (!whiteboardToCommandsMap.containsKey(tokens[2])){
                    outputCommandQueues.get(threadNum).offer("Whiteboard does not exist. Select a different board or make a board.");
                } else{
                    clientToWhiteboardMap.put(tokens[0], tokens[2]);
                    outputCommandQueues.get(threadNum).offer("You are currently on board "+ tokens[2]);
                }
            }
        } else { // Invalid Input
            System.err.println("Invalid Input");
            outputCommandQueues.get(threadNum).offer("Invalid input.");
        }
        // Should never get here--make sure to return in each of the valid cases above.
        //        throw new UnsupportedOperationException();
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
    private void handleOutputs(final Socket socket, final Integer threadNum) throws IOException, InterruptedException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try {
            while (true){ // constantly poll the commands queue
                BlockingQueue<String> commandsQueue = outputCommandQueues.get(threadNum);
                while (commandsQueue.peek() != null){
                    System.out.println(threadNum.toString());
                    String output = (String) commandsQueue.take();
                    //                    System.out.println(output); // so server can see what is being output DELETE later
                    out.println(output);
                    if (output.equals("Thank you!")) { // this is if thank you is the disconnect message
                        numOfClients.getAndDecrement();
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
     * Start a WhiteboardServer running on the specified port.
     * 
     * @param port
     *            The network port on which the server should listen.
     */
    public static void runWhiteboardServer(final int port) throws IOException {
        WhiteboardServer server = new WhiteboardServer(port);
        server.serve();
    }
}