package server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

/**
 * WhiteboardServer Test runs the JUnit tests for the Whiteboard Server class.
 * The parsing of the text message protocol is tested. All components that do
 * not require sockets are tested.
 * 
 */
public class WhiteboardServerTest {
    /**
     * TESTING createBoards Asserts that Board1, Board2, and Board3 are created.
     */
    @Test
    public void createBoardsTest() throws IOException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        whiteboardServer.createBoards();
        assertEquals(whiteboardServer.whiteboardToCommandsMap.size(), 3);
        assertEquals(
                whiteboardServer.whiteboardToCommandsMap.containsKey("Board1"),
                true);
        assertEquals(
                whiteboardServer.whiteboardToCommandsMap.containsKey("Board2"),
                true);
        assertEquals(
                whiteboardServer.whiteboardToCommandsMap.containsKey("Board3"),
                true);
    }

    /**
     * TESTING handleRequest (all commands that create a relatively short stream
     * of commands, longer ones are manually tested) (and
     * getExistingWhiteboardsAll, getExistingWhiteboardsOne with the addBoard
     * and newUsername commands) (and getSameUsersWhiteboard with the
     * selectBoard command) Partition the input space as follows: Input: new
     * username (doesn't exist), new username exists, addBoard whiteboard
     * exists, addBoard whiteboard doesn't exist selectBoard with no user,
     * selectBoard with no board, selectBoard normal draw single user, erase
     * single user (multiple users is tested with telnet) command in regex but
     * no specified action, command not in regular expression
     * 
     * @throws InterruptedException
     */
    @Test
    public void newUsernameNonExist() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("new username bob", threadNum);

        String one = whiteboardServer.commandQueues.get(threadNum).take();
        String two = whiteboardServer.commandQueues.get(threadNum).take();
        String three = whiteboardServer.commandQueues.get(threadNum).take();
        String four = whiteboardServer.commandQueues.get(threadNum).take();
        String five = whiteboardServer.commandQueues.get(threadNum).take();

        assertEquals(one.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(two.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(three.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(
                four.substring(0, 29).equals("Done sending whiteboard names"),
                true);
        assertEquals(five.substring(0, 19).equals("Select a whiteboard"), true);
        assertEquals(whiteboardServer.clientToWhiteboardMap.get("bob"), "");
        assertEquals(whiteboardServer.clientToThreadNumMap.get("bob"),
                threadNum);
    }

    @Test
    public void newUsernameExists() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        Integer threadNum2 = 1;
        whiteboardServer.handleRequest("new username bob", threadNum);
        whiteboardServer.handleRequest("new username bob", threadNum2);

        String one = whiteboardServer.commandQueues.get(threadNum).take();
        String two = whiteboardServer.commandQueues.get(threadNum).take();
        String three = whiteboardServer.commandQueues.get(threadNum).take();
        String four = whiteboardServer.commandQueues.get(threadNum).take();
        String five = whiteboardServer.commandQueues.get(threadNum).take();

        assertEquals(one.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(two.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(three.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(
                four.substring(0, 29).equals("Done sending whiteboard names"),
                true);
        assertEquals(five.substring(0, 19).equals("Select a whiteboard"), true);
        assertEquals(whiteboardServer.clientToWhiteboardMap.get("bob"), "");
        assertEquals(whiteboardServer.clientToThreadNumMap.get("bob"),
                threadNum);
        String six = whiteboardServer.commandQueues.get(threadNum2).take();
        assertEquals(
                six.substring(0, 53)
                        .equals("Username already taken. Please select a new username."),
                true);
    }

    @Test
    public void addBoardNonExist() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("addBoard test", threadNum);

        String one = whiteboardServer.commandQueues.get(threadNum).take();
        String two = whiteboardServer.commandQueues.get(threadNum).take();
        String three = whiteboardServer.commandQueues.get(threadNum).take();
        String four = whiteboardServer.commandQueues.get(threadNum).take();
        String five = whiteboardServer.commandQueues.get(threadNum).take();
        String six = whiteboardServer.commandQueues.get(threadNum).take();

        assertEquals(one.substring(0, 16).equals("Board test added"), true);
        assertEquals(two.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(three.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(four.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(five.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(
                six.substring(0, 29).equals("Done sending whiteboard names"),
                true);
    }

    @Test
    public void addBoardExists() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("addBoard test", threadNum);
        whiteboardServer.handleRequest("addBoard test", threadNum);

        String one = whiteboardServer.commandQueues.get(threadNum).take();
        String two = whiteboardServer.commandQueues.get(threadNum).take();
        String three = whiteboardServer.commandQueues.get(threadNum).take();
        String four = whiteboardServer.commandQueues.get(threadNum).take();
        String five = whiteboardServer.commandQueues.get(threadNum).take();
        String six = whiteboardServer.commandQueues.get(threadNum).take();
        String seven = whiteboardServer.commandQueues.get(threadNum).take();
        String eight = whiteboardServer.commandQueues.get(threadNum).take();
        String nine = whiteboardServer.commandQueues.get(threadNum).take();
        String ten = whiteboardServer.commandQueues.get(threadNum).take();
        String eleven = whiteboardServer.commandQueues.get(threadNum).take();
        String twelve = whiteboardServer.commandQueues.get(threadNum).take();

        assertEquals(one.substring(0, 16).equals("Board test added"), true);
        assertEquals(two.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(three.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(four.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(five.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(
                six.substring(0, 29).equals("Done sending whiteboard names"),
                true);
        assertEquals(seven.substring(0, 26)
                .equals("Whiteboard already exists."), true);
        assertEquals(eight.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(nine.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(ten.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(eleven.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(
                twelve.substring(0, 29).equals("Done sending whiteboard names"),
                true);
    }

    @Test
    public void selectBoardNoUser() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("addBoard test", threadNum);
        whiteboardServer.handleRequest("bob selectBoard test", threadNum);

        String one = whiteboardServer.commandQueues.get(threadNum).take();
        String two = whiteboardServer.commandQueues.get(threadNum).take();
        String three = whiteboardServer.commandQueues.get(threadNum).take();
        String four = whiteboardServer.commandQueues.get(threadNum).take();
        String five = whiteboardServer.commandQueues.get(threadNum).take();
        String six = whiteboardServer.commandQueues.get(threadNum).take();
        String seven = whiteboardServer.commandQueues.get(threadNum).take();

        assertEquals(one.substring(0, 16).equals("Board test added"), true);
        assertEquals(two.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(three.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(four.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(five.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(
                six.substring(0, 29).equals("Done sending whiteboard names"),
                true);
        assertEquals(seven.substring(0, 24).equals("Username does not exist."),
                true);
    }

    @Test
    public void selectBoardNoBoard() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("new username bob", threadNum);
        whiteboardServer.handleRequest("bob selectBoard test", threadNum);

        String one = whiteboardServer.commandQueues.get(threadNum).take();
        String two = whiteboardServer.commandQueues.get(threadNum).take();
        String three = whiteboardServer.commandQueues.get(threadNum).take();
        String four = whiteboardServer.commandQueues.get(threadNum).take();
        String five = whiteboardServer.commandQueues.get(threadNum).take();
        String six = whiteboardServer.commandQueues.get(threadNum).take();

        assertEquals(one.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(two.substring(0, 21).equals("Existing Whiteboards "), true);
        assertEquals(three.substring(0, 21).equals("Existing Whiteboards "),
                true);
        assertEquals(
                four.substring(0, 29).equals("Done sending whiteboard names"),
                true);
        assertEquals(five.substring(0, 19).equals("Select a whiteboard"), true);
        assertEquals(
                six.substring(0, 68)
                        .equals("Whiteboard does not exist. Select a different board or make a board."),
                true);
    }

    @Test
    public void selectBoardUserBoard() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("new username bob", threadNum);
        whiteboardServer.handleRequest("bob selectBoard Board1", threadNum);
        ArrayList<String> commandsList = new ArrayList<String>();
        for (String commands : whiteboardServer.commandQueues.get(threadNum)) {
            commandsList.add(commands);
        }
        assertEquals(
                commandsList.get(0).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(1).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(2).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(3).substring(0, 29)
                        .equals("Done sending whiteboard names"), true);
        assertEquals(
                commandsList.get(4).substring(0, 19)
                        .equals("Select a whiteboard"), true);
        assertEquals(
                commandsList.get(5).substring(0, 16).equals("Updating Clients"),
                true);
        assertEquals(
                commandsList.get(6).substring(0, 14).equals("sameClient bob"),
                true);
        assertEquals(
                commandsList.get(7).substring(0, 25)
                        .equals("Done sending client names"), true);
        assertEquals(
                commandsList.get(8).substring(0, 19)
                        .equals("bob on board Board1"), true);
    }

    @Test
    public void drawSingleUser() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("new username bob", threadNum);
        whiteboardServer.handleRequest("bob selectBoard Board1", threadNum);
        whiteboardServer.handleRequest("Board1 draw 0 0 100 100 50 12 34 56",
                threadNum);

        ArrayList<String> commandsList = new ArrayList<String>();
        for (String commands : whiteboardServer.commandQueues.get(threadNum)) {
            commandsList.add(commands);
        }
        assertEquals(
                commandsList.get(0).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(1).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(2).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(3).substring(0, 29)
                        .equals("Done sending whiteboard names"), true);
        assertEquals(
                commandsList.get(4).substring(0, 19)
                        .equals("Select a whiteboard"), true);
        assertEquals(
                commandsList.get(5).substring(0, 16).equals("Updating Clients"),
                true);
        assertEquals(
                commandsList.get(6).substring(0, 14).equals("sameClient bob"),
                true);
        assertEquals(
                commandsList.get(7).substring(0, 25)
                        .equals("Done sending client names"), true);
        assertEquals(
                commandsList.get(8).substring(0, 19)
                        .equals("bob on board Board1"), true);
        assertEquals(
                commandsList.get(9).substring(0, 35)
                        .equals("Board1 draw 0 0 100 100 50 12 34 56"), true);
    }

    @Test
    public void eraseSingleUser() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("new username bob", threadNum);
        whiteboardServer.handleRequest("bob selectBoard Board1", threadNum);
        whiteboardServer
                .handleRequest("Board1 erase 0 0 100 100 50", threadNum);

        ArrayList<String> commandsList = new ArrayList<String>();
        for (String commands : whiteboardServer.commandQueues.get(threadNum)) {
            commandsList.add(commands);
        }
        assertEquals(
                commandsList.get(0).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(1).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(2).substring(0, 21)
                        .equals("Existing Whiteboards "), true);
        assertEquals(
                commandsList.get(3).substring(0, 29)
                        .equals("Done sending whiteboard names"), true);
        assertEquals(
                commandsList.get(4).substring(0, 19)
                        .equals("Select a whiteboard"), true);
        assertEquals(
                commandsList.get(5).substring(0, 16).equals("Updating Clients"),
                true);
        assertEquals(
                commandsList.get(6).substring(0, 14).equals("sameClient bob"),
                true);
        assertEquals(
                commandsList.get(7).substring(0, 25)
                        .equals("Done sending client names"), true);
        assertEquals(
                commandsList.get(8).substring(0, 19)
                        .equals("bob on board Board1"), true);
        assertEquals(
                commandsList.get(9).substring(0, 27)
                        .equals("Board1 erase 0 0 100 100 50"), true);
    }

    @Test
    public void commandInRegexNoAction() throws IOException,
            InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("Disconnect Bob", threadNum);
        ArrayList<String> commandsList = new ArrayList<String>();
        for (String commands : whiteboardServer.commandQueues.get(threadNum)) {
            commandsList.add(commands);
        }
        assertEquals(
                commandsList.get(0).substring(0, 26)
                        .equals("In Server Regex, no action"), true);
    }

    @Test
    public void commandNotInRegex() throws IOException, InterruptedException {
        WhiteboardServer whiteboardServer = new WhiteboardServer();
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<String>(
                100000);
        whiteboardServer.commandQueues.add(blockingQueue); // Normally generated
                                                           // by method running
                                                           // network
                                                           // connections
        Integer threadNum = 0;
        whiteboardServer.handleRequest("Command Not in the Regex", threadNum);

        ArrayList<String> commandsList = new ArrayList<String>();
        for (String commands : whiteboardServer.commandQueues.get(threadNum)) {
            commandsList.add(commands);
        }
        assertEquals(
                commandsList.get(0).substring(0, 19)
                        .equals("Not in Server Regex"), true);
    }
}
