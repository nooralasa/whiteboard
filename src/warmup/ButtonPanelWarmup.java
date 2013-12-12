package warmup;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * ButtonPanel represents the buttons panel in the Collaborative Whiteboards GUI.
 * It supports the GUI for accessing the toolkit provided for the clients to make changes  to the Canvas.
 */
public class ButtonPanelWarmup extends JPanel {
    private JButton drawButton;
    private final JLabel strokeState;    
    private CanvasWarmUp canvas;
    /**
     * Button panel for the GUI containing the Draw button, Erase button, Help button, and Choose Color button.
     * Also contains a slider to select strokeSize. Contains appropriate labels
     * @param int width
     * @param int height
     * @param WhiteboardGUI whiteboard
     */

    public ButtonPanelWarmup (int width, int height, final CanvasWarmUp canvas){
        this.canvas = canvas;
        this.setPreferredSize(new Dimension(width, height));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Draw Button
        drawButton = new JButton();
        drawButton.setName("drawButton");
        drawButton.setText("Erase");
        drawButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    //changes between draw modes - this changes the label of strokeState to reflect the draw state
                    if (canvas.drawMode){
                        canvas.drawMode = false;
                        drawButton.setText("Draw");
                        strokeState.setText("Stroke State: Erase");
                    } else{
                        canvas.drawMode = true;
                        drawButton.setText("Erase");
                        strokeState.setText("Stroke State: Draw");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });


        //Stroke State Label, displays whether in draw or erase
        strokeState = new JLabel();
        strokeState.setName("strokeState");
        strokeState.setText("Stroke State: Draw");

        //layout
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(drawButton)
                .addComponent(strokeState)
 
                );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(drawButton)
                        .addComponent(strokeState))


                );
    }

}
