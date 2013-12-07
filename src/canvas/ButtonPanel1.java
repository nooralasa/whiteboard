package canvas;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import canvas.Canvas;
import canvas.Canvas1;
import canvas.Whiteboard1;
import canvas.whiteboardclient.WhiteboardClient;

public class ButtonPanel1 extends JPanel {
    private JButton drawButton;
    private JButton helpButton;
    private JButton colorButton;
    private JSlider strokeSize;
    private JLabel strokeSizeLabel;
    private final JLabel strokeState;
    private Canvas1 canvas;
    private Canvas canvas2;
    private WhiteboardClient canvas3;
    private Whiteboard1 whiteboard;
    static final int SLIDER_MIN= 0;
    static final int SLIDER_MAX = 30;
    static final int SLIDER_INIT = 0; 


    public ButtonPanel1 (int width, int height, final Canvas1 canvas, final Whiteboard1 whiteboard){
        this.canvas = canvas;
        this.whiteboard = whiteboard;
        this.setPreferredSize(new Dimension(width, height));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        drawButton = new JButton();
        drawButton.setName("drawButton");
        drawButton.setText("Draw");
        drawButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
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
        helpButton = new JButton();
        helpButton.setName("helpButton");
        helpButton.setText("Help");
        helpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    whiteboard.helpBox();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        colorButton = new JButton();
        colorButton.setName("colorButton");
        colorButton.setText("Color");
        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    whiteboard.colorChooser();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
       
        strokeState = new JLabel();
        strokeState.setName("strokeState");
        strokeState.setText("Stroke State: Erase");
        
        strokeSizeLabel = new JLabel();
        strokeSizeLabel.setName("strokeSizeLabel");
        strokeSizeLabel.setText("Stroke Size");

      //Create the slider
        strokeSize = new JSlider(JSlider.HORIZONTAL,
                                              SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
        strokeSize.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                JSlider source =(JSlider) e.getSource();
                //only when not adjusting slider
                if (!source.getValueIsAdjusting()) {
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
        
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                
                .addComponent(drawButton)
                .addComponent(colorButton)
                .addComponent(helpButton)
                .addComponent(strokeState)
                .addGroup(layout.createParallelGroup()
                        .addComponent(strokeSize)
                        .addComponent(strokeSizeLabel))
                
                );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
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