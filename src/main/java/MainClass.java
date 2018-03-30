import java.awt.*;

public class MainClass {

    static GeneratorThread thr = new GeneratorThread( "generator-thread-1" );
    static BlockFinderThread bhr = new BlockFinderThread( "generator-thread-1" );

    public static void main(String [] args)
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

        thr.launch();
        bhr.launch();
        Blockchain.showWindow();
    }
}
