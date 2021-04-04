package chip8;

public class Main {

    /**
     * Execute a file on the CHIP-8 interpreter
     * @param args file to be executed by the CHIP-8 interpreter
     */
    public static void main(String[] args) {
        if (args.length < 1) System.out.println("No file specified");
        Chip8 chip8 = new Chip8(args[0]);
    }

}