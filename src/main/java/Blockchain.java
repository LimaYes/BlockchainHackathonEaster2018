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
        target = Long.MAX_VALUE / 100;
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
        Logger.getGlobal().info("Found new block! (height: " + log.size() + ", tx num = " + currentBlock.number + ")");

        if(log.size()>0)
            currentBlock.prevBlock = (Block) log.get(log.size()-1);
        log.add(currentBlock);

        dd.addBlock(log.size(), currentBlock.number, currentBlock.duration, currentBlock);

        Logger.getGlobal().info("Add - POW: " + currentBlock.number + ", Total: " + currentBlock.total + ", Duration: " + currentBlock.duration + ")");

        currentBlock = new Block();
        lastAdded = new Date();

        retarget();

        _mutex.unlock();

    }

        /* You are only allowed to edit inbetween this comment and the comment signalizing the end.
        You furthermore are only allowed to use the information in the Block objects stored in the Blockchain array called log.
        Performance and storage must be in O(1) regardless of the length of the blockchain.
         */
        
    static int POW_RETARGET_DEPTH = 20;
    static int WE_WANT_X_POW_PER_MINUTE = 10;
    private static void retarget() {
        // here, retarget the target value!!!
        int powcnt = 0;
        double nTargetTimespan = 0;
        double nActualTimespan = 0;
        long powTarget = 0;

        int powMass = 0;
        int totalDuration = 0;
        long targetMass = 0;
        int logDepth = 0;
        int curBlockIdx = log.size()-1;

        if(curBlockIdx < POW_RETARGET_DEPTH)
            logDepth = logDepth = Math.min(curBlockIdx, POW_RETARGET_DEPTH);
        else
            logDepth = POW_RETARGET_DEPTH;

        Logger.getGlobal().info("depth = " + logDepth );

        int numBlocks = 1;

        if(curBlockIdx == 0) {
            powMass = ((Block)log.get(0)).total;
            totalDuration = ((Block)log.get(0)).duration;
            targetMass = ((Block)log.get(0)).target;
            Logger.getGlobal().info("First Block - POWs = " +((Block)log.get(0)).total + ", Duration = " + ((Block)log.get(0)).duration + ", target = " + ((Block)log.get(0)).target );
        }
        else {
            for(int i = curBlockIdx; i >= (curBlockIdx + 1 - logDepth); --i) {
                powMass += ((Block)log.get(i)).total;
                totalDuration += ((Block)log.get(i)).duration;

                targetMass =+ ((Block)log.get(i - 1)).target;

                Logger.getGlobal().info("i = " + i + ", POWs = " +((Block)log.get(i)).total + ", Duration = " + ((Block)log.get(i)).duration + ", target = " + ((Block)log.get(i - 1)).target );
            }
        }

        if(powMass==0) powMass = 1;

        long avgTarget = targetMass / numBlocks;

        double curPowSecs = ((double)((Block)log.get(curBlockIdx)).duration / (double)((Block)log.get(curBlockIdx)).total);
        double avgPowSecs = ((double)totalDuration / (double)powMass);
        double tgtPowSecs = 60.0 / (double)(WE_WANT_X_POW_PER_MINUTE);

        long curTarget = 0;
        long newTarget = 0;
        long newAvgTgt = 0;
        long newCurTgt = 0;

        if (curBlockIdx == 0)
            curTarget = ((Block)(log.get(curBlockIdx))).target;
        else
            curTarget = ((Block)(log.get(curBlockIdx - 1))).target;

        // If Difficulty Is Within 5% Of Target, Keep Using It
        if ((curPowSecs <= (tgtPowSecs * 1.05)) && (curPowSecs >= (tgtPowSecs * 0.95))) {
            newTarget = curTarget;
            Logger.getGlobal().info("Keep Last Target: " + newTarget);
        }

        // Determine If It's Better To Use Adj Average Diff, or Adj Current Diff
        else {
            newAvgTgt = (long)((double)avgTarget * (avgPowSecs / tgtPowSecs));
            newCurTgt = (long)((double)curTarget * (curPowSecs / tgtPowSecs));

            if (curPowSecs > tgtPowSecs) {
                if (newAvgTgt < curTarget)
                    newTarget = newCurTgt;
                else
                    newTarget = newAvgTgt;
            }
            else {
                if (newAvgTgt > curTarget)
                    newTarget = newCurTgt;
                else
                    newTarget = newAvgTgt;
            }
        }

        powTarget = newTarget;

        Logger.getGlobal().info("Cur POW(sec) = " + curPowSecs + ", Avg POW(sec) = " + avgPowSecs + ", Tgt POW(sec) = " + tgtPowSecs);
        Logger.getGlobal().info("Cur Tgt = " + curTarget + ", Avg Tgt = " + avgTarget + ", New Tgt = " + newTarget);


        BigInteger myTarget = MAXIMAL_WORK_TARGET;
        myTarget = myTarget.divide(BigInteger.valueOf(Long.MAX_VALUE/100)); // Note, our target in compact form is in range 1..LONG_MAX/100
        myTarget = myTarget.multiply(BigInteger.valueOf(powTarget));
        if(myTarget.compareTo(MAXIMAL_WORK_TARGET) == 1)
            myTarget = MAXIMAL_WORK_TARGET;
        if(myTarget.compareTo(BigInteger.ONE) == 2)
            myTarget = BigInteger.ONE;
        ((Block)(log.get(curBlockIdx))).target = powTarget;

        CURRENT = myTarget;
        Logger.getGlobal().info("new minimal target = " + myTarget.toString(16) + ", powT = " + powTarget + ", lastpowT = " +  targetMass + ", #blocks = " + log.size() + ", powMass = " + powMass + ", actTime = " + nActualTimespan + ", targetTime = " + nTargetTimespan + ", adjRatio = " + (nActualTimespan/nTargetTimespan));


        for(int i = curBlockIdx; i >= (curBlockIdx + 1 - logDepth); --i) {
            Logger.getGlobal().info("ii = " + i + ", target = " + ((Block)log.get(i)).target );
        }
    }

        /* End of editing section
         */
        
}
