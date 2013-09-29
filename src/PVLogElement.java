import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.Color;
import java.io.*;

import javax.swing.*;

import org.jfree.chart.*;
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
	private static Double BateryCapacity = 0.;
	private static Double PVNormalizeCoef = 1.;
	private static Double Voltage = 220.;
	private static Double CosPhi = 0.6;
	private static Double ConsumptionEnergy = 0.;
	private static TimeSeries ConsumptionSeries = null;
	private static TimeSeries PVProductionSeries = null;
	private static TimeSeries ProductionSeries = null;
	
	public void setConsumptionPreference(File csvFile) throws Exception {
		ConsumptionLog = csvFile;
		BeginCalc = EndCalc = null;
		buildConsumptionGraph();
		buildProductionGraphAndCalcResult();
	}
	public void setPVPreference(File csvFile,Double power, Double azimuth, Double inclination) throws Exception {
		buildPVProductionGraph();
		buildProductionGraphAndCalcResult();		
	}
	
	public void setCalculationInterval(Date begin, Date end) throws Exception {
		buildConsumptionGraph();
		buildPVProductionGraph();
		buildProductionGraphAndCalcResult();
	}
	
	public DateRange getDataInterval(){
		return new DateRange(BeginData,EndData);
	}
	
	public void setBatteryPreference(Double capacity) throws Exception {
		buildProductionGraphAndCalcResult();
	}
	
	public static void setSeries(TimeSeries aConsumptionSeries,TimeSeries aPVProductionSeries,TimeSeries aProductionSeries) {
		ConsumptionSeries = aConsumptionSeries;
		PVProductionSeries = aPVProductionSeries;
		ProductionSeries = aProductionSeries;
		try {
			Properties config = new Properties();
			config.load(new FileReader("config.properties"));
			Voltage = Double.parseDouble(config.getProperty("voltage", "220."));
			CosPhi = Double.parseDouble(config.getProperty("cosphi", "0.6"));
			PVNormalizeCoef = Double.parseDouble(config.getProperty("pv.normalize.coefficient","1."));
			Latitude = Double.parseDouble(config.getProperty("pv.latitude", "50.517577"));
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
    ICsvMapReader mapReader = null;
    final String[] header = mapReader.getHeader(true);
    for(int i=0; i<header.length; i++){
    	System.out.print(header[i]);
    }
    final CellProcessor[] processors = getConsumptionProcessors();
    
    Map<String, Object> customerMap;
    treeMap = new TreeMap<Date, String>();
    while((customerMap = mapReader.read(header, processors)) != null) {
        treeMap.put((Date) customerMap.get(header[1]), customerMap.get(header[4]).toString());
    }
    ConsumptionSeries.clear();
    Date prevKey=(BeginCalc==null)?treeMap.firstKey():BeginCalc;
    Double sum = 0.;
    Double energy = 0.;
	for (Map.Entry<Date, String> e : PVLogElement.treeMap.entrySet()){
		if(new Date(prevKey.getTime() + 60000).before(e.getKey())){
			System.out.println("\nHole in data found  between " + prevKey.toString() + " and " + e.getKey().toString());
		}
		if((BeginCalc==null || EndCalc==null) || (e.getKey().after(BeginCalc) && e.getKey().before(EndCalc))){
			energy = Double.parseDouble(e.getValue())*Voltage*CosPhi*60./1000.;
			sum += energy;
			ConsumptionSeries.add(new Minute(e.getKey()), energy);
		}
		prevKey = e.getKey();
	}				
}

private static void buildPVProductionGraph() {
	
}

private static void buildProductionGraphAndCalcResult() {
	
}
	
public static void readWithCsvMapReader2(File csvFile, TimeSeries PVSeries, JDateChooser dateChooserBegin, JDateChooser dateChooserEnd, boolean initDate) throws Exception {
    ICsvMapReader mapReader = null;
    try {
            mapReader = new CsvMapReader(new FileReader(csvFile.getAbsolutePath()), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
            
            // the header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);
            for(int i=0; i<header.length; i++){
            	System.out.print(header[i]);
            }
            final CellProcessor[] processors = getConsumptionProcessors();
            
            Map<String, Object> customerMap;
            treeMap = new TreeMap<Date, String>();
            while((customerMap = mapReader.read(header, processors)) != null) {
                treeMap.put((Date) customerMap.get(header[1]), customerMap.get(header[4]).toString());
            }
            System.out.println("\nTreeMap");
            /*for (Map.Entry<Date, String> e : treeMap.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }*/
            if(initDate && dateChooserBegin.getDate()==null){
            	dateChooserBegin.setDate((Date)treeMap.firstKey());
            }
            if(initDate && dateChooserEnd.getDate()==null){
            	dateChooserEnd.setDate((Date)treeMap.lastKey());
            }
            PVSeries.clear();
            Date prevKey=(dateChooserBegin.getDate()==null)?treeMap.firstKey():dateChooserBegin.getDate();
            Double sum = 0.;
            Double energy = 0.;
        	for (Map.Entry<Date, String> e : PVLogElement.treeMap.entrySet()){
    			if(new Date(prevKey.getTime() + 60000).before(e.getKey())){
    				System.out.println("\nHole in data found  between " + prevKey.toString() + " and " + e.getKey().toString());
    			}
        		if((dateChooserBegin.getDate()==null || dateChooserEnd.getDate()==null) || (e.getKey().after(dateChooserBegin.getDate()) && e.getKey().before(dateChooserEnd.getDate()))){
        			energy = Double.parseDouble(e.getValue())*Voltage*CosPhi*60./1000.;
        			sum += energy;
        			PVSeries.add(new Minute(e.getKey()), energy);
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

public static void readWithCsvMapReader3(File csvFile, TimeSeries PVSeries, JDateChooser dateChooserBegin, JDateChooser dateChooserEnd) throws Exception {
    ICsvMapReader mapReader = null;
    try {
            mapReader = new CsvMapReader(new FileReader(csvFile.getAbsolutePath()), CsvPreference.TAB_PREFERENCE);
            
            // the header columns are used as the keys to the Map
            final String[] header = new String[] { "Date", "Time", "Value", null };
            for(int i=0; i<header.length; i++){
            	System.out.print(header[i]);
            }
            final CellProcessor[] processors = getPVProcessors();
            
            Map<String, Object> customerMap;
            treeMap = new TreeMap<Date, String>();
            
            int year = (dateChooserBegin.getDate()!=null)?dateChooserBegin.getDate().getYear():new Date().getYear();
            
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
            System.out.println("\nTreeMap");
            /*for (Map.Entry<Date, String> e : treeMap.entrySet()) {
                System.out.println(e.getKey() + " " + e.getValue());
            }*/
            PVSeries.clear();
            Double sum = 0.;
            Double energy = 0.;
        	for (Map.Entry<Date, String> e : PVLogElement.treeMap.entrySet()){
        		if((dateChooserBegin.getDate()==null || dateChooserEnd.getDate()==null) || (e.getKey().after(dateChooserBegin.getDate()) && e.getKey().before(dateChooserEnd.getDate()))){
        			energy = PVNormalizeCoef*Double.parseDouble(e.getValue())*Math.cos(Azimuth)*Math.cos(Latitude-Inclination);
        			sum += energy;
        			PVSeries.add(new Hour(e.getKey()), energy);
        			System.out.println("\n" + e.getKey() + " " + e.getValue() + " " + energy);
        		}
        	}			
    }
    finally {
            if( mapReader != null ) {
                    mapReader.close();
            }
    }
}    
}

