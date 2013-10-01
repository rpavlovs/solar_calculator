import javax.swing.*;
import javax.swing.filechooser.*;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.border.TitledBorder;

import com.toedter.calendar.JDateChooser;

import java.awt.FlowLayout;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.data.general.*;

import java.awt.Component;

public class MainWindow {

	private JFrame frame;
	private File consumptionFile;
	private File pvLogFile;
	private boolean showGraph;

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
		frame.setBounds(100, 100, 1370, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		showGraph = true;
		MouseAdapter mouseadapter = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// showGraph = false;
				System.out.println("\n showGraph: " + showGraph);
			}

			public void mouseReleased(MouseEvent e) {
				// showGraph = true;
				System.out.println("\n showGraph: " + showGraph);
			}
		};
		
		Font font = new Font("SansSerif", Font.PLAIN, 14);

		final XYTextAnnotation[] annotation = new XYTextAnnotation[4];
		annotation[0]= new XYTextAnnotation("", 10.D,
				10.D);
		annotation[0].setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
		annotation[0].setOutlineVisible(false);
		annotation[0].setPaint(Color.red);
		annotation[0].setFont(font);
		annotation[0].setBackgroundPaint(Color.black);

		annotation[1] = new XYTextAnnotation("", 10.D,
				10.D);
		annotation[1].setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
		annotation[1].setOutlineVisible(false);
		annotation[1].setPaint(Color.green);
		annotation[1].setFont(font);
		annotation[1].setBackgroundPaint(Color.black);

		annotation[2] = new XYTextAnnotation("", 10.D,
				10.D);
		annotation[2].setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
		annotation[2].setOutlineVisible(false);
		annotation[2].setPaint(Color.magenta);
		annotation[2].setFont(font);
		annotation[2].setBackgroundPaint(Color.black);

		annotation[3] = new XYTextAnnotation("", 10.D,
				10.D);
		annotation[3].setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
		annotation[3].setOutlineVisible(false);
		annotation[3].setPaint(Color.orange);
		annotation[3].setFont(font);
		annotation[3].setBackgroundPaint(Color.black);

		JPanel panel_5 = new JPanel();
		panel_5.setBounds(-1, 5, 1355, 66);

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(panel_5, popupMenu);

		JMenuItem mntmExportData = new JMenuItem("Export data");
		mntmExportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileopen = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"CSV", "csv", "txt");
				fileopen.setFileFilter(filter);
				int ret = fileopen.showDialog(frame, "Save calculation log as");
				if (ret == JFileChooser.APPROVE_OPTION) {
					PVLogElement.setExportLog(fileopen.getSelectedFile());
				}
			}
		});
		popupMenu.add(mntmExportData);
		panel_5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JPanel panel = new JPanel();
		panel_5.add(panel);
		panel.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Power consumption",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JButton consumptionButton = new JButton("Choose file");
		final TimeSeries ConsumptionSeries = new TimeSeries("Consumption");
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
				int ret = fileopen.showDialog(frame,
						"Open Consumption Log file");
				if (ret == JFileChooser.APPROVE_OPTION) {
					JLabel lblNewLabel1 = new JLabel(
							"Please wait, it's really loading..");
					lblNewLabel1.setBounds(500, 500, 200, 15);
					frame.getContentPane().add(lblNewLabel1);
					consumptionFile = fileopen.getSelectedFile();
					System.out.println("Consumption file is:"
							+ consumptionFile.getName());
					try {

						frame.setVisible(true);
						PVLogElement.setConsumptionPreference(consumptionFile);
						DateRange range = PVLogElement.getDataInterval();
						dateChooserBegin.setDate(range.getLowerDate());
						dateChooserEnd.setDate(range.getUpperDate());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JPanel panel_1 = new JPanel();
		panel_5.add(panel_1);
		panel_1.setBorder(new TitledBorder(null, "Insolation",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JButton pvLogButton = new JButton("Choose file");
		final TimeSeries ProductionSeries = new TimeSeries("Production");
		final TimeSeries PVSeries = new TimeSeries("PV production");
		final TimeSeries BASeries = new TimeSeries("Battery charging");
		final TimeSeries BBSeries = new TimeSeries("Battery charge");

		panel_1.add(pvLogButton);

		JLabel lblAzimuth = new JLabel("Azimuth:");
		panel_1.add(lblAzimuth);
		final JSpinner AzimuthSpinner = new JSpinner();
		final JSpinner InclinationSpinner = new JSpinner();
		final JSpinner PVPowerSpinner = new JSpinner();
		ChangeListener PVPreferenceListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				try {
					System.out.println("\n Preference changed: " + showGraph);
					if (showGraph) {
						PVLogElement.setPVPreference(pvLogFile,
								(Double) PVPowerSpinner.getValue(),
								(Double) AzimuthSpinner.getValue(),
								(Double) InclinationSpinner.getValue());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		AzimuthSpinner.addChangeListener(PVPreferenceListener);
		AzimuthSpinner.addMouseListener(mouseadapter);
		AzimuthSpinner.setModel(new SpinnerNumberModel(0.0, 0.0, 90.0, 1.0));
		panel_1.add(AzimuthSpinner);

		JLabel lblInclonation = new JLabel("Inclination:");
		panel_1.add(lblInclonation);

		InclinationSpinner.addChangeListener(PVPreferenceListener);
		InclinationSpinner.addMouseListener(mouseadapter);
		InclinationSpinner
				.setModel(new SpinnerNumberModel(0.0, 0.0, 90.0, 0.0));
		panel_1.add(InclinationSpinner);
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
					try {

						frame.setVisible(true);
						PVLogElement.setPVPreference(pvLogFile,
								(Double) PVPowerSpinner.getValue(),
								(Double) AzimuthSpinner.getValue(),
								(Double) InclinationSpinner.getValue());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JPanel panel_2 = new JPanel();
		panel_5.add(panel_2);
		panel_2.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Time interval",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblNewLabel = new JLabel("From:");
		panel_2.add(lblNewLabel);
		PropertyChangeListener intervalListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				try {
					frame.setVisible(true);
					if (dateChooserBegin.getDate() != null
							&& dateChooserEnd.getDate() != null
							&& dateChooserBegin.getDate().after(
									dateChooserEnd.getDate())) {
						dateChooserEnd.setDate(dateChooserBegin.getDate());
					}
					PVLogElement.setCalculationInterval(
							dateChooserBegin.getDate(),
							dateChooserEnd.getDate());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};

		dateChooserBegin.addPropertyChangeListener(intervalListener);
		panel_2.add(dateChooserBegin);

		JLabel lblNewLabel_1 = new JLabel("To:");
		panel_2.add(lblNewLabel_1);

		dateChooserEnd.addPropertyChangeListener(intervalListener);
		panel_2.add(dateChooserEnd);

		JPanel panel_3 = new JPanel();
		panel_5.add(panel_3);
		panel_3.setBorder(new TitledBorder(null, "PV nominal power",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		PVPowerSpinner.setModel(new SpinnerNumberModel(5.0, 0.0, 100.0, 0.05));
		final JSlider slider_1 = new JSlider();
		slider_1.addMouseListener(mouseadapter);
		slider_1.setMaximum(1000);
		slider_1.setValue(5);
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				PVPowerSpinner.setValue(slider_1.getValue() / 10.);
			}
		});
		PVPowerSpinner.addMouseListener(mouseadapter);
		PVPowerSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Double val = 10. * (Double) PVPowerSpinner.getValue();
				slider_1.setValue(val.intValue());
				try {
					System.out.println("\n PVPower change: " + showGraph);
					if (showGraph) {
						PVLogElement.setPVPreference(pvLogFile,
								(Double) PVPowerSpinner.getValue(),
								(Double) AzimuthSpinner.getValue(),
								(Double) InclinationSpinner.getValue());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		panel_3.add(slider_1);

		panel_3.add(PVPowerSpinner);

		JLabel label = new JLabel("kW");
		panel_3.add(label);

		JLabel label_1 = new JLabel("");
		panel_5.add(label_1);

		JPanel panel_4 = new JPanel();
		panel_5.add(panel_4);
		panel_4.setBorder(new TitledBorder(null, "Battery capacity",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		final JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.05));
		final JSlider slider = new JSlider();
		slider.setMaximum(1000);
		slider.setValue(0);
		slider.addMouseListener(mouseadapter);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				spinner.setValue(slider.getValue() / 10.);
			}
		});
		spinner.addMouseListener(mouseadapter);
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Double val = (Double) spinner.getValue() * 10.;
				slider.setValue(val.intValue());
				try {
					if (showGraph) {
						PVLogElement.setBatteryPreference((Double) spinner
								.getValue());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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
		TimeSeriesCollection dataset1 = new TimeSeriesCollection();
		dataset1.addSeries(ConsumptionSeries);
		dataset1.addSeries(ProductionSeries);
//		dataset1.addSeries(PVSeries);
//		TimeSeriesCollection dataset2 = new TimeSeriesCollection();
//		dataset2.addSeries(BASeries);
		TimeSeriesCollection dataset3 = new TimeSeriesCollection();
		dataset3.addSeries(BBSeries);

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Solar Estimator Chart",
				// chart title
				"Date",
				// x-axis label
				"kW",
				// y-axis label
				dataset1,
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

//		NumberAxis axis2 = new NumberAxis("kW");
//		plot.setRangeAxis(1, axis2);
//		plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
//		// axis2.setFixedDimension(10.0);
//		// axis2.setAutoRangeIncludesZero(false);
//		// axis2.setLabelPaint(Color.yellow);
//		// axis2.setTickLabelPaint(Color.yellow);
//
//		plot.setDataset(1, dataset2);
//		plot.mapDatasetToRangeAxis(1, 1);
		XYItemRenderer renderer2 = new StandardXYItemRenderer();
//		renderer2.setSeriesPaint(0, Color.orange);
//		plot.setRenderer(1, renderer2);

		NumberAxis axis3 = new NumberAxis("%");
		axis3.setRange(0.D, 1.D);
//		axis3.setRangeWithMargins(0., 1.);
		axis3.setStandardTickUnits(NumberAxis.createStandardTickUnits());
		axis3.setNumberFormatOverride(new DecimalFormat("##%"));		
		plot.setRangeAxis(2, axis3);
		plot.setRangeAxisLocation(2, AxisLocation.TOP_OR_RIGHT);
		plot.setDataset(2, dataset3);
		plot.mapDatasetToRangeAxis(2, 2);
		renderer2.setSeriesPaint(1, Color.yellow);
		plot.setRenderer(2, renderer2);
		// XYItemRenderer r = plot.getRenderer();
		// if (r instanceof XYLineAndShapeRenderer) {
		// XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
		// renderer.setBaseShapesVisible(true);
		// renderer.setBaseShapesFilled(true);
		// }
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("MM/dd-HH"));
		axis.setVerticalTickLabels(true);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.setBounds(-1, 70, 1355, 592);
		frame.getContentPane().add(chartPanel);

		plot.addAnnotation(annotation[0]);
		plot.addAnnotation(annotation[1]);
		plot.addAnnotation(annotation[2]);
		plot.addAnnotation(annotation[3]);

		PVLogElement.setSeriesAndAnnotations(ConsumptionSeries, PVSeries,
				ProductionSeries, BASeries, BBSeries, annotation);
       
        Properties config = new Properties();
		InputStream in = this.getClass()
					.getResourceAsStream("/config.properties");
		try {
			if (in != null) {
				config.load(in);
				PVLogElement.setDefaultPreference(config);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		InclinationSpinner.setValue(PVLogElement.getLatitude());

	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
