package chip8;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Miguel Moreno
 * This class implements the User Interface
 */
public class UI {

    //Refresh rate of the screen in hz
    private int refreshRate = 60;
    private ScreenPanel display;
    private JFrame frame;

    public UI(Screen s) {
        frame = new JFrame("CHIP-8 Interpreter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        //frame.getContentPane().setBackground(Color.BLACK);
        display = new ScreenPanel(s);
        display.setDoubleBuffered(true);
        frame.add(display);
        frame.pack();
        frame.setVisible(true);
        refreshScreen();
    }

    /**
     * This method sets up a timer which will refresh the screen
     */
    private void refreshScreen() {
        int delay = 1000/refreshRate;
        ActionListener task = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                display.paintScreen();
            }
        };
        new Timer(delay, task).start();
    }

}
