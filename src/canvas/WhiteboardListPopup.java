package canvas;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class WhiteboardListPopup extends JPanel{
    private String desiredWhiteboardName;
    private String desiredWhiteboardPick;
    private JList whiteboardsInServerList;
    private JLabel whiteboardLabel1;
    private JLabel whiteboardLabel2;
    private JTextField addWhiteboardText;
    private JButton chooseWhiteboard;
    
    public WhiteboardListPopup(int width, int height, String[] whiteboardsInServerArray,final WhiteboardGUI whiteboard) {
        this.setPreferredSize(new Dimension(width, height));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        whiteboardLabel1 = new JLabel("Create a new whiteboard or");
        whiteboardLabel2 = new JLabel("select one from the list:");


        whiteboardsInServerList = new JList<String>(whiteboardsInServerArray);
        whiteboardsInServerList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        // Action Listener for when a whiteboard is selected in the JList
        whiteboardsInServerList.setSelectedIndex(0);
        
        whiteboardsInServerList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    desiredWhiteboardPick = (String) whiteboardsInServerList.getSelectedValue();
                }
            }
        });
        addWhiteboardText = new JTextField();
        addWhiteboardText.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                String text = addWhiteboardText.getText();
                if(text != null && !(text.contains(" ") && !(text.equals("")))){
                    desiredWhiteboardName = text;
                    whiteboard.chooseNewWhiteboard(desiredWhiteboardName);
                }
            }
        });
        //creates the button to switch to a new whiteboard
        chooseWhiteboard = new JButton();
        chooseWhiteboard.setName("chooseWhiteboard");
        chooseWhiteboard.setText("Choose Whiteboard");
        chooseWhiteboard.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    addWhiteboardText.setText(desiredWhiteboardPick);
                    desiredWhiteboardName = desiredWhiteboardPick;
                    whiteboard.chooseNewWhiteboard(desiredWhiteboardName);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        // Layout

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(whiteboardLabel1)
                .addComponent(addWhiteboardText)
                .addComponent(whiteboardLabel2)
                .addComponent(whiteboardsInServerList)
                .addComponent(chooseWhiteboard));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(whiteboardLabel1)
                .addComponent(addWhiteboardText)
                .addComponent(whiteboardLabel2)
                .addComponent(whiteboardsInServerList)
                .addComponent(chooseWhiteboard));

    }
    public String getDesiredName(){
        return desiredWhiteboardPick;
    }
}
