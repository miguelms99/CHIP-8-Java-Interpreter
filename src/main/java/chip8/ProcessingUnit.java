package chip8;

import java.util.ArrayDeque;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class will execute CHIP-8 instructions
 */
public class ProcessingUnit {

    private final Memory memory;
    private final Timers timers;
    private final Screen screen;
    private final Keyboard keyboard;
    private long cycleCount = 0;
    private int frequency; //In instruction per second. 0 for unlimited
    private final long startTime;
    private final ArrayDeque<Short> stack = new ArrayDeque<>();
    private short PC;

    //See javadoc below
    private final boolean alternative8XY68XYE;
    private final boolean alternativeFX55FX65;

    /**
     * Construct a ProcessingUnit object. The ProcessingUnit will execute CHIP-8 instructions and will need the
     * necessary CHIP-8 components, i.e. memory, timers, screen and keyboard.
     * Alternative implementation for the 8XY6, 8XYE, FX55 and FX65 instructions can be activated. These alternative
     * implementations differ from those used in early CHIP-8 interpreters but may offer better compatibility with
     * popular CHIP-8 programs.
     * The alternative implementation for the 8XY6 and 8XYE instructions will shift the VX register instead of
     * the VY register and the latter register will not be used at all.
     * The alternative implementation for the FX55 and FX65 instructions will not modify the I register.
     * @param memory memory to be used
     * @param timers timers to be used
     * @param screen screen to be used
     * @param keyboard keyboard to be used
     * @param frequency the frequency at which instructions will be executed, in instructions per second
     * @param alternative8XY68XYE true if the newer implementation for the 8XY6 and 8XYE instructions should be used
     * @param alternativeFX55FX65 true if the newer implementation for the FX55 and FX65 instructions should be used
     */
    public ProcessingUnit(Memory memory, Timers timers, Screen screen, Keyboard keyboard, int frequency,
                          boolean alternative8XY68XYE, boolean alternativeFX55FX65) {
        this.memory = memory;
        this.timers = timers;
        this.screen = screen;
        this.keyboard = keyboard;
        PC = memory.programLocation;
        this.frequency = frequency;
        startTime = System.nanoTime();
        this.alternative8XY68XYE = alternative8XY68XYE;
        this.alternativeFX55FX65 = alternativeFX55FX65;
    }

    /**
     * Fetches the next 16-bit opcode
     * @return next 16-bit unsigned opcode in the memory
     */
    public short nextOpcode() {
        byte[] byteArray = memory.getByte(PC, (short) 2);
        return (short) (((byteArray[0]<<8)&0xFF00) | (byteArray[1]&0xFF));
    }

    /**
     * Getter for the total count of cycles
     * @return total cycles executed since the emulation started
     */
    public long getCycleCount() {
        return cycleCount;
    }

    /**
     * Getter for the program counter
     * @return current program counter
     */
    public short getProgramCounter() {
        return PC;
    }

    private byte[] shortToNibble(short s) {
        byte[] nibble = new byte[4];
        for (int i = 0; i < nibble.length; i++) {
            nibble[nibble.length-i-1] = (byte) ((s >>> (i*4)) & 0xF);
        }
        return nibble;
    }

    /**
     * Sync the processing unit to match the expected speed specified by the frequency.
     * Unless the parameter is true, the processing unit will not sync if the next instruction to be executed
     * does not requiere to be accurate in time. The instructions which requiere to be accurate in time are those
     * which draw onto the screen, read from keyboard or access the timers.
     * @param forceSync if true the processing unit will sync no mather what the next instruction is
     */
    public void syncTime(boolean forceSync) {
        /*
        The reason for avoiding to sync the processing unit until needed
        is that sleep() is unusable at less than 1 millisecond accuracy.
         */
        short nextOpcode = nextOpcode();
        byte[] nextOpcodeNibble = shortToNibble(nextOpcode);
        if (forceSync
                || (nextOpcode & 0xFFFF) == 0x00E0
                || nextOpcodeNibble[0] == 0xD
                || (nextOpcodeNibble[0] == 0xE && nextOpcodeNibble[2] == 0x9 && nextOpcodeNibble[3] == 0xE)
                || (nextOpcodeNibble[0] == 0xE && nextOpcodeNibble[2] == 0xA && nextOpcodeNibble[3] == 0x1)
                || (nextOpcodeNibble[0] == 0xF && nextOpcodeNibble[2] == 0x0 && nextOpcodeNibble[3] == 0x7)
                || (nextOpcodeNibble[0] == 0xF && nextOpcodeNibble[2] == 0x0 && nextOpcodeNibble[3] == 0xA)
                || (nextOpcodeNibble[0] == 0xF && nextOpcodeNibble[2] == 0x1 && nextOpcodeNibble[3] == 0x5)
                || (nextOpcodeNibble[0] == 0xF && nextOpcodeNibble[2] == 0x1 && nextOpcodeNibble[3] == 0x8)) {
            if (frequency!=0) {
                long runningTime = System.nanoTime()-startTime;
                long expectedRunningTime = (long) ((double) cycleCount /frequency*1_000_000_000L);
                long delayMs = (expectedRunningTime - runningTime)/1_000_000;
                if (delayMs>=1) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) { }
                }
            }
        }
    }

    /**
     * Sync the processing unit to match the expected speed specified by the frequency.
     * The processing unit will not sync if the next instruction to be executed does not requiere to be accurate in
     * time. The instructions which requiere to be accurate in time are those which draw onto the screen, read from
     * keyboard or access the timers.
     */
    public void syncTime() {
        syncTime(false);
    }

    /**
     * Will decode and execute an opcode. If the opcode is not valid the program will exit.
     * @param opcode the opcode to be decoded and executed
     */
    public void executeOpcode(short opcode) {
        byte[] opcodeNibble = shortToNibble(opcode);
        int opcodeInt = opcode & 0xFFFF;
        short nnn = (short) (opcode & 0x0FFF);
        byte nn = (byte) (opcode & 0x00FF);
        byte n = (byte) (opcode & 0x000F);
        byte x = opcodeNibble[1];
        byte y = opcodeNibble[2];

        outerSwitch:
        switch (opcodeNibble[0]) {
            case 0x0:
                if (opcodeInt == 0x00E0) opcode00E0();
                else if (opcodeInt == 0x00EE) opcode00EE();
                else opcode0NNN();
                break;
            case 0x1:
                opcode1NNN(nnn);
                break;
            case 0x2:
                opcode2NNN(nnn);
                break;
            case 0x3:
                opcode3XNN(x, nn);
                break;
            case 0x4:
                opcode4XNN(x, nn);
                break;
            case 0x5:
                opcode5XY0(x, y);
                break;
            case 0x6:
                opcode6XNN(x, nn);
                break;
            case 0x7:
                opcode7XNN(x, nn);
                break;
            case 0x8:
                switch (opcodeNibble[3]) {
                    case 0x0:
                        opcode8XY0(x, y);
                        break outerSwitch;
                    case 0x1:
                        opcode8XY1(x, y);
                        break outerSwitch;
                    case 0x2:
                        opcode8XY2(x, y);
                        break outerSwitch;
                    case 0x3:
                        opcode8XY3(x, y);
                        break outerSwitch;
                    case 0x4:
                        opcode8XY4(x, y);
                        break outerSwitch;
                    case 0x5:
                        opcode8XY5(x, y);
                        break outerSwitch;
                    case 0x6:
                        opcode8XY6(x, y);
                        break outerSwitch;
                    case 0x7:
                        opcode8XY7(x, y);
                        break outerSwitch;
                    case 0xE:
                        opcode8XYE(x, y);
                        break outerSwitch;
                }
            case 0x9:
                opcode9XY0(x, y);
                break;
            case 0xA:
                opcodeANNN(nnn);
                break;
            case 0xB:
                opcodeBNNN(nnn);
                break;
            case 0xC:
                opcodeCXNN(x, nn);
                break;
            case 0xD:
                opcodeDXYN(x, y, n);
                break;
            case 0xE:
                if (opcodeNibble[2] == 0x9 && opcodeNibble[3] == 0xE) {
                    opcodeEX9E(x);
                    break;
                } else if (opcodeNibble[2] == 0xA && opcodeNibble[3] == 0x1) {
                    opcodeEXA1(x);
                    break;
                }
            case 0xF:
                if (opcodeNibble[2] == 0x0 && opcodeNibble[3] == 0x7) {
                    opcodeFX07(x);
                    break;
                } else if (opcodeNibble[2] == 0x0 && opcodeNibble[3] == 0xA) {
                    opcodeFX0A(x);
                    break;
                } else if (opcodeNibble[2] == 0x1 && opcodeNibble[3] == 0x5) {
                    opcodeFX15(x);
                    break;
                } else if (opcodeNibble[2] == 0x1 && opcodeNibble[3] == 0x8) {
                    opcodeFX18(x);
                    break;
                } else if (opcodeNibble[2] == 0x1 && opcodeNibble[3] == 0xE) {
                    opcodeFX1E(x);
                    break;
                } else if (opcodeNibble[2] == 0x2 && opcodeNibble[3] == 0x9) {
                    opcodeFX29(x);
                    break;
                } else if (opcodeNibble[2] == 0x3 && opcodeNibble[3] == 0x3) {
                    opcodeFX33(x);
                    break;
                } else if (opcodeNibble[2] == 0x5 && opcodeNibble[3] == 0x5) {
                    opcodeFX55(x);
                    break;
                } else if (opcodeNibble[2] == 0x6 && opcodeNibble[3] == 0x5) {
                    opcodeFX65(x);
                    break;
                }
            default:
                System.err.println("Opcode not valid: " + Integer.toHexString(opcodeInt)
                        + "\nAt memory location: " + Integer.toHexString(PC) + "\nCycle: " + cycleCount);
                System.exit(1);
        }
        cycleCount++;
    }

    //The following 35 methods contain the code that will execute the 35 CHIP-8 instructions

    //Execute machine language subroutine at address NNN. It is now considered deprecated and won't do anything.
    private void opcode0NNN() {
        PC += 2;
    }

    //Clear the screen
    private void opcode00E0() {
        screen.clear();
        PC += 2;
    }

    //Return from a subroutine
    private void opcode00EE() {
        if (stack.size() == 0) {
            System.err.println("Error at opcode 00EE, stack empty");
            return;
        }
        PC = stack.pop();
        PC += 2;
    }

    //Jump to address NNN
    private void opcode1NNN(short nnn) {
        PC = nnn;
    }

    //Execute subroutine at address NNN
    private void opcode2NNN(short nnn) {
        stack.push(PC);
        PC = nnn;
    }

    //Skip the following instruction if the value of register VX equals NN
    private void opcode3XNN(byte x, byte nn) {
        if (memory.V[x] == nn) PC += 2;
        PC += 2;
    }

    //Skip the following instruction if the value of register VX is not equal to NN
    private void opcode4XNN(byte x, byte nn) {
        if (memory.V[x] != nn) PC += 2;
        PC += 2;
    }

    //Skip the following instruction if the value of register VX is equal to the value of register VY
    private void opcode5XY0(byte x, byte y) {
        if (memory.V[x] == memory.V[y]) PC += 2;
        PC += 2;
    }

    //Store number NN in register VX
    private void opcode6XNN(byte x, byte nn) {
        memory.V[x] = nn;
        PC += 2;
    }

    //Add the value NN to register VX
    private void opcode7XNN(byte x, byte nn) {
        memory.V[x] += nn;
        PC += 2;
    }

    //Store the value of register VY in register VX
    private void opcode8XY0(byte x, byte y) {
        memory.V[x] = memory.V[y];
        PC += 2;
    }

    //Set VX to VX OR VY
    private void opcode8XY1(byte x, byte y) {
        memory.V[x] |= memory.V[y];
        PC += 2;
    }

    //Set VX to VX AND VY
    private void opcode8XY2(byte x, byte y) {
        memory.V[x] &= memory.V[y];
        PC += 2;
    }

    //Set VX to VX XOR VY
    private void opcode8XY3(byte x, byte y) {
        memory.V[x] ^= memory.V[y];
        PC += 2;
    }

    //Add the value of register VY to register VX
    //Set VF to 01 if a carry occurs
    //Set VF to 00 if a carry does not occur
    private void opcode8XY4(byte x, byte y) {
        short sum = (short) (((memory.V[x] & 0xFF) + (memory.V[y] & 0xFF)) & 0x01FF);
        memory.V[x] = (byte) (sum & 0x00FF);
        memory.V[0xF] = (byte) ((sum>>>8) == 1 ? 1 : 0);
        PC += 2;
    }

    //Subtract the value of register VY from register VX
    //Set VF to 00 if a borrow occurs
    //Set VF to 01 if a borrow does not occur
    private void opcode8XY5(byte x, byte y) {
        short subtraction = (short) (((memory.V[x] & 0xFF) - (memory.V[y] & 0xFF)) & 0x01FF);
        memory.V[x] = (byte) (subtraction & 0x00FF);
        memory.V[0xF] = (byte) ((subtraction>>>8) == 1 ? 0 : 1);
        PC += 2;
    }

    //Store the value of register VY shifted right one bit in register VX
    //Set register VF to the least significant bit prior to the shift
    //If using alternative 8XY6 and 8XYE instructions the value in register VX is shifted
    //and the register VY is not used.
    private void opcode8XY6(byte x, byte y) {
        byte registerValue = memory.V[alternative8XY68XYE ? x : y];
        memory.V[x] = (byte) ((registerValue >>> 1) & 0x7F);
        memory.V[0xF] = (byte) (registerValue & 0x1);
        PC += 2;
    }

    //Set register VX to the value of VY minus VX
    //Set VF to 00 if a borrow occurs
    //Set VF to 01 if a borrow does not occur
    private void opcode8XY7(byte x, byte y) {
        short subtraction = (short) (((memory.V[y] & 0xFF) - (memory.V[x] & 0xFF)) & 0x01FF);
        memory.V[x] = (byte) (subtraction & 0x00FF);
        memory.V[0xF] = (byte) ((subtraction>>>8) == 1 ? 0 : 1);
        PC += 2;
    }

    //Store the value of register VY shifted left one bit in register VX
    //Set register VF to the most significant bit prior to the shift
    //If using alternative 8XY6 and 8XYE instructions the value in register VX is shifted
    //and the register VY is not used.
    private void opcode8XYE(byte x, byte y) {
        byte registerValue = memory.V[alternative8XY68XYE ? x : y];
        memory.V[x] = (byte) ((registerValue << 1) & 0xFE);
        memory.V[0xF] = (byte) ((registerValue >>> 7) & 0x1);
        PC += 2;
    }

    //Skip the following instruction if the value of register VX is not equal to the value of register VY
    private void opcode9XY0(byte x, byte y) {
        if (memory.V[x] != memory.V[y]) PC += 2;
        PC += 2;
    }

    //Store memory address NNN in register I
    private void opcodeANNN(short nnn) {
        memory.I = nnn;
        PC += 2;
    }

    //Jump to address NNN + V0
    private void opcodeBNNN(short nnn) {
        PC = (short) ((memory.V[0] & 0xFF) + nnn);
    }

    //Set VX to a random number with a mask of NN
    private void opcodeCXNN(byte x, byte nn) {
        byte randomNum = (byte) ThreadLocalRandom.current().nextInt(0, 256);
        memory.V[x] = (byte) (randomNum & nn);
        PC += 2;
    }

    //Draw a sprite at position VX, VY with N bytes of sprite data starting at the address stored in I
    //Set VF to 01 if any set pixels are changed to unset, and 00 otherwise
    private void opcodeDXYN(byte x, byte y, byte n) {
        byte[] sprite = memory.getByte(memory.I, n);
        memory.V[0xF] = (byte) (screen.drawSprite(sprite, memory.V[x], memory.V[y]) ? 1 : 0);
        PC += 2;
    }

    //Skip the following instruction if the key corresponding to the hex value currently stored in register VX is pressed
    private void opcodeEX9E(byte x) {
        if (keyboard.isKeyPressed(memory.V[x])) PC += 2;
        PC += 2;
    }

    //Skip the following instruction if the key corresponding to the hex value currently stored in register VX is not pressed
    private void opcodeEXA1(byte x) {
        if (!keyboard.isKeyPressed(memory.V[x])) PC += 2;
        PC += 2;
    }

    //Store the current value of the delay timer in register VX
    private void opcodeFX07(byte x) {
        memory.V[x] = timers.getDelayTimer();
        PC += 2;
    }

    //Wait for a key press and store the result in register VX
    private void opcodeFX0A(byte x) {
        byte key = 0;
        try {
            key = keyboard.waitKey(-1, true);
        } catch (InterruptedException e) { }
        if (!(key>=0 && key<keyboard.numKeys)) {
            //Defaults to key 0 if there is an error getting the key
            System.err.println("Error at opcode FX0A, invalid key press");
            key = 0;
        }
        memory.V[x] = key;
        PC += 2;
    }

    //Set the delay timer to the value of register VX
    private void opcodeFX15(byte x) {
        timers.setDelayTimer(memory.V[x]);
        PC += 2;
    }

    //Set the sound timer to the value of register VX
    private void opcodeFX18(byte x) {
        timers.setSoundTimer(memory.V[x]);
        PC += 2;
    }

    //Add the value stored in register VX to register I
    private void opcodeFX1E(byte x) {
        memory.I += (memory.V[x] & 0xFF);
        PC += 2;
    }

    //Set I to the memory address of the sprite data corresponding to the hexadecimal digit stored in register VX
    private void opcodeFX29(byte x) {
        memory.I = (short) (memory.fontLocation + (memory.V[x] & 0xFF) * Memory.FONT[0].length);
        PC += 2;
    }

    //Store the binary-coded decimal equivalent of the value stored in register VX at addresses I, I+1, and I+2
    private void opcodeFX33(byte x) {
        short num = (short) (memory.V[x] & 0xFF);
        byte[] digits = new byte[3];
        digits[2] = (byte) (num % 10);
        num /= 10;
        digits[1] = (byte) (num % 10);
        num /= 10;
        digits[0] = (byte) (num % 10);
        memory.setByte(digits, memory.I);
        PC += 2;
    }

    //Store the values of registers V0 to VX inclusive in memory starting at address I
    //I is set to I + X + 1 after operation
    //If using alternative FX55 and FX65 instructions the I register is not modified
    private void opcodeFX55(byte x) {
        byte[] values = new byte[x+1];
        System.arraycopy(memory.V, 0, values, 0, x + 1);
        memory.setByte(values, memory.I);
        if (!alternativeFX55FX65) memory.I += x + 1;
        PC += 2;
    }

    //Fill registers V0 to VX inclusive with the values stored in memory starting at address I
    //I is set to I + X + 1 after operation
    //If using alternative FX55 and FX65 instructions the I register is not modified
    private void opcodeFX65(byte x) {
        byte[] values = memory.getByte(memory.I, (byte) (x + 1));
        System.arraycopy(values, 0, memory.V, 0, values.length);
        if (!alternativeFX55FX65) memory.I += x + 1;
        PC += 2;
    }

}
