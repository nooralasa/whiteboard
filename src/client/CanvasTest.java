package client;
import static org.junit.Assert.*;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    @Test
    public void getAndSetClientColor(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setClientColor(100,29,33);
        int[] expected = {100,29,33};

        assertEquals(true, expected.length == canvas.getClientColor().length);
        assertEquals(true, expected[0] == canvas.getClientColor()[0]);
        assertEquals(true, expected[1] == canvas.getClientColor()[1]);
        assertEquals(true, expected[2] == canvas.getClientColor()[2]);
        canvas.setClientColor(100,235,252);
        int[] expected1 = {100,235,252};
        assertEquals(true, expected1.length == canvas.getClientColor().length);
        assertEquals(true, expected1[0] == canvas.getClientColor()[0]);
        assertEquals(true, expected1[1] == canvas.getClientColor()[1]);
        assertEquals(true, expected1[2] == canvas.getClientColor()[2]);
        int[] expected2 = {21,111,234};
        canvas.setClientColor(21,111,234);
        assertEquals(true, expected2.length == canvas.getClientColor().length);
        assertEquals(true, expected2[0] == canvas.getClientColor()[0]);
        assertEquals(true, expected2[1] == canvas.getClientColor()[1]);
        assertEquals(true, expected2[2] == canvas.getClientColor()[2]);
        canvas.setClientColor(234,66,88);
        int[] expected3 = {234,66,88};
        assertEquals(true, expected3.length == canvas.getClientColor().length);
        assertEquals(true, expected3[0] == canvas.getClientColor()[0]);
        assertEquals(true, expected3[1] == canvas.getClientColor()[1]);
        assertEquals(true, expected3[2] == canvas.getClientColor()[2]);
        
    }
    
    @Test
    /**
     * Tests 
     */
    public void getAndSetServerColor(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setServerColor(100,29,33);
        int[] expected = {100,29,33};

        assertEquals(true, expected.length == canvas.getServerColor().length);
        assertEquals(true, expected[0] == canvas.getServerColor()[0]);
        assertEquals(true, expected[1] == canvas.getServerColor()[1]);
        assertEquals(true, expected[2] == canvas.getServerColor()[2]);
        canvas.setServerColor(100,235,252);
        int[] expected1 = {100,235,252};
        assertEquals(true, expected1.length == canvas.getServerColor().length);
        assertEquals(true, expected1[0] == canvas.getServerColor()[0]);
        assertEquals(true, expected1[1] == canvas.getServerColor()[1]);
        assertEquals(true, expected1[2] == canvas.getServerColor()[2]);
        int[] expected2 = {21,111,234};
        canvas.setServerColor(21,111,234);
        assertEquals(true, expected2.length == canvas.getServerColor().length);
        assertEquals(true, expected2[0] == canvas.getServerColor()[0]);
        assertEquals(true, expected2[1] == canvas.getServerColor()[1]);
        assertEquals(true, expected2[2] == canvas.getServerColor()[2]);
        canvas.setServerColor(234,66,88);
        int[] expected3 = {234,66,88};
        assertEquals(true, expected3.length == canvas.getServerColor().length);
        assertEquals(true, expected3[0] == canvas.getServerColor()[0]);
        assertEquals(true, expected3[1] == canvas.getServerColor()[1]);
        assertEquals(true, expected3[2] == canvas.getServerColor()[2]);
    }
    @Test
    public void testFieldsInCanvasConstructor(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        //check width
        assertEquals(100,canvas.getCanvasWidth());
        //check height
        assertEquals(100,canvas.getCanvasHeight());
        //check initial drawMode (true by default)
        assertEquals(true, canvas.getDrawMode());
        
    }
}
