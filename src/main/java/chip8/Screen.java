package chip8;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Miguel Moreno
 * Information about the screen to be displayed
 */
public class Screen {

    //A lock is needed to avoid printing the screen while a sprite is being drawn onto it
    private ReentrantLock lock = new ReentrantLock(true);

    /**
     * Height of the screen
     */
    public static final short SCREEN_HEIGHT = 32;

    /**
     * Width of the screen
     */
    public static final short SCREEN_WIDTH = 64;

    /**
     * True if the sprites should wrap around
     */
    public final boolean screenWrap;

    /**
     * True if out of bounds coordinates should be allowed, if allowed they wrap around the screen
     */
    public final boolean outOfBoundsCoordinates;

    //false is black and true is white
    //An empty screen is black, sprites are drawn in white
    private final boolean[][] screenArray = new boolean[SCREEN_WIDTH][SCREEN_HEIGHT];

    //true if the screen has been modified since last time it was shown
    private volatile boolean drawFlag = true;

    /**
     * Creates a blank screen. Sprites wrap around the screen and out of bounds coordinates are allowed.
     */
    public Screen() {
        this(true,true);
    }

    /**
     * Creates a new Screen
     * @param screenWrap If sprites wrap around the screen or not.
     * @param outOfBoundsCoordinates If out of bounds coordinates are allowed or not.
     *                               If allowed they will wrap around.
     */
    public Screen(boolean screenWrap, boolean outOfBoundsCoordinates) {
        this.screenWrap = screenWrap;
        this.outOfBoundsCoordinates = outOfBoundsCoordinates;
    }

    /**
     * Copy constructor
     * @param screen Screen to copy
     */
    public Screen(Screen screen) {
        screenWrap = screen.screenWrap;
        outOfBoundsCoordinates = screen.outOfBoundsCoordinates;
        for (int i = 0; i< screenArray.length; i++) {
            screenArray[i] = Arrays.copyOf(screen.screenArray[i], screen.screenArray[i].length);
        }
        drawFlag = screen.drawFlag;
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
     * @return A copy of the screen boolean array
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
     * @return A copy of the screen boolean array
     */
    public boolean[][] getScreenArray() {
        return getScreenArray(false);
    }


    /**
     * Draw a sprite in the screen
     * @param sprite an array of bytes representing the sprite
     * @param x the column in which to draw the sprite
     * @param y the row in which to draw the sprite
     * @return true if an on pixel has been overwritten to off
     */
    public boolean drawSprite(byte[] sprite, byte x, byte y) {

        lock.lock();
        try {

            //Check if coordinates are out of bounds
            if (x<0 || x>= SCREEN_WIDTH || y<0 || y>= SCREEN_HEIGHT) {
                if (!outOfBoundsCoordinates) {
                    System.err.println("Sprite coordinates out of bounds");
                    return false;
                }
                else {
                    x %= SCREEN_WIDTH;
                    y %= SCREEN_HEIGHT;
                    //In case the remainder is negative
                    if (x < 0) x += SCREEN_WIDTH;
                    if (y < 0) y += SCREEN_HEIGHT;
                }
            }

            //Set to true if an on pixel has been overwritten to off
            boolean pixelOverwrite = false;

            for (byte line : sprite) {
                byte mask = 1;
                for (byte i = 7; i>=0; i--) {
                    //Avoid wrapping if wrap is set to false
                    if (!(x+i>= SCREEN_WIDTH && !screenWrap)) {
                        //Calculate the pixel
                        boolean pixel = (line & mask) == mask;
                        //Check if an on pixel will be overwritten to off
                        if (pixel && screenArray[(x + i) % SCREEN_WIDTH][y % SCREEN_HEIGHT]) pixelOverwrite=true;
                        //XOR the pixel
                        screenArray[(x+i)%SCREEN_WIDTH][y%SCREEN_HEIGHT] ^= pixel;
                    }
                    mask <<= 1;
                }
                y++;
                if (y>= SCREEN_HEIGHT && !screenWrap) break;
            }

            drawFlag = true;

            return pixelOverwrite;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns a String representation of the screen
     * @return String which represent pixels of the screen
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        char c = 'X'; //Character used to represent a white pixel
        for (int i = 0; i< SCREEN_HEIGHT; i++) {
            for (int j = 0; j< SCREEN_WIDTH; j++) {
                if (screenArray[j][i]) s.append(c);
                else s.append(" ");
            }
            s.append("\n");
        }
        return s.toString();
    }

    public boolean getDrawFlag() {
        return drawFlag;
    }

}