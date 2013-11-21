import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.io.*;

import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.data.time.*;
import org.supercsv.cellprocessor.*;
import org.supercsv.cellprocessor.ift.*;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.*;
import org.supercsv.prefs.*;
import org.supercsv.util.CsvContext;

public class PVLogElement {

	public static TreeMap<Date, Double> treeMap;
	private static Date BeginData = null;
	private static Date EndData = null;
	private static Date BeginCalc = null;
	private static Date EndCalc = null;
	private static File ConsumptionLog = null;
	private static File PVLog = null;
	private static File ExportLog = null;
	private static Double PVPower = 5.0;
	private static Double Azimuth = 0.;
	private static Double Latitude = 0.;
	private static Double Inclination = 0.;
	private static Double BatteryCapacity = 0.;
	private static Double PVNormalizeCoef = 1.;
	private static Double Voltage = 220.;
	private static Double CosPhi = 0.6;
	private static Double ChargeMaxCoef = 0.2;
	private static Double DischargeMaxCoef = 0.2;
	private static Double ConsumptionEnergy = 0.;
	private static Double PVProductionEnergy = 0.;
	private static Double GridInEnergy = 0.;
	private static Double GridOutEnergy = 0.;
	private static TimeSeries ConsumptionSeries = null;
	private static TimeSeries PVProductionSeries = null;
	private static TimeSeries ProductionSeries = null;
	private static TimeSeries BASeries = null;
	private static TimeSeries BBSeries = null;
	private static XYTextAnnotation annotation[] = null;
	private static Double MaxConEnergy = 0.;
	private static Double MaxPVEnergy = 0.;

	// private static final String[] eheader = new String[] { "date",
	// "consumption_in", "consumption_out",
	// "pv_in", "pv_power", "pv_azimuth", "pv_inclination", "pv_out",
	// "bat_capacity", "bat_energy", "bat_charge", "bat_out", "pv_res" };
	//
	// private static CellProcessor[] getExportProcessors() {
	// final CellProcessor[] processors = new CellProcessor[] {
	// new FmtDate("yyyy-MM-dd HH:mm"), // date
	// new Optional(new FmtNumber("0.##")), // consumption_in
	// new Optional(new FmtNumber("0.##")), // consumption_out
	// new Optional(new FmtNumber("0.##")), // pv_in
	// new Optional(new FmtNumber("0.##")), // pv_power
	// new Optional(new FmtNumber("0.##")), // pv_azimuth
	// new Optional(new FmtNumber("0.##")), // pv_inclination
	// new Optional(new FmtNumber("0.##")), // pv_out
	// new Optional(new FmtNumber("0.##")), // bat_capacity
	// new Optional(new FmtNumber("0.##")), // bat_energy
	// new Optional(new FmtNumber("0.##")), // bat_charge
	// new Optional(new FmtNumber("0.##")), // bat_out
	// new Optional(new FmtNumber("0.##")), // pv_res
	// };
	// return processors;
	// }

	public static Double getLatitude() {
		return Math.toDegrees(Latitude);
	}

	public static Double getConsumptionEnergy() {
		return ConsumptionEnergy;
	}

	public static void setExportLog(File csvFile) {
		ExportLog = csvFile;
	}

	public static void setConsumptionPreference(File csvFile) throws Exception {
		ConsumptionLog = csvFile;
		BeginData = EndData = BeginCalc = EndCalc = null;
		buildConsumptionGraph();
		BeginCalc = BeginData;
		EndCalc = EndData;
		buildProductionGraphAndCalcResult();
	}

	public static void setPVPreference(File csvFile, Double power,
			Double azimuth, Double inclination) throws Exception {
		PVLog = csvFile;
		PVPower = power;
		Azimuth = Math.toRadians(azimuth);
		Inclination = Math.toRadians(inclination);
		buildPVProductionGraph();
		buildProductionGraphAndCalcResult();
	}

	public static void setCalculationInterval(Date begin, Date end)
			throws Exception {
		BeginCalc = begin;
		EndCalc = end;
		if (BeginCalc != null) {
			BeginCalc.setHours(0);
			BeginCalc.setMinutes(-1);
			BeginCalc.setSeconds(59);
			if (BeginCalc.before(BeginData)) {
				BeginCalc = BeginData;
			}
		}
		if (EndCalc != null) {
			EndCalc.setHours(24);
			EndCalc.setMinutes(0);
			EndCalc.setSeconds(1);
			if (EndCalc.after(EndData)) {
				EndCalc = EndData;
			}
		}
		buildConsumptionGraph();
		buildPVProductionGraph();
		buildProductionGraphAndCalcResult();
	}

	public static DateRange getDataInterval() {
		return new DateRange(BeginData, EndData);
	}

	public static void setBatteryPreference(Double capacity) throws Exception {
		BatteryCapacity = capacity;
		buildProductionGraphAndCalcResult();
	}

	public static void setSeriesAndAnnotations(TimeSeries aConsumptionSeries,
			TimeSeries aPVProductionSeries, TimeSeries aProductionSeries,
			TimeSeries aBASeries, TimeSeries aBBSeries, XYTextAnnotation [] an) {
		ConsumptionSeries = aConsumptionSeries;
		PVProductionSeries = aPVProductionSeries;
		ProductionSeries = aProductionSeries;
		BASeries = aBASeries;
		BBSeries = aBBSeries;
		annotation = an;

		Voltage = 220.D;
		CosPhi = 0.6D;
		PVNormalizeCoef = 0.2D;
		Latitude = Math.toRadians(50.517577D);
		ChargeMaxCoef = 0.2D;
		DischargeMaxCoef = 0.2D;
	}

	public static void setDefaultPreference(Properties config) {
		Voltage = Double.parseDouble(config.getProperty("voltage", "220."));
		CosPhi = Double.parseDouble(config.getProperty("cosphi", "0.6"));
		PVNormalizeCoef = Double.parseDouble(config.getProperty(
				"pv.normalize.coefficient", "0.2"));
		Latitude = Math.toRadians(Double.parseDouble(config.getProperty(
				"pv.latitude", "50.517577")));
		ChargeMaxCoef = Double.parseDouble(config.getProperty(
				"battery.charge.max", "0.2"));
		DischargeMaxCoef = Double.parseDouble(config.getProperty(
				"battery.discharge.max", "0.2"));
	}

	private static void buildConsumptionGraph() throws Exception {
		if (ConsumptionLog == null) {
			return;
		}
		ICsvMapReader mapReader = null;
		ICsvMapWriter mapWriter = null;
		try {
			mapReader = new CsvMapReader(new FileReader(
					ConsumptionLog.getAbsolutePath()),
					CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

			final String[] header = mapReader.getHeader(true);
			final CellProcessor[] processors = new CellProcessor[header.length];
			for(int i=0; i<header.length; i++){
				if(header[i]==null){
					processors[i]=null;
				} else {
					header[i] = header[i].trim();
				if("Time".compareTo(header[i])==0){
						processors[i] = new ParseDate("dd/MM/yyyy HH:mm", true);
				} else 
					if ("Amps_Raw_Data".compareTo(header[i])==0) {
						processors[i] = new StrReplace(",", ".", new ParseDouble()); 
				} else {
						processors[i] = null;
				}
				}
			}

			Map<String, Object> customerMap;
			treeMap = new TreeMap<Date, Double>();
			while ((customerMap = mapReader.read(header, processors)) != null) {
				treeMap.put((Date) customerMap.get("Time"),
						(Double) customerMap.get("Amps_Raw_Data"));
			}
			ConsumptionSeries.clear();
			Date prevKey = (BeginCalc == null) ? treeMap.firstKey() : BeginCalc;
			ConsumptionEnergy = 0.;
			Double energy = 0.;
			MaxConEnergy = 0.;
			BeginData = treeMap.firstKey();
//			BeginData.setMinutes(59);
//			BeginData.setSeconds(59);
			EndData = treeMap.lastKey();
			final String[] eheader = new String[] { "date", "consumption_in",
					"consumption_out" };
			final CellProcessor[] eprocessors = new CellProcessor[] {
					new FmtDate("yyyy-MM-dd HH:mm"), // date
					new Optional(new FmtNumber("0.######")), // consumption_in
					new Optional(new FmtNumber("0.######")), // consumption_out
			};
			if (ExportLog != null) {
				mapWriter = new CsvMapWriter(new FileWriter(
						ExportLog.getAbsolutePath() + "_cons.csv"),
						CsvPreference.STANDARD_PREFERENCE);
				mapWriter.writeHeader(eheader);
			}
			for (Map.Entry<Date, Double> e : PVLogElement.treeMap.entrySet()) {
				if (new Date(prevKey.getTime() + 60000).before(e.getKey())) {
					System.out.println("\nHole in data found  between "
							+ prevKey.toString() + " and "
							+ e.getKey().toString());
				}
				if (BeginData.before(e.getKey())&&((BeginCalc == null || EndCalc == null)
						|| (e.getKey().after(BeginCalc) && e.getKey().before(
								EndCalc)))) {
					final Map<String, Object> res = new HashMap<String, Object>();
					res.put("date", e.getKey());
					res.put("consumption_in", e.getValue());
					energy = e.getValue() * Voltage * CosPhi * 60. / 1000.;
					res.put("consumption_out", energy);
					ConsumptionEnergy += energy;
					ConsumptionSeries.add(new Minute(e.getKey()), energy);
					if (MaxConEnergy < energy) {
						MaxConEnergy = energy;
					}
					if (ExportLog != null) {
						mapWriter.write(res, eheader, eprocessors);
					}
				}
				prevKey = e.getKey();
			}
		} finally {
			if (mapWriter != null) {
				mapWriter.close();
			}
			if (mapReader != null) {
				mapReader.close();
			}
		}
	}

	private static void buildPVProductionGraph() throws Exception {
		if (PVLog == null) {
			return;
		}
		ICsvMapReader mapReader = null;
		ICsvMapWriter mapWriter = null;
		try {
			mapReader = new CsvMapReader(
					new FileReader(PVLog.getAbsolutePath()),
					CsvPreference.TAB_PREFERENCE);
			final String[] header = new String[] { "Date", "Time", "Value",
					null };
			// for(int i=0; i<header.length; i++){
			// System.out.print(header[i]);
			// }
			final CellProcessor[] processors = new CellProcessor[] {
					new ParseDate("dd.MM.", true), // date
					new ParseDate("HH:mm", true), // time
					new StrReplace(",", ".", new ParseDouble()), //
					null };

			Map<String, Object> customerMap;
			treeMap = new TreeMap<Date, Double>();

			int year = (BeginData != null) ? BeginData.getYear() : new Date()
					.getYear();

			boolean continueLooping = true;
			do {
				try {
					customerMap = mapReader.read(header, processors);
					if (customerMap != null) {
						Date date = (Date) customerMap.get(header[0]);
						Date time = (Date) customerMap.get(header[1]);
						date.setYear(year);
						date.setHours(time.getHours());
						date.setMinutes(time.getMinutes());
						treeMap.put(date, (Double) customerMap.get(header[2]));
					}
					continueLooping = customerMap != null;
				} catch (org.supercsv.exception.SuperCsvConstraintViolationException ex) {
					// logger.log(Level.SEVERE,
					// "NON CORRECT VALUE ENCOUNTERD ON ROW "+beanReader.getRowNumber(),
					// ex);
					// treatedOk = false;
				} catch (org.supercsv.exception.SuperCsvCellProcessorException ex) {
					// logger.log(Level.SEVERE,
					// "PARSER EXCEPTION ON ROW "+beanReader.getRowNumber(),
					// ex);
					// treatedOk = false;
				} catch (org.supercsv.exception.SuperCsvException ex) {
					// logger.log(Level.SEVERE,
					// "ERROR ON ROW "+beanReader.getRowNumber(), ex);
					// treatedOk = false;
				}
			} while (continueLooping);
			final String[] eheader = new String[] { "date", "pv_in",
					"pv_power", "pv_azimuth", "pv_inclination", "pv_out" };
			final CellProcessor[] eprocessors = new CellProcessor[] {
					new FmtDate("yyyy-MM-dd HH:mm"), // date
					new Optional(new FmtNumber("0.######")), // pv_in
					new Optional(new FmtNumber("0.##")), // pv_power
					new Optional(new FmtNumber("0.#")), // pv_azimuth
					new Optional(new FmtNumber("0.#")), // pv_inclination
					new Optional(new FmtNumber("0.######")), // pv_out
			};
			if (ExportLog != null) {
				mapWriter = new CsvMapWriter(new FileWriter(
						ExportLog.getAbsolutePath() + "_pv.csv"),
						CsvPreference.STANDARD_PREFERENCE);
				mapWriter.writeHeader(eheader);
			}
			PVProductionSeries.clear();
			PVProductionEnergy = 0.;
			MaxPVEnergy = 0.;
			Double energy = 0.;
			Double coef = PVPower * PVNormalizeCoef * Math.cos(Azimuth)
					* Math.cos(Latitude - Inclination);
			for (Map.Entry<Date, Double> e : PVLogElement.treeMap.entrySet()) {
				if ((BeginCalc == null || EndCalc == null)
						|| (e.getKey().after(BeginCalc) && e.getKey().before(
								EndCalc))) {
					final Map<String, Object> res = new HashMap<String, Object>();
					res.put("pv_power", PVPower);
					res.put("pv_azimuth", Azimuth);
					res.put("pv_inclination", Inclination);
					res.put("date", e.getKey());
					res.put("pv_in", e.getValue());
					energy = e.getValue() * coef;
					res.put("pv_out", energy);
					PVProductionSeries.add(new Hour(e.getKey()), energy);
					if(EndCalc.getTime()-e.getKey().getTime()<3600000){
						energy = energy * (EndCalc.getTime() - e.getKey().getTime())/3600000;
					}
					PVProductionEnergy += energy;
					if (MaxPVEnergy < energy) {
						MaxPVEnergy = energy;
					}
					if (ExportLog != null) {
						mapWriter.write(res, eheader, eprocessors);
					}
				}
			}
		} finally {
			if (mapWriter != null) {
				mapWriter.close();
			}
			if (mapReader != null) {
				mapReader.close();
			}
		}
	}

	/**
	 * @throws Exception
	 */
	private static void buildProductionGraphAndCalcResult() throws Exception {
		if (ProductionSeries == null) {
			return;
		}
		if (ExportLog != null) {

		}
		ICsvMapWriter mapWriter = null;
		try {
			Double currentBatteryEnergy = 0.;
			ProductionSeries.clear();
			BASeries.clear();
			BBSeries.clear();
			GridInEnergy = 0.;
			GridOutEnergy = 0.;
			final String[] eheader = new String[] { "date", "cons", "pv",
					"bat", "out" };
			final CellProcessor[] eprocessors = new CellProcessor[] {
					new FmtDate("yyyy-MM-dd HH:mm"), // date
					new Optional(new FmtNumber("0.######")), // cons
					new Optional(new FmtNumber("0.######")), // pv
					new Optional(new FmtNumber("0.######")), // bat
					new Optional(new FmtNumber("0.######")) // out
			};
			if (ExportLog != null) {
				mapWriter = new CsvMapWriter(new FileWriter(
						ExportLog.getAbsolutePath() + "_res.csv"),
						CsvPreference.STANDARD_PREFERENCE);
				mapWriter.writeHeader(eheader);
			}
			Iterator<TimeSeriesDataItem> itr = ConsumptionSeries.getItems()
					.iterator();
			while (itr.hasNext()) {
				TimeSeriesDataItem cel = (TimeSeriesDataItem) itr.next();
				Minute min = (Minute) cel.getPeriod();
				Double ce = (Double) cel.getValue();
				TimeSeriesDataItem pvel = PVProductionSeries.getDataItem(min
						.getHour());
				if (pvel != null) {
					final Map<String, Object> res = new HashMap<String, Object>();
					res.put("date", cel.getPeriod().getStart());
					res.put("cons", ce);
					Double pve = (Double) pvel.getValue();
					res.put("pv", pve);
					Double pe = 0.;
					if (ce >= pve) {
						if (ce - pve > BatteryCapacity * DischargeMaxCoef
								|| ce - pve > currentBatteryEnergy) {
							pe = currentBatteryEnergy > BatteryCapacity
									* DischargeMaxCoef ? BatteryCapacity
									* DischargeMaxCoef : currentBatteryEnergy;
						} else {
							pe = currentBatteryEnergy > ce - pve ? ce - pve
									: currentBatteryEnergy;
						}
						currentBatteryEnergy -= pe;
						BASeries.add(cel.getPeriod(), -pe);
						pe += pve;
						GridInEnergy += ce-pe;
					} else {
						if (pve - ce > BatteryCapacity * ChargeMaxCoef) {
							pe = currentBatteryEnergy < BatteryCapacity*60.D ? BatteryCapacity
									* ChargeMaxCoef
									: BatteryCapacity*60.D - currentBatteryEnergy;
						} else {
							pe = currentBatteryEnergy+pve-ce < BatteryCapacity*60.D ? pve
									- ce : BatteryCapacity*60.D
									- currentBatteryEnergy;
						}
						currentBatteryEnergy += pe;
						BASeries.add(cel.getPeriod(), pe);
						pe = pve - pe;
						GridOutEnergy += pe-ce;
					}
					res.put("bat", currentBatteryEnergy);
					BBSeries.add(cel.getPeriod(), currentBatteryEnergy/(BatteryCapacity*60.));
					res.put("out", pe-ce);
					ProductionSeries.add(cel.getPeriod(), pe);
//					ProductionEnergy += pe/60.;
					// System.out.println("\n" + cel.getPeriod() + " use: " +
					// cel.getValue() + " pv: " + pvel.getValue() + " b: " +
					// currentBatteryEnergy + " p: " + pe);
					if (ExportLog != null) {
						mapWriter.write(res, eheader, eprocessors);
					}
				} else {
					GridInEnergy += ce;
				}
			}
			if (BeginCalc != null) {
				Double MaxEnergy = MaxConEnergy > MaxPVEnergy ? MaxConEnergy
						: MaxPVEnergy;
				DecimalFormat df = new DecimalFormat("#.000");
				annotation[0].setText(" Consumption "
						+ df.format(ConsumptionEnergy/60.) + " kWh ");
				annotation[0].setX(BeginCalc.getTime() + 200.D);
				annotation[0].setY(MaxEnergy);
				annotation[1].setText(" PV Production "
						+ df.format(PVProductionEnergy) + " kWh ");
				annotation[1].setX(BeginCalc.getTime() + 200.D);
				annotation[1].setY(MaxEnergy - MaxEnergy * 0.04D);
				annotation[2].setText(" Grid consumption "
						+ df.format(GridInEnergy/60.) + " kWh ");
				annotation[2].setX(BeginCalc.getTime() + 200.D);
				annotation[2].setY(MaxEnergy - MaxEnergy * 0.08D);
				annotation[3].setText(" Grid feed in "
						+ df.format(GridOutEnergy/60.) + " kWh ");
				annotation[3].setX(BeginCalc.getTime() + 200.D);
				annotation[3].setY(MaxEnergy - MaxEnergy * 0.12D);
			}
		} finally {
			if (mapWriter != null) {
				mapWriter.close();
			}
		}
	}

}