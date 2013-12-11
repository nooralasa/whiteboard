package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Whiteboard Client represents a client working on the Whiteboard.
 */
public class WhiteboardClient {
    private boolean outActive = true;
    private BufferedReader in;
    private PrintWriter out;
    protected String whiteboardName;
    protected final BlockingQueue<String> outputCommandsQueue; //For communication with the server
    private final List<String> usersInWhiteboard;
    private WhiteboardGUI whiteboards; //The GUI representation of a Whiteboard
    private int width;
    private int height;
    private String ipAddress;
    private int portNumber;

    /**
     * Makes a WhiteboardClient
     * 
     * @param width width of the whiteboard in pixels
     * @param height height of the whiteboard in pixels
     * @param serverIPAddress the ipAddress the Server is running on
     * @param port the port the Client uses to conect to the Server
     */
    public WhiteboardClient(final int clientWidth, final int clientHeight, final String serverIPAddress, final int port) {
        outputCommandsQueue = new ArrayBlockingQueue<String>(10000000);     //MAX_INT doesn't work as an argument, thus a very large number is passed on
        usersInWhiteboard = Collections.synchronizedList(new ArrayList<String>());
        width = clientWidth;
        height = clientHeight;
        ipAddress = serverIPAddress;
        portNumber = port;
    }

    public void createGUI(){
        whiteboards = new WhiteboardGUI(width,height, outputCommandsQueue);  
        outputCommandsQueue.offer(whiteboards.getUsername(""));     //Asks for the username
        createWhiteboard(whiteboards.clientName);
    }

    /**
     * Creates the Whiteboard Window and makes the Whiteboard.
     * @param whiteboard
     */
    public void createWhiteboard(String whiteboard) {
        whiteboards.createWindow(whiteboard);
        whiteboards.makeWhiteboard();
    }

    /**
     * Connects to the server.
     * 
     * @throws IOException
     *             if the main server socket is broken (IOExceptions from
     *             individual clients do *not* terminate serve())
     */
    public void connectToServer(){
        try {
            final Socket clientSocket = new Socket(ipAddress, portNumber);
            // start a new thread to handle the connection
            Thread inputThread = new Thread(new Runnable() {
                public void run() {
                    System.out.println("Starting Client Input Thread");
                    try {
                        handleServerResponse(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            // start a new thread to handle outputs
            Thread outputThread = new Thread(new Runnable() {
                public void run() {
                    System.out.println("Starting Client Output Thread");
                    try {
                        handleOutputs(clientSocket);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            inputThread.start();   
            outputThread.start();  
        } catch (UnknownHostException e) {
            e.printStackTrace(); // Unknown host
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace(); // but don't terminate serve()
            System.exit(1);
        } finally {
        }  
    }


    /**
     * Handle a single connection with the server. Returns when client disconnects.
     * 
     * @param socket
     *            socket where the client is connected
     * @throws IOException
     *             if connection has an error or terminates unexpectedly
     */
    private void handleServerResponse(Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            while(true) {
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    handleResponse(line);
//                    System.out.println("Server Response: " + line);
                }
            }   
        }catch (SocketException e) {
            System.err.println("Input reader closed");
        }finally {
        }
    }

    /**
     * Handler for input, performing requested operations and returning an output message.
     * 
     * @param input message from server
     */
    private void handleResponse(String input) {
        String regex = "(Existing Whiteboards [^=]*)|(sameClient [^=]*)|(removeClient [^=]*)|(Username already taken. Please select a new username.)|"
                + "(Whiteboard already exists.)|(Select a whiteboard)|(Whiteboard does not exist. Select a different board or make a board.)|"
                + "([^=]* on board [^=]*)|(Updating Clients)|(Board [^=]* added)|([^=]* draw -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ [^=]* [^=]* [^=]*)|"
                + "([^=]* erase -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+)|(Done sending whiteboard names)|(Done sending client names)|(Not in Server Regex)|"
                + "(In Server Regex, no action)";
        if (!input.matches(regex)) {
            System.err.println("Not in Client Regex");
            System.err.println(input);
            return ;
        }
        String[] tokens = input.split(" ");
        if (tokens.length > 1) {
            if (tokens[0].equals("Username")){
                outputCommandsQueue.offer(whiteboards.getUsername("Username already taken.\n"));
            } else if (((tokens[0].equals("Select")) && tokens[2].equals("whiteboard")) || (tokens[0].equals("Whiteboard") && tokens[2].equals("exists"))){
                whiteboards.chooseWhiteboardPopup();
            } else if (tokens[1].equals("on") && tokens[2].equals("board")) {
                //updates the client's whiteboard
                whiteboardName = tokens[3];
                // Updates the Canvas' Whiteboard Name 
                whiteboards.canvas.setWhiteboardName(tokens[3]);              
            } else if ((tokens[0].equals("Existing")) && (tokens[1].equals("Whiteboards"))){
                if (!whiteboards.getExistingWhiteboards().contains(tokens[2])){
                    whiteboards.getExistingWhiteboards().add(tokens[2]);
                }
            } else if (tokens[0].equals("Updating") && tokens[1].equals("Clients")){
                usersInWhiteboard.clear();
            } else if (tokens[0].equals("sameClient")){
                if (!usersInWhiteboard.contains(tokens[1])){
                    usersInWhiteboard.add(tokens[1]);
                }
            } else if (tokens[0].equals("removeClient")){
                if (usersInWhiteboard.contains(tokens[1])){
                    usersInWhiteboard.remove(tokens[1]);
                    whiteboards.getSidePanel().updateClientsList(usersInWhiteboard);
                }
            } else if ((tokens[0].equals("Done")) && (tokens[1].equals("sending")) && (tokens[2].equals("whiteboard"))){
                if (!(whiteboardName != null)){
                    whiteboardName = "Board1";
                }
                whiteboards.getSidePanel().updateWhiteboardsList(whiteboards.getExistingWhiteboards(), whiteboardName);
            } else if ((tokens[0].equals("Done")) && (tokens[1].equals("sending")) && (tokens[2].equals("client"))){
                whiteboards.getSidePanel().updateClientsList(usersInWhiteboard);
            } else if (tokens[0].equals("Board") && tokens[2].equals("added")) {
                whiteboardName = tokens[1];
                outputCommandsQueue.offer(whiteboards.clientName + " selectBoard " + tokens[1]);
                whiteboards.canvas.setWhiteboardName(tokens[1]);              
                whiteboards.updateTitle(whiteboardName);
            } else if (tokens[0].equals(whiteboardName) && (tokens[1].equals("draw"))){
                int x1 = Integer.parseInt(tokens[2]);
                int y1 = Integer.parseInt(tokens[3]);
                int x2 = Integer.parseInt(tokens[4]);
                int y2 = Integer.parseInt(tokens[5]);
                int newStrokeSize = Integer.parseInt(tokens[6]);
                String redValue = tokens[7];
                String greenValue = tokens[8];
                String blueValue = tokens[9];
                whiteboards.getCanvas().commandDraw(x1, y1, x2, y2, newStrokeSize, redValue, greenValue, blueValue);
            } else if (tokens[0].equals(whiteboardName) && (tokens[1].equals("erase"))){
                int x1 = Integer.parseInt(tokens[2]);
                int y1 = Integer.parseInt(tokens[3]);
                int x2 = Integer.parseInt(tokens[4]);
                int y2 = Integer.parseInt(tokens[5]);
                int newStrokeSize = Integer.parseInt(tokens[6]);
                whiteboards.getCanvas().commandErase(x1, y1, x2, y2, newStrokeSize);            
            } else if (tokens[2].equals("Server") && (tokens[3].equals("Regex"))){
                System.err.println("Command " + input); // Don't need to do anything      
            } else if (tokens[0].equals("In") && (tokens[1].equals("Server"))){
                System.err.println("Command " + input); // Don't need to do anything      
            } 
        } else {
            System.err.println("In Server Regex, no action");
            System.err.println(input);
            return ;
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
    private void handleOutputs(final Socket socket) throws IOException, InterruptedException {
        out = new PrintWriter(socket.getOutputStream(), true);
        try {
            while (outActive){ // constantly poll the commands queue 
                while (outputCommandsQueue.peek() != null){
                    String output = (String) outputCommandsQueue.take();
                    //                    System.out.println("Output to Server: " + output); // so we can see what is being output DELETE later
                    out.println(output);
                    if (output.substring(0,10).equals("Disconnect")) { // this is if thank you is the disconnect messages
                        outputCommandsQueue.clear(); // Clears the outputCommandsQueue
                        outActive = false;
                        out.close();
                        in.close();
                        socket.close();
                        break;
                    }
                }
            }
        } finally {
            System.out.println("Output thread done");
        }
    }

    /**
     * Runs the Whiteboard Client.
     * @param ipAddress Server IP Address
     * @param port Server Port
     */
    public static void runWhiteboardClient(final String ipAddress, final int port, final int clientWidth, final int clientHeight){
        WhiteboardClient client = new WhiteboardClient(clientWidth,clientHeight, ipAddress, port);
        client.connectToServer();
        client.createGUI();
    }

    /*
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        int port = 4444; // default port
        String ipAddress = "127.0.0.1"; // Localhost IP Address by default
        int clientWidth = 800;
        int clientHeight = 600;
        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while ( !arguments.isEmpty()) {
                String flag = arguments.remove();
                System.out.println(flag);
                try {
                    if (flag.equals("--ip")) {
                        ipAddress = arguments.remove();
                        System.out.println(ipAddress);
                    } else if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        System.out.println(port);
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
            System.err.println("usage: Whiteboard Client [--ip ipAddress] [--port PORT]");
            return;
        }
        runWhiteboardClient(ipAddress, port, clientWidth, clientHeight);
    }
}