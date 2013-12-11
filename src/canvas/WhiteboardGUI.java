package canvas;

import java.awt.BorderLayout;
import java.awt.Window;
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

public class WhiteboardGUI extends JFrame {
    public Canvas canvas;
    private ButtonPanel buttonPanel;
    private SidePanel sidePanel;
    public String clientName;
    private final List<String> existingWhiteboards;
    public final BlockingQueue<String> outputCommandsQueue;

    /**
     * Make a Whiteboard, which is //TODO: finish this
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
    
    public void updateTitle(String currentBoard) {
        this.setTitle(clientName + " working on " + currentBoard);
    }
    
    public void createWindow(String clientName) {
        setState(java.awt.Frame.NORMAL);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO: does this conflict with the other one later in the program
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(sidePanel, BorderLayout.EAST);
        pack();
    }

    public List<String> getExistingWhiteboards() {
        return existingWhiteboards;
    }

    public SidePanel getSidePanel(){
        return sidePanel;
    }

    public ButtonPanel getButtonPanel(){
        return buttonPanel;
    }
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Gets the username from the user.
     * @param message represents the special message to attach depending on the situation
     */
    public String getUsername(String message){
        final JFrame popup = new JFrame(); // Popup asking for Username
        Object[] possibilities = null;
        
        String desiredClientName = (String) JOptionPane.showInputDialog(popup, message + "Input your desired username:", "Username", JOptionPane.PLAIN_MESSAGE, null, possibilities, "username");

        while (desiredClientName.equals("") || desiredClientName.contains(" ")|| desiredClientName.equals(null)) {
            String noSpaceMessage = "Please enter a username with no spaces composed of at least 1 alphanumeric character \n";
            desiredClientName = (String) JOptionPane.showInputDialog(popup, noSpaceMessage + "Please input a valid username:", "Username", JOptionPane.PLAIN_MESSAGE, null, possibilities, "default");

        }
        this.clientName = desiredClientName;
        return "new username " + desiredClientName;
    }

    /**
     * Lets the user choose their whiteboard.
     */
    public void chooseWhiteboard(){
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
    public void chooseNewWhiteboard(String desiredWhiteboardName){
        this.updateTitle(desiredWhiteboardName);
        canvas.fillWithWhite(); // Erases the Board
        if (existingWhiteboards.contains(desiredWhiteboardName)){
            outputCommandsQueue.offer(clientName + " selectBoard " + desiredWhiteboardName);
        }
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

    /*
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10000000);
        WhiteboardGUI client = new WhiteboardGUI(800,600,queue);
        client.createWindow("test"); // TODO: this needs to be updated with the username correctly
        client.makeWhiteboard(); //TODO: needs a more descriptive name (not accurate at all)
    }
}