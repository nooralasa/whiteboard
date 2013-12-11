package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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

/**
 * Whiteboard Server represents a server that allows many clients to collaborate
 * on whiteboards simultaneously over a network connection.
 */
public class WhiteboardServer {
    private final AtomicInteger threadID = new AtomicInteger(-1);
    protected final Map<String, String> clientToWhiteboardMap;
    protected final Map<String, ArrayList<String>> whiteboardToClientsMap;
    protected final Map<String, ArrayList<String>> whiteboardToCommandsMap;
    protected final Map<String, Integer> clientToThreadNumMap;
    protected final List<BlockingQueue<String>> commandQueues;
    protected final List<Boolean> outputThreadActive;

    /**
     * Creates a Whiteboard Server.
     * 
     * @throws IOException
     */
    public WhiteboardServer() throws IOException {
        // Maps each client to the Whiteboard it is working on
        clientToWhiteboardMap = Collections.synchronizedMap(new HashMap<String, String>());

        // Maps each Whiteboard to all of its clients (similar to
        // clientToWhiteboardMap), used to save repeat iterations in message
        // passing
        whiteboardToClientsMap = Collections.synchronizedMap(new HashMap<String, ArrayList<String>>());

        // Maps each Whiteboard to its Commands History
        whiteboardToCommandsMap = Collections.synchronizedMap(new HashMap<String, ArrayList<String>>());

        // List of Blocking Queues used to send messages to clients
        commandQueues = Collections.synchronizedList(new ArrayList<BlockingQueue<String>>());

        // Maps each client to its threadnum which corresponds to its Blocking
        // Queue in the commandsQueue
        clientToThreadNumMap = Collections.synchronizedMap(new HashMap<String, Integer>());

        // List of Booleans to keep output threads active
        outputThreadActive = Collections.synchronizedList(new ArrayList<Boolean>());

        // Creates the Server's starting Whiteboards
        createBoards();
    }

    /**
     * Run the Whiteboard Server, listening for client connections and handling
     * them. Never returns unless an exception is thrown.
     * 
     * @param port
     *            represents the listening port of the Whiteboard Server's
     *            socket
     * @throws IOException
     *             if the main server socket is broken (IOExceptions from
     *             individual clients do *not* terminate serve())
     */
    public void serve(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            // Block until a client connects
            final Socket socket = serverSocket.accept();
            threadID.getAndIncrement();

            // Creates the blocking queue for each client and adds it to the
            // commandQueues list
            BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(100000);
            commandQueues.add(blockingQueue);

            final Integer threadNum = threadID.get();
            // Create the Input and Output Threads for each client
            createThreads(socket, threadNum);
        }
    }

    /**
     * Creates 3 Boards for the Server to start with
     */
    protected void createBoards() {
        ArrayList<String> emptyCommandAndClientList = new ArrayList<String>();
        whiteboardToCommandsMap.put("Board1", emptyCommandAndClientList);
        whiteboardToCommandsMap.put("Board2", emptyCommandAndClientList);
        whiteboardToCommandsMap.put("Board3", emptyCommandAndClientList);
    }

    /**
     * Creates and starts threads to handle inputs and outputs between the
     * server and each client.
     * 
     * @param socket
     *            represents the socket that the client is connected to
     * @param threadNum
     *            represents the position of the client's blockingQueue in the
     *            commandQueues list
     */
    private void createThreads(final Socket socket, final Integer threadNum) {
        // Thread to handle inputs from the client
        Thread inputThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Starting Client Input Thread with Thread " + threadNum);
                try {
                    handleClientInput(socket, threadNum);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Input Client Thread with Thread " + threadNum + " done running.");
                }
            }
        });

        // Thread to handle outputs to the client
        Thread outputThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Starting Output Thread with Thread " + threadNum);
                try {
                    boolean outputThreadStatus = true;
                    outputThreadActive.add(outputThreadStatus);
                    handleOutputs(socket, threadNum);
                } catch (SocketException e) {
                    System.err.println("Client socket closed for Thread " + threadNum.toString());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Output Thread with Thread " + threadNum + " done running.");
                }
            }
        });
        inputThread.start();
        outputThread.start();
    }

    /**
     * Polls the output BlockingQueue of each client and writes items as text
     * messages to the client's socket.
     * 
     * @param socket
     *            represents the socket that the client is connected to
     * @throws IOException
     *             if connection has an error or terminates unexpectedly
     * @throws InterruptedException
     */
    private void handleOutputs(final Socket socket, final Integer threadNum)
            throws IOException, InterruptedException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try {
            // Constantly poll the client's Blocking Queue in the Commands Queue
            while (outputThreadActive.get(threadNum)) {
                BlockingQueue<String> commandsQueue = commandQueues
                        .get(threadNum);
                while (commandsQueue.peek() != null) {
                    String output = (String) commandsQueue.take();
                    out.println(output);
                }
            }
        } finally {
            System.out.println("Socket for Thread " + threadNum.toString() + " closed");
        }
    }

    /**
     * Listens to the client socket for messages and passes inputs to
     * handleRequest to be handled.
     * 
     * @param socket
     *            represents the socket the client is connected to
     * @throws IOException
     *             if connection has an error or terminates unexpectedly
     * @throws InterruptedException
     */
    private void handleClientInput(final Socket socket, final Integer threadNum)
            throws IOException, InterruptedException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                if (line.substring(0, 10).equals("Disconnect")) { // If the server receives the disconnectmessage
                    in.close();
                    socket.close();
                    String[] tokens = line.split(" ");
                    // Remove client from clientTothreadNumMap and from
                    // clientToWhiteboardMap
                    removeDisconnectedUser(tokens[1], threadNum);
                    clientToWhiteboardMap.remove(tokens[1]);
                    clientToThreadNumMap.remove(tokens[1]);
                    break;
                } else {
                    handleRequest(line, threadNum);
                }
            }
        } catch (SocketException e) {
            System.err.println("Input: Client socket closed for Thread " + threadNum.toString());
        } finally {
            System.err.println("Input Thread for Thread " + threadNum.toString() + " done");
        }
    }

    /**
     * Parses client input and performs the appropriate operations.
     * 
     * @param input
     *            represents the text message from the client
     * @param threadNum
     *            represents the position of the client's blockingQueue in the
     *            commandQueues list
     */
    protected void handleRequest(final String input, final Integer threadNum) {
        String regex = "([^=]* selectBoard [^=]*)|([^=]* draw -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ [^=]* [^=]* [^=]*)|([^=]* erase -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+)|"
                + "(Disconnect [^=]*)|(new username [^=]*)|(addBoard [^=]*)";
        // If the input is not defined in the regex
        if (!input.matches(regex)) {
            System.err.println("Not in Server Regex");
            System.err.println(input);
            commandQueues.get(threadNum).offer("Not in Server Regex");
            return;
        }
        String[] tokens = input.split(" ");
        if (tokens.length > 1) {
            if ((tokens[0].equals("new")) && (tokens[1].equals("username"))) {
                // If the username doesn't already exist in the server
                if (!clientToWhiteboardMap.containsKey(tokens[2])) {
                    clientToWhiteboardMap.put(tokens[2], "");
                    clientToThreadNumMap.put(tokens[2], threadNum);
                    // Send existing Whiteboard names only to the user that just
                    // joined
                    getExistingWhiteboardsOne(threadNum);
                    commandQueues.get(threadNum).offer("Select a whiteboard");
                } else { // If the username already exists in the server
                    commandQueues.get(threadNum).offer("Username already taken. Please select a new username.");
                }
            } else if (tokens[0].equals("addBoard")) {
                if (whiteboardToCommandsMap.containsKey(tokens[1])) {
                    // If the Whiteboard already exists in the server
                    commandQueues.get(threadNum).offer("Whiteboard already exists.");
                } else {
                    // Adds a new Whiteboard to the Server
                    ArrayList<String> commandList = new ArrayList<String>();
                    whiteboardToCommandsMap.put(tokens[1], commandList);
                    commandQueues.get(threadNum).offer("Board " + tokens[1] + " added");
                }
                // Send existing Whiteboard names to all users
                getExistingWhiteboardsAll();
            } else if ((tokens[1].equals("draw"))
                    || (tokens[1].equals("erase"))) {
                // Put the input in the Whiteboard commandlist
                whiteboardToCommandsMap.get(tokens[0]).add(input);
                // Putting commands into all collaborator queues
                for (String keys : clientToWhiteboardMap.keySet()) {
                    if (clientToWhiteboardMap.get(keys).equals(tokens[0])) {
                        commandQueues.get(clientToThreadNumMap.get(keys)).offer(input);
                    }
                }
            } else if (tokens[1].equals("selectBoard")) {
                // If the client doesn't exist
                if (!clientToWhiteboardMap.containsKey(tokens[0])) {
                    commandQueues.get(threadNum).offer("Username does not exist.");
                    // If the whiteboard doesn't exist
                } else if (!whiteboardToCommandsMap.containsKey(tokens[2])) {
                    commandQueues.get(threadNum).offer("Whiteboard does not exist. Select a different board or make a board.");
                } else {
                    // Map the client to its Whiteboard
                    clientToWhiteboardMap.put(tokens[0], tokens[2]);
                    // Update all clients of the collaborator names
                    getSameUsersWhiteboard();
                    commandQueues.get(threadNum).offer(tokens[0] + " on board " + tokens[2]);
                    System.out.println(tokens[0] + " on board " + tokens[2]);
                    // Send Whiteboard history of commands to the client
                    for (String command : whiteboardToCommandsMap.get(tokens[2])) {
                        commandQueues.get(threadNum).offer(command);
                    }
                }
            } else {
                System.err.println("In Server Regex, no action");
                System.err.println(input);
                commandQueues.get(threadNum).offer("In Server Regex, no action");
            }
        }
    }

    /**
     * Sends the names of the current Whiteboards on the server to all clients
     */
    protected void getExistingWhiteboardsAll() {
        for (String whiteboard : whiteboardToCommandsMap.keySet()) {
            String whiteboards = "Existing Whiteboards " + whiteboard;
            for (BlockingQueue<String> commandQueue : commandQueues) {
                commandQueue.offer(whiteboards);
            }
        }
        String message = "Done sending whiteboard names";
        for (BlockingQueue<String> commandQueue : commandQueues) {
            commandQueue.offer(message);
        }
    }

    /**
     * Sends the names of the current Whiteboards on the server to one specified
     * client
     */
    protected void getExistingWhiteboardsOne(final int threadNum) {
        for (String whiteboard : whiteboardToCommandsMap.keySet()) {
            String whiteboards = "Existing Whiteboards " + whiteboard;
            commandQueues.get(threadNum).offer(whiteboards);
        }
        String message = "Done sending whiteboard names";
        commandQueues.get(threadNum).offer(message);
    }

    /**
     * Sends out to all clients the names of the other clients working on the
     * same Whiteboard
     */
    protected void getSameUsersWhiteboard() {
        // Updating whiteboardToClientsMap
        whiteboardToClientsMap.clear();
        for (String client : clientToWhiteboardMap.keySet()) {
            // If the whiteboardToClientsMap doesn't have the Whiteboard name
            if (!whiteboardToClientsMap.containsKey(clientToWhiteboardMap.get(client))) {
                // Create a list of clients working on the same Whiteboard and
                // add the current client to the list
                ArrayList<String> currentClients = new ArrayList<String>();
                currentClients.add(client);
                whiteboardToClientsMap.put(clientToWhiteboardMap.get(client),currentClients);
            } else {
                // Add the current client to the list of clients working on the
                // same Whiteboard (only if the client isn't already in the
                // list)
                if (!whiteboardToClientsMap.get(clientToWhiteboardMap.get(client)).contains(client)) {
                    whiteboardToClientsMap.get(clientToWhiteboardMap.get(client)).add(client);
                }
            }
        }

        // Go through all of the Whiteboards and send all collaborating clients
        // the names of the other collaborators
        for (String whiteboard : whiteboardToClientsMap.keySet()) {
            ArrayList<String> sameClients = whiteboardToClientsMap.get(whiteboard);
            ArrayList<String> sameClientsCommands = new ArrayList<String>();

            // Generates the String commands to denote collaborators
            for (String client : sameClients) {
                String clientCommand = "sameClient " + client;
                sameClientsCommands.add(clientCommand);
            }

            // Send each collaborator that shares the Whiteboard the names of
            // the other collaborators
            for (String client : sameClients) {
                String sending = "Updating Clients";
                commandQueues.get(clientToThreadNumMap.get(client)).offer(sending);
                for (String clientCommands : sameClientsCommands) {
                    commandQueues.get(clientToThreadNumMap.get(client)).offer(clientCommands);
                }
                String doneSending = "Done sending client names";
                commandQueues.get(clientToThreadNumMap.get(client)).offer(doneSending);
            }
        }
    }

    /**
     * Sends out to all clients the name of the client who disconnected and was
     * working on the same Whiteboard
     * 
     * @param client
     *            the name of the client who disconnected
     */
    protected void removeDisconnectedUser(String client, final int threadNum) {
        // Updating whiteboardToClientsMap
        whiteboardToClientsMap.get(clientToWhiteboardMap.get(client)).remove(client);
        String clientCommand = "removeClient " + client;

        // Go through all of the Whiteboards and send all collaborating clients
        // the names of the other collaborators
        for (String whiteboard : whiteboardToClientsMap.keySet()) {
            ArrayList<String> sameClients = whiteboardToClientsMap.get(whiteboard);

            // Send each collaborator that shares the Whiteboard the names of
            // the other collaborators
            for (String c : sameClients) {
                commandQueues.get(clientToThreadNumMap.get(c)).offer(clientCommand);
            }
        }
        outputThreadActive.set(threadNum, false); // Makes the output thread
                                                  // finish
    }

    /**
     * Start a WhiteboardServer using the given arguments.
     * 
     * Usage: WhiteboardServer [--port PORT]
     * 
     * PORT is an optional integer in the range 0 to 65535 inclusive, specifying
     * the port the server should be listening on for incoming connections. E.g.
     * "WhiteboardServer --port 1234" starts the server listening on port 1234.
     * 
     * @throws IOException
     * 
     */
    public static void main(String[] args) throws IOException {
        int port = 4444; // Default port
        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while (!arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > 65535) {
                            throw new IllegalArgumentException("port " + port + " out of range");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: WhiteBoardServer [--port PORT]");
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
     *            represents network port on which the server should listen.
     */
    public static void runWhiteboardServer(final int port) throws IOException {
        WhiteboardServer server = new WhiteboardServer();
        server.serve(port);
    }
}