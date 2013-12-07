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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

/**
 * Canvas represents a drawing surface that allows the user to draw
 * on it freehand, with the mouse.
 */
public class WhiteboardClient extends JPanel {
    private Image drawingBuffer; // Image where the user's drawing is stored
    private JColorChooser tcc = new JColorChooser(Color.BLACK);
    private String clientName;
    private String whiteboard; // whiteboard name of the client
    private JFrame window;
    private boolean drawMode;
    private int strokeSize;
    private final BlockingQueue<String> inputCommandsQueue; // may need this later for another thread to poll if too laggy
    private final BlockingQueue<String> outputCommandsQueue;
    private final List<String> existingWhiteboards;
    /**
     * Make a canvas.
     * @param width width in pixels
     * @param height height in pixels
     */
    public WhiteboardClient(int width, int height) {
        this.setPreferredSize(new Dimension(width, height));
        addDrawingController();
        // note: we can't call makeDrawingBuffer here, because it only
        // works *after* this canvas has been added to a window.  Have to
        // wait until paintComponent() is first called.
        drawMode = true;
        inputCommandsQueue = new ArrayBlockingQueue<String>(10000000); // may need this later see note in field dec
        outputCommandsQueue = new ArrayBlockingQueue<String>(10000000);
        existingWhiteboards = Collections.synchronizedList(new ArrayList<String>());
        connectToServer();
        getUsername("");
    }

    /**
     * Creates a popup window to get the username from the user.
     * @param message represents the special message to attach depending on the situation
     */
    private void getUsername(String message){
        JFrame popup = new JFrame(); // Popup asking for Username
        Object[] possibilities = null;
        String desiredClientName = (String) JOptionPane.showInputDialog(popup, message + "Input your desired username:", "Username", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        this.clientName = desiredClientName;
        outputCommandsQueue.offer("new username " + desiredClientName);
    }

    /**
     * Lets the user choose their whiteboard.
     */
    private void chooseWhiteboard(){
        // all the names of the existing whiteboards are in the list of strings existingWhiteboards
        // should use a jcombo box or something to display the choices
        // could maybe `have a textfield similar to the getusername one to choose own.
        // MAKE SURE to set the selected whiteboard as this.whiteboard and then wipe the board
 // for now needs to be fixed by erwin
        JFrame popup = new JFrame(); // Popup asking for Whiteboard Name
        Object[] possibilities = null;
        String desiredWhiteboardName = (String) JOptionPane.showInputDialog(popup, "Select a Whiteboard:", "Whiteboard Name", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        this.whiteboard = desiredWhiteboardName;
        outputCommandsQueue.offer(clientName + " selectBoard " + desiredWhiteboardName);
    }

    /**
     * Creates a popup window to get a new whiteboard name from the user.
     * @param message represents the special message to attach depending on the situation
     */
    private void getNewWhiteboardName(String message){
        JFrame popup = new JFrame(); // Popup asking for Whiteboard Name
        Object[] possibilities = null;
        String desiredWhiteboardName = (String) JOptionPane.showInputDialog(popup, message + "Input your desired whiteboard name:", "Whiteboard Name", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        this.whiteboard = desiredWhiteboardName;
        outputCommandsQueue.offer("addBoard " + desiredWhiteboardName);
        System.out.println("GOT NEW WHITEBOARD");

    }

    /**
     * Connects to the server.
     * 
     * @throws IOException
     *             if the main server socket is broken (IOExceptions from
     *             individual clients do *not* terminate serve())
     */
    public void connectToServer(){
        String hostName = "18.189.22.230";
        int portNumber = 4444;
        try {
            final Socket clientSocket = new Socket(hostName, portNumber);
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
        String regex = "(Existing Whiteboards [^=]*)|(Username already taken. Please select a new username.)|(Whiteboard already exists.)|"
                + "(Instructions: username yourUsername, selectBoard board#, help, bye, board# draw x1 y1 c2 y2, board# erase x1 y1 x2 y2)|(Thank you!)|"
                + "(No existing whiteboards.)|(Whiteboard does not exist. Select a different board or make a board.)|(You are currently on board [^=]*)|"
                + "(Board [^=]* added)|([^=]* draw -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ [^=]* [^=]* [^=]*)|([^=]* erase -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+)|(Done sending whiteboard names.)";
        if (!input.matches(regex)) {
            // invalid input
            System.err.println("Invalid Input");
            return ;
        }
        String[] tokens = input.split(" ");
        // Choosing a whiteboard to work on
        if (tokens.length > 1) {
            if (tokens[1].equals("Username")){
                getUsername("Username already taken.\n");
            } else if (((tokens[0].equals("No")) && tokens[1].equals("existing")) || (tokens[0].equals("Whiteboard") && tokens[2].equals("exists"))){
                getNewWhiteboardName(input + "\n");
            } else if ((tokens[0].equals("Existing")) && (tokens[1].equals("Whiteboards"))){
                existingWhiteboards.add(tokens[2]);
            } else if ((tokens[0].equals("Done")) && (tokens[1].equals("sending")) && (tokens[2].equals("whiteboard"))){
                System.out.println("GOT HERE");
                chooseWhiteboard();
            } else if (tokens[0].equals("Board") && tokens[2].equals("added")) {
                whiteboard = tokens[1];
                outputCommandsQueue.offer(clientName + " selectBoard " + tokens[1]);

                //TODO: create a white whiteboard and name the title of the jframe or something to indicate the name of the whiteboard
            } else if ((tokens[0].equals("Instructions:"))){ // probably should get rid of this and make it so that the help box doesn't call server
                helpBox();
            } else if (tokens[0].equals("Thank") && tokens[1].equals("you!")) {
                System.err.println("Connection terminated"); //TODO: terminate connection
            } else if (tokens[0].equals(whiteboard) && (tokens[1].equals("draw"))){
                int x1 = Integer.parseInt(tokens[2]);
                int y1 = Integer.parseInt(tokens[3]);
                int x2 = Integer.parseInt(tokens[4]);
                int y2 = Integer.parseInt(tokens[5]);
//                int stroke = Integer.parseInt(tokens[2]);            
                commandDraw(x1, y1, x2, y2);
            } else if (tokens[0].equals(whiteboard) && (tokens[1].equals("erase"))){
                int x1 = Integer.parseInt(tokens[2]);
                int y1 = Integer.parseInt(tokens[3]);
                int x2 = Integer.parseInt(tokens[4]);
                int y2 = Integer.parseInt(tokens[5]);
//                int stroke = Integer.parseInt(tokens[2]);            
                commandErase(x1, y1, x2, y2);            
            } else {
                System.err.println("Invalid Input");
                return ;
            }
        } else {
            System.err.println("Invalid Input");
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

    /**
     * Pops up help box
     */
    public void helpBox(){
        //default title and icon
        JOptionPane.showMessageDialog(window,
                "I'm guessing you need some help. Too bad.", "Help Message",
                JOptionPane.WARNING_MESSAGE);
    }
    /**
     * Pops up color chooser
     */
    public void colorChooser(){
        JFrame popupColor = new JFrame("Color Chooser");
        popupColor.add(tcc, BorderLayout.CENTER);
        popupColor.pack();
        popupColor.setVisible(true);
    }

    /**
     * If there is no drawing buffer, it makes a drawing buffer. Otherwise, it copies the drawing buffer to the screen.
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        // If this is the first time paintComponent() is being called,
        // make our drawing buffer.
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        // Copy the drawing buffer to the screen.
        g.drawImage(drawingBuffer, 0, 0, null);
    }

    /*
     * Make the drawing buffer and makes its a white blank canvas.
     */
    private void makeDrawingBuffer() {
        drawingBuffer = createImage(getWidth(), getHeight());
        fillWithWhite();
    }

    /*
     * Makes the drawing buffer entirely white.
     */
    private void fillWithWhite() {
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0,  0,  getWidth(), getHeight());

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }


    /*
     * Draw line/stroke segment size
     */
    public void setStrokeState(int value) {
        this.strokeSize = value;
    }

    /*
     * Draws a line segment between two points (x1,y1) and (x2,y2) 
     * with a specified stroke size and color (in RGB), specified 
     * in pixels relative to the upper left corner of the drawing buffer
     */
    public void drawLineSegment(int x1, int y1, int x2, int y2) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
            System.out.println("make a drawing buffer");
        }
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        g.setColor(tcc.getColor());

//        setStrokeState(newStrokeSize);
        g.setStroke(new BasicStroke(strokeSize));

        //colors in RGB
        String red = Integer.toString(tcc.getColor().getRed());
        String green = Integer.toString(tcc.getColor().getGreen());
        String blue = Integer.toString(tcc.getColor().getBlue());
        g.drawLine(x1, y1, x2, y2);
        //        System.out.println("Drawing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2 + " Stroke Size " + strokeSize + " R " + red + " G " + green + " B " + blue);

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        String drawCommand = whiteboard + " draw " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + strokeSize + " " + red + " " + green + " " + blue;
        outputCommandsQueue.offer(drawCommand);
    }

    /*
     * Draws a line segment between two points (x1,y1) and (x2,y2) 
     * with a specified stroke size and color (in RGB), specified 
     * in pixels relative to the upper left corner of the drawing buffer
     */
    public void commandDraw(int x1, int y1, int x2, int y2) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
            System.out.println("make a drawing buffer");
        }
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        g.setColor(tcc.getColor());

//        setStrokeState(newStrokeSize);
        g.setStroke(new BasicStroke(strokeSize));

        //colors in RGB
        String red = Integer.toString(tcc.getColor().getRed());
        String green = Integer.toString(tcc.getColor().getGreen());
        String blue = Integer.toString(tcc.getColor().getBlue());
        g.drawLine(x1, y1, x2, y2);
        //        System.out.println("Drawing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2 + " Stroke Size " + strokeSize + " R " + red + " G " + green + " B " + blue);

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        String drawCommand = whiteboard + " draw " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + strokeSize + " " + red + " " + green + " " + blue;
    }
    
    /*
     * Draw a white line between two points (x1, y1) and (x2, y2), specified in
     * pixels relative to the upper-left corner of the drawing buffer.
     */
    public void commandErase(int x1, int y1, int x2, int y2) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        g.setColor(Color.WHITE);
//        setStrokeState(newStrokeSize);

        g.setStroke(new BasicStroke(strokeSize));
        g.drawLine(x1, y1, x2, y2);
        //        System.out.println("Erasing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2);
        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        String eraseCommand = whiteboard + " " + "erase" +  " " + x1 + " " + y1 + " " + x2 + " " + y2;
    }
    
    /*
     * Draw a white line between two points (x1, y1) and (x2, y2), specified in
     * pixels relative to the upper-left corner of the drawing buffer.
     */
    public void eraseLineSegment(int x1, int y1, int x2, int y2) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        g.setColor(Color.WHITE);
//        setStrokeState(newStrokeSize);

        g.setStroke(new BasicStroke(strokeSize));
        g.drawLine(x1, y1, x2, y2);
        //        System.out.println("Erasing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2);
        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        String eraseCommand = whiteboard + " " + "erase" +  " " + x1 + " " + y1 + " " + x2 + " " + y2;
        outputCommandsQueue.offer(eraseCommand);
    }

    /*
     * Add the mouse listener that supports the user's freehand drawing.
     */
    private void addDrawingController() {
        DrawingController controller = new DrawingController();
        addMouseListener(controller);
        addMouseMotionListener(controller);
    }

    /*
     * DrawingController handles the user's freehand drawing.
     */
    private class DrawingController implements MouseListener, MouseMotionListener {
        // store the coordinates of the last mouse event, so we can
        // draw a line segment from that last point to the point of the next mouse event.
        private int lastX, lastY; 

        /*
         * When left mouse button is pressed down, start drawing.
         */
        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
            if (!drawMode){
                eraseLineSegment(lastX,lastY, lastX ,lastY);
            } else{
                drawLineSegment(lastX, lastY, lastX, lastY);
            }
        }
        

        /*
         * When mouse moves while a button is pressed down,
         * draw a line segment.
         */
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if (!drawMode){
                eraseLineSegment(lastX,lastY, x ,y);
            } else{
                drawLineSegment(lastX, lastY, x, y);
            }
            lastX = x;
            lastY = y;
        }

        // Ignore all these other mouse events.
        public void mouseMoved(MouseEvent e) { }
        public void mouseClicked(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
    }

    /*
     * Main program. Make a window containing a Canvas.
     */
    public void makeCanvas(final int x, final int y, final WhiteboardClient canvas) {
        // set up the UI (on the event-handling thread)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame window = new JFrame(clientName);
                window.setState(java.awt.Frame.NORMAL);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setLayout(new BorderLayout());
                window.add(canvas, BorderLayout.CENTER);
                ButtonPanel buttonPanel = new ButtonPanel(x, 50, canvas);
                window.add(buttonPanel, BorderLayout.SOUTH);
                window.pack();
                window.setVisible(true);
                //TODO: need a panel that displays the other users working on the same whiteboard.
            }
        });
    }


    /*
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        WhiteboardClient client = new WhiteboardClient(800,600);
        client.makeCanvas(800,600,client); // TODO: what is this? should not be passing self into own method can just use this...
    }

}
