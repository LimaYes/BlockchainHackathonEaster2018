import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;


public class BlockFinderThread implements Runnable {

    private Thread t;
    private String threadName;
    Date last = new Date();
    int min = 30;
    int max = 60;
    double chance_for_quickblock = 0.15;
    int hittime = BlockFinderThread.randInt(min, max);
    static Random rand = new Random();

    public static int randInt(int min, int max) {
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }


    BlockFinderThread(String name) {
        threadName = name;
    }

    public void launch () {
        Logger.getGlobal().info("Starting " + threadName + ", next hit in " + hittime + " s");
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }

    public void run() {

        while(true) {
            Date now = new Date();
            long seconds = (now.getTime()-last.getTime())/1000;
            if(seconds>=hittime){
                last = new Date();
                hittime = BlockFinderThread.randInt(min, max);
                // make a quick one
                if(rand.nextDouble()<chance_for_quickblock) hittime=2;

                Blockchain.generateNewBlock();
                Logger.getGlobal().info("new block generated, next hit in " + hittime + " s");

                if(hittime==2)
                    Logger.getGlobal().warning("QUICK BLOCK AHEAD!");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
