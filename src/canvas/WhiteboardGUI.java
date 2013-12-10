package canvas;

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
    public Canvas canvas;
    private ButtonPanel buttonPanel;
    private SidePanel sidePanel;
    public String clientName;
    private final List<String> existingWhiteboards;
    public final BlockingQueue<String> outputCommandsQueue;
    private String currentWhiteboard;
    private JFrame popupWhiteboard;


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
        this.popupWhiteboard =  new JFrame();
        popupWhiteboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    /**
     * Updates the title of the JFrame GUI
     */
    public void updateTitle(String currentBoard) {
        this.setTitle(clientName + " working on " + currentBoard);
    }
    
    /**
     * Creates a new window displaying the GUI - canvas, buttonPanel,sidePanel,etc
     */
    public void createWindow(String clientName) {
        setState(java.awt.Frame.NORMAL);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO: does this conflict with the other one later in the program
        setLayout(new BorderLayout());
        add(canvas, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(sidePanel, BorderLayout.EAST);
        pack();
    }

    /**
     * Getter method for existingWhiteboards
     */
    
    public List<String> getExistingWhiteboards() {
        return existingWhiteboards;
    }

    /**
     * Getter method for sidePanel
     */
    public SidePanel getSidePanel(){
        return sidePanel;
    }

    /**
     * Getter method for buttonPanel
     */
    
    public ButtonPanel getButtonPanel(){
        return buttonPanel;
    }
    
    /**
     * Getter method for Canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Gets the desired username from the user, checking against the existing usernames. Usernames
     * CANNOT contain spaces or just be an empty string
     * @param String message represents the special message to attach depending on the situation
     */
    
    public String getUsername(String message){
        final JFrame popup = new JFrame(); // Popup asking for Username
        Object[] possibilities = null;
        popup.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        String desiredClientName = (String) JOptionPane.showInputDialog(popup, message + "Input your desired username:", "Username", JOptionPane.PLAIN_MESSAGE, null, possibilities, "username");

        while (desiredClientName.equals("") || desiredClientName.contains(" ")|| desiredClientName.equals(null)) {
            String noSpaceMessage = "Please enter a username with no spaces composed of at least 1 alphanumeric character \n";
            desiredClientName = (String) JOptionPane.showInputDialog(popup, noSpaceMessage + "Please input a valid username:", "Username", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");

        }
        this.clientName = desiredClientName;
        return "new username " + desiredClientName;
    }

    /**
     * Lets the user choose their whiteboard. Popups a Jframe that has a textfield to input the desired 
     */
    public void chooseWhiteboardPopup(){
        // all the names of the existing whiteboards are in the list of strings existingWhiteboards
        // should use a jcombo box or something to display the choices
        // could maybe `have a textfield similar to the getusername one to choose own.
        // MAKE SURE to set the selected whiteboard as this.whiteboard and then wipe the board
        // for now needs to be fixed by erwin

           //converts to an array
           String[] whiteboardsInServerArray = new String[existingWhiteboards.size()];
           for(int i = 0; i < existingWhiteboards.size(); i++){
               whiteboardsInServerArray[i] = existingWhiteboards.get(i);
           }
           WhiteboardListPopup p = new WhiteboardListPopup(300,300, whiteboardsInServerArray,this);
           /*
            Object[] possibilities = null;
            String whiteboardNames = "Existing Whiteboards ";
    
            whiteboardNames += "\n";   
            String message = "Enter the name of an existing whiteboard or type in a new whiteboard name";
            String desiredWhiteboardName = (String) JOptionPane.showInputDialog(popup, whiteboardNames + message, "Whiteboard Name", JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
            */
           
           //Lay out everything.
           popupWhiteboard.setState(java.awt.Frame.NORMAL);
           popupWhiteboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // TODO: does this conflict with the other one later in the program
           popupWhiteboard.add(p);
           popupWhiteboard.pack();
           popupWhiteboard.setVisible(true);

    }
    
    public void chooseWhiteboard(String desiredName){
        popupWhiteboard.dispose();
        this.updateTitle(desiredName);
        this.currentWhiteboard = desiredName;
     
         
         if (existingWhiteboards.contains(desiredName)){
             outputCommandsQueue.offer(clientName + " selectBoard " + desiredName);
         } else{
             outputCommandsQueue.offer("addBoard " + desiredName);
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

    /**
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

    /**
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10000000);
        WhiteboardGUI client = new WhiteboardGUI(800,600,queue);
        client.createWindow("test"); // TODO: this needs to be updated with the username correctly
        client.makeWhiteboard(); //TODO: needs a more descriptive name (not accurate at all)
    }
}