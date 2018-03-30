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
        if(currentBlock.number == limit) {
            _mutex.unlock();
            return false; // do not allow more than 25 into it
        }

        BigInteger test = new BigInteger(hex, 16);
        if(test.compareTo(CURRENT)==-1){
            _mutex.unlock();
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


        currentBlock = new Block();
        lastAdded = new Date();

        retarget();

        _mutex.unlock();

    }

    /* You are only allowed to edit inbetween this comment and the comment signalizing the end.
    You furthermore are only allowed to use the information in the Block objects stored in the Blockchain array called log.
    Performance and storage must be in O(1) regardless of the length of the blockchain.
     */

    static int POW_RETARGET_DEPTH = 10;
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
        for(int i=log.size()-1; i>=Math.max(log.size() - POW_RETARGET_DEPTH, 0); --i) {
            powMass += ((Block)log.get(i)).number;
            totalDuration += ((Block)log.get(i)).duration;
            targetMass =+ ((Block)log.get(i)).target;
        }



        // Dirty fix, always assume a pow of one, otherwise things just stall
        if(powMass==0) powMass = 1;

        double darkTarget = (double)targetMass;
        darkTarget /= log.size();
        nActualTimespan = totalDuration;

        nTargetTimespan = nActualTimespan;
        nTargetTimespan = powMass * (60 / WE_WANT_X_POW_PER_MINUTE);

        if (nActualTimespan < nTargetTimespan / 3.0)
            nActualTimespan = nTargetTimespan / 3.0;
        if (nActualTimespan > nTargetTimespan * 3.0)
            nActualTimespan = nTargetTimespan * 3.0;

        double tmp = darkTarget;
        darkTarget = (darkTarget / nTargetTimespan)*nActualTimespan;

        if((nActualTimespan>nTargetTimespan && darkTarget<tmp) || (darkTarget > Long.MAX_VALUE / 100)){
            darkTarget = Long.MAX_VALUE / 100;
        }
        else if((nActualTimespan<nTargetTimespan  && darkTarget>tmp) || (darkTarget < 1)){
            darkTarget = 1;
        }
        powTarget = (long)darkTarget;


        BigInteger myTarget = MAXIMAL_WORK_TARGET;
        myTarget = myTarget.divide(BigInteger.valueOf(Long.MAX_VALUE/100)); // Note, our target in compact form is in range 1..LONG_MAX/100
        myTarget = myTarget.multiply(BigInteger.valueOf(powTarget));
        if(myTarget.compareTo(MAXIMAL_WORK_TARGET) == 1)
            myTarget = MAXIMAL_WORK_TARGET;
        if(myTarget.compareTo(BigInteger.ONE) == 2)
            myTarget = BigInteger.ONE;
        ((Block)(log.get(log.size()-1))).target = powTarget;

        CURRENT = myTarget;
        Logger.getGlobal().info("new minimal target = " + myTarget.toString(16) + ", powT = " + powTarget + ", lastpowT = " +  targetMass + ", #blocks = " + log.size() + ", powMass = " + powMass + ", actTime = " + nActualTimespan + ", targetTime = " + nTargetTimespan + ", adjRatio = " + (nActualTimespan/nTargetTimespan));
    }

    /* End of editing section
     */

}
