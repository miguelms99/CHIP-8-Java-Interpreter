package chip8;

import javax.swing.*;
import java.awt.*;

/**
 * @author Miguel Moreno
 * This class defines the JPanel that manages the screen
 */
public class ScreenPanel extends JPanel {

    //How many pixels a CHIP-8 screen pixel has
    private final int scale = 10;

    private final Screen screen;
    private boolean[][] screenArray;

    /**
     * Creates a screen panel which will display a screen
     * @param screen Screen to be displayed
     */
    public ScreenPanel(Screen screen) {
        this.screen = screen;
        paintScreen();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Screen.SCREEN_WIDTH * scale, Screen.SCREEN_HEIGHT * scale);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        for (int i = 0; i< screenArray.length; i++) {
            for (int j = 0; j< screenArray[i].length; j++) {
                if (screenArray[i][j]) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillRect(i * scale, j * scale, scale, scale);
            }
        }
    }

    /**
     * This method will be used to refresh the screen
     */
    public void paintScreen() {
        if (screen.getDrawFlag()) {
            screenArray = screen.getScreenArray(true);
            repaint();
            Toolkit.getDefaultToolkit().sync(); //This is needed so that the panel doesn't lag
        }
    }

}