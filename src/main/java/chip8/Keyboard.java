package chip8;

import java.util.Arrays;

/**
 * Stores the status of the key presses needed for CHIP-8 to work
 */
public class Keyboard {

    /**
     * Number of keys the keypad has
     */
    public final int numKeys = 16;

    //The following array will hold 1 if the key is being pressed right now and 0 if the key is not being pressed.
    private final boolean[] keyArray = new boolean[numKeys];

    //Last key changed. Only used if the method waitKey is waiting for a key change,
    //otherwise it wont get updated. -1 means it's waiting for a key change
    private int lastKeyChanged = -1;
    private boolean lastKeyChangedStatus;

    /**
     * Gets the status of the key
     * @param key Hexadecimal identifier of the key
     * @return true if the key is being pressed right now, false otherwise
     */
    public synchronized boolean isKeyPressed(byte key) {
        if (!(key>=0 && key<numKeys)) return false;
        return keyArray[key];
    }

    /**
     * Set the status of a key
     * @param key Hexadecimal identifier of the key
     * @param keyStatus True if the key is being pressed, false if the key is being released
     */
    public synchronized void setKey(int key, boolean keyStatus) {
        if (key>=0 && key<numKeys) {
            if (keyArray[key] != keyStatus) {
                keyArray[key] = keyStatus;
                if (lastKeyChanged == -1) {
                    lastKeyChanged = key;
                    lastKeyChangedStatus = keyStatus;
                }
                notify(); //Notify if the status of some key has changed
            }
        }
    }

    /**
     * Get the array of currently key presses.
     * If the key is currently pressed it's value is true, otherwise it is false
     * Keys are ordered by their hexadecimal identifier
     * @return A copy of the array containing which keys are currently pressed
     */
    public synchronized boolean[] getKeyArray() {
        return Arrays.copyOf(keyArray, keyArray.length);
    }

    /**
     * Halts execution until there is a change in the current key presses i.e. a key currently pressed
     * is released or a key not currently pressed is pressed.
     * @param waitTime Maximum wait time if greater than 0.
     * @param onlyKeyRelease If true execution will resume only when a key is released
     * @return Hexadecimal identifier of the key which changed
     * @throws InterruptedException
     */
    public synchronized byte waitKey(long waitTime, boolean onlyKeyRelease) throws InterruptedException {
        byte key = -1;
        long maximumWait = System.nanoTime()/1_000_000 + waitTime;
        long newWaitTime;
        do {
            if (waitTime > 0) {
                newWaitTime = maximumWait - System.nanoTime()/1_000_000;
                if (!(newWaitTime >= 1)) break;
                wait(newWaitTime);
            } else {
                wait();
            }
            key = (byte) lastKeyChanged;
            lastKeyChanged = -1;
        } while (onlyKeyRelease && lastKeyChangedStatus);
        return key;
    }

    /**
     * Halts execution until there is a change in the current key presses i.e. a key currently pressed
     * is released or a key not currently pressed is pressed.
     * @param waitTime Maximum wait time if greater than 0.
     * @return Hexadecimal identifier of the key which changed
     * @throws InterruptedException
     */
    public byte waitKey(long waitTime) throws InterruptedException {
        return waitKey(waitTime, false);
    }


}
