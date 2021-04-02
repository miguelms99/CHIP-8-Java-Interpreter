package chip8;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * This class implements runnable and will play a sound when needed
 */
class Sound implements Runnable {

    private final Timers timers;
    private final double sampleRate = 44100;
    private final double hz = 320;
    private final double volume = 30;
    private final SourceDataLine sdl;
    private volatile boolean isPlaying; //True when sound should be played

    Sound(Timers timers) throws LineUnavailableException {
        this.timers = timers;
        AudioFormat af = new AudioFormat((float) sampleRate, 8, 1, true, false);
        sdl = AudioSystem.getSourceDataLine(af);
        sdl.open();
        isPlaying = false;
    }

    @Override
    public void run() {
        new Thread(() -> {
            /*
            This thread fills up the data line buffer using the write method.
            The write method will block when trying to write more data that can currently be written.
            If the sound timer were to be shortened while the write method is blocked then the sound may not stop in time,
            thus this new thread is needed to avoid blocking the main thread.
             */
            byte[] b = new byte[sdl.getBufferSize() / 2]; //Fill half the buffer at a time
            while (true) {
                if (Thread.currentThread().isInterrupted()) break;
                try {
                    synchronized (Sound.this) {
                        while (!isPlaying) {
                            //Will get notified when a sound is needed
                            Sound.this.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
                for (int i = 0; i < b.length; i++) {
                    double angle = i / (sampleRate / hz) * 2.0 * Math.PI;
                    b[i] = (byte) (Math.sin(angle) * volume);
                }
                if (isPlaying) sdl.write(b, 0, b.length);
            }
        }).start();

        //Main loop
        while (true) {
            if (Thread.currentThread().isInterrupted()) break;
            try {
                synchronized (timers) {
                    while (timers.getSoundTimerStopTime() < System.nanoTime()) {
                        //Wait while silent
                        //Will get notified if the sound timer is modified
                        timers.wait();
                    }
                }
            } catch (InterruptedException e) {
                break;
            }
            synchronized (this) {
                sdl.start(); //Starts playing sound
                isPlaying = true;
                this.notify(); //The other thread will fill up the buffer
            }
            try {
                synchronized (timers) {
                    //Calculate the duration of the sound
                    long duration = (timers.getSoundTimerStopTime() - System.nanoTime()) / 1_000_000;
                    while (duration > 0) {
                        //Wait for the duration of the sound
                        //Will get notified if the sound timer changes
                        timers.wait(duration);
                        //Calculate the duration again in case the sound timer changed
                        duration = (timers.getSoundTimerStopTime() - System.nanoTime()) / 1_000_000;
                    }
                }
            } catch (InterruptedException e) {
                break;
            }
            synchronized (this) {
                sdl.stop(); //Stops the sound
                isPlaying = false;
            }
        }
        sdl.close(); //Close if interrupted
    }
}
