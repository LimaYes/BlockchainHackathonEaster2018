import javafx.util.Pair;

import java.awt.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

class Block
{
    public int number;
    public int total;
    public int duration;
    public long target;
    Block prevBlock = null;

    public Block() {
        target = Long.MAX_VALUE / 10000;
    }

}

public class Blockchain {
    static private final Lock _mutex = new ReentrantLock(true);

    public static List log = new ArrayList<>();
    public static final BigInteger MAXIMAL_WORK_TARGET = new BigInteger("0000FFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    public static BigInteger CURRENT = new BigInteger("0000FFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    public static Date lastAdded = new Date();
    public static Block currentBlock = new Block();
    public static int limit = 25;
    public static DynamicChart dd = new DynamicChart();

    public static void showWindow() {
        EventQueue.invokeLater(() -> Blockchain.dd.display());
    }

    public static boolean checkWork(String hex){
        _mutex.lock();

        BigInteger test = new BigInteger(hex, 16);
        if(test.compareTo(CURRENT)==-1){
            _mutex.unlock();

            currentBlock.total++;

            if(currentBlock.number == limit)
                return false; // do not allow more than 25 into it
            else
                return true;
        }
        _mutex.unlock();
        return false;
    }

    public static void add(){
        _mutex.lock();
        currentBlock.number++;

        if(currentBlock.number == limit)
            Logger.getGlobal().info("proof-of-work limit for current block hit, pausing");

        _mutex.unlock();
    }

    public static void generateNewBlock(){
        _mutex.lock();
        Date now = new Date();
        long seconds = (now.getTime()-lastAdded.getTime())/1000;
        currentBlock.duration = (int)seconds;
        Logger.getGlobal().info("Found new block! (height: " + log.size() + ", tx num = " + currentBlock.number + ", duration = " + seconds + ")");

        if(log.size()>0)
            currentBlock.prevBlock = (Block) log.get(log.size()-1);
        log.add(currentBlock);

        dd.addBlock(log.size(), currentBlock.number, currentBlock.duration, currentBlock);


        currentBlock = new Block();
        lastAdded = new Date();

        retarget();

        _mutex.unlock();

    }

        /* You are only allowed to edit inbetween this comment and the comment signalizing the end.
        You furthermore are only allowed to use the information in the Block objects stored in the Blockchain array called log.
        Performance and storage must be in O(1) regardless of the length of the blockchain.
         */

        static int WE_WANT_X_POW_PER_MINUTE = 10;
        private static void retarget() {
    
            if(log.size()<=1) {
                Logger.getGlobal().info("skipping retarget for first block");
                return;
            }
    
            long powTarget = 0;
            int currentBlockHeight = log.size()-1; // the starting block height for analysis
            int nActualTimespan = ((Block) log.get(currentBlockHeight)).duration;;
            int nActualPows = ((Block) log.get(currentBlockHeight)).number;
            long targetForThisBlock = ((Block) log.get(currentBlockHeight)).prevBlock.target;
            double nTargetTimespan = 0;
            double ratio = 0;
            if (nActualTimespan < 60*0.25){ // For too short blocks, let us just leave the target untouched
               powTarget = targetForThisBlock;
            }else {
    
                nTargetTimespan = (nActualPows * 60) / WE_WANT_X_POW_PER_MINUTE;
                ratio = nActualTimespan / nTargetTimespan;
    
                if (((Block) log.get(log.size() - 1)).number == 25) {
                    // if cap was hit, allow drastic changes
                    if (ratio < 0.10) ratio = 0.10;
                    else if (ratio > 1.9) ratio = 1.9;
                } else {
                    // Otherwise be conservative
                    // But take care, 20% means 34% up again
                    if (ratio < 0.75) ratio = 0.75;
                    else if (ratio > 1.34) ratio = 1.34;
                }
    
                powTarget = (long) (targetForThisBlock * ratio);
            }
    
            BigInteger myTarget = MAXIMAL_WORK_TARGET;
            myTarget = myTarget.divide(BigInteger.valueOf(Long.MAX_VALUE/10000)); // Note, our target in compact form is in range 1..LONG_MAX/100
            myTarget = myTarget.multiply(BigInteger.valueOf(powTarget));
            if(myTarget.compareTo(MAXIMAL_WORK_TARGET) == 1)
                myTarget = MAXIMAL_WORK_TARGET;
            if(myTarget.compareTo(BigInteger.ONE) == 2)
                myTarget = BigInteger.ONE;
    
    
    
            ((Block)(log.get(log.size()-1))).target = powTarget;
    
            CURRENT = myTarget;
            Logger.getGlobal().info("new minimal target = " + myTarget.toString(16) + ", thisTarget = " + targetForThisBlock + ", newT = " + powTarget + ", actTime = " + nActualTimespan + ", targetTime = " + nTargetTimespan + ", adjRatio = " + ratio);
    
        }
    
    /* End of editing section */

}

