package client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.swing.JColorChooser;
import javax.swing.JPanel;


/**
 * Canvas represents a drawing surface that allows the user to draw
 * on it freehand, with the mouse.
 */
public class Canvas extends JPanel{
    // Image storing the whiteboard
    private Image drawingBuffer;
    protected boolean drawMode;
    private int strokeSize;
    private final JColorChooser tcc = new JColorChooser(Color.BLACK);
    private final JColorChooser serverTcc = new JColorChooser(Color.BLACK);
    protected String whiteboardName = "Board1";
    protected final BlockingQueue<String> outputCommandsQueue;
    private int width;
    private int height;

    /**
     * Make a canvas.
     * @param width width in pixels
     * @param height height in pixels
     */
    public Canvas (int canvasWidth, int canvasHeight, BlockingQueue<String> queue) {
        width = canvasWidth;
        height = canvasHeight;
        this.setPreferredSize(new Dimension(width, height));
        addDrawingController();
        drawMode = true;
        outputCommandsQueue = queue;
        checkRep();
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
        // If this is the first time paintComponent() is being called,
        // make our drawing buffer.
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        // Copy the drawing buffer to the screen.
        g.drawImage(drawingBuffer, 0, 0, null);
        checkRep();
    }

    /*
     * Make the drawing buffer.
     */
    private void makeDrawingBuffer() {
        drawingBuffer = createImage(getWidth(), getHeight());
        fillWithWhite();
    }

    /**
     * Returns the tcc.
     * @return
     */
    protected JColorChooser getTcc(){
        return tcc;
    }
    
    
    protected void setWhiteboardName(String newName){
        whiteboardName = newName;
    }
    
    /*
     * Make the drawing buffer entirely white.
     */
    protected void fillWithWhite() {
        final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0,  0,  getWidth(), getHeight());

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        checkRep();
    }

    /*
     * Draw line/stroke segment size
     */
    protected void setStrokeState(int value) {
        this.strokeSize = value;
        checkRep();
    }

    /*
     * Draw a line between two points (x1, y1) and (x2, y2), specified in
     * pixels relative to the upper-left corner of the drawing buffer.
     */
    protected void drawLineSegment(int x1, int y1, int x2, int y2) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
            System.out.println("make a drawing buffer");
        }
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        g.setColor(tcc.getColor());
        g.setStroke(new BasicStroke(strokeSize));

        //colors in RGB
        String red = Integer.toString(tcc.getColor().getRed());
        String green = Integer.toString(tcc.getColor().getGreen());
        String blue = Integer.toString(tcc.getColor().getBlue());

        g.drawLine(x1, y1, x2, y2);
        //        System.out.println("Drawing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2 + " Stroke Size " + strokeSize + " R " + red + " G " + green + " B " + blue);

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        String drawCommand = whiteboardName + " draw " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + strokeSize + " " + red + " " + green + " " + blue;

        outputCommandsQueue.offer(drawCommand);
        checkRep();
    }

    /*
     * Draw a white line between two points (x1, y1) and (x2, y2), specified in
     * pixels relative to the upper-left corner of the drawing buffer.
     */
    protected void eraseLineSegment(int x1, int y1, int x2, int y2) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(strokeSize));
        g.drawLine(x1, y1, x2, y2);
        //        System.out.println("Erasing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2);
        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        String eraseCommand = whiteboardName + " " + "erase" +  " " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + strokeSize;
        outputCommandsQueue.offer(eraseCommand);
        checkRep();
    }

    /*
     * Draws a line segment between two points (x1,y1) and (x2,y2) 
     * with a specified stroke size and color (in RGB), specified 
     * in pixels relative to the upper left corner of the drawing buffer
     */
    protected void commandDraw(int x1, int y1, int x2, int y2, int currentStrokeSize, String red, String green, String blue) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

        g.setStroke(new BasicStroke(currentStrokeSize));
        int redValue = Integer.parseInt(red);
        int greenValue = Integer.parseInt(green);
        int blueValue = Integer.parseInt(blue);
        serverTcc.setColor(redValue, greenValue, blueValue);
        g.setColor(serverTcc.getColor());
        //colors in RGB

        g.drawLine(x1, y1, x2, y2);
        //        System.out.println("Drawing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2 + " Stroke Size " + strokeSize + " R " + red + " G " + green + " B " + blue);

        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        checkRep();
    }

    /*
     * Draw a white line between two points (x1, y1) and (x2, y2), specified in
     * pixels relative to the upper-left corner of the drawing buffer.
     */
    protected void commandErase(int x1, int y1, int x2, int y2, int newStroke) {
        if (drawingBuffer == null) {
            makeDrawingBuffer();
        }
        Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(newStroke));
        g.drawLine(x1, y1, x2, y2);
        //        System.out.println("Erasing line x1 " + x1 + " y1 " + y1 + " x2 " + x2 + " y2 " + y2);
        // IMPORTANT!  every time we draw on the internal drawing buffer, we
        // have to notify Swing to repaint this component on the screen.
        this.repaint();
        checkRep();
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
         * When left mouse button is pressed down, start drawing.
         */
        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
            if (!drawMode){
                eraseLineSegment(lastX,lastY, lastX ,lastY);
            } else{
                drawLineSegment(lastX, lastY, lastX, lastY);
            }
        }

        /*
         * When mouse moves while a button is pressed down,
         * draw a line segment.
         */
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if (!drawMode){
                eraseLineSegment(lastX,lastY, x ,y);
            } else{
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

    /**
     * Checks that the rep invariant is maintained:
     *      drawMode is true or false
     *      height is greater than 0
     *      width is greater than 0
     *      strokeSize is greater than or equal to 0
     *      tcc is not null
     *      tcc has valid red, green and blue values
     *      serverTcc is not null
     *      serverTcc has valid red, green and blue values
     *      whiteboard name is not null
     *      whiteboard name is not an empty string
     *      whiteboard name does not contain any spaces
     *      outputCommandsQueue is not null
     *      outputCommandsQueue length is 0 or more
     */
    private void checkRep(){
        assert (drawMode || !drawMode);
        assert (height > 0);
        assert (width > 0);
        assert (strokeSize >= 0);
        assert (tcc != null);
        assert ((tcc.getColor().getRed() >= 0) && (tcc.getColor().getRed() <= 255));
        assert ((tcc.getColor().getGreen() >= 0) && (tcc.getColor().getGreen() <= 255));
        assert ((tcc.getColor().getBlue() >= 0) && (tcc.getColor().getBlue() <= 255));
        assert (tcc == serverTcc);
        assert (serverTcc != null);
        assert ((serverTcc.getColor().getRed() >= 0) && (serverTcc.getColor().getRed() <= 255));
        assert ((serverTcc.getColor().getGreen() >= 0) && (serverTcc.getColor().getGreen() <= 255));
        assert ((serverTcc.getColor().getBlue() >= 0) && (serverTcc.getColor().getBlue() <= 255));
        assert (whiteboardName != null);
        assert (!whiteboardName.equals(""));
        assert (!whiteboardName.contains(" "));
        assert (outputCommandsQueue != null);
        assert (outputCommandsQueue.size() > -1);
    }
}