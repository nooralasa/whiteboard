package canvas;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SidePanel extends JPanel {
    private JScrollPane usersInWhiteboard;
    private JLabel usersInWhiteboardLabel;
    private JScrollPane whiteboardsInServer;
    private JLabel whiteboardsInServerLabel;
    private DefaultListModel<String> usersInWhiteboardListModel;
    public DefaultListModel<String> whiteboardsInServerListModel;
    private JList<String> usersInWhiteboardList;
    private JList<String> whiteboardsInServerList;
    private JButton selectWhiteboard;
    private String selectedWhiteboard;

    public SidePanel(int width, int height, final WhiteboardGUI whiteboard){
        this.setPreferredSize(new Dimension(width, height));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Creates the Label for the Same Users in the Whiteboard
        usersInWhiteboardLabel = new JLabel();
        usersInWhiteboardLabel.setName("usersInWhiteboardLabel");
        usersInWhiteboardLabel.setText("Users in Whiteboard");

        // Creates the JList for the Same Users in the Whiteboard
        usersInWhiteboardListModel = new DefaultListModel<String>();
        usersInWhiteboardList = new JList<String>(usersInWhiteboardListModel);
        usersInWhiteboardList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        usersInWhiteboardList.setLayoutOrientation(JList.VERTICAL);
        usersInWhiteboardList.setVisibleRowCount(-1);
        usersInWhiteboard = new JScrollPane(usersInWhiteboardList);
        usersInWhiteboard.setPreferredSize(new Dimension(250, 80)); //TODO: should be set from height and width

        // Creates the Label for the Whiteboards on the Server
        whiteboardsInServerLabel = new JLabel();
        whiteboardsInServerLabel.setName("whiteboardsInServer");
        whiteboardsInServerLabel.setText("Whiteboards in Server");
        // Creates the JList for the Whiteboards on the Server
        whiteboardsInServerListModel = new DefaultListModel<String>();
        whiteboardsInServerList = new JList<String>(whiteboardsInServerListModel);
        whiteboardsInServerList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        whiteboardsInServerList.setLayoutOrientation(JList.VERTICAL);
        whiteboardsInServerList.setVisibleRowCount(-1);
        whiteboardsInServerList.addListSelectionListener(new ListSelectionListener() { 
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    selectedWhiteboard = (String) whiteboardsInServerList.getSelectedValue();
                }
            }
        });
        whiteboardsInServer = new JScrollPane(whiteboardsInServerList);
        whiteboardsInServer.setPreferredSize(new Dimension(250, 80));
   
        selectWhiteboard = new JButton();
        selectWhiteboard.setName("selectWhiteboard");
        selectWhiteboard.setText("Select Whiteboard");
        selectWhiteboard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    whiteboard.chooseNewWhiteboard(selectedWhiteboard);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(usersInWhiteboardLabel)
                .addComponent(usersInWhiteboard)
                .addComponent(whiteboardsInServerLabel)
                .addComponent(whiteboardsInServer)
                .addComponent(selectWhiteboard));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(usersInWhiteboardLabel)
                .addComponent(usersInWhiteboard)
                .addComponent(whiteboardsInServerLabel)
                .addComponent(whiteboardsInServer)
                .addComponent(selectWhiteboard));
    }

    /**
     * Updates the JList with the whiteboards
     * @param whiteboards whiteboards to display in the JList
     */
    public void updateWhiteboardsList(List<String> whiteboards){
        whiteboardsInServerListModel.removeAllElements();
        for (String whiteboard : whiteboards){
            whiteboardsInServerListModel.addElement(whiteboard);
        }
    }
    
    /**
     * Updates the JList with the clients.
     * @param clients clients to display in the JList
     */
    public void updateClientsList(List<String> clients){
        usersInWhiteboardListModel.removeAllElements();
        for (String client : clients){
            usersInWhiteboardListModel.addElement(client);
        }
    }
}