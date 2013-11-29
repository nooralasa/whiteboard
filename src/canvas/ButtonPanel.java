package canvas;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class ButtonPanel extends JPanel {
    private JButton drawButton;
    private final JLabel strokeState;
    private Whiteboard canvas;
    private Canvas canvas2;


    public ButtonPanel(int width, int height, Whiteboard workingCanvas){
        this.canvas = workingCanvas;
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

        strokeState = new JLabel();
        strokeState.setName("strokeState");
        strokeState.setText("Stroke State: Erase");

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addComponent(drawButton)
                .addComponent(strokeState)
                );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(drawButton)
                        .addComponent(strokeState))                        
                );
    }
    
    public ButtonPanel(int width, int height, Canvas workingCanvas){
        this.canvas2 = workingCanvas;
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
                    if (canvas2.drawMode){
                        canvas2.drawMode = false;
                        drawButton.setText("Draw");
                        strokeState.setText("Stroke State: Erase");
                    } else{
                        canvas2.drawMode = true;

                        drawButton.setText("Erase");
                        strokeState.setText("Stroke State: Draw");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        strokeState = new JLabel();
        strokeState.setName("strokeState");
        strokeState.setText("Stroke State: Erase");

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addComponent(drawButton)
                .addComponent(strokeState)
                );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(drawButton)
                        .addComponent(strokeState))                        
                );
    }


}
