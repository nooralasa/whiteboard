package canvas;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;

import canvas.whiteboardclient.WhiteboardClient;

public class SidePanel extends JPanel {
    private JScrollPane usersInWhiteboard;
    private JLabel usersInWhiteboardLabel;
    private JScrollPane whiteboardsInServer;
    private JLabel whiteboardsInServerLabel;
    private DefaultListModel<String> usersInWhiteboardListModel;
    private DefaultListModel<String> whiteboardsInServerListModel;
    private WhiteboardClient canvas;
    private JList usersInWhiteboardList;
    private JList whiteboardsInServerList;
    

    public SidePanel(int width, int height, WhiteboardClient workingCanvas){
        this.canvas = workingCanvas;
        this.setPreferredSize(new Dimension(width, height));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
   
        usersInWhiteboardLabel = new JLabel();
        usersInWhiteboardLabel.setName("usersInWhiteboardLabel");
        usersInWhiteboardLabel.setText("Users in Whiteboard");

        
        usersInWhiteboardListModel = new DefaultListModel<String>();
        usersInWhiteboardListModel.addElement("Jane Doe");
        usersInWhiteboardList = new JList<String>(usersInWhiteboardListModel);

        usersInWhiteboardList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        usersInWhiteboardList.setLayoutOrientation(JList.VERTICAL);
        usersInWhiteboardList.setVisibleRowCount(-1);
        usersInWhiteboard = new JScrollPane(usersInWhiteboardList);
        usersInWhiteboard.setPreferredSize(new Dimension(250, 80));
        
        whiteboardsInServerLabel = new JLabel();
        whiteboardsInServerLabel.setName("whiteboardsInServer");
        whiteboardsInServerLabel.setText("Whiteboards in Server");

        
        whiteboardsInServerListModel = new DefaultListModel<String>();
        whiteboardsInServerListModel.addElement("add elements whiteboards");
        whiteboardsInServerList = new JList<String>(whiteboardsInServerListModel);

        whiteboardsInServerList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        whiteboardsInServerList.setLayoutOrientation(JList.VERTICAL);
        whiteboardsInServerList.setVisibleRowCount(-1);
        whiteboardsInServer = new JScrollPane(usersInWhiteboardList);
        whiteboardsInServer.setPreferredSize(new Dimension(250, 80));

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(usersInWhiteboardLabel)
                        .addComponent(usersInWhiteboard)
                        .addComponent(whiteboardsInServerLabel)
                        .addComponent(whiteboardsInServer));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(usersInWhiteboardLabel)
                        .addComponent(usersInWhiteboard)
                        .addComponent(whiteboardsInServerLabel)
                        .addComponent(whiteboardsInServer));
    }
}
   
