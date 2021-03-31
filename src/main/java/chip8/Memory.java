package chip8;

/**
 * @author Miguel Moreno
 * This class implements the memory
 */
public class Memory {

    //Size of the memory
    public final short MEMORY_SIZE = 4096;

    //Location of the program
    public final short PROGRAM_LOCATION = 0x200;

    //Location of the pixel font
    public final short FONT_LOCATION = 0x50;

    //Memory and register bank
    private byte[] memory;

    /**
     * Creates a Memory and loads the font sprites into it
     */
    Memory() {
        memory = new byte[MEMORY_SIZE];

        //Copy font sprites to memory
        short i = FONT_LOCATION;
        for (byte[] j : FONT) {
            for (byte k : j) {
                memory[i++] = k;
            }
        }
    }

    //Font sprites
    public static final byte[][] FONT = {
            {(byte)0xF0,(byte)0x90,(byte)0x90,(byte)0x90,(byte)0xF0},
            {(byte)0x20,(byte)0x60,(byte)0x20,(byte)0x20,(byte)0x70},
            {(byte)0xF0,(byte)0x10,(byte)0xF0,(byte)0x80,(byte)0xF0},
            {(byte)0xF0,(byte)0x10,(byte)0xF0,(byte)0x10,(byte)0xF0},
            {(byte)0x90,(byte)0x90,(byte)0xF0,(byte)0x10,(byte)0x10},
            {(byte)0xF0,(byte)0x80,(byte)0xF0,(byte)0x10,(byte)0xF0},
            {(byte)0xF0,(byte)0x80,(byte)0xF0,(byte)0x90,(byte)0xF0},
            {(byte)0xF0,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x40},
            {(byte)0xF0,(byte)0x90,(byte)0xF0,(byte)0x90,(byte)0xF0},
            {(byte)0xF0,(byte)0x90,(byte)0xF0,(byte)0x10,(byte)0xF0},
            {(byte)0xF0,(byte)0x90,(byte)0xF0,(byte)0x90,(byte)0x90},
            {(byte)0xE0,(byte)0x90,(byte)0xE0,(byte)0x90,(byte)0xE0},
            {(byte)0xF0,(byte)0x80,(byte)0x80,(byte)0x80,(byte)0xF0},
            {(byte)0xE0,(byte)0x90,(byte)0x90,(byte)0x90,(byte)0xE0},
            {(byte)0xF0,(byte)0x80,(byte)0xF0,(byte)0x80,(byte)0xF0},
            {(byte)0xF0,(byte)0x80,(byte)0xF0,(byte)0x80,(byte)0x80},
    };

    /**
     * Get the byte at a memory address
     */
    public byte getByte(short address) {
        if (address > MEMORY_SIZE) {
            System.err.println("Address out of range");
            return 0;
        }
        return memory[address];
    }

    /**
     * Set the byte at a memory address
     */
    public void setByte(byte data, short address) {
        if (address > MEMORY_SIZE) System.err.println("Address out of range");
        else memory[address] = data;
    }

    public void setByte(byte[] data, short address) {
        if ((address - 1 + data.length) > MEMORY_SIZE) System.err.println("Address out of range");
        else for (byte b : data) memory[address++] = b;
    }

}