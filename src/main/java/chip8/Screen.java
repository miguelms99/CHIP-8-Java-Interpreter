package chip8;

import java.util.Arrays;

/**
 * @author Miguel Moreno
 * This class implements the screen
 */
public class Screen {

    //Dimensions of the screen
    public static final short SCREEN_HEIGHT = 32;
    public static final short SCREEN_WIDTH = 64;

    //True if sprites should wrap around
    public final boolean SCREEN_WRAP;

    //false is black and true is white
    //An empty screen is black, sprites are drawn in white
    private boolean[][] screenArray;

    //True if the screen has been modified since last time it was shown
    public boolean drawFlag;

    /**
     * Creates a blank screen
     */
    Screen() {
        SCREEN_WRAP = true;
        screenArray = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];
        drawFlag = true;
    }

    Screen(Screen s) {
        SCREEN_WRAP = s.SCREEN_WRAP;
        screenArray = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];
        boolean[][] screen2 = s.getScreenArray();
        for (int i = 0; i< screenArray.length; i++) {
            screenArray[i] = Arrays.copyOf(screen2[i], screen2[i].length);
        }
        drawFlag = s.drawFlag;
    }

    /**
     * Clears the screen by settings all pixels to false
     */
    public void clear() {
        for (short i = 0; i< screenArray.length; i++) {
            for (short j = 0; j< screenArray[i].length; j++) {
                screenArray[i][j] = false;
            }
        }
        drawFlag = true;
    }

    /**
     * get the screen boolean array
     */
    public boolean[][] getScreenArray() {
        return screenArray;
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
                    if (pixel==true && screenArray[(x+i)% SCREEN_WIDTH][y% SCREEN_HEIGHT]==true) pixelOverwrite=true;
                    //XOR the pixel
                    screenArray[(x+i)% SCREEN_WIDTH][y% SCREEN_HEIGHT] ^= pixel;
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
                if (screenArray[j][i]) s += c;
                else s += " ";
            }
            s += "\n";
        }
        return s;
    }

}