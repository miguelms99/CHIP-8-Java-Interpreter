package chip8;

/**
 * @author Miguel Moreno
 * This class implements the screen
 */
public class Screen {

    //Dimensions of the screen
    public final short SCREEN_HEIGHT = 32;
    public final short SCREEN_WIDTH = 64;

    //True if sprites should wrap around
    public final boolean SCREEN_WRAP = true;

    //false is black and true is white
    //An empty screen is black, sprites are drawn in white
    private boolean[][] screen;

    //True if the screen has been modified since last time it was shown
    public boolean drawFlag;

    /**
     * Creates a screen
     */
    Screen() {
        screen = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];
        clear();
    }

    /**
     * Clears the screen by settings all pixels to false
     */
    public void clear() {
        for (short i=0; i<screen.length; i++) {
            for (short j=0; j<screen[i].length; j++) {
                screen[i][j] = false;
            }
        }
        drawFlag = true;
    }

    /**
     * get the screen
     */
    public boolean[][] getScreen() {
        return screen;
    }

    /**
     * Draw a sprite in the screen
     * @param sprite an array of bytes representing the sprite
     * @param x the column in which to draw the sprite
     * @param y the row in which to draw the sprite
     * @return whether an on pixel has been overwritten to off or not
     */
    public boolean drawSprite(byte[] sprite, short x, short y) {

        //Check if coordinates are out of bounds
        if (x<0 || x>= SCREEN_WIDTH || y<0 || y>= SCREEN_HEIGHT) {
            System.err.println("Sprite coordinates out of bounds");
            return false;
        }

        //Set to true if an on pixel has been overwritten to off
        boolean pixelOverwrite = false;

        for (byte line : sprite) {
            byte mask = 1;
            for (byte i = 7; i>=0; i--) {
                //Avoid wrapping if wrap is set to false
                if (!(x+i>= SCREEN_WIDTH && !SCREEN_WRAP)) {
                    //Calculate the pixel
                    boolean pixel = (line & mask) == mask;
                    //Check if an on pixel will be overwritten to off
                    if (pixel==true && screen[(x+i)% SCREEN_WIDTH][y% SCREEN_HEIGHT]==true) pixelOverwrite=true;
                    //XOR the pixel
                    screen[(x+i)% SCREEN_WIDTH][y% SCREEN_HEIGHT] ^= pixel;
                }
                mask <<= 1;
            }
            y++;
            if (y>= SCREEN_HEIGHT && !SCREEN_WRAP) break;
        }

        drawFlag = true;

        return pixelOverwrite;
    }

    @Override
    /**
     * A String representation of the screen
     */
    public String toString() {
        String s = "";
        char c = 'X'; //Character used to represent a white pixel
        for (int i = 0; i< SCREEN_HEIGHT; i++) {
            for (int j = 0; j< SCREEN_WIDTH; j++) {
                if (screen[j][i]) s += c;
                else s += " ";
            }
            s += "\n";
        }
        return s;
    }

}