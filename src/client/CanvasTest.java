package client;
import static org.junit.Assert.*;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

/**
 * Canvas Test runs the JUnit tests for the Canvas class. 
 * The drawLineSegment() and eraseLineSegment() tests are tested.
 * test fillWithWhite() entire board
 * 
 **/
public class CanvasTest {
    @Test
    public void testDrawLineSegment() throws InterruptedException{
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        int x1 = 0;
        int y1 = 0;
        int x2 = 10;
        int y2 = 10;
        int red = 255;
        int blue = 255;
        int green = 255;
        int strokeSize = 0;
        canvas.drawLineSegment(x1, y1, x2, y2);
        String command = canvas.getCommandQueue().take();
        System.out.println(command);
        String expected = "Board1" + " draw " + Integer.toString(x1) + " " + Integer.toString(y1) + " " + Integer.toString(x2) + " " + Integer.toString(y2) + " " + Integer.toString(strokeSize) + " " + Integer.toString(red) + " " + Integer.toString(green) + " " + Integer.toString(blue);
        assertEquals(true, command.equals(expected));
        
        
    }
    @Test
    public void testCommandDraw() throws InterruptedException{
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        int x1 = 0;
        int y1 = 0;
        int x2 = 10;
        int y2 = 10;
        int red = 255;
        int blue = 255;
        int green = 255;
        int strokeSize = 0;
        canvas.drawLineSegment(x1, y1, x2, y2);
        String command = canvas.getCommandQueue().peek();
        System.out.println(command);
        String expected = "Board1" + " draw " + Integer.toString(x1) + " " + Integer.toString(y1) + " " + Integer.toString(x2) + " " + Integer.toString(y2) + " " + Integer.toString(strokeSize) + " " + Integer.toString(red) + " " + Integer.toString(green) + " " + Integer.toString(blue);
        assertEquals(true, command.equals(expected));
    }
    @Test
    public void testEraseLineSegment(){
        
    }
    @Test
    public void testCommandErase() throws InterruptedException{
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        int x1 = 0;
        int y1 = 0;
        int x2 = 10;
        int y2 = 10;
        int red = 255;
        int blue = 255;
        int green = 255;
        int strokeSize = 0;
        canvas.drawLineSegment(x1, y1, x2, y2);
        String command = canvas.getCommandQueue().take();
        System.out.println(command);
        String expected = "Board1" + " draw " + Integer.toString(x1) + " " + Integer.toString(y1) + " " + Integer.toString(x2) + " " + Integer.toString(y2) + " " + Integer.toString(strokeSize) + " " + Integer.toString(red) + " " + Integer.toString(green) + " " + Integer.toString(blue);
        assertEquals(true, command.equals(expected));
    }
    @Test
    public void testSetStrokeState(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        assertEquals(0, canvas.getStrokeState());
        canvas.setStrokeState(5);
        assertEquals(5, canvas.getStrokeState());
        canvas.setStrokeState(15);
        assertEquals(15, canvas.getStrokeState());
        canvas.setStrokeState(25);
        assertEquals(25, canvas.getStrokeState());
        canvas.setStrokeState(5);
        assertEquals(5, canvas.getStrokeState());
        canvas.setStrokeState(35);
        assertEquals(35, canvas.getStrokeState());
        //no negative stroke state, gets set to 0
        canvas.setStrokeState(-50);
        assertEquals(0, canvas.getStrokeState());
    }
    @Test
    public void setWhiteboardNameTest(){
        //tests multiple settings of the whiteboard name (on the same canvas) to a string
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setWhiteboardName("testboard");
        String outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"testboard");
        canvas.setWhiteboardName("test2");
        outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"test2");
        canvas.setWhiteboardName("test3");
        outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"test3");
        canvas.setWhiteboardName("test4");
        outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"test4");
        
    }
}
