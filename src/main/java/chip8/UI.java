package chip8;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Miguel Moreno
 * User interface for the CHIP-8 interpreter
 */
public class UI {

    private final int refreshRate = 60; //in hz
    private final ScreenPanel display;
    private final JFrame frame;
    /*
    CHIP-8 has the following layout of 16 keys which are identified by an hexadecimal digit:
    1 2 3 c
    4 5 6 d
    7 8 9 e
    a 0 b f
    On a qwerty keyboard the following will be are used:
    1 2 3 4
    q w e r
    a s d f
    z x c v
     */
    private final int[] keyCodes = {88, 49, 50, 51,
            81, 87, 69, 65,
            83, 68, 90, 67,
            52, 82, 70, 86};
    private final Keyboard keyboard;
    private final int inputFocus = JComponent.WHEN_IN_FOCUSED_WINDOW;

    /**
     * Display the user interface.
     * The interface will show the screen of the CHIP-8 and it will register keyboard input.
     * @param screen CHIP-8 screen to display
     * @param keyboard keyboard used to register key presses needed for input in CHIP-8
     */
    public UI(Screen screen, Keyboard keyboard) {
        this.keyboard = keyboard;
        frame = new JFrame("CHIP-8 Interpreter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        display = new ScreenPanel(screen);
        display.setDoubleBuffered(true);
        frame.add(display);
        frame.pack();
        frame.setVisible(true);
        setKeyBindings();
        refreshScreen();
    }

    //This method will set all the keybindings
    private void setKeyBindings() {
        for (int i = 0; i<keyCodes.length; i++) {
            display.getInputMap(inputFocus).put(KeyStroke.getKeyStroke(keyCodes[i], 0, false), Integer.toHexString(i) + "down");
            display.getInputMap(inputFocus).put(KeyStroke.getKeyStroke(keyCodes[i], 0, true), Integer.toHexString(i) + "up");
            display.getActionMap().put(Integer.toHexString(i) + "down", new setKeyPress(i, true));
            display.getActionMap().put(Integer.toHexString(i) + "up", new setKeyPress(i, false));
        }
    }

    //This method sets up a timer which will refresh the screen
    private void refreshScreen() {
        int delay = 1000/refreshRate;
        ActionListener task = actionEvent -> display.paintScreen();
        new Timer(delay, task).start();
    }

    private class setKeyPress extends AbstractAction {
        boolean keyStatus; //true if the key is being pressed, false if the key is being released
        int key; //hex identifier

        setKeyPress(int key, boolean keyStatus) {
            this.key = key;
            this.keyStatus = keyStatus;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            keyboard.setKey(key, keyStatus);
        }
    }

}
