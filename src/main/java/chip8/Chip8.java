package chip8;

import javax.swing.*;

import static java.lang.Thread.sleep;

/**
 * @author Miguel Moreno
 * Class in which the main game loop occurs
 */
public class Chip8 {

    public static void main(String[] args)  {

        Screen screen = new Screen();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UI(screen);
            }
        });

        //Some code to test the ui
        int i=0;
        int j=0;
        while (true) {
            screen.drawSprite(Memory.FONT[0], (short) (i%64), (short) (j%32));
            try {
                sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            screen.drawSprite(Memory.FONT[0], (short) (i%64), (short) (j%32));

            try {
                sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
            if (i%64==0) j+=9;

        }

    }

}
