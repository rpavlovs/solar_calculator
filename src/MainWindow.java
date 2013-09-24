import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JSlider;
import javax.swing.JSpinner;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;



public class MainWindow {

	private JFrame frame;
	private File consumptionFile;
	private File pvLogFile;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
		frame.setBounds(100, 100, 1019, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setVisible(true);	
			
		final JButton consumptionButton = new JButton("consumption_123.csv");
		consumptionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		
				System.out.println("Consumtion button was pressed!");
				JFileChooser fileopen = new JFileChooser();
				int ret = fileopen.showDialog(frame, "Open consumption file");
				if (ret == JFileChooser.APPROVE_OPTION) {
				    consumptionFile = fileopen.getSelectedFile();
					System.out.println("Consumption file is:"+consumptionFile.getName());
					consumptionButton.setText(consumptionFile.getName());					
				}
			}
		});
		consumptionButton.setBounds(10, 30, 128, 26);
		frame.getContentPane().add(consumptionButton);
		
		final JButton pvLogButton = new JButton("PV panel log..");
		pvLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Consumtion button was pressed!");
				JFileChooser fileopen = new JFileChooser();
				int ret = fileopen.showDialog(frame, "Open PV Log file");
				if (ret == JFileChooser.APPROVE_OPTION) {
					JLabel lblNewLabel1 = new JLabel("Please wait, it's really loading..");
	        		lblNewLabel1.setBounds(500, 500, 200, 15);
	        		frame.getContentPane().add(lblNewLabel1);
				    pvLogFile = fileopen.getSelectedFile();
					System.out.println("PV Log file is:"+pvLogFile.getName());
					pvLogButton.setText(pvLogFile.getName());
					try {
						
		        		frame.setVisible(true);
		        		
						PVLogElement.readWithCsvMapReader(pvLogFile, frame, lblNewLabel1);
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		pvLogButton.setBounds(138, 30, 132, 26);
		frame.getContentPane().add(pvLogButton);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(0, 79, 1007, 10);
		frame.getContentPane().add(separator);
		
		JTextArea textArea = new JTextArea();
		textArea.setBounds(322, 28, 73, 24);
		frame.getContentPane().add(textArea);
		
		JLabel lblNewLabel = new JLabel("From:");
		lblNewLabel.setBounds(282, 35, 54, 15);
		frame.getContentPane().add(lblNewLabel);
		
		JButton btnNewButton_2 = new JButton("New button");
		btnNewButton_2.setBounds(398, 28, 24, 25);
		frame.getContentPane().add(btnNewButton_2);
		
		JLabel lblNewLabel_1 = new JLabel("To:");
		lblNewLabel_1.setBounds(430, 34, 25, 15);
		frame.getContentPane().add(lblNewLabel_1);
		
		JButton button = new JButton("New button");
		button.setBounds(530, 29, 24, 25);
		frame.getContentPane().add(button);
		
		JTextArea textArea_1 = new JTextArea();
		textArea_1.setBounds(455, 28, 73, 24);
		frame.getContentPane().add(textArea_1);
		
		JSlider slider = new JSlider();
		slider.setBounds(558, 39, 224, 17);
		frame.getContentPane().add(slider);
		
		JSlider slider_1 = new JSlider();
		slider_1.setBounds(781, 40, 228, 16);
		frame.getContentPane().add(slider_1);
		
		JSpinner spinner = new JSpinner();
		spinner.setBounds(691, 17, 43, 20);
		frame.getContentPane().add(spinner);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setBounds(921, 15, 43, 20);
		frame.getContentPane().add(spinner_1);
		
		JLabel lblNewLabel_2 = new JLabel("Battery Capacity:");
		lblNewLabel_2.setBounds(566, 17, 125, 15);
		frame.getContentPane().add(lblNewLabel_2);
		
		JLabel lblPvNominalPower = new JLabel("PV nominal Power:");
		lblPvNominalPower.setBounds(787, 16, 139, 15);
		frame.getContentPane().add(lblPvNominalPower);
		
		JLabel lblNewLabel_3 = new JLabel("kWh");
		lblNewLabel_3.setBounds(740, 15, 39, 23);
		frame.getContentPane().add(lblNewLabel_3);
		
		JLabel label = new JLabel("kWh");
		label.setBounds(968, 12, 39, 23);
		frame.getContentPane().add(label);
		
	


	}
	
	
	
}
