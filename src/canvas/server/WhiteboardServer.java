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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

public class WhiteboardServer {
    private final ServerSocket serverSocket;
    private final AtomicInteger numOfClients = new AtomicInteger(0);
    private final AtomicInteger threadID = new AtomicInteger(-1);
    private final Map<String, String> clientToWhiteboardMap;
    private final Map<String, ArrayList<String>> whiteboardToCommandsMap;
    private final Map<String, Integer> clientToThreadNumMap;
    private final List<BlockingQueue<String>> commandQueues;

    /**
     * Creates a Whiteboard Server.
     * @param port
     * @throws IOException
     */
    public WhiteboardServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientToWhiteboardMap = Collections.synchronizedMap(new HashMap<String, String>()); // maps each client to the whiteboard it is working on
        whiteboardToCommandsMap = Collections.synchronizedMap(new HashMap<String, ArrayList<String>>()); // maps each whiteboard to its commands
        commandQueues = Collections.synchronizedList(new ArrayList<BlockingQueue<String>>());
        clientToThreadNumMap = Collections.synchronizedMap(new HashMap<String, Integer>());
        createBoards();
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
            BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(100000);
            commandQueues.add(blockingQueue);
            final Integer threadNum = threadID.get();
            createThreads(socket, threadNum);
        }
    }

    /**
     * Creates 3 Boards for the Server to start with
     */
    private void createBoards(){
        ArrayList<String> emptyCommandAndClientList = new ArrayList<String>();
        whiteboardToCommandsMap.put("Board1", emptyCommandAndClientList);
        whiteboardToCommandsMap.put("Board2", emptyCommandAndClientList);
        whiteboardToCommandsMap.put("Board3", emptyCommandAndClientList);

    }
    
    

    /**
     * Sends Existing Whiteboards to All Clients
     */
    private void getExistingWhiteboardsAll(){
        for (String whiteboard : whiteboardToCommandsMap.keySet()){
            String whiteboards = "Existing Whiteboards " + whiteboard;
            for (BlockingQueue<String> commandQueue : commandQueues){
                commandQueue.offer(whiteboards);
            }
        }
        String message = "Done sending whiteboard names";
        for (BlockingQueue<String> commandQueue : commandQueues){
            commandQueue.offer(message);
        }
    }

    /**
     * Sends Existing Whiteboards to One Clients
     */
    private void getExistingWhiteboardsOne(final int threadNum){
        for (String whiteboard : whiteboardToCommandsMap.keySet()){
            String whiteboards = "Existing Whiteboards " + whiteboard;
            commandQueues.get(threadNum).add(whiteboards);
        }
        String message = "Done sending whiteboard names";
        commandQueues.get(threadNum).add(message);
    }
    
    /**
     * Sends out to all clients that are working on the same whiteboard the names of the other clients working on the whiteboard
     * @param threadNum
     * @param whiteboardName
     */
    private void getSameUsersWhiteboard(final int threadNum, String whiteboardName){
        ArrayList<String> sameClients = new ArrayList<String>(); // clients working on the same whiteboard
        ArrayList<String> sameClientsCommands = new ArrayList<String>();
        // Gets all the clients working on the same Whiteboard and stores them in a list
        for (String clients : clientToWhiteboardMap.keySet()){
            if (clientToWhiteboardMap.get(clients).equals(whiteboardName)){
                sameClients.add(clients);
                String clientCommand = "sameClient " + clients;
                sameClientsCommands.add(clientCommand);
            }
        }

        // Send each client that shares the whiteboard all of the sameClients
        for (String clients : sameClients){
            for (String clientCommands : sameClientsCommands){
                commandQueues.get(clientToThreadNumMap.get(clients)).add(clientCommands);
            }
            String doneSending = "Done sending client names";
            commandQueues.get(clientToThreadNumMap.get(clients)).add(doneSending);
        }
    }

    /**
     * Creates and starts threads to handle inputs and outputs between the server and specific client.
     * @param socket socket that the client is connected to
     * @param threadNum the threadNum of the client (corresponding to the position of the commandQueue in the list)
     */
    private void createThreads(final Socket socket, final Integer threadNum){
        final String welcome = "Welcome to this Whiteboard Server. ";
        final String hello = " people are collaborating including you. Type 'help' for help.";
        // start a new thread to handle the connection
        Thread inputThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Starting Input Thread with Thread " + threadNum);
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
                    handleClientInput(socket, threadNum);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        // start a new thread to handle outputs
        Thread outputThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Starting Output Thread with Thread " + threadNum);
                try {
                    handleOutputs(socket, threadNum);
                } catch (IOException | InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        inputThread.start();   
        outputThread.start();    
    }

    /**
     * Polls the output commandQueues of each client and writes items as text messages to the client's socket. 
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
                BlockingQueue<String> commandsQueue = commandQueues.get(threadNum);
                while (commandsQueue.peek() != null){
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
     * Listens to the client socket for messages and passes inputs to the handleRequest().
     * 
     * @param socket
     *            socket where the client is connected
     * @throws IOException
     *             if connection has an error or terminates unexpectedly
     * @throws InterruptedException 
     */
    private void handleClientInput(final Socket socket, final Integer threadNum) throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                handleRequest(line, threadNum);
            }
        } finally {
            in.close();
            socket.close();
        }
    }

    /**
     * Parses client input, performing appropriate operations. 
     * 
     * @param input message from client
     * @return message to client
     */
    private void handleRequest(final String input, final Integer threadNum) {
        String regex = "([^=]* selectBoard [^=]*)|([^=]* draw -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ [^=]* [^=]* [^=]*)|([^=]* erase -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+)|"
                + "(help)|(bye)|(new username [^=]*)|(addBoard [^=]*)";
        if (!input.matches(regex)) { // Invalid Input
            System.err.println("Not in REGEX :(");
            commandQueues.get(threadNum).add("Invalid Input Received by Server");
            return ;
        }
        String[] tokens = input.split(" ");
        // Adding username to the list of usernames
        if ((tokens[0].equals("new")) && (tokens[1].equals("username"))){
            // If the client doesn't exist
            if (!clientToWhiteboardMap.containsKey(tokens[2])){
                clientToWhiteboardMap.put(tokens[2],"");
                clientToThreadNumMap.put(tokens[2], threadNum);
                getExistingWhiteboardsOne(threadNum); // send existing whiteboards only to the user that just joined
                commandQueues.get(threadNum).add("Select a whiteboard");
            } else{ // case in which the username is already in the map
                commandQueues.get(threadNum).add("Username already taken. Please select a new username.");
            }
        } else if (tokens[0].equals("addBoard")){
            if (whiteboardToCommandsMap.containsKey(tokens[1])){
                commandQueues.get(threadNum).add("Whiteboard already exists."); // I don't think this is possible anymore
            } else{
                ArrayList<String> commandList = new ArrayList<String>();
                whiteboardToCommandsMap.put(tokens[1], commandList);
                commandQueues.get(threadNum).add("Board " + tokens[1] + " added");
            }
            getExistingWhiteboardsAll(); // Send existing whiteboards to all users
        } else if (tokens[0].equals("help")) {
            // 'help' request
            commandQueues.get(threadNum).add("Help"); // actually probably don't need to send a help message as the help message should be stored locally on the client
        } else if (tokens[0].equals("bye")) {
            //terminate connection
            System.err.println("Connection terminated");
            commandQueues.get(threadNum).add("Thank you!"); // probably don't need this since the client should be allowed to terminate connection on own end
        } else if (tokens.length > 1) {
            if ((tokens[1].equals("draw")) || (tokens[1].equals("erase"))){
                // put the input in the whiteboard commandlist
                ArrayList<String> commands = whiteboardToCommandsMap.get(tokens[0]);
                commands.add(input);
                whiteboardToCommandsMap.put(tokens[0], commands);
                // putting commands into all relevant queues
                for (String keys : clientToWhiteboardMap.keySet()){
                    if (clientToWhiteboardMap.get(keys).equals(tokens[0])){
                        commandQueues.get(clientToThreadNumMap.get(keys)).add(input);
                    }
                }
            } else if (tokens[1].equals("selectBoard")) { // Selecting board
                if (!clientToWhiteboardMap.containsKey(tokens[0])){
                    commandQueues.get(threadNum).add("Username does not exist.");
                } else if (!whiteboardToCommandsMap.containsKey(tokens[2])){
                    commandQueues.get(threadNum).add("Whiteboard does not exist. Select a different board or make a board.");
                } else{
                    clientToWhiteboardMap.put(tokens[0], tokens[2]);
                    getSameUsersWhiteboard(threadNum, tokens[2]);
                    commandQueues.get(threadNum).add(tokens[0] + " on board " + tokens[2]); 
                    System.out.println(tokens[0] + " on board " + tokens[2]);
                    for (String command : whiteboardToCommandsMap.get(tokens[2])){ // sending all previous commands
                        commandQueues.get(threadNum).add(command);
                    }
                }
            }
        } else { // Invalid Input
            System.err.println("Invalid Input NO ACTION");
            commandQueues.get(threadNum).add("Invalid Input HERE");
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
     * Starts a WhiteboardServer running on the specified port.
     * 
     * @param port
     *            The network port on which the server should listen.
     */
    public static void runWhiteboardServer(final int port) throws IOException {
        WhiteboardServer server = new WhiteboardServer(port);
        server.serve();
    }
}