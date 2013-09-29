import javax.swing.*;
import javax.swing.filechooser.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.awt.EventQueue;
import java.awt.event.*;

import javax.swing.border.TitledBorder;

import com.toedter.calendar.JDateChooser;

import java.awt.FlowLayout;

import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.GridLayout;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import net.miginfocom.swing.MigLayout;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.BevelBorder;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.jfree.data.general.*;

import java.util.Date;
import java.util.Calendar;

public class MainWindow {

	private JFrame frame;
	private File consumptionFile;
	private File pvLogFile;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1300, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBounds(-1, 5, 1287, 66);
		panel_5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panel = new JPanel();
		panel_5.add(panel);
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Power consumption", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		final JButton consumptionButton = new JButton("Choose file");
		final TimeSeries ConsumptionSeries = new TimeSeries("Consumption, kWh");
		final JDateChooser dateChooserBegin = new JDateChooser();
		final JDateChooser dateChooserEnd = new JDateChooser();
		panel.add(consumptionButton);
		consumptionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		
				System.out.println("Consumption button was pressed!");
				JFileChooser fileopen = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
				        "CSV", "csv", "txt");
				fileopen.setFileFilter(filter);
				int ret = fileopen.showDialog(frame,"Open Consumption Log file");
				if (ret == JFileChooser.APPROVE_OPTION) {
					JLabel lblNewLabel1 = new JLabel("Please wait, it's really loading..");
	        		lblNewLabel1.setBounds(500, 500, 200, 15);
	        		frame.getContentPane().add(lblNewLabel1);
				    consumptionFile = fileopen.getSelectedFile();
					System.out.println("Consumption file is:"+consumptionFile.getName());
//					consumptionButton.setText(consumptionFile.getName());
					try {
						
		        		frame.setVisible(true);
		        		PVLogElement.readWithCsvMapReader2(consumptionFile, ConsumptionSeries, dateChooserBegin, dateChooserEnd, true);
		        		
//						PVLogElement.readWithCsvMapReader(consumptionFile, frame, lblNewLabel1);
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
		JPanel panel_1 = new JPanel();
		panel_5.add(panel_1);
		panel_1.setBorder(new TitledBorder(null, "Insolation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		final JButton pvLogButton = new JButton("Choose file");
		final TimeSeries ProductionSeries = new TimeSeries("Production, kWh");
		final TimeSeries PVSeries = new TimeSeries("PV production, kWh");
		final TimeSeries BASeries = new TimeSeries("Battery interchange, kWh"); // TODO: А может и не надо

		panel_1.add(pvLogButton);
		
		JLabel lblAzimuth = new JLabel("Azimuth:");
		panel_1.add(lblAzimuth);
		
		JSpinner spinner_3 = new JSpinner();
		spinner_3.setModel(new SpinnerNumberModel(new Integer(0), null, null, new Integer(0)));
		panel_1.add(spinner_3);
		
		JLabel lblInclonation = new JLabel("Inclination:");
		panel_1.add(lblInclonation);
		
		JSpinner spinner_2 = new JSpinner();
		panel_1.add(spinner_2);
		pvLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("PV Panel Log button was pressed!");
				JFileChooser fileopen = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
				        "CSV", "csv", "txt");
				fileopen.setFileFilter(filter);
				int ret = fileopen.showDialog(frame, "Open PV Log file");
				if (ret == JFileChooser.APPROVE_OPTION) {
					pvLogFile = fileopen.getSelectedFile();
					System.out.println("PV Log file is:"+pvLogFile.getName());
					try {
						
		        		frame.setVisible(true);
		        		PVLogElement.readWithCsvMapReader3(pvLogFile, PVSeries, dateChooserBegin, dateChooserEnd);
		        		
//						PVLogElement.readWithCsvMapReader(consumptionFile, frame, lblNewLabel1);
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
//					pvLogButton.setText(pvLogFile.getName());
				}
			}
		});
		
		JPanel panel_2 = new JPanel();
		panel_5.add(panel_2);
		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Time interval", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblNewLabel = new JLabel("From:");
		panel_2.add(lblNewLabel);
		
		dateChooserBegin.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				try {
	        		frame.setVisible(true);
	        		if(dateChooserBegin.getDate().after(dateChooserEnd.getDate())){
	        			dateChooserBegin.setDate(dateChooserEnd.getDate());
	        		}
	        		PVLogElement.readWithCsvMapReader2(consumptionFile, ConsumptionSeries, dateChooserBegin, dateChooserEnd, false);
	        		PVLogElement.readWithCsvMapReader3(pvLogFile, PVSeries, dateChooserBegin, dateChooserEnd);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		panel_2.add(dateChooserBegin);
		
		JLabel lblNewLabel_1 = new JLabel("To:");
		panel_2.add(lblNewLabel_1);
		
		dateChooserEnd.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				try {
	        		frame.setVisible(true);
	        		if(dateChooserEnd.getDate().before(dateChooserBegin.getDate())){
	        			dateChooserEnd.setDate(dateChooserBegin.getDate());
	        		}
	        		PVLogElement.readWithCsvMapReader2(consumptionFile, ConsumptionSeries, dateChooserBegin, dateChooserEnd, false);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		panel_2.add(dateChooserEnd);
		
		JPanel panel_3 = new JPanel();
		panel_5.add(panel_3);
		panel_3.setBorder(new TitledBorder(null, "PV nominal power", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		final JSpinner spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.05));
		final JSlider slider_1 = new JSlider();
		slider_1.setMaximum(1000);
		slider_1.setValue(0);
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				spinner_1.setValue(slider_1.getValue()/10.);
			}
		});
		spinner_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Double val = 10. * (Double)spinner_1.getValue();
				slider_1.setValue(val.intValue());
			}
		});		
		panel_3.add(slider_1);
		
		panel_3.add(spinner_1);
		
		JLabel label = new JLabel("kWh");
		panel_3.add(label);
		
		JLabel label_1 = new JLabel("");
		panel_5.add(label_1);
		
		JPanel panel_4 = new JPanel();
		panel_5.add(panel_4);
		panel_4.setBorder(new TitledBorder(null, "Battery capacity", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		final JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.05));
		final JSlider slider = new JSlider();
		slider.setMaximum(1000);
		slider.setValue(0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				spinner.setValue(slider.getValue()/10.);
			}
		});
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Double val = (Double)spinner.getValue()*10.;
				slider.setValue(val.intValue());
			}
		});
		frame.getContentPane().setLayout(null);
		panel_4.add(slider);
		
		panel_4.add(spinner);
		
		JLabel lblNewLabel_3 = new JLabel("kWh");
		panel_4.add(lblNewLabel_3);
		
		JLabel label_2 = new JLabel("");
		panel_5.add(label_2);
		
		JLabel label_3 = new JLabel("");
		panel_5.add(label_3);
		frame.getContentPane().add(panel_5);
		
		JLabel lblNewLabel1 = new JLabel("");
		lblNewLabel1.setBounds(9, 82, 87, 14);
		frame.getContentPane().add(lblNewLabel1);
		
		PVSeries.addChangeListener(new SeriesChangeListener() {
			public void seriesChanged(SeriesChangeEvent e) {
				frame.getContentPane().repaint();
			}
		});
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(ConsumptionSeries);
		dataset.addSeries(ProductionSeries);
		dataset.addSeries(PVSeries);
		dataset.addSeries(BASeries);
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Solar Estimator Chart",
				// chart title
				"Date",
				// x-axis label
				"kWh",
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
		plot.setRenderer(new XYDifferenceRenderer(Color.red, Color.green, false));   
//		XYItemRenderer r = plot.getRenderer();
//		if (r instanceof XYLineAndShapeRenderer) {
//		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
//		renderer.setBaseShapesVisible(true);
//		renderer.setBaseShapesFilled(true);
//		}
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("MM/dd-hh:mm"));
		
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setBounds(-1, 70, 1287, 592);
		frame.getContentPane().add(chartPanel);
		
		PVLogElement.setSeries(ConsumptionSeries,PVSeries,ProductionSeries);
	}
}
