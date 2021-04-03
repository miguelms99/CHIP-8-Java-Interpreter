package chip8;

import java.util.Arrays;

/**
 * @author Miguel Moreno
 * Memory and registers used by the CHIP-8 processing unit
 */
public class Memory {

    /**
     * Size of the memory
     */
    public final short memorySize = 4096;

    /**
     * Location of the beginning of the program
     */
    public final short programLocation = 0x200;

    /**
     * Location of the beginning of the pixel font
     */
    public final short fontLocation = 0x50;

    //Memory and register bank
    private final byte[] memory;

    /**
     * 16 general purpose 8-bit registers (V0 to VF)
     */
    public final byte[] V = new byte[16];

    /**
     * 16-bit address register I
     */
    public short I;

    /**
     * Creates a Memory with fonts sprites but no program loaded onto it
     */
    public Memory() {
        memory = new byte[memorySize];

        //Copy font sprites to memory
        short i = fontLocation;
        for (byte[] j : FONT) {
            for (byte k : j) {
                memory[i++] = k;
            }
        }
    }

    /**
     * Font sprites
     */
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
     * Get the byte at a memory address,
     * @param address Address
     * @return Byte at address, 0 if address is out of range
     */
    public byte getByte(short address) {
        if (address > memorySize) {
            System.err.println("Address out of range");
            return 0;
        }
        return memory[address];
    }

    /**
     * Get multiple bytes at a memory address
     * @param address Address of the first byte
     * @param numBytes Number of bytes to return
     * @return Array of the number of bytes requested, empty array if any of the bytes are out of range
     */
    public byte[] getByte(short address, short numBytes) {
        if (address - 1 + numBytes > memorySize) {
            System.err.println("Address out of range");
            return new byte[] {};
        }
        return Arrays.copyOfRange(memory, address, address + numBytes);
    }

    /**
     * Write a byte at a memory address
     * @param data Byte to write to the memory
     * @param address Address in which to write the byte
     */
    public void setByte(byte data, short address) {
        if (address > memorySize) System.err.println("Address out of range");
        else memory[address] = data;
    }

    /**
     * Write multiple bytes to the memory, will not write anything if any byte is out of range
     * @param data Array of bytes to write to the memory
     * @param address Address in which to write the first byte
     */
    public void setByte(byte[] data, short address) {
        if ((address - 1 + data.length) > memorySize) System.err.println("Address out of range");
        else for (byte b : data) memory[address++] = b;
    }

}