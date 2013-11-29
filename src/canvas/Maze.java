package canvas;

public class Maze {
    public enum Tile {Empty, Blocked};
    public int width;
    public int length;
    public Tile[] pixels;
    
    public Maze(int width, int length){
        this.width = width;
        this.length = length;
        this.pixels = new Tile[width*length];
    }

}
