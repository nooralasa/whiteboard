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
    /**
     * Tests the set stroke state, setting values greater than or equal to zero.
     * Negative values get set to zero
     * The partition space is split up testing zero, small integers, and the MAX_INT
     */

    @Test
    public void testSetStrokeStateMAXVALUE(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setStrokeState(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, canvas.getStrokeState());
    }
    @Test
    public void testSetStrokeStateSmallInteger(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setStrokeState(5);
        assertEquals(5, canvas.getStrokeState());
    }
    @Test
    public void testSetStrokeStateLargeInteger(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setStrokeState(2627);
        assertEquals(2627, canvas.getStrokeState());
    }
    @Test
    public void testSetStrokeStateZero(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setStrokeState(0);
        assertEquals(0, canvas.getStrokeState());
    }
    @Test
    public void testSetStrokeStateNegativeInteger(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setStrokeState(-199);
        assertEquals(0, canvas.getStrokeState());
    }
    @Test
    public void testSetStrokeStateMININT(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setStrokeState(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, canvas.getStrokeState());
    }
    
    /**
     * Set whiteboard name test sets the whiteboard name to a string with numbers
     * different capitalizations, numbers, and special characters. Spaces are covered by the GUI input
     * (and so do not need to be tested in the canvas tests)
     */
    @Test
    public void setWhiteboardNameTestDefault(){
        //tests multiple settings of the whiteboard name (on the same canvas) to a string
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setWhiteboardName("testboard");
        String outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"testboard");
    }
    @Test
    public void setWhiteboardNameTestCapitals(){
        //tests multiple settings of the whiteboard name (on the same canvas) to a string
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setWhiteboardName("SdWdBca");
        String outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"SdWdBca");
    }
    @Test
    public void setWhiteboardNameTestSpecialChar(){
        //tests multiple settings of the whiteboard name (on the same canvas) to a string
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setWhiteboardName("@#^2#df2@");
        String outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"@#^2#df2@");
    }
    @Test
    public void setWhiteboardNameTestNumbers(){
        //tests multiple settings of the whiteboard name (on the same canvas) to a string
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setWhiteboardName("9219523692362");
        String outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"9219523692362");
    }
    @Test
    public void setWhiteboardNameTest(){
        //tests multiple settings of the whiteboard name (on the same canvas) to a string
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setWhiteboardName("test_BOARDA_SDGAW@");
        String outcome = canvas.getWhiteboardName();
        assertEquals(outcome,"test_BOARDA_SDGAW@");
    }
    /**
     * Test values between 0 and 255 for setting the client color.
     * Test 0,0,0 and 255,255,255 as well as different values in between
     */
    @Test
    public void getAndSetClientColor000(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setClientColor(0,0,0);
        int[] expected = {0,0,0};

        assertEquals(true, expected.length == canvas.getClientColor().length);
        assertEquals(true, expected[0] == canvas.getClientColor()[0]);
        assertEquals(true, expected[1] == canvas.getClientColor()[1]);
        assertEquals(true, expected[2] == canvas.getClientColor()[2]);
        
    }
    @Test
    public void getAndSetClientColor255255255(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setClientColor(255,255,255);
        int[] expected = {255,255,255};

        assertEquals(true, expected.length == canvas.getClientColor().length);
        assertEquals(true, expected[0] == canvas.getClientColor()[0]);
        assertEquals(true, expected[1] == canvas.getClientColor()[1]);
        assertEquals(true, expected[2] == canvas.getClientColor()[2]);

        
    }
    @Test
    public void getAndSetClientColorDiffValues(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setClientColor(100,255,234);
        int[] expected = {100,255,234};

        assertEquals(true, expected.length == canvas.getClientColor().length);
        assertEquals(true, expected[0] == canvas.getClientColor()[0]);
        assertEquals(true, expected[1] == canvas.getClientColor()[1]);
        assertEquals(true, expected[2] == canvas.getClientColor()[2]);
        
    }
    
    @Test
    public void getAndSetClientColorDiffValues2(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setClientColor(235,262,134);
        int[] expected = {235,262,134};

        assertEquals(true, expected.length == canvas.getClientColor().length);
        assertEquals(true, expected[0] == canvas.getClientColor()[0]);
        assertEquals(true, expected[1] == canvas.getClientColor()[1]);
        assertEquals(true, expected[2] == canvas.getClientColor()[2]);
        
    }
    
    
    /**
     * Tests the get and set server color methods. 
     * Test 0,0,0 and 255,255,255 as well as different values in between
     */
    @Test
    public void getAndSetServerColor000(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setServerColor(0,0,0);
        int[] expected = {0,0,0};

        assertEquals(true, expected.length == canvas.getClientColor().length);
        assertEquals(true, expected[0] == canvas.getServerColor()[0]);
        assertEquals(true, expected[1] == canvas.getServerColor()[1]);
        assertEquals(true, expected[2] == canvas.getServerColor()[2]);
        
    }
    @Test
    public void getAndSetServerColor255255255(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setServerColor(255,255,255);
        int[] expected = {255,255,255};

        assertEquals(true, expected.length == canvas.getClientColor().length);
        assertEquals(true, expected[0] == canvas.getServerColor()[0]);
        assertEquals(true, expected[1] == canvas.getServerColor()[1]);
        assertEquals(true, expected[2] == canvas.getServerColor()[2]);

        
    }
    @Test
    public void getAndSetServerColorDiffValues(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setServerColor(100,255,234);
        int[] expected = {100,255,234};

        assertEquals(true, expected.length == canvas.getClientColor().length);
        assertEquals(true, expected[0] == canvas.getServerColor()[0]);
        assertEquals(true, expected[1] == canvas.getServerColor()[1]);
        assertEquals(true, expected[2] == canvas.getServerColor()[2]);
        
    }
    
    @Test
    public void getAndSetServerColorDiffValues2(){
        BlockingQueue<String> outputCommandsQueue = new ArrayBlockingQueue<String>(10000000); ;
        Canvas canvas = new Canvas(100,100, outputCommandsQueue);
        canvas.setServerColor(235,262,134);
        int[] expected = {235,262,134};

        assertEquals(true, expected.length == canvas.getServerColor().length);
        assertEquals(true, expected[0] == canvas.getServerColor()[0]);
        assertEquals(true, expected[1] == canvas.getServerColor()[1]);
        assertEquals(true, expected[2] == canvas.getServerColor()[2]);
        
    }
    
    /**
     * Tests the fields in the canvas constructor. Tests to make sure that draw mode
     * defaults to true, the canvas width and heights are placed properly. The canvas
     * constructor does not take in values less than zero (by specifications)
     */
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
