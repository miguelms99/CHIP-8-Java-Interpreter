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
    private final int refreshRate = 60;
    private final ScreenPanel display;
    private final JFrame frame;
    /*
    CHIP-8 has the following layout of 16 keys which are identified by an hexadecimal digit:
    1 2 3 c
    4 5 6 d
    7 8 9 e
    a 0 b f
    On a qwerty keyboard the following keys are used:
    1 2 3 4
    q w e r
    a s d f
    z x c v
    keyCodes contains the key codes for those keys sorted by the hexadecimal identifier
     */
    private final int[] keyCodes = {88, 49, 50, 51,
            81, 87, 69, 65,
            83, 68, 90, 67,
            52, 82, 70, 86};
    private final Keyboard keyboard;
    private final int inputFocus = JComponent.WHEN_IN_FOCUSED_WINDOW;

    public UI(Screen s, Keyboard keyboard) {
        this.keyboard = keyboard;
        frame = new JFrame("CHIP-8 Interpreter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        display = new ScreenPanel(s);
        display.setDoubleBuffered(true);
        frame.add(display);
        frame.pack();
        frame.setVisible(true);
        setKeyBindings();
        refreshScreen();
    }

    /**
     * This method will set all the key bindings
     */
    private void setKeyBindings() {
        for (int i = 0; i<keyCodes.length; i++) {
            //Sets the input map for the key press
            display.getInputMap(inputFocus).put(KeyStroke.getKeyStroke(keyCodes[i], 0, false), Integer.toHexString(i) + "down");
            //Sets the input map for the rey release
            display.getInputMap(inputFocus).put(KeyStroke.getKeyStroke(keyCodes[i], 0, true), Integer.toHexString(i) + "up");
            //Sets the action map for the key press using setKeyPress()
            display.getActionMap().put(Integer.toHexString(i) + "down", new setKeyPress(i, true));
            //Sets the action map for the rey release using setKeyPress()
            display.getActionMap().put(Integer.toHexString(i) + "up", new setKeyPress(i, false));
        }
    }

    /**
     * This method sets up a timer which will refresh the screen
     */
    private void refreshScreen() {
        int delay = 1000/refreshRate;
        ActionListener task = actionEvent -> display.paintScreen();
        new Timer(delay, task).start();
    }

    private class setKeyPress extends AbstractAction {

        boolean keyStatus; //true if the key is being pressed, false if the key is being released
        int key; //hexadecimal identifier of the key

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
