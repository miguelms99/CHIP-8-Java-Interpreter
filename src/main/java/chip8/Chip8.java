package chip8;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Execute a CHIP-8 program
 */
public class Chip8 {

    private UI ui;
    private final Screen screen;
    private final Timers timers;
    private final Keyboard keyboard;
    private final Memory memory;
    private final ProcessingUnit processingUnit;

    private final static int defaultFrequency = 600;
    private final static boolean defaultAlternative8XY68XYE = true;
    private final static boolean defaultAlternativeFX55FX65 = true;

    private volatile boolean endLoop = false; //Used to end the main loop

    /**
     * Executes a CHIP-8 program.
     * Alternative implementation for the 8XY6, 8XYE, FX55 and FX65 instructions can be activated. These alternative
     * implementations differ from those used in early CHIP-8 interpreters but may offer better compatibility with
     * popular CHIP-8 programs.
     * The alternative implementation for the 8XY6 and 8XYE instructions will shift the VX register instead of
     * the VY register and the latter register will not be used at all.
     * The alternative implementation for the FX55 and FX65 instructions will not modify the I register.
     * @param file file to be executed by the CHIP-8 interpreter
     * @param frequency the frequency at which instructions will be executed, in instructions per second
     * @param alternative8XY68XYE true if the newer implementation for the 8XY6 and 8XYE instructions should be used
     * @param alternativeFX55FX65 true if the newer implementation for the FX55 and FX65 instructions should be used
     */
    public Chip8(String file, int frequency, boolean alternative8XY68XYE, boolean alternativeFX55FX65) {
        byte[] program = {};
        try (FileInputStream input = new FileInputStream(file)){
            program = input.readAllBytes();
        } catch (IOException e) {
            System.err.println("Error when reading the file " + file);
            e.printStackTrace();
            System.exit(1);
        }
        memory = new Memory();
        memory.setByte(program, memory.programLocation);
        timers = new Timers();
        keyboard = new Keyboard();
        screen = new Screen();
        SwingUtilities.invokeLater(() -> ui = new UI(screen, keyboard));
        processingUnit = new ProcessingUnit(memory, timers, screen, keyboard, frequency, alternative8XY68XYE, alternativeFX55FX65);
        executeLoop();
    }

    /**
     * Executes a CHIP-8 program
     * @param file file to be executed by the CHIP-8 interpreter
     */
    public Chip8(String file) {
        this(file, defaultFrequency, defaultAlternative8XY68XYE, defaultAlternativeFX55FX65);
    }

    /**
     * Ends execution of the CHIP-8 program
     */
    public void endExecution() {
        endLoop = true;
    }

    private void executeLoop() {
        short nextOpcode;
        while(!endLoop) {
            nextOpcode = processingUnit.nextOpcode();
            processingUnit.syncTime();
            processingUnit.executeOpcode(nextOpcode);
            SwingUtilities.invokeLater(() -> ui.refreshScreen());
        }
    }

}
