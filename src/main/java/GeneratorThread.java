import java.util.logging.Logger;

import java.util.Objects;
import java.util.Random;

class RandomString {

    /**
     * Generate a random string.
     */
    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    public static final String upper = "ABCDEF";
    public static final String digits = "0123456789";
    public static final String alphanum = upper + digits;
    private final Random random;
    private final char[] symbols;
    private final char[] buf;

    public RandomString(int length, Random random, String symbols) {
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an alphanumeric string generator.
     */
    public RandomString(int length, Random random) {
        this(length, random, alphanum);
    }

    /**
     * Create an alphanumeric strings from a secure generator.
     */
    public RandomString(int length) {
        this(length, new Random());
    }

    /**
     * Create session identifiers.
     */
    public RandomString() {
        this(32);
    }

}

public class GeneratorThread implements Runnable {

    private Thread t;
    private String threadName;

    GeneratorThread(String name) {
        threadName = name;
    }

    public void launch () {
        Logger.getGlobal().info("Starting " + threadName);
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }

    public void run() {

        while(true) {

            String guess = (new RandomString()).nextString();
            boolean check = Blockchain.checkWork(guess);
            if(check){
                Logger.getGlobal().info("   -- found proof-of-work: " + guess);
                Blockchain.add();
            }
        }

    }
}
