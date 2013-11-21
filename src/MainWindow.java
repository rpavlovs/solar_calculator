import javax.swing.*;
import javax.swing.filechooser.*;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.awt.Color;
import java.awt.Dimension;
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
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Align;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.data.general.*;

import java.awt.Component;
import java.awt.BorderLayout;

import javax.swing.border.LineBorder;

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
// TODO: -Command line parameters would simplify testing and use (e.g. alpha.exe /load.csv /pv.txt)
//		Нужно определить весь перечень параметров которые будут доступны из командной строки где-то из расчета 15-30 минут на один параметр.
//		Сделайте эти два.
		
// -c=consumption consumption file
// -i=insolation insolation file
// start datetime
// end datetime
// -df=dateformat datetime format
// 
		
// TODO: Надо вместо выбора абсолютной мощности генерации сделать просто коэффициент к загружаемым данным. Значения коэффициента 0.8, 0.9, 1.0, 1.1, 1.2. Выпадающий список с -20%, -10%, 0%, +10%, +20%		
// TODO: Сделайте добавление начального значения SOC.		
// TODO: Перенести сюда config и использовать его-же для хранения указания параметров коммандной строки.
// TODO: Прогресс индикатор не нужен. Нужно только сообщение с перичислением интервалов или меток даты/времени, за которые данные отсутвуют. Сколько это займет?
// TODO: -Sometimes we need to modify raw data in Excel or open office. There are small changes in date format pursuant to this which currently lead to error. Could we define this in config, e.g. format would change from dd/mm/yyyy hh:mm to dd.mm.yyyy hh:mm		
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
		frame.setBounds(100, 100, 1280, 768);
		frame.setMinimumSize(new Dimension(1366, 768));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setTitle("Solar Estimator");

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
		annotation[0]= new XYTextAnnotation("1", 10.D,
				10.D);
		annotation[0].setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
		annotation[0].setOutlineVisible(false);
		annotation[0].setPaint(Color.red);
		annotation[0].setFont(font);
		annotation[0].setBackgroundPaint(Color.black);

		annotation[1] = new XYTextAnnotation("2", 10.D,
				10.D);
		annotation[1].setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
		annotation[1].setOutlineVisible(false);
		annotation[1].setPaint(Color.green);
		annotation[1].setFont(font);
		annotation[1].setBackgroundPaint(Color.black);

		annotation[2] = new XYTextAnnotation("3", 10.D,
				10.D);
		annotation[2].setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
		annotation[2].setOutlineVisible(false);
		annotation[2].setPaint(Color.magenta);
		annotation[2].setFont(font);
		annotation[2].setBackgroundPaint(Color.black);

		annotation[3] = new XYTextAnnotation("4", 10.D,
				10.D);
		annotation[3].setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
		annotation[3].setOutlineVisible(false);
		annotation[3].setPaint(Color.orange);
		annotation[3].setFont(font);
		annotation[3].setBackgroundPaint(Color.black);

		final JPanel panel_5 = new JPanel();
		FlowLayout fl_panel_5 = new FlowLayout(FlowLayout.CENTER, 5, 5);
		panel_5.setLayout(fl_panel_5);

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

		JPanel panel = new JPanel();
		panel_5.add(panel);
		panel.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Power consumption",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		FlowLayout fl_panel = new FlowLayout(FlowLayout.LEADING, 5, 5);
		panel.setLayout(fl_panel);

		final JButton consumptionButton = new JButton("Choose file");
		final TimeSeries ConsumptionSeries = new TimeSeries("Consumption");
		final JDateChooser dateChooserBegin = new JDateChooser();
		dateChooserBegin.setDateFormatString("yyyy-MM-dd HH:mm");
		final JDateChooser dateChooserEnd = new JDateChooser();
		dateChooserEnd.setDateFormatString("yyyy-MM-dd HH:mm");
		panel.add(consumptionButton);
		consumptionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Consumption button was pressed!");
				URL curdir = this.getClass().getClassLoader().getResource(".");
				JFileChooser fileopen = new JFileChooser(curdir.getPath());
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
						JOptionPane.showMessageDialog(frame,
								e1.getLocalizedMessage(),
							    "Erro open file",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		JPanel panel_1 = new JPanel();
		panel_5.add(panel_1);
		panel_1.setBorder(new TitledBorder(null, "Insolation",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

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
					JOptionPane.showMessageDialog(frame,
							ex.getLocalizedMessage(),
						    "Erro open file",
						    JOptionPane.ERROR_MESSAGE);
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
				URL curdir = this.getClass().getClassLoader().getResource(".");
				JFileChooser fileopen = new JFileChooser(curdir.getPath());
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
						JOptionPane.showMessageDialog(frame,
								e1.getLocalizedMessage(),
							    "Erro open file",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		JPanel panel_2 = new JPanel();
		panel_5.add(panel_2);
		panel_2.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Time interval",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

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
					JOptionPane.showMessageDialog(frame,
							e1.getLocalizedMessage(),
						    "Erro open file",
						    JOptionPane.ERROR_MESSAGE);
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
		panel_3.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

		PVPowerSpinner.setModel(new SpinnerNumberModel(5.0, 0.0, 100.0, 1.0));
		final JSlider slider_1 = new JSlider();
		slider_1.setMaximum(100);
		slider_1.addMouseListener(mouseadapter);
		slider_1.setValue(5);
		slider_1.setPreferredSize(new Dimension(170,23));
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				PVPowerSpinner.setValue(slider_1.getValue() * 1.);
			}
		});
		PVPowerSpinner.addMouseListener(mouseadapter);
		PVPowerSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Double val = (Double) PVPowerSpinner.getValue();
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
					JOptionPane.showMessageDialog(frame,
							ex.getLocalizedMessage(),
						    "Erro open file",
						    JOptionPane.ERROR_MESSAGE);
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
		spinner.setModel(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
		final JSlider slider = new JSlider();
		slider.setMaximum(100);
		slider.setValue(0);
		slider_1.setPreferredSize(new Dimension(170,23));
		slider.addMouseListener(mouseadapter);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				spinner.setValue(slider.getValue() * 1.);
			}
		});
		spinner.addMouseListener(mouseadapter);
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Double val = (Double) spinner.getValue();
				slider.setValue(val.intValue());
				try {
					if (showGraph) {
						PVLogElement.setBatteryPreference((Double) spinner
								.getValue());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(frame,
							ex.getLocalizedMessage(),
						    "Erro open file",
						    JOptionPane.ERROR_MESSAGE);					
				}
			}
		});
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		panel_4.add(slider);

		panel_4.add(spinner);

		JLabel lblNewLabel_3 = new JLabel("kWh");
		panel_4.add(lblNewLabel_3);

		JLabel label_2 = new JLabel("");
		panel_5.add(label_2);

		JLabel label_3 = new JLabel("");
		panel_5.add(label_3);
		frame.getContentPane().add(panel_5, BorderLayout.NORTH);

		JLabel lblNewLabel1 = new JLabel("");
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

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"",
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
		XYDifferenceRenderer renderer1 = new XYDifferenceRenderer(Color.red, Color.green, false);
		renderer1.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator() {
		      private static final long serialVersionUID = 1L;
		      public String generateToolTip(XYDataset dataset, int series, int item) {
		         String toolTipStr = "a";
		         return toolTipStr;
		      }
		   });		
		renderer1.setSeriesToolTipGenerator(1, new StandardXYToolTipGenerator() {
		      private static final long serialVersionUID = 1L;
		      public String generateToolTip(XYDataset dataset, int series, int item) {
		         String toolTipStr = "b";
		         return toolTipStr;
		      }
		   });		
		plot.setRenderer(renderer1);
//		plot.getRenderer().setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator() {
//		      private static final long serialVersionUID = 1L;
//		      public String generateToolTip(XYDataset dataset, int series, int item) {
//		         String toolTipStr = "asdf";
//		         return toolTipStr;
//		      }
//		   });

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
		plot.setRangeAxis(1, axis3);
		plot.setRangeAxisLocation(2, AxisLocation.TOP_OR_RIGHT);
		plot.setDataset(1, dataset3);
		plot.mapDatasetToRangeAxis(1, 1);
		renderer2.setSeriesPaint(1, Color.yellow);
		renderer2.setSeriesToolTipGenerator(0, new StandardXYToolTipGenerator() {
		      private static final long serialVersionUID = 1L;
		      public String generateToolTip(XYDataset dataset, int series, int item) {
		         String toolTipStr = "c";
		         return toolTipStr;
		      }
		   });
		plot.setRenderer(1, renderer2);
		// XYItemRenderer r = plot.getRenderer();
		// if (r instanceof XYLineAndShapeRenderer) {
		// XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
		// renderer.setBaseShapesVisible(true);
		// renderer.setBaseShapesFilled(true);
		// }
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
		axis.setVerticalTickLabels(true);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setBackground(Color.WHITE);
		
//		ImageIcon imageicon = new ImageIcon("c:\\Users\\parmax\\workspace\\solar_calculator\\image003.jpg"); 
//		plot.setBackgroundImage(imageicon.getImage()); 
//		plot.setBackgroundImageAlignment(Align.CENTER);
		
		chartPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		chartPanel.setMaximumDrawWidth(1920);
		chartPanel.setMaximumDrawHeight(1080);
		chartPanel.setHorizontalAxisTrace(true);
		frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
		chartPanel.setLayout(new BorderLayout(0, 0));

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
			JOptionPane.showMessageDialog(frame,
					e.getLocalizedMessage(),
				    "Erro open file",
				    JOptionPane.ERROR_MESSAGE);			
		}
		
		InclinationSpinner.setValue(PVLogElement.getLatitude());

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				chartPanel.setSize(frame.getContentPane().getWidth(),frame.getContentPane().getHeight()-panel_5.getHeight());
			}
		});
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
