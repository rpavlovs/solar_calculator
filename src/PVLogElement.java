import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import org.jfree.ui.RectangleInsets;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;


public class PVLogElement {
	
	public static Map<Date, String> treeMap;
	
	private static CellProcessor[] getProcessors() {
		final CellProcessor[] processors = new CellProcessor[] {
				new NotNull(),  //device
				new ParseDate("dd/mm/yyyy HH:mm"),  //date + time
				new NotNull(),  //GHG_Factor
				new NotNull(),  //Tariff_Cost
				new NotNull(),  //Amps_Raw_Data
				new NotNull(),  //Amps_Raw Data_Min
				new NotNull(),  //Amps_Raw_Data_Max
				new NotNull(),  //kW_Raw_Data
				new NotNull(),  //kW_Raw_Data_Min
				new NotNull(),  //kW_Raw_Data_Max
				new NotNull(),  //Cost_Raw_Data
				new NotNull(),  //Cost_Raw_Data_Min
				new NotNull(),  //Cost_Raw_Data_Max
				new NotNull(),  //GHG_Raw_Data
				new NotNull(),  //GHG_Raw_Data_Min
				new NotNull(),   //GHG_Raw_Data_Max
				new NotNull()
		};
		
		return processors;
		
	}
public static void readWithCsvMapReader(File csvFile, JFrame frame, JLabel lblNewLabel1) throws Exception {
        
        ICsvMapReader mapReader = null;
        try {
                mapReader = new CsvMapReader(new FileReader(csvFile.getAbsolutePath()), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
                
                // the header columns are used as the keys to the Map
                final String[] header = mapReader.getHeader(true);
                for(int i=0; i<header.length; i++){
                	System.out.print(header[i]);
                }
                final CellProcessor[] processors = getProcessors();
                
                Map<String, Object> customerMap;
                treeMap = new TreeMap<Date, String>();
                while((customerMap = mapReader.read(header, processors)) != null) {
                //for(int i=0; i< 10; i++){
                	customerMap = mapReader.read(header, processors);
                    treeMap.put((Date) customerMap.get(header[1]), customerMap.get(header[4]).toString());                        
                }
                System.out.println("\nTreeMap");
                /*for (Map.Entry<Date, String> e : treeMap.entrySet()) {
                    System.out.println(e.getKey() + " " + e.getValue());
                }*/
                
                
        		
        		
                XYDataset dataset = createDataset();
        		JFreeChart chart = createChart(dataset);
        		ChartPanel chartPanel = new ChartPanel(chart);
        		chartPanel.setBounds(0, 100, 1007, 500);
        		frame.getContentPane().remove(lblNewLabel1);
        		frame.getContentPane().add(chartPanel);
        		frame.getContentPane().repaint();
        		
                
        }
        finally {
                if( mapReader != null ) {
                        mapReader.close();
                }
        }
        
}

private static JFreeChart createChart(XYDataset dataset) {
	// create the chart...
	JFreeChart chart = ChartFactory.createTimeSeriesChart(
	"Solar Estimator Chart",
	// chart title
	"Date",
	// x-axis label
	"Ampers",
	// y-axis label
	dataset,
	// data
	true,
	// include legend
	true,
	// tooltips
	false
	// urls
	);
	chart.setBackgroundPaint(Color.white);
	XYPlot plot = (XYPlot) chart.getPlot();
	plot.setBackgroundPaint(Color.lightGray);
	plot.setDomainGridlinePaint(Color.white);
	plot.setRangeGridlinePaint(Color.white);
	plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	plot.setDomainCrosshairVisible(true);
	plot.setRangeCrosshairVisible(true);
	XYItemRenderer r = plot.getRenderer();
	if (r instanceof XYLineAndShapeRenderer) {
	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
	renderer.setBaseShapesVisible(true);
	renderer.setBaseShapesFilled(true);
	}
	DateAxis axis = (DateAxis) plot.getDomainAxis();
	axis.setDateFormatOverride(new SimpleDateFormat("mm/dd-hh:mm"));
	return chart;
}

private static XYDataset createDataset() {
	TimeSeries series1 = new TimeSeries("Amps Raw Data");
	TimeSeriesCollection dataset = new TimeSeriesCollection();
	for (Map.Entry<Date, String> e : PVLogElement.treeMap.entrySet()){
		
		series1.add(new Minute(e.getKey()), Double.parseDouble(e.getValue().replaceAll(",", ".")));
	}
	dataset.addSeries(series1);
	return dataset;
	}

}
