package canvas;

public class whiteboardServer {

    
    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client
     */
    private String handleRequest(String input) {
        String regex = "(look)|(-?\\d+ draw -?\\d+ -?\\d+ -?\\d+ -?\\d+)|(-?\\d+ erase -?\\d+ -?\\d+ -?\\d+ -?\\d+)|"
                + "(help)|(bye)";
        if ( ! input.matches(regex)) {
            // invalid input
            return null;
        }
        String[] tokens = input.split(" ");
        if (tokens[1].equals("look")) {
            // 'look' request
            return canvas.getBoardMessage();
        } else if (tokens[0].equals("help")) {
            // 'help' request
            return canvas.helpMessage();
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            //terminate connection
            return null;
        } else {
            int x1 = Integer.parseInt(tokens[2]);
            int y1 = Integer.parseInt(tokens[3]);
            int x2 = Integer.parseInt(tokens[4]);
            int y2 = Integer.parseInt(tokens[5]);
            if (tokens[0].equals("draw")) {
                // 'draw x1 y1 x2 y2' request
                return canvas.drawLineSegment(x1,y1,x2,y2);
            } else if (tokens[0].equals("erase")) {
                // 'draw x1 y1 x2 y2' request
                return canvas.eraseLineSegment(x1,y1,x2,y2);
            }
        }
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }
}
