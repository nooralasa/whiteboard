package client;

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
public class ButtonPanel extends JPanel {
    private JButton drawButton;
    private JButton helpButton;
    private JButton colorButton;
    private JSlider strokeSize;
    private JLabel strokeSizeLabel;
    private final JLabel strokeState;    
    private WhiteboardGUI whiteboard;
    private Canvas canvas;
    static final int SLIDER_MIN= 0;
    static final int SLIDER_MAX = 30;
    static final int SLIDER_INIT = 0; 
    /**
     * Button panel for the GUI containing the Draw button, Erase button, Help button, and Choose Color button.
     * Also contains a slider to select strokeSize. Contains appropriate labels
     * @param int width
     * @param int height
     * @param WhiteboardGUI whiteboard
     */

    public ButtonPanel (int width, int height, final WhiteboardGUI whiteboard){
        this.whiteboard = whiteboard;
        this.canvas = whiteboard.canvas;
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

        // Help Button
        helpButton = new JButton();
        helpButton.setName("helpButton");
        helpButton.setText("Help");
        helpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    //calls the help message
                    whiteboard.helpBox();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        // Color Button
        colorButton = new JButton();
        colorButton.setName("colorButton");
        colorButton.setText("Color");
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    //pops up the color chooser (clicking color button) in the whiteboardGUI
                    whiteboard.colorChooser();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //Stroke State Label, displays whether in draw or erase
        strokeState = new JLabel();
        strokeState.setName("strokeState");
        strokeState.setText("Stroke State: Draw");

        //Stroke size label
        strokeSizeLabel = new JLabel();
        strokeSizeLabel.setName("strokeSizeLabel");
        strokeSizeLabel.setText("Stroke Size");

        // Creates the slider for stroke size
        strokeSize = new JSlider(JSlider.HORIZONTAL,
                SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
        strokeSize.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                JSlider source =(JSlider) e.getSource();
                //only when not adjusting slider
                if (!source.getValueIsAdjusting()) {
                    //calls the method in canvas to set the stroke size
                    canvas.setStrokeState(source.getValue());
                }
            }
        });
        strokeSize.setMajorTickSpacing(5);
        strokeSize.setMinorTickSpacing(1);
        strokeSize.setPaintTicks(true);
        strokeSize.setPaintLabels(true);
        Font font = new Font("Serif", Font.ITALIC, 15);
        strokeSize.setFont(font);

        //layout
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(drawButton)
                .addComponent(colorButton)
                .addComponent(helpButton)
                .addComponent(strokeState)
                .addGroup(layout.createParallelGroup()
                        .addComponent(strokeSize)
                        .addComponent(strokeSizeLabel))

                );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(colorButton)
                        .addComponent(drawButton)
                        .addComponent(helpButton)
                        .addComponent(strokeState)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(strokeSizeLabel) 
                                .addComponent(strokeSize)))

                );
    }

}
