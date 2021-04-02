package chip8;

import javax.swing.*;

public class Chip8 {

    private static Screen screen;
    private static UI ui;
    private static Timers timers;
    private static Keyboard keyboard;

    public static void main(String[] args) {

        timers = new Timers();
        keyboard = new Keyboard();


        SwingUtilities.invokeLater(() -> {
            screen = new Screen();
            ui = new UI(screen, keyboard);
        });

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