package chip8;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Miguel Moreno
 * This class implements the screen
 */
public class Screen {

    //A lock is needed to avoid printing the screen while a sprite is being drawn onto it
    ReentrantLock lock = new ReentrantLock(true);

    //Dimensions of the screen
    public static final short SCREEN_HEIGHT = 32;
    public static final short SCREEN_WIDTH = 64;

    //True if sprites should wrap around
    public final boolean SCREEN_WRAP;

    //false is black and true is white
    //An empty screen is black, sprites are drawn in white
    private boolean[][] screenArray;

    //True if the screen has been modified since last time it was shown
    private boolean drawFlag;

    //True if coordinates out of bounds are allowed to be used to draw sprites
    private boolean outOfBoundsCoordinates;

    /**
     * Creates a blank screen
     */
    Screen() {
        SCREEN_WRAP = true;
        screenArray = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];
        drawFlag = true;
        outOfBoundsCoordinates = true;
    }

    Screen(Screen s) {
        SCREEN_WRAP = s.SCREEN_WRAP;
        outOfBoundsCoordinates = s.outOfBoundsCoordinates;
        screenArray = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];
        for (int i = 0; i< screenArray.length; i++) {
            screenArray[i] = Arrays.copyOf(s.screenArray[i], s.screenArray[i].length);
        }
        drawFlag = s.drawFlag;
    }

    /**
     * Clears the screen by settings all pixels to false
     */
    public void clear() {
        lock.lock();
        try {
            for (short i = 0; i< screenArray.length; i++) {
                for (short j = 0; j< screenArray[i].length; j++) {
                    screenArray[i][j] = false;
                }
            }
            drawFlag = true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get a copy of the screen boolean array
     * @param setFlagToFalse if true then drawFlag will be set to false
     */
    public boolean[][] getScreenArray(boolean setFlagToFalse) {
        lock.lock();
        try {
            boolean[][] screenCopy = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];
            for (int i = 0; i< screenCopy.length; i++) {
                screenCopy[i] = Arrays.copyOf(screenArray[i], screenArray[i].length);
            }
            if (setFlagToFalse) drawFlag = false;
            return screenCopy;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get a copy of the screen boolean array
     */
    public boolean[][] getScreenArray() {
        return getScreenArray(false);
    }


    /**
     * Draw a sprite in the screen
     * @param sprite an array of bytes representing the sprite
     * @param x the column in which to draw the sprite
     * @param y the row in which to draw the sprite
     * @return whether an on pixel has been overwritten to off or not
     */
    public boolean drawSprite(byte[] sprite, short x, short y) {

        lock.lock();
        try {

            //Check if coordinates are out of bounds
            if (x<0 || x>= SCREEN_WIDTH || y<0 || y>= SCREEN_HEIGHT) {
                System.err.println("Sprite coordinates out of bounds");
                if (!outOfBoundsCoordinates) {
                    return false;
                }
                else {
                    x %= SCREEN_WIDTH;
                    y %= SCREEN_HEIGHT;
                }
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
                        screenArray[(x+i)%SCREEN_WIDTH][y%SCREEN_HEIGHT] ^= pixel;
                    }
                    mask <<= 1;
                }
                y++;
                if (y>= SCREEN_HEIGHT && !SCREEN_WRAP) break;
            }

            drawFlag = true;

            return pixelOverwrite;

        } finally {
            lock.unlock();
        }
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

    public boolean getDrawFlag() {
        return drawFlag;
    }

}