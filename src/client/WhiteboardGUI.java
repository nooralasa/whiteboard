package client;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;


/**
 * Whiteboard GUI represents the graphical user interface for the collaborative whiteboards.
 */
public class WhiteboardGUI extends JFrame {
    protected Canvas canvas;
    private ButtonPanel buttonPanel;
    private SidePanel sidePanel;
    protected String clientName;
    private final List<String> existingWhiteboards;
    protected final BlockingQueue<String> outputCommandsQueue;

    /**
     * 
     * WhiteboardGUI controls the client's display. Stores the client's username, existing whiteboards in server list, and the current whiteboard being 
     * used by the client. Makes use of the Button and Side panels in the GUI. The GUI controls the drawing on the canvas.
     * Also outputs commands when drawing/erasing/etc to the server
     * @param width width in pixels
     * @param height height in pixels
     * @param outputCommandsQueue
     */
    public WhiteboardGUI(int width, int height, BlockingQueue<String> outputCommandsQueue) {
        this.outputCommandsQueue = outputCommandsQueue;
        this.canvas = new Canvas(width,height, outputCommandsQueue);
        this.buttonPanel = new ButtonPanel(width, 50, this);
        this.sidePanel = new SidePanel(250, height, this);
        this.existingWhiteboards = Collections.synchronizedList(new ArrayList<String>());
    }

    /**
     * Updates the title of the JFrame GUI
     */
    protected void updateTitle(String currentBoard) {
        this.setTitle(clientName + " working on " + currentBoard);
    }

    /**
     * Creates a new window displaying the GUI - canvas, buttonPanel,sidePanel,etc
     */
    protected void createWindow(String clientName) {
        setState(java.awt.Frame.NORMAL);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(sidePanel, BorderLayout.EAST);
        pack();
    }

    /**
     * Getter method for existingWhiteboards
     */
    protected List<String> getExistingWhiteboards() {
        return existingWhiteboards;
    }

    /**
     * Getter method for sidePanel
     */
    protected SidePanel getSidePanel(){
        return sidePanel;
    }

    /**
     * Getter method for buttonPanel
     */
    protected ButtonPanel getButtonPanel(){
        return buttonPanel;
    }

    /**
     * Getter method for Canvas
     */
    protected Canvas getCanvas() {
        return canvas;
    }

    /**
     * Gets the desired username from the user, checking against the existing usernames. Usernames
     * CANNOT contain spaces or just be an empty string
     * @param String message represents the special message to attach depending on the situation
     */
    protected String getUsername(String message){
        final JFrame popup = new JFrame(); // Popup asking for Username
        Object[] possibilities = null;
        popup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        String desiredClientName = (String) JOptionPane.showInputDialog(popup, message + "Input your desired username:", "Username", JOptionPane.PLAIN_MESSAGE, null, possibilities, "username");

        while (desiredClientName.equals("") || desiredClientName.contains(" ")|| desiredClientName.equals(null)) {

            String noSpaceMessage = "Please enter a username with no spaces composed of at least 1 alphanumeric character \n";
            desiredClientName = (String) JOptionPane.showInputDialog(popup, noSpaceMessage + "Please input a valid username:", "Username", JOptionPane.PLAIN_MESSAGE, null, possibilities, "default");

        }
        this.clientName = desiredClientName;
        return "new username " + desiredClientName;
    }

    /**
     * Lets the user choose their whiteboard. Popups a Jframe that has a textfield to input the desired 
     */
    protected void chooseWhiteboardPopup(){
        JFrame popup = new JFrame(); // Popup asking for Whiteboard Name
        Object[] possibilities = null;
        String whiteboardNames = "Existing Whiteboards: ";
        for (String name : existingWhiteboards){
            whiteboardNames += name + " ";
        }
        whiteboardNames += "\n";   
        String message = "Enter the name of an existing whiteboard or type in a new whiteboard name";
        String desiredWhiteboardName = (String) JOptionPane.showInputDialog(popup, whiteboardNames + message, "Whiteboard Name", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        
        while (desiredWhiteboardName.equals("") || desiredWhiteboardName.contains(" ")|| desiredWhiteboardName.equals(null)) {
            String noSpaceMessage = "Please enter a whiteboard name with no spaces composed of at least 1 character \n";
            desiredWhiteboardName = (String) JOptionPane.showInputDialog(popup, whiteboardNames + message + "\n" + noSpaceMessage + "Please input a valid whiteboard name:", "Whiteboard Name", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
        }
        
        sidePanel.selectedWhiteboard = desiredWhiteboardName;
        this.updateTitle(desiredWhiteboardName);
        if (existingWhiteboards.contains(desiredWhiteboardName)){
            outputCommandsQueue.offer(clientName + " selectBoard " + desiredWhiteboardName);
        } else{
            outputCommandsQueue.offer("addBoard " + desiredWhiteboardName);
        }
    }

    /**
     * Let's the user choose a new whiteboard.
     */
    protected void chooseNewWhiteboard(String desiredWhiteboardName){
        this.updateTitle(desiredWhiteboardName);
        canvas.fillWithWhite(); // Erases the Board
        if (existingWhiteboards.contains(desiredWhiteboardName)){
            outputCommandsQueue.offer(clientName + " selectBoard " + desiredWhiteboardName);
        }
    }

    /**
     * Pops up help box
     */
    protected void helpBox(){
        //default title and icon
        String helpMessage = "Collaborative Whiteboard Instructions \n\nDrawing and Erasing \n\n";
        helpMessage += "To draw, simply click and draw your mouse cursor across the screen. \nDo the same for erasing. \n";
        helpMessage += "You can view the state of the pen at the bottom of the interface.\n";
        helpMessage += "To change the color of the pen, click the color button and select the desired color from the color palette.\n";
        helpMessage += "To change the stroke size of the pen, drag the slider on the bottom of the interface to adjust the stroke size.\n\n";
        helpMessage += "Changing Whiteboards\n\n";
        helpMessage += "Select the desired whiteboard you wish to switch to in the list on the right denoted by Whiteboards in Server \nand click the Switch Whiteboards button.\n";
        helpMessage += "You can view the other users working on the same whiteboard as you in the list labeled Users in Whiteboard.\n";
        helpMessage += "You can logoff by clicking the X in the top left hand corner of the interface (right hand corner if you are using a PC).\n\n";
        helpMessage += "We hope you enjoy using the Collaborative Whiteboard.\n\n";
        helpMessage += "Developed by Erwin, Noor, and Vincent.";
        JOptionPane.showMessageDialog(this, helpMessage, "Help",
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Pops up color chooser
     */
    protected void colorChooser(){
        JFrame popupColor = new JFrame("Color Chooser");
        popupColor.add(canvas.getTcc(), BorderLayout.CENTER);
        popupColor.pack();
        popupColor.setVisible(true);
    }

    /**
     * Uses an actionlistener on the to disconnect the client from the server
     */
    protected void makeWhiteboard() {
        // set up the UI (on the event-handling thread)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        outputCommandsQueue.offer("Disconnect " + clientName);
                        setVisible(false);
                        dispose();
                    }
                });
                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                setVisible(true);
            }
        });
    }

    /**
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10000000);
        WhiteboardGUI client = new WhiteboardGUI(800,600,queue);
        client.createWindow("test");
        client.makeWhiteboard();
    }
}