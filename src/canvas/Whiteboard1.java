package canvas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import canvas.whiteboardclient.WhiteboardClient;
import canvas.whiteboardclient.WhiteboardClient1;

public class Whiteboard1 extends JFrame {
    
    public Canvas1 canvas;
    //private WhiteboardClient1 client;
    private ButtonPanel1 buttonPanel;
    private String clientName;
    private String whiteboard;
    private final List<String> existingWhiteboards;
    


    /**
     * Make a whiteboard.
     * @param width width in pixels
     * @param height height in pixels
     */
    public Whiteboard1(int width, int height, BlockingQueue<String> outputCommandsQueue, String newWhiteboard) {
       // this.client = new WhiteboardClient1(width, height);
        this.canvas = new Canvas1(width,height, outputCommandsQueue);
        this.buttonPanel = new ButtonPanel1(width, 50, canvas, this);
        this.existingWhiteboards = Collections.synchronizedList(new ArrayList<String>());
        
        getUsername("");

        setTitle(newWhiteboard);
        setState(java.awt.Frame.NORMAL);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }
    
    public  List<String> getExistingWhiteboards() {
        return existingWhiteboards;
    }
    
    public Canvas1 getCanvas() {
        return canvas;
    }
    
    public static void addClient(WhiteboardClient1 client) {
        //TODO add client
    }
    
    /**
     * Gets the username from the user.
     * @param message represents the special message to attach depending on the situation
     */
    public String getUsername(String message){
        JFrame popup = new JFrame(); // Popup asking for Username
        Object[] possibilities = null;
        String desiredClientName = (String) JOptionPane.showInputDialog(popup, message + "Input your desired username:", "Username", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        this.clientName = desiredClientName;
        return "new username " + desiredClientName;
        }
    
    /**
     * Lets the user choose their whiteboard.
     */
    public String chooseWhiteboard(){
        // all the names of the existing whiteboards are in the list of strings existingWhiteboards
        // should use a jcombo box or something to display the choices
        // could maybe `have a textfield similar to the getusername one to choose own.
        // MAKE SURE to set the selected whiteboard as this.whiteboard and then wipe the board
        // for now needs to be fixed by erwin
        JFrame popup = new JFrame(); // Popup asking for Whiteboard Name
        Object[] possibilities = null;
        String whiteboardNames = "Existing Whiteboards ";
        for (String name : existingWhiteboards){
            whiteboardNames += name + " ";
        }
        whiteboardNames += "\n";   
        String message = "Enter the name of an existing whiteboard or type in a new whiteboard name";
        String desiredWhiteboardName = (String) JOptionPane.showInputDialog(popup, whiteboardNames + message, "Whiteboard Name", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        this.whiteboard = desiredWhiteboardName;
        return desiredWhiteboardName;
    }
    
    
    /**
     * Creates a popup window to get a new whiteboard name from the user.
     * @param message represents the special message to attach depending on the situation
     */
    private String getNewWhiteboardName(String message){
        JFrame popup = new JFrame(); // Popup asking for Whiteboard Name
        Object[] possibilities = null;
        String desiredWhiteboardName = (String) JOptionPane.showInputDialog(popup, message + "Input your desired whiteboard name:", "Whiteboard Name", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        this.whiteboard = desiredWhiteboardName;
        return "addBoard " + desiredWhiteboardName;
        //client.outputCommandsQueue.offer("addBoard " + desiredWhiteboardName);
    }
    
    /**
     * Gets the Whiteboard Name from the user.
     * @param message represents the special message to attach depending on the situation
     */
    private String getWhiteboard(String message){
        JFrame popup = new JFrame(); // Popup asking for Whiteboard Name
        Object[] possibilities = null;
        String desiredWhiteboardName = (String) JOptionPane.showInputDialog(popup, message + "Input your desired whiteboard name:", "Whiteboard Name", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        this.whiteboard = desiredWhiteboardName;
        return "addBoard " + desiredWhiteboardName;
        //client.outputCommandsQueue.offer("addBoard " + desiredWhiteboardName); //TODO: same as 45
    }
    
    /**
     * Pops up help box
     */
    public void helpBox(){
        //default title and icon
        JOptionPane.showMessageDialog(this,
                "I'm guessing you need some help. Too bad.", "Help Message",
                JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Pops up color chooser
     */
    public void colorChooser(){
        JFrame popupColor = new JFrame("Color Chooser");
        popupColor.add(canvas.getTcc(), BorderLayout.CENTER);
        popupColor.pack();
        popupColor.setVisible(true);
    }
    
    /*
     * Main program. Make a window containing a Canvas.
     */
    public void makeWhiteboard() {
        // set up the UI (on the event-handling thread)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
            }
        });
    }
    
    /*
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10000000);
        Whiteboard1 client = new Whiteboard1(800,600,queue,"test");
        client.makeWhiteboard(); // TODO: what is this? should not be passing self into own method can just use this...
    }
    
    

    
    

}
