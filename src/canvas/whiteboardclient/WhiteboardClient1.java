package canvas.whiteboardclient;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import canvas.ButtonPanel;
import canvas.Whiteboard1;
//import canvas.SidePanel;

/**
 * Canvas represents a drawing surface that allows the user to draw
 * on it freehand, with the mouse.
 */
public class WhiteboardClient1 {
    private String clientName;
    private String whiteboard; // whiteboard name of the client
    private final BlockingQueue<String> inputCommandsQueue; // may need this later for another thread to poll if too laggy
    public final BlockingQueue<String> outputCommandsQueue;
    private final List<String> usersInWhiteboard;
    private Whiteboard1 whiteboards;
    private int height;
    private int width;
    /**
     * Make a canvas.
     * @param width width in pixels
     * @param height height in pixels
     */
    public WhiteboardClient1(int width, int height, String ipAddress, int portNumber) {
        outputCommandsQueue = new ArrayBlockingQueue<String>(10000000);
        inputCommandsQueue = new ArrayBlockingQueue<String>(10000000); // may need this later see note in field dec
        usersInWhiteboard = Collections.synchronizedList(new ArrayList<String>());
        System.out.println("Got here");
        connectToServer(ipAddress, portNumber);
        System.out.println("got here");
        whiteboards = new Whiteboard1(width,height, outputCommandsQueue);
        outputCommandsQueue.offer(whiteboards.getUsername(""));
        //createWhiteboard("whiteboard");
    }

    public void createWhiteboard(String whiteboard) {
        System.out.println("We are here");
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
    public void connectToServer(String ipAddress, int portNumber){
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
                        // TODO Auto-generated catch block
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
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                handleResponse(line);
                System.out.println("Server Response: " + line);
            }
        } finally {
            in.close();
            socket.close();
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     */
    private void handleResponse(String input) {
        String regex = "(Existing Whiteboards [^=]*)|(sameClient [^=]*)|(Username already taken. Please select a new username.)|(Whiteboard already exists.)|"
                + "(Instructions: username yourUsername, selectBoard board#, help, bye, board# draw x1 y1 c2 y2, board# erase x1 y1 x2 y2)|(Thank you!)|"
                + "(Select a whiteboard)|(Whiteboard does not exist. Select a different board or make a board.)|(You are currently on board [^=]*)|"
                + "(Board [^=]* added)|([^=]* draw -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ [^=]* [^=]* [^=]*)|([^=]* erase -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+)|(Done sending whiteboard names)|(Done sending client names)";
        if (!input.matches(regex)) {
            // invalid input
            System.err.println("Not in Regex");
            System.err.println(input);
            return ;
        }
        String[] tokens = input.split(" ");
        System.err.println("Tokens coming");
        for (String token : tokens){
            System.err.println(token);
        }
        System.err.println("No more token");
        // Choosing a whiteboard to work on
        if (tokens.length > 1) {
            System.out.println("Token length > 1");
            if (tokens[1].equals("Username")){
                outputCommandsQueue.offer(whiteboards.getUsername("Username already taken.\n"));
            } else if (((tokens[0].equals("Select")) && tokens[2].equals("whiteboard")) || (tokens[0].equals("Whiteboard") && tokens[2].equals("exists"))){
                String desiredWhiteboardName = whiteboards.chooseWhiteboard();
                whiteboards.canvas.canvas = desiredWhiteboardName;
                if (whiteboards.getExistingWhiteboards().contains(desiredWhiteboardName)){
                    System.out.println("Is it noor? " + whiteboards.clientName);
                    outputCommandsQueue.offer(whiteboards.clientName + " selectBoard " + desiredWhiteboardName);
                } else{
                    System.out.println("Is it noor? " + whiteboards.clientName);
                    outputCommandsQueue.offer("addBoard " + desiredWhiteboardName);
                }             
            } else if ((tokens[0].equals("Existing")) && (tokens[1].equals("Whiteboards"))){
                if (!whiteboards.getExistingWhiteboards().contains(tokens[2])){
                    whiteboards.getExistingWhiteboards().add(tokens[2]);
                }
            }else if (tokens[0].equals("sameClient")){
                if (!usersInWhiteboard.contains(tokens[1])){
                    usersInWhiteboard.add(tokens[1]);
                }
            } else if ((tokens[0].equals("Done")) && (tokens[1].equals("sending")) && (tokens[2].equals("whiteboard"))){
                System.out.println("Done receiving whiteboards!");
                System.out.println(whiteboards.getExistingWhiteboards().toString());
                // here should be the command to update the panel displaying the whiteboards 
            } else if ((tokens[0].equals("Done")) && (tokens[1].equals("sending")) && (tokens[2].equals("client"))){
                System.out.println("Done sending clients!");
                System.out.println(usersInWhiteboard.toString());
                // here should be the command to update the panel displaying the clients on the same whiteboard 
                usersInWhiteboard.clear();
            }else if (tokens[0].equals("Board") && tokens[2].equals("added")) {
                whiteboard = tokens[1];
                outputCommandsQueue.offer(whiteboards.clientName + " selectBoard " + tokens[1]);
                //TODO: create a white whiteboard and name the title of the jframe or something to indicate the name of the whiteboard
            } else if ((tokens[0].equals("Instructions:"))){ // probably should get rid of this and make it so that the help box doesn't call server
                whiteboards.helpBox();
            } else if (tokens[0].equals("Thank") && tokens[1].equals("you!")) {
                System.err.println("Connection terminated"); //TODO: terminate connection
            } else if (tokens[0].equals(whiteboard) && (tokens[1].equals("draw"))){
                int x1 = Integer.parseInt(tokens[2]);
                int y1 = Integer.parseInt(tokens[3]);
                int x2 = Integer.parseInt(tokens[4]);
                int y2 = Integer.parseInt(tokens[5]);
                int newStrokeSize = Integer.parseInt(tokens[6]);
                String redValue = tokens[7];
                String greenValue = tokens[8];
                String blueValue = tokens[9];
                whiteboards.getCanvas().commandDraw(x1, y1, x2, y2, newStrokeSize, redValue, greenValue, blueValue);
            } else if (tokens[0].equals(whiteboard) && (tokens[1].equals("erase"))){
                int x1 = Integer.parseInt(tokens[2]);
                int y1 = Integer.parseInt(tokens[3]);
                int x2 = Integer.parseInt(tokens[4]);
                int y2 = Integer.parseInt(tokens[5]);
                int newStrokeSize = Integer.parseInt(tokens[6]);
                whiteboards.getCanvas().commandErase(x1, y1, x2, y2, newStrokeSize);            
            } else {
                System.err.println("Invalid Input Tokens greater than 1");
                System.err.println(input);
                return ;
            }
        } else {
            System.err.println("Invalid Input Tokens less than 1");
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
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try {
            while (true){ // constantly poll the commands queue //TODO: check with TA what to do about this since when the socket disconnects will still be in the while loop
                while (outputCommandsQueue.peek() != null){
                    String output = (String) outputCommandsQueue.take();
                    System.out.println("Output to Server: " + output); // so we can see what is being output DELETE later
                    out.println(output);
                    if (output.equals("Thank you!")) { // this is if thank you is the disconnect message
                        out.close();
                        socket.close();
                        break;
                    }
                }
            }
        } finally {
        }
    }

    public static void runWhiteboardClient(String ipAddress, int port){
        WhiteboardClient1 client1 = new WhiteboardClient1(800,600, ipAddress, port);
        client1.createWhiteboard(client1.whiteboards.clientName);
    }

    /*
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        int port = 4444; // default port
        String ipAddress = "127.0.0.1";

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
        runWhiteboardClient(ipAddress, port);
        System.out.println("It's here");
    }


}
