package chip8;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Stores the status of the key presses needed for CHIP-8 to work
 */
public class Keyboard {

    //Number of keys the keypad has
    private final int numKeys = 16;
    //The following array will hold 1 if the key is being pressed right now
    //and 0 if the key is not being pressed.
    private final AtomicIntegerArray keyArray = new AtomicIntegerArray(numKeys);

    /**
     * Gets the status of the key
     * @param key Hexadecimal identifier of the key
     * @return true if the key is being pressed right now, false otherwise
     */
    public boolean isKeyPressed(byte key) {
        if (!(key>=0 && key<numKeys)) return false;
        return keyArray.get(key)==1;
    }

    /**
     * Set the status of a key
     * @param key Hexadecimal identifier of the key
     * @param keyStatus True if the key is being pressed, false if the key is being released
     */
    public void setKey(int key, boolean keyStatus) {
        if (key>=0 && key<numKeys) {
            keyArray.set(key, keyStatus ? 1 : 0);
        }
    }


}
