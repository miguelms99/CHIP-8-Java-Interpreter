package chip8;

import javax.sound.sampled.LineUnavailableException;

/**
 * Timers required by CHIP-8
 */
public class Timers {

    /*
    Timers will not get updated continuously.
    The value of the timer only gets calculated when it is required by the getter method.
    Because of this, there is no reason to hold the actual value of timer.
    Only the time at which the timer will reach 0 is needed.
     */

    //Time when the delay timer will reach 0
    private volatile long delayTimerStopTime;

    //Time when the sound timer will reach 0
    private volatile long soundTimerStopTime;

    //Sound thread
    private Thread soundThread;


    /**
     * Creates a new Timers with no timers active
     */
    public Timers() {
        delayTimerStopTime = 0;
        soundTimerStopTime = 0;
        //This new thread will make the sounds whenever soundTimerStopTime>System.nanoTime().
        try {
            soundThread = new Thread(new Sound(this));
            soundThread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            soundThread.interrupt();
        }
    }

    /**
     * Setter for the delay timer
     * @param delayTimer Value of the delay timer
     */
    public void setDelayTimer(byte delayTimer) {
        //Calculate the time it reaches 0
        delayTimerStopTime = System.nanoTime() + ((delayTimer&0xFF)*1_000_000_000L/60);
    }

    /**
     * Setter for the sound timer
     * @param soundTimer Value of the sound timer
     */
    public synchronized void setSoundTimer(byte soundTimer) {
        //Calculate the time the sound should stop
        soundTimerStopTime = System.nanoTime() + ((soundTimer&0xFF)*1_000_000_000L/60);
        //Notify the thread waiting to play a sound
        this.notify();
    }

    /**
     * Getter for the delay timer
     * @return Value of the delay timer
     */
    public byte getDelayTimer() {
        return (byte) Math.max(Math.round((delayTimerStopTime-System.nanoTime())/((double) 1_000_000_000/60)), 0);
    }

    /**
     * Getter for the sound timer
     * @return Value of the sound timer
     */
    public byte getSoundTimer() {
        return (byte) Math.max(Math.round((soundTimerStopTime-System.nanoTime())/((double) 1_000_000_000/60)), 0);
    }

    /**
     * Gets the time at which the delay timer will reach 0.
     * Time is in nanoseconds and uses System.nanoTime() as the time source.
     * @return Time in nanoseconds at which the delay timer will reach 0, uses System.nanoTime() as the time source.
     */
    public long getDelayTimerStopTime() {
        return delayTimerStopTime;
    }

    /**
     * Gets the time at which the sound timer will reach 0.
     * Time is in nanoseconds and uses System.nanoTime() as the time source.
     * @return Time in nanoseconds at which the sound timer will reach 0, uses System.nanoTime() as the time source.
     */
    public long getSoundTimerStopTime() {
        return soundTimerStopTime;
    }


}
