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

    //How many instructions have been executed
    private long cycleCounter = 0;

    //How many instructions per second should be executed, 0 for unlimited
    private int frequency;

    //Time at which the CHIP-8 emulation started
    private final long startTime;

    //Stack
    private final ArrayDeque<Short> stack = new ArrayDeque<>();

    //Program counter
    private short PC;

    //Most of the modern CHIP-8 interpreters have instructions with slightly different implementations to those used by
    //older interpreters such as the one in the RCA COSMAC VIP computer. A lot of newer CHIP-8 programs won't work with
    //the older implementations so a flag is needed to be able to chose between the implementations. By default the
    //newer implementation will be used as it seems to be the more common one among popular CHIP-8 programs.
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
    public long getCycleCounter() {
        return cycleCounter;
    }

    /**
     * Getter for the program counter
     * @return current program counter
     */
    public short getProgramCounter() {
        return PC;
    }

    //Convert a short to an array of four nibbles
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
        /*It is needed to slow down the processing unit to match the frequency set in the frequency attribute
        This method will wait in order to match the required frequency
        This method will only get called before executing an instruction which requieres to be accurate in time.
        For example, the method will get called before drawing on the screen, modifying a timer or waiting for user input
        but it will not be called before performing an arithmetic operation on the memory.
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
                long expectedRunningTime = (long) ((double) cycleCounter/frequency*1_000_000_000L);
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

        outerSwitch:
        switch (opcodeNibble[0]) {
            case 0x0:
                if (opcodeInt == 0x00E0) opcode00E0();
                else if (opcodeInt == 0x00EE) opcode00EE();
                else opcode0NNN();
                break;
            case 0x1:
                opcode1NNN(opcode);
                break;
            case 0x2:
                opcode2NNN(opcode);
                break;
            case 0x3:
                opcode3XNN(opcode);
                break;
            case 0x4:
                opcode4XNN(opcode);
                break;
            case 0x5:
                opcode5XY0(opcode);
                break;
            case 0x6:
                opcode6XNN(opcode);
                break;
            case 0x7:
                opcode7XNN(opcode);
                break;
            case 0x8:
                switch (opcodeNibble[3]) {
                    case 0x0:
                        opcode8XY0(opcode);
                        break outerSwitch;
                    case 0x1:
                        opcode8XY1(opcode);
                        break outerSwitch;
                    case 0x2:
                        opcode8XY2(opcode);
                        break outerSwitch;
                    case 0x3:
                        opcode8XY3(opcode);
                        break outerSwitch;
                    case 0x4:
                        opcode8XY4(opcode);
                        break outerSwitch;
                    case 0x5:
                        opcode8XY5(opcode);
                        break outerSwitch;
                    case 0x6:
                        opcode8XY6(opcode);
                        break outerSwitch;
                    case 0x7:
                        opcode8XY7(opcode);
                        break outerSwitch;
                    case 0xE:
                        opcode8XYE(opcode);
                        break outerSwitch;
                }
            case 0x9:
                opcode9XY0(opcode);
                break;
            case 0xA:
                opcodeANNN(opcode);
                break;
            case 0xB:
                opcodeBNNN(opcode);
                break;
            case 0xC:
                opcodeCXNN(opcode);
                break;
            case 0xD:
                opcodeDXYN(opcode);
                break;
            case 0xE:
                if (opcodeNibble[2] == 0x9 && opcodeNibble[3] == 0xE) {
                    opcodeEX9E(opcode);
                    break;
                } else if (opcodeNibble[2] == 0xA && opcodeNibble[3] == 0x1) {
                    opcodeEXA1(opcode);
                    break;
                }
            case 0xF:
                if (opcodeNibble[2] == 0x0 && opcodeNibble[3] == 0x7) {
                    opcodeFX07(opcode);
                    break;
                } else if (opcodeNibble[2] == 0x0 && opcodeNibble[3] == 0xA) {
                    opcodeFX0A(opcode);
                    break;
                } else if (opcodeNibble[2] == 0x1 && opcodeNibble[3] == 0x5) {
                    opcodeFX15(opcode);
                    break;
                } else if (opcodeNibble[2] == 0x1 && opcodeNibble[3] == 0x8) {
                    opcodeFX18(opcode);
                    break;
                } else if (opcodeNibble[2] == 0x1 && opcodeNibble[3] == 0xE) {
                    opcodeFX1E(opcode);
                    break;
                } else if (opcodeNibble[2] == 0x2 && opcodeNibble[3] == 0x9) {
                    opcodeFX29(opcode);
                    break;
                } else if (opcodeNibble[2] == 0x3 && opcodeNibble[3] == 0x3) {
                    opcodeFX33(opcode);
                    break;
                } else if (opcodeNibble[2] == 0x5 && opcodeNibble[3] == 0x5) {
                    opcodeFX55(opcode);
                    break;
                } else if (opcodeNibble[2] == 0x6 && opcodeNibble[3] == 0x5) {
                    opcodeFX65(opcode);
                    break;
                }
            default:
                System.err.println("Opcode not valid: " + Integer.toHexString(opcodeInt)
                        + "\nAt memory location: " + Integer.toHexString(PC) + "\nCycle: " + cycleCounter);
                System.exit(1);
        }
        cycleCounter++;
    }

    //The following 35 methods contain the code that will decode and execute the 35 CHIP-8 instructions

    //Execute machine language subroutine at address NNN
    private void opcode0NNN() {
        //This instruction originally was intended to execute machine code on the RCA COSMAC VIP computer but is now
        //considered deprecated. The program counter will get incremented but no actual machine code will be executed.
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
    private void opcode1NNN(short opcode) {
        PC = (short) (opcode & 0x0FFF);
    }

    //Execute subroutine at address NNN
    private void opcode2NNN(short opcode) {
        stack.push(PC);
        PC = (short) (opcode & 0x0FFF);
    }

    //Skip the following instruction if the value of register VX equals NN
    private void opcode3XNN(short opcode) {
        if (memory.V[(opcode>>>8) & 0x000F] == (byte) (opcode & 0x00FF)) {
            PC += 2;
        }
        PC += 2;
    }

    //Skip the following instruction if the value of register VX is not equal to NN
    private void opcode4XNN(short opcode) {
        if (memory.V[(opcode>>>8) & 0x000F] != (byte) (opcode & 0x00FF)) {
            PC += 2;
        }
        PC += 2;
    }

    //Skip the following instruction if the value of register VX is equal to the value of register VY
    private void opcode5XY0(short opcode) {
        if (memory.V[(opcode>>>8) & 0x000F] == memory.V[(opcode>>>4) & 0x000F]) {
            PC += 2;
        }
        PC += 2;
    }

    //Store number NN in register VX
    private void opcode6XNN(short opcode) {
        memory.V[(opcode>>>8) & 0x000F] = (byte) (opcode & 0x00FF);
        PC += 2;
    }

    //Add the value NN to register VX
    private void opcode7XNN(short opcode) {
        memory.V[(opcode>>>8) & 0x000F] += (byte) (opcode & 0x00FF);
        PC += 2;
    }

    //Store the value of register VY in register VX
    private void opcode8XY0(short opcode) {
        memory.V[(opcode>>>8) & 0x000F] = memory.V[(opcode>>>4) & 0x000F];
        PC += 2;
    }

    //Set VX to VX OR VY
    private void opcode8XY1(short opcode) {
        memory.V[(opcode>>>8) & 0x000F] |= memory.V[(opcode>>>4) & 0x000F];
        PC += 2;
    }

    //Set VX to VX AND VY
    private void opcode8XY2(short opcode) {
        memory.V[(opcode>>>8) & 0x000F] &= memory.V[(opcode>>>4) & 0x000F];
        PC += 2;
    }

    //Set VX to VX XOR VY
    private void opcode8XY3(short opcode) {
        memory.V[(opcode>>>8) & 0x000F] ^= memory.V[(opcode>>>4) & 0x000F];
        PC += 2;
    }

    //Add the value of register VY to register VX
    //Set VF to 01 if a carry occurs
    //Set VF to 00 if a carry does not occur
    private void opcode8XY4(short opcode) {
        short sum = (short) (((memory.V[(opcode>>>8) & 0x000F] & 0xFF) + (memory.V[(opcode>>>4) & 0x000F] & 0xFF)) & 0x01FF);
        memory.V[(opcode>>>8) & 0x000F] = (byte) (sum & 0x00FF);
        memory.V[0xF] = (sum>>>8) == 1 ? (byte) 1 : (byte) 0;
        PC += 2;
    }

    //Subtract the value of register VY from register VX
    //Set VF to 00 if a borrow occurs
    //Set VF to 01 if a borrow does not occur
    private void opcode8XY5(short opcode) {
        short subtraction = (short) (((memory.V[(opcode>>>8) & 0x000F] & 0xFF) - (memory.V[(opcode>>>4) & 0x000F] & 0xFF)) & 0x01FF);
        memory.V[(opcode>>>8) & 0x000F] = (byte) (subtraction & 0x00FF);
        memory.V[0xF] = (subtraction>>>8) == 1 ? (byte) 0 : (byte) 1;
        PC += 2;
    }

    //Store the value of register VY shifted right one bit in register VX
    //Set register VF to the least significant bit prior to the shift
    //If using alternative 8XY6 and 8XYE instructions the value in register VX is shifted
    //and the register VY is not used.
    private void opcode8XY6(short opcode) {
        if (alternative8XY68XYE) {
            byte VXvalue = memory.V[(opcode>>>8) & 0x000F];
            memory.V[(opcode>>>8) & 0x000F] = (byte) ((VXvalue >>> 1) & 0x7F);
            memory.V[0xF] = (byte) (VXvalue & 0x1);
            PC += 2;
        } else{
            byte VYvalue = memory.V[(opcode>>>4) & 0x000F];
            memory.V[(opcode>>>8) & 0x000F] = (byte) ((VYvalue >>> 1) & 0x7F);
            memory.V[0xF] = (byte) (VYvalue & 0x1);
            PC += 2;
        }
    }

    //Set register VX to the value of VY minus VX
    //Set VF to 00 if a borrow occurs
    //Set VF to 01 if a borrow does not occur
    private void opcode8XY7(short opcode) {
        short subtraction = (short) (((memory.V[(opcode>>>4) & 0x000F] & 0xFF) - (memory.V[(opcode>>>8) & 0x000F] & 0xFF)) & 0x01FF);
        memory.V[(opcode>>>8) & 0x000F] = (byte) (subtraction & 0x00FF);
        memory.V[0xF] = (subtraction>>>8) == 1 ? (byte) 0 : (byte) 1;
        PC += 2;
    }

    //Store the value of register VY shifted left one bit in register VX
    //Set register VF to the most significant bit prior to the shift
    //If using alternative 8XY6 and 8XYE instructions the value in register VX is shifted
    //and the register VY is not used.
    private void opcode8XYE(short opcode) {
        if (alternative8XY68XYE) {
            byte VXvalue = memory.V[(opcode>>>8) & 0x000F];
            memory.V[(opcode>>>8) & 0x000F] = (byte) ((VXvalue << 1) & 0xFE);
            memory.V[0xF] = (byte) ((VXvalue >>> 7) & 0x1);
            PC += 2;
        } else {
            byte VYvalue = memory.V[(opcode>>>4) & 0x000F];
            memory.V[(opcode>>>8) & 0x000F] = (byte) ((VYvalue << 1) & 0xFE);
            memory.V[0xF] = (byte) ((VYvalue >>> 7) & 0x1);
            PC += 2;
        }
    }

    //Skip the following instruction if the value of register VX is not equal to the value of register VY
    private void opcode9XY0(short opcode) {
        if (memory.V[(opcode>>>8) & 0x000F] != memory.V[(opcode>>>4) & 0x000F]) {
            PC += 2;
        }
        PC += 2;
    }

    //Store memory address NNN in register I
    private void opcodeANNN(short opcode) {
        memory.I = (short) (opcode & 0x0FFF);
        PC += 2;
    }

    //Jump to address NNN + V0
    private void opcodeBNNN(short opcode) {
        PC = (short) ((memory.V[0] & 0xFF) + (opcode & 0x0FFF));
    }

    //Set VX to a random number with a mask of NN
    private void opcodeCXNN(short opcode) {
        byte randomNum = (byte) ThreadLocalRandom.current().nextInt(0, 256);
        memory.V[(opcode>>>8) & 0x000F] = (byte) (randomNum & (opcode & 0x00FF));
        PC += 2;
    }

    //Draw a sprite at position VX, VY with N bytes of sprite data starting at the address stored in I
    //Set VF to 01 if any set pixels are changed to unset, and 00 otherwise
    private void opcodeDXYN(short opcode) {
        byte x = memory.V[(opcode>>>8) & 0x000F];
        byte y = memory.V[(opcode>>>4) & 0x000F];
        byte[] sprite = memory.getByte(memory.I, (byte) (opcode & 0x000F));
        memory.V[0xF] = screen.drawSprite(sprite, x, y) ? (byte) 1 : (byte) 0;
        PC += 2;
    }

    //Skip the following instruction if the key corresponding to the hex value currently stored in register VX is pressed
    private void opcodeEX9E(short opcode) {
        byte key = memory.V[(opcode>>>8) & 0x000F];
        if (keyboard.isKeyPressed(key)) {
            PC += 2;
        }
        PC += 2;
    }

    //Skip the following instruction if the key corresponding to the hex value currently stored in register VX is not pressed
    private void opcodeEXA1(short opcode) {
        byte key = memory.V[(opcode>>>8) & 0x000F];
        if (!keyboard.isKeyPressed(key)) {
            PC += 2;
        }
        PC += 2;
    }

    //Store the current value of the delay timer in register VX
    private void opcodeFX07(short opcode) {
        memory.V[(opcode>>>8) & 0x000F] = timers.getDelayTimer();
        PC += 2;
    }

    //Wait for a key press and store the result in register VX
    private void opcodeFX0A(short opcode) {
        byte key = 0;
        try {
            key = keyboard.waitKey(-1);
        } catch (InterruptedException e) { }
        if (!(key>=0 && key<keyboard.numKeys)) {
            //Defaults to key 0 if there is an error getting the key
            System.err.println("Error at opcode FX0A, invalid key press");
            key = 0;
        }
        memory.V[(opcode>>>8) & 0x000F] = key;
        PC += 2;
    }

    //Set the delay timer to the value of register VX
    private void opcodeFX15(short opcode) {
        timers.setDelayTimer(memory.V[(opcode>>>8) & 0x000F]);
        PC += 2;
    }

    //Set the sound timer to the value of register VX
    private void opcodeFX18(short opcode) {
        timers.setSoundTimer(memory.V[(opcode>>>8) & 0x000F]);
        PC += 2;
    }

    //Add the value stored in register VX to register I
    private void opcodeFX1E(short opcode) {
        memory.I += (memory.V[(opcode>>>8) & 0x000F] & 0xFF);
        PC += 2;
    }

    //Set I to the memory address of the sprite data corresponding to the hexadecimal digit stored in register VX
    private void opcodeFX29(short opcode) {
        memory.I = (short) (memory.fontLocation + (memory.V[(opcode>>>8) & 0x000F] & 0xFF) * Memory.FONT[0].length);
        PC += 2;
    }

    //Store the binary-coded decimal equivalent of the value stored in register VX at addresses I, I+1, and I+2
    private void opcodeFX33(short opcode) {
        short num = (short) (memory.V[(opcode>>>8) & 0x000F] & 0xFF);
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
    private void opcodeFX55(short opcode) {
        byte X = (byte) ((opcode>>>8) & 0x000F);
        byte[] values = new byte[X+1];
        System.arraycopy(memory.V, 0, values, 0, X + 1);
        memory.setByte(values, memory.I);
        if (!alternativeFX55FX65) memory.I += X + 1;
        PC += 2;
    }

    //Fill registers V0 to VX inclusive with the values stored in memory starting at address I
    //I is set to I + X + 1 after operation
    //If using alternative FX55 and FX65 instructions the I register is not modified
    private void opcodeFX65(short opcode) {
        byte X = (byte) ((opcode>>>8) & 0x000F);
        byte[] values = memory.getByte(memory.I, (byte) (X + 1));
        System.arraycopy(values, 0, memory.V, 0, values.length);
        if (!alternativeFX55FX65) memory.I += X + 1;
        PC += 2;
    }

}
