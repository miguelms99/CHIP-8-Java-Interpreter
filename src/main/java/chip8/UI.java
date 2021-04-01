package chip8;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * @author Miguel Moreno
 * This class implements the User Interface
 */
public class UI {

    //Refresh rate of the screen in hz
    private final int refreshRate = 60;
    private final ScreenPanel display;

    public UI(Screen s) {
        JFrame frame = new JFrame("CHIP-8 Interpreter");
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
        ActionListener task = actionEvent -> display.paintScreen();
        new Timer(delay, task).start();
    }

}
