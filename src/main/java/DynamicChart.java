import java.awt.*;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class DynamicChart {

    private int n = 1;
    final XYSeries series = new XYSeries("Actual Transactions in Block");
    final XYSeries series2 = new XYSeries("Normalized count in TX/Minute (5 blk sliding window)");
    final XYSeries series3 = new XYSeries("Abs Error (actual tx in block vs optimal case)");

    void display() {
        JFrame f = new JFrame("Blockchain Retargeting Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTabbedPane jtp = new JTabbedPane();
        jtp.add(String.valueOf(n), createPane());
        f.add(jtp, BorderLayout.CENTER);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

     public void addBlock(int index, int count, int time, Block b){
         series.add(index, count);
         series3.add(index, Math.abs(time*(((double)Blockchain.WE_WANT_X_POW_PER_MINUTE)/60.0)-count));
         double averager = 0;
         int cntr = 0;
         Block tmp = b;
         int totalduration = 0;
         while(cntr<5 && tmp!=null){
             cntr++;
             averager += tmp.number;
             totalduration += tmp.duration;
             tmp = tmp.prevBlock;
         }
         double txpersec = (((double)averager)*60)/((double)totalduration);
         Logger.getGlobal().info("AVERAGE TX/SEC WAS " + txpersec + ", totalTx = " + averager + ", time = " + totalduration + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
         if(cntr!=5)
             series2.add(index, 0);
         else
             series2.add(index, Math.min(25,txpersec));
     }

    private ChartPanel createPane() {

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        dataset.addSeries(series3);
        dataset.addSeries(series2);
        JFreeChart chart = ChartFactory.createXYLineChart("Blockchain Regarteting Test", "Blockchain Height",
                "#tx", dataset, PlotOrientation.VERTICAL, true, false, false);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(){
            Stroke soild = new BasicStroke(2.0f);
            Stroke dashed =  new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f);
            @Override
            public Stroke getItemStroke(int row, int column) {
                if (row == 2){
                    double x = dataset.getXValue(row, column);
                    if ( x > 4){
                        return dashed;
                    } else {
                        return soild;
                    }
                } else
                    return super.getItemStroke(row, column);
            }
        };
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseShapesFilled(true);
        renderer.setBaseStroke(new BasicStroke(3));
        plot.setRenderer(renderer);


        return new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1600, 800);
            }
        };
    }

}