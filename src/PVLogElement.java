import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.Color;
import java.io.*;

import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.supercsv.cellprocessor.*;
import org.supercsv.cellprocessor.constraint.*;
import org.supercsv.cellprocessor.ift.*;
import org.supercsv.io.*;
import org.supercsv.prefs.*;

import com.toedter.calendar.JDateChooser;

public class PVLogElement {
		
	public static TreeMap<Date, String> treeMap;
	private static Date BeginData = null;
	private static Date EndData = null;
	private static Date BeginCalc = null;
	private static Date EndCalc = null;
	private static File ConsumptionLog = null;
	private static File PVLog = null;
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
	private static Double ProductionEnergy = 0.;
	private static TimeSeries ConsumptionSeries = null;
	private static TimeSeries PVProductionSeries = null;
	private static TimeSeries ProductionSeries = null;
	private static XYTextAnnotation annotationCE = null;
	private static XYTextAnnotation annotationPV = null;
	private static XYTextAnnotation annotationPB = null;	
	private static Double MaxEnergy = 0.;

	public static Double getConsumptionEnergy() {
		return ConsumptionEnergy;
	}
	
	public static void setConsumptionPreference(File csvFile) throws Exception {
		ConsumptionLog = csvFile;
		BeginData = EndData = BeginCalc = EndCalc = null;
		buildConsumptionGraph();
		BeginCalc = BeginData;
		EndCalc = EndData;
		buildProductionGraphAndCalcResult();
	}
	public static void setPVPreference(File csvFile,Double power, Double azimuth, Double inclination) throws Exception {
		PVLog = csvFile;
		PVPower = power;
		Azimuth = Math.toRadians(azimuth);
		Inclination = Math.toRadians(inclination);
		buildPVProductionGraph();
		buildProductionGraphAndCalcResult();		
	}
	
	public static void setCalculationInterval(Date begin, Date end) throws Exception {
		BeginCalc = begin;
		EndCalc = end;
		buildConsumptionGraph();
		buildPVProductionGraph();
		buildProductionGraphAndCalcResult();
	}
	
	public static DateRange getDataInterval(){
		return new DateRange(BeginData,EndData);
	}
	
	public static void setBatteryPreference(Double capacity) throws Exception {
		BatteryCapacity = capacity;
		buildProductionGraphAndCalcResult();
	}
	
	public static void setSeriesAndAnnotations(TimeSeries aConsumptionSeries,TimeSeries aPVProductionSeries,TimeSeries aProductionSeries,XYTextAnnotation anCE,XYTextAnnotation anPV,XYTextAnnotation anPB) {
		ConsumptionSeries = aConsumptionSeries;
		PVProductionSeries = aPVProductionSeries;
		ProductionSeries = aProductionSeries;
		annotationCE = anCE;
		annotationPV = anPV;
		annotationPB = anPB;
		try {
			Properties config = new Properties();
			config.load(new FileReader("config.properties"));
			Voltage = Double.parseDouble(config.getProperty("voltage", "220."));
			CosPhi = Double.parseDouble(config.getProperty("cosphi", "0.6"));
			PVNormalizeCoef = Double.parseDouble(config.getProperty("pv.normalize.coefficient","0.2"));
			Latitude = Math.toRadians(Double.parseDouble(config.getProperty("pv.latitude", "50.517577")));
			ChargeMaxCoef = Double.parseDouble(config.getProperty("battery.charge.max", "0.2"));
			DischargeMaxCoef = Double.parseDouble(config.getProperty("battery.discharge.max", "0.2"));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private static CellProcessor[] getConsumptionProcessors() {
		final CellProcessor[] processors = new CellProcessor[] {
				null,  //device
				new ParseDate("dd/MM/yyyy HH:mm",true),  //date + time
				null,  //GHG_Factor
				null,  //Tariff_Cost
				new StrReplace(",",".",new ParseDouble()),  //Amps_Raw_Data
				null,  //Amps_Raw Data_Min
				null,  //Amps_Raw_Data_Max
				null,  //kW_Raw_Data
				null,  //kW_Raw_Data_Min
				null,  //kW_Raw_Data_Max
				null,  //Cost_Raw_Data
				null,  //Cost_Raw_Data_Min
				null,  //Cost_Raw_Data_Max
				null,  //GHG_Raw_Data
				null,  //GHG_Raw_Data_Min
				null,   //GHG_Raw_Data_Max
				null
		};
		
		return processors;
		
	}
	
	private static CellProcessor[] getPVProcessors() {
		final CellProcessor[] processors = new CellProcessor[] {
				new ParseDate("dd.MM.",true),  //date
				new ParseDate("HH:mm",true),  //time
				new StrReplace(",",".",new ParseDouble()),  //
				null
		};
		
		return processors;
		
	}
	
private static void buildConsumptionGraph() throws Exception {
	if (ConsumptionLog == null) {
		return;
	}
    ICsvMapReader mapReader = null;
    try {
    mapReader = new CsvMapReader(new FileReader(ConsumptionLog.getAbsolutePath()), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
    
    final String[] header = mapReader.getHeader(true);
//    for(int i=0; i<header.length; i++){
//    	System.out.print(header[i]);
//    }
    final CellProcessor[] processors = getConsumptionProcessors();
    
    Map<String, Object> customerMap;
    treeMap = new TreeMap<Date, String>();
    while((customerMap = mapReader.read(header, processors)) != null) {
        treeMap.put((Date) customerMap.get(header[1]), customerMap.get(header[4]).toString());
    }
    ConsumptionSeries.clear();
    Date prevKey=(BeginCalc==null)?treeMap.firstKey():BeginCalc;
    ConsumptionEnergy = 0.;
    Double energy = 0.;
    BeginData=treeMap.firstKey();
    EndData=treeMap.lastKey();
	for (Map.Entry<Date, String> e : PVLogElement.treeMap.entrySet()){
		if(new Date(prevKey.getTime() + 60000).before(e.getKey())){
			System.out.println("\nHole in data found  between " + prevKey.toString() + " and " + e.getKey().toString());
		}
		if((BeginCalc==null || EndCalc==null) || (e.getKey().after(BeginCalc) && e.getKey().before(EndCalc))){
			energy = Double.parseDouble(e.getValue())*Voltage*CosPhi*60./1000.;
			ConsumptionEnergy += energy;
			ConsumptionSeries.add(new Minute(e.getKey()), energy);
			if(MaxEnergy<energy) {
				MaxEnergy=energy;
			}
		}
		prevKey = e.getKey();
	}
    }
    finally {
        if( mapReader != null ) {
                mapReader.close();
        }
}
}

private static void buildPVProductionGraph() throws Exception {
	if(PVLog == null){
		return;
	}
    ICsvMapReader mapReader = null;
    try {
            mapReader = new CsvMapReader(new FileReader(PVLog.getAbsolutePath()), CsvPreference.TAB_PREFERENCE);
            final String[] header = new String[] { "Date", "Time", "Value", null };
//            for(int i=0; i<header.length; i++){
//            	System.out.print(header[i]);
//            }
            final CellProcessor[] processors = getPVProcessors();
            
            Map<String, Object> customerMap;
            treeMap = new TreeMap<Date, String>();
            
            int year = (BeginData!=null)?BeginData.getYear():new Date().getYear();
            
            boolean continueLooping = true;
            do {
            	try {
            		customerMap = mapReader.read(header, processors);
            		if(customerMap != null){
            			Date date = (Date) customerMap.get(header[0]);
            			Date time = (Date) customerMap.get(header[1]);
            			date.setYear(year);
            			date.setHours(time.getHours());
            			date.setMinutes(time.getMinutes());
            			treeMap.put(date, customerMap.get(header[2]).toString());
            		}
            		continueLooping = customerMap != null;
            	} catch (org.supercsv.exception.SuperCsvConstraintViolationException ex) {
//            		logger.log(Level.SEVERE, "NON CORRECT VALUE ENCOUNTERD ON ROW "+beanReader.getRowNumber(), ex);
//            		treatedOk = false;
            	} catch (org.supercsv.exception.SuperCsvCellProcessorException ex){
//            		logger.log(Level.SEVERE, "PARSER EXCEPTION ON ROW "+beanReader.getRowNumber(), ex);
//            		treatedOk = false;
            	} catch (org.supercsv.exception.SuperCsvException ex){
//            		logger.log(Level.SEVERE, "ERROR ON ROW "+beanReader.getRowNumber(), ex);
//            		treatedOk = false;
            	}
            } while (continueLooping);
            PVProductionSeries.clear();
            PVProductionEnergy = 0.;
            Double energy = 0.;
            Double coef = PVPower*PVNormalizeCoef*Math.cos(Azimuth)*Math.cos(Latitude-Inclination);
        	for (Map.Entry<Date, String> e : PVLogElement.treeMap.entrySet()){
        		if((BeginCalc==null || EndCalc==null) || (e.getKey().after(BeginCalc) && e.getKey().before(EndCalc))){
        			energy = Double.parseDouble(e.getValue())*coef;
        			PVProductionEnergy += energy*60.;
        			PVProductionSeries.add(new Hour(e.getKey()), energy);
        			if(MaxEnergy<energy) {
        				MaxEnergy=energy;
        			}
        		}
        	}			
    }
    finally {
            if( mapReader != null ) {
                    mapReader.close();
            }
    }
}    

private static void buildProductionGraphAndCalcResult() {
	if(ProductionSeries==null) {
		return;
	}
	Double currentBatteryEnergy = 0.;
	ProductionSeries.clear();
	ProductionEnergy = 0.;
	Iterator itr = ConsumptionSeries.getItems().iterator();
    while(itr.hasNext()) {
    	TimeSeriesDataItem cel = (TimeSeriesDataItem)itr.next();
    	Minute min = (Minute)cel.getPeriod();
    	TimeSeriesDataItem pvel = PVProductionSeries.getDataItem(min.getHour());
    	if(pvel!=null) {
    		Double ce = (Double)cel.getValue();
    		Double pve = (Double)pvel.getValue();
    		Double pe = 0.;
    		if(ce>=pve){
    			if(ce-pve > BatteryCapacity*DischargeMaxCoef || ce-pve > currentBatteryEnergy){
    				pe = currentBatteryEnergy>BatteryCapacity*DischargeMaxCoef?BatteryCapacity*DischargeMaxCoef:currentBatteryEnergy;
    			} else {
    				pe = currentBatteryEnergy>ce-pve?ce-pve:currentBatteryEnergy;
    			}
				currentBatteryEnergy -= pe;
				pe += pve;
    		} else {
    			if(pve-ce > BatteryCapacity*ChargeMaxCoef){
    				pe = currentBatteryEnergy<BatteryCapacity?BatteryCapacity*ChargeMaxCoef:BatteryCapacity-currentBatteryEnergy;
    			} else {
    				pe = currentBatteryEnergy<BatteryCapacity?pve-ce:BatteryCapacity-currentBatteryEnergy;
    			}
				currentBatteryEnergy += pe;
				pe = pve - pe;
    		}
    		ProductionSeries.add(cel.getPeriod(),pe);
    		ProductionEnergy += pe;
   			System.out.println("\n" + cel.getPeriod() + " use: " + cel.getValue() + " pv: " + pvel.getValue() + " b: " + currentBatteryEnergy + " p: " + pe);
    	}
    }
	if(BeginCalc!=null) {
	DecimalFormat df = new DecimalFormat("#.0");
	annotationCE.setText("Consumption " +df.format(ConsumptionEnergy) + " kWh");
	annotationCE.setX(BeginCalc.getTime()+200.D);
	annotationCE.setY(MaxEnergy);
	annotationPB.setText("Production " + df.format(ProductionEnergy) + " kWh");
	annotationPB.setX(BeginCalc.getTime()+200.D);
	annotationPB.setY(MaxEnergy-0.25D);
	annotationPV.setText("PV production " + df.format(PVProductionEnergy) + " kWh");
	annotationPV.setX(BeginCalc.getTime()+200.D);
	annotationPV.setY(MaxEnergy-0.5D);
	}
}

}

