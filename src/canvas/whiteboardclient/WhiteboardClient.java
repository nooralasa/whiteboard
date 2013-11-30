package canvas.whiteboardclient;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import canvas.ButtonPanel;

/**
 * Canvas represents a drawing surface that allows the user to draw
 * on it freehand, with the mouse.
 */
public class WhiteboardClient extends JPanel {
    // image where the user's drawing is stored
    private Image drawingBuffer;
    //whiteboard client number
    private int client;
    private JColorChooser tcc = new JColorChooser(Color.BLACK);
    private String clientName;
    private String chosenWhiteboard;
   
    public boolean drawMode;

    /**
     * Make a canvas.
     * @param width width in pixels
     * @param height height in pixels
     */
    public WhiteboardClient(int width, int height, int client) {
        this.setPreferredSize(new Dimension(width, height));
        addDrawingController();
        this.client = client;
        // note: we can't call makeDrawingBuffer here, because it only
        // works *after* this canvas has been added to a window.  Have to
        // wait until paintComponent() is first called.
        drawMode = false;
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        // If this is the first time paintComponent() is being called,
        // make our drawing buffer.
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }

        // Copy the drawing buffer to the screen.
        g.drawImage(drawingBuffer, 0, 0, null);
    }

    /*
     * Make the drawing buffer and draw some starting content for it.
     */
    private void makeDrawingBuffer() {
        drawingBuffer = createImage(getWidth(), getHeight());
        fillWithWhite();
        drawSmile();
    }

    /*
     * Make the drawing buffer entirely white.
     */
    private void fillWithWhite() {
              
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0,  0,  getWidth(), getHeight());

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }

    /*
     * Draw a happy smile on the drawing buffer.
     */
    private void drawSmile() {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        // all positions and sizes below are in pixels
        final Rectangle smileBox = new Rectangle(20, 20, 100, 100); // x, y, width, height
        final Point smileCenter = new Point(smileBox.x + smileBox.width/2, smileBox.y + smileBox.height/2);
        final int smileStrokeWidth = 3;
        final Dimension eyeSize = new Dimension(9, 9);
        final Dimension eyeOffset = new Dimension(smileBox.width/6, smileBox.height/6);

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(smileStrokeWidth));

        // draw the smile -- an arc inscribed in smileBox, starting at -30 degrees (southeast)
        // and covering 120 degrees
        g.drawArc(smileBox.x, smileBox.y, smileBox.width, smileBox.height, -30, -120);
        
        // draw some eyes to make it look like a smile rather than an arc
        for (int side: new int[] { -1, 1 }) {
            g.fillOval(smileCenter.x + side * eyeOffset.width - eyeSize.width/2,
                    smileCenter.y - eyeOffset.height - eyeSize.width/2,
                    eyeSize.width,
                    eyeSize.height);
        }

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
    }

    /*
     * Draw a line between two points (x1, y1) and (x2, y2), specified in
     * pixels relative to the upper-left corner of the drawing buffer.
     */
    public String drawLineSegment(int x1, int y1, int x2, int y2) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
            System.out.println("make a drawing buffer");
        }
        
        System.out.println("here");
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        
        g.setColor(tcc.getColor());


        g.drawLine(x1, y1, x2, y2);
        System.out.println("Drawing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2);

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        String returnString = client + " " + "draw" +  " " + x1 + " " + y1 + " " + x2 + " " + y2;

        return returnString;
    }
    
    /*
     * Draw a line between two points (x1, y1) and (x2, y2), specified in
     * pixels relative to the upper-left corner of the drawing buffer.
     */
    public String eraseLineSegment(int x1, int y1, int x2, int y2) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        g.setColor(Color.WHITE);
        g.drawLine(x1, y1, x2, y2);
        System.out.println("Erasing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2);

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        String returnString = client + " " + "erase" +  " " + x1 + " " + y1 + " " + x2 + " " + y2;

        return returnString;
    }
    
    /*
     * Add the mouse listener that supports the user's freehand drawing.
     */
    private void addDrawingController() {
        DrawingController controller = new DrawingController();
        addMouseListener(controller);
        addMouseMotionListener(controller);
    }

    /*
     * DrawingController handles the user's freehand drawing.
     */
    private class DrawingController implements MouseListener, MouseMotionListener {
        // store the coordinates of the last mouse event, so we can
        // draw a line segment from that last point to the point of the next mouse event.
        private int lastX, lastY; 

        /*
         * When mouse button is pressed down, start drawing.
         */
        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
        }

        /*
         * When mouse moves while a button is pressed down,
         * draw a line segment.
         */
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if(!drawMode){
                eraseLineSegment(lastX,lastY, x ,y);
            }else{
                drawLineSegment(lastX, lastY, x, y);
            }
            lastX = x;
            lastY = y;
        }

        // Ignore all these other mouse events.
        public void mouseMoved(MouseEvent e) { }
        public void mouseClicked(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
    }

    /*
     * Main program. Make a window containing a Canvas.
     */
    public void makeCanvas(final int x, final int y, final WhiteboardClient canvas) {
        // set up the UI (on the event-handling thread)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println("Running this make Canvas method");
                JFrame window = new JFrame("Freehand Canvas");
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setLayout(new BorderLayout());
                window.add(canvas, BorderLayout.CENTER);
                window.add(tcc, BorderLayout.PAGE_START);
                ButtonPanel buttonPanel = new ButtonPanel(x, 50, canvas);
                window.add(buttonPanel, BorderLayout.SOUTH);
                window.pack();
                window.setVisible(true);
                System.out.println("Finished this make canvas method");
            }
        });
    }
    
    public String helpMessage() {
        // TODO Auto-generated method stub
        return "help";
    }

    public String getBoardMessage() {
        // TODO Auto-generated method stub
        return "look";
    }

    /*
     * Main program. Make a window containing a Canvas.
     */
    public static void main(String[] args) {
        WhiteboardClient canvas = new WhiteboardClient(800,600,1);
        canvas.makeCanvas(800,600,canvas);

    }
}