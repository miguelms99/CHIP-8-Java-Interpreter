package chip8;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class in which the game loop occurs
 */
public class Chip8 {

    private UI ui;
    private Screen screen;
    private Timers timers;
    private Keyboard keyboard;
    private Memory memory;

    public Chip8(String file) {
        byte[] program = {};
        try (FileInputStream input = new FileInputStream(file)){
            program = input.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        memory = new Memory();
        memory.setByte(program, memory.programLocation);
        timers = new Timers();
        keyboard = new Keyboard();
        screen = new Screen();
        SwingUtilities.invokeLater(() -> ui = new UI(screen, keyboard));
    }

}
