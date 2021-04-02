package chip8;

import javax.swing.*;

/**
 * @author Miguel Moreno
 * Class in which the main game loop occurs
 */
public class Chip8 {

    public static void main(String[] args) {

        Screen screen = new Screen();

        SwingUtilities.invokeLater(() -> new UI(screen));

        Timers timers = new Timers();

        //Some code to test the sound
        int i=0;
        while (i++<10) {
            timers.setSoundTimer((byte) 6);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Some code to test the ui
        i=0;
        int j=0;
        while (true) {
            screen.drawSprite(Memory.FONT[0], (short) (i%64), (short) (j%32));
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            screen.drawSprite(Memory.FONT[0], (short) (i%64), (short) (j%32));
            i++;
            if (i%64==0) j+=9;
        }

    }
}