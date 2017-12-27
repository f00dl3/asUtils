/*
by Anthony Stump
Created: 4 Oct 2017
Updated: 27 Dec 2017
*/


package asUtils.Model;

import asUtils.Shares.StumpJunk;
import asUtils.Shares.MyDBConnector;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.*;

import asUtils.Shares.JunkyBeans;

public class ModelWorkerHRRR {
	
	public static void main(String args[]) {
		
                ModelBeans modelBeans = new ModelBeans();
                JunkyBeans junkyBeans = new JunkyBeans();
                
		final String getHour = args[0];
		final String round = args[1];
		final DateTime tDateTime = new DateTime(DateTimeZone.UTC).minusHours(4);
		final DateTimeFormatter getDateFormat = DateTimeFormat.forPattern("yyyyMMdd");
		final String getDate = getDateFormat.print(tDateTime);
		final File appPath = junkyBeans.getAppShareSys();
		final File xml2Path = modelBeans.getXml2Path();
		final File helpers = junkyBeans.getHelpers();
		final File jsonOutFile = new File(xml2Path+"/hrrrOut_"+round+".json");
		final String pointInputAsString = ModelShare.pointInputAsString("KOJC");
		final String modelName = "HRRR";
		final String hrrrBase="http://nomads.ncep.noaa.gov/pub/data/nccf/com/hrrr/prod/hrrr."+getDate;
		final String hFHq = "SELECT FHour FROM WxObs.GFSFHA WHERE DoGet=1 AND HRRR=1 AND Round="+round+";";
		final String heightQ = "SELECT HeightMb FROM WxObs.ModelHeightLevels WHERE GFS=1 ORDER BY HeightMB DESC;";

		int gribSpot = 3;
		int iSx = 1;
		int iSs = 5;
		
		JSONObject hrrrJSON = new JSONObject();
		
		List<String> hFHList = new ArrayList<>();
		List<String> heightList = new ArrayList<>();
		
		try (
			Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();
			ResultSet resultSetHFH = stmt.executeQuery(hFHq);
		) { while (resultSetHFH.next()) { hFHList.add(resultSetHFH.getString("FHour")); } }
		catch (Exception e) { e.printStackTrace(); }
				
		try (
			Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();
			ResultSet resultSetHeights = stmt.executeQuery(heightQ);
		) { while (resultSetHeights.next()) { heightList.add(resultSetHeights.getString("HeightMb")); } }
		catch (Exception e) { e.printStackTrace(); }

		final String filters = ModelShare.filters("g2fd")+"\\|"+ModelShare.filters("g2fr");

		System.out.println("DEBUG pointInputAsString: "+pointInputAsString);

		for (String tFHourStr : hFHList) { 
		
			int tFHour = Integer.parseInt(tFHourStr);
			String tFHour2D = String.format("%02d", tFHour);
			String tFHour4D = String.format("%04d", tFHour);
			File sounding = new File(xml2Path.getPath()+"/outHRRR_"+tFHour4D+".csv");
			File tFHGlob = new File(xml2Path.getPath()+"/fx"+tFHour4D);
			File tFXCtlFile = new File(tFHGlob.getPath()+".ctl");
			String tFHData = hrrrBase+"/hrrr.t"+getHour+"z.wrfprsf"+tFHour2D+".grib2";

			System.out.println(" -> Processing "+modelName+" data - FH +"+tFHour4D+"!");
			
			StumpJunk.runProcess("(\""+helpers.getPath()+"/get_inv.pl\" "+tFHData+".idx | grep \""+filters+"\" | \""+helpers.getPath()+"/get_grib.pl\" "+tFHData+" "+tFHGlob.getPath()+")");
			try { StumpJunk.runProcessOutFile("\""+appPath.getPath()+"/wgrib2\" "+tFHGlob.getPath()+" "+pointInputAsString, sounding, false); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
			StumpJunk.sedFileReplace(sounding.getPath(), ":lon", ",lon");
			try { StumpJunk.runProcessOutFile("\""+appPath.getPath()+"/g2ctl\" "+tFHGlob.getPath(), tFXCtlFile, false); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
			StumpJunk.runProcess("\""+appPath.getPath()+"/gribmap\" -v -i "+tFXCtlFile.getPath());
			
			double tTC100 = 0.001; double tTC150 = 0.001;
			double tTC200 = 0.001; double tTC250 = 0.001;
			double tTC300 = 0.001; double tTC350 = 0.001;
			double tTC400 = 0.001; double tTC450 = 0.001;
			double tTC500 = 0.001; double tTC550 = 0.001;
			double tTC600 = 0.001; double tTC650 = 0.001;
			double tTC700 = 0.001; double tTC750 = 0.001;
			double tTC800 = 0.001; double tTC850 = 0.001;
			double tTC900 = 0.001; double tTC950 = 0.001;
			double tTC1000 = 0.001; double tTC0 = 0.001;

			double tRH100 = 0.001; double tRH150 = 0.001;
			double tRH200 = 0.001; double tRH250 = 0.001;
			double tRH300 = 0.001; double tRH350 = 0.001;
			double tRH400 = 0.001; double tRH450 = 0.001;
			double tRH500 = 0.001; double tRH550 = 0.001;
			double tRH600 = 0.001; double tRH650 = 0.001;
			double tRH700 = 0.001; double tRH750 = 0.001;
			double tRH800 = 0.001; double tRH850 = 0.001;
			double tRH900 = 0.001; double tRH950 = 0.001;
			double tRH1000 = 0.001; double tRH0 = 0.001;

			double tWU100 = 0.001; double tWU150 = 0.001;
			double tWU200 = 0.001; double tWU250 = 0.001;
			double tWU300 = 0.001; double tWU350 = 0.001;
			double tWU400 = 0.001; double tWU450 = 0.001;
			double tWU500 = 0.001; double tWU550 = 0.001;
			double tWU600 = 0.001; double tWU650 = 0.001;
			double tWU700 = 0.001; double tWU750 = 0.001;
			double tWU800 = 0.001; double tWU850 = 0.001;
			double tWU900 = 0.001; double tWU950 = 0.001;
			double tWU1000 = 0.001; double tWU0 = 0.001;
			
			double tWV100 = 0.001; double tWV150 = 0.001;
			double tWV200 = 0.001; double tWV250 = 0.001;
			double tWV300 = 0.001; double tWV350 = 0.001;
			double tWV400 = 0.001; double tWV450 = 0.001;
			double tWV500 = 0.001; double tWV550 = 0.001;
			double tWV600 = 0.001; double tWV650 = 0.001;
			double tWV700 = 0.001; double tWV750 = 0.001;
			double tWV800 = 0.001; double tWV850 = 0.001;
			double tWV900 = 0.001; double tWV950 = 0.001;
			double tWV1000 = 0.001; double tWV0 = 0.001;
			
			double tCAPE = 0.001; double tCIN = 0.001;
			double tPRATE = 0.001; double tHGT500 = 0.001;
			double tPWAT = 0.001;
			double tLI = 0.001;
			
			Scanner hrrrScanner = null; try {		
				hrrrScanner = new Scanner(sounding);
				while(hrrrScanner.hasNext()) {
					String line = hrrrScanner.nextLine();

					if(line.startsWith(((iSx+0)+(iSs*0))+":")) { String[] lineTmp = line.split(","); try { tTC100 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*1))+":")) { String[] lineTmp = line.split(","); try { tTC150 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*2))+":")) { String[] lineTmp = line.split(","); try { tTC200 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*3))+":")) { String[] lineTmp = line.split(","); try { tTC250 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*4))+":")) { String[] lineTmp = line.split(","); try { tTC300 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*5))+":")) { String[] lineTmp = line.split(","); try { tTC350 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*6))+":")) { String[] lineTmp = line.split(","); try { tTC400 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*7))+":")) { String[] lineTmp = line.split(","); try { tTC450 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*8)+1)+":")) { String[] lineTmp = line.split(","); try { tTC500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*9)+1)+":")) { String[] lineTmp = line.split(","); try { tTC550 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*10)+1)+":")) { String[] lineTmp = line.split(","); try { tTC600 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*11)+1)+":")) { String[] lineTmp = line.split(","); try { tTC650 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*12)+1)+":")) { String[] lineTmp = line.split(","); try { tTC700 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*13)+1)+":")) { String[] lineTmp = line.split(","); try { tTC750 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*14)+1)+":")) { String[] lineTmp = line.split(","); try { tTC800 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*15)+1)+":")) { String[] lineTmp = line.split(","); try { tTC850 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*16)+1)+":")) { String[] lineTmp = line.split(","); try { tTC900 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*17)+1)+":")) { String[] lineTmp = line.split(","); try { tTC950 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+0)+(iSs*18)+1)+":")) { String[] lineTmp = line.split(","); try { tTC1000 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("98:")) { String[] lineTmp = line.split(","); try { tTC0 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }

					if(line.startsWith(((iSx+1)+(iSs*0))+":")) { String[] lineTmp = line.split(","); try { tRH100 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*1))+":")) { String[] lineTmp = line.split(","); try { tRH150 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*2))+":")) { String[] lineTmp = line.split(","); try { tRH200 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*3))+":")) { String[] lineTmp = line.split(","); try { tRH250 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*4))+":")) { String[] lineTmp = line.split(","); try { tRH300 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*5))+":")) { String[] lineTmp = line.split(","); try { tRH350 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*6))+":")) { String[] lineTmp = line.split(","); try { tRH400 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*7))+":")) { String[] lineTmp = line.split(","); try { tRH450 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*8)+1)+":")) { String[] lineTmp = line.split(","); try { tRH500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*9)+1)+":")) { String[] lineTmp = line.split(","); try { tRH550 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*10)+1)+":")) { String[] lineTmp = line.split(","); try { tRH600 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*11)+1)+":")) { String[] lineTmp = line.split(","); try { tRH650 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*12)+1)+":")) { String[] lineTmp = line.split(","); try { tRH700 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*13)+1)+":")) { String[] lineTmp = line.split(","); try { tRH750 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*14)+1)+":")) { String[] lineTmp = line.split(","); try { tRH800 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*15)+1)+":")) { String[] lineTmp = line.split(","); try { tRH850 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*16)+1)+":")) { String[] lineTmp = line.split(","); try { tRH900 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*17)+1)+":")) { String[] lineTmp = line.split(","); try { tRH950 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+1)+(iSs*18)+1)+":")) { String[] lineTmp = line.split(","); try { tRH1000 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("100:")) { String[] lineTmp = line.split(","); try { tRH0 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }

					if(line.startsWith(((iSx+3)+(iSs*0))+":")) { String[] lineTmp = line.split(","); try { tWU100 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*1))+":")) { String[] lineTmp = line.split(","); try { tWU150 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*2))+":")) { String[] lineTmp = line.split(","); try { tWU200 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*3))+":")) { String[] lineTmp = line.split(","); try { tWU250 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*4))+":")) { String[] lineTmp = line.split(","); try { tWU300 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*5))+":")) { String[] lineTmp = line.split(","); try { tWU350 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*6))+":")) { String[] lineTmp = line.split(","); try { tWU400 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*7))+":")) { String[] lineTmp = line.split(","); try { tWU450 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*8)+1)+":")) { String[] lineTmp = line.split(","); try { tWU500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*9)+1)+":")) { String[] lineTmp = line.split(","); try { tWU550 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*10)+1)+":")) { String[] lineTmp = line.split(","); try { tWU600 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*11)+1)+":")) { String[] lineTmp = line.split(","); try { tWU650 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*12)+1)+":")) { String[] lineTmp = line.split(","); try { tWU700 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*13)+1)+":")) { String[] lineTmp = line.split(","); try { tWU750 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*14)+1)+":")) { String[] lineTmp = line.split(","); try { tWU800 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*15)+1)+":")) { String[] lineTmp = line.split(","); try { tWU850 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*16)+1)+":")) { String[] lineTmp = line.split(","); try { tWU900 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*17)+1)+":")) { String[] lineTmp = line.split(","); try { tWU950 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+3)+(iSs*18)+1)+":")) { String[] lineTmp = line.split(","); try { tWU1000 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("101:")) { String[] lineTmp = line.split(","); try { tWU0 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }

					if(line.startsWith(((iSx+4)+(iSs*0))+":")) { String[] lineTmp = line.split(","); try { tWV100 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*1))+":")) { String[] lineTmp = line.split(","); try { tWV150 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*2))+":")) { String[] lineTmp = line.split(","); try { tWV200 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*3))+":")) { String[] lineTmp = line.split(","); try { tWV250 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*4))+":")) { String[] lineTmp = line.split(","); try { tWV300 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*5))+":")) { String[] lineTmp = line.split(","); try { tWV350 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*6))+":")) { String[] lineTmp = line.split(","); try { tWV400 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*7))+":")) { String[] lineTmp = line.split(","); try { tWV450 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*8)+1)+":")) { String[] lineTmp = line.split(","); try { tWV500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*9)+1)+":")) { String[] lineTmp = line.split(","); try { tWV550 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*10)+1)+":")) { String[] lineTmp = line.split(","); try { tWV600 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*11)+1)+":")) { String[] lineTmp = line.split(","); try { tWV650 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*12)+1)+":")) { String[] lineTmp = line.split(","); try { tWV700 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*13)+1)+":")) { String[] lineTmp = line.split(","); try { tWV750 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*14)+1)+":")) { String[] lineTmp = line.split(","); try { tWV800 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*15)+1)+":")) { String[] lineTmp = line.split(","); try { tWV850 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*16)+1)+":")) { String[] lineTmp = line.split(","); try { tWV900 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*17)+1)+":")) { String[] lineTmp = line.split(","); try { tWV950 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith(((iSx+4)+(iSs*18)+1)+":")) { String[] lineTmp = line.split(","); try { tWV1000 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("102:")) { String[] lineTmp = line.split(","); try { tWV0 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					
					if(line.startsWith("105:")) { String[] lineTmp = line.split(","); try { tCAPE = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("106:")) { String[] lineTmp = line.split(","); try { tCIN = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("103:")) { String[] lineTmp = line.split(","); try { tPRATE = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("107:")) { String[] lineTmp = line.split(","); try { tPWAT = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("41:")) { String[] lineTmp = line.split(","); try { tHGT500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("109:")) { String[] lineTmp = line.split(","); try { tLI = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					
				}
				
			}
			catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
			
			double tTD100 = ModelShare.calcDwpt(tTC100, tRH100); double tTD150 = ModelShare.calcDwpt(tTC150, tRH150); 
			double tTD200 = ModelShare.calcDwpt(tTC200, tRH200); double tTD250 = ModelShare.calcDwpt(tTC250, tRH250); 
			double tTD300 = ModelShare.calcDwpt(tTC300, tRH300); double tTD350 = ModelShare.calcDwpt(tTC350, tRH350); 
			double tTD400 = ModelShare.calcDwpt(tTC400, tRH400); double tTD450 = ModelShare.calcDwpt(tTC450, tRH450); 
			double tTD500 = ModelShare.calcDwpt(tTC500, tRH500); double tTD550 = ModelShare.calcDwpt(tTC550, tRH550); 
			double tTD600 = ModelShare.calcDwpt(tTC600, tRH600); double tTD650 = ModelShare.calcDwpt(tTC650, tRH650); 
			double tTD700 = ModelShare.calcDwpt(tTC700, tRH700); double tTD750 = ModelShare.calcDwpt(tTC750, tRH750); 
			double tTD800 = ModelShare.calcDwpt(tTC800, tRH800); double tTD850 = ModelShare.calcDwpt(tTC850, tRH850); 
			double tTD900 = ModelShare.calcDwpt(tTC900, tRH900); double tTD950 = ModelShare.calcDwpt(tTC950, tRH950); 
			double tTD1000 = ModelShare.calcDwpt(tTC1000, tRH1000); double tTD0 = ModelShare.calcDwpt(tTC0, tRH0); 
			
			double tWD100 = ModelShare.windDirCalc(tWU100, tWV100); double tWD150 = ModelShare.windDirCalc(tWU150, tWV150);
			double tWS100 = ModelShare.windSpdCalc(tWU100, tWV100); double tWS150 = ModelShare.windSpdCalc(tWU150, tWV150);
			double tWD200 = ModelShare.windDirCalc(tWU200, tWV200); double tWD250 = ModelShare.windDirCalc(tWU250, tWV250);
			double tWS200 = ModelShare.windSpdCalc(tWU200, tWV200); double tWS250 = ModelShare.windSpdCalc(tWU250, tWV250);
			double tWD300 = ModelShare.windDirCalc(tWU300, tWV300); double tWD350 = ModelShare.windDirCalc(tWU350, tWV350);
			double tWS300 = ModelShare.windSpdCalc(tWU300, tWV300); double tWS350 = ModelShare.windSpdCalc(tWU350, tWV350);
			double tWD400 = ModelShare.windDirCalc(tWU400, tWV400); double tWD450 = ModelShare.windDirCalc(tWU450, tWV450);
			double tWS400 = ModelShare.windSpdCalc(tWU400, tWV400); double tWS450 = ModelShare.windSpdCalc(tWU450, tWV450);
			double tWD500 = ModelShare.windDirCalc(tWU500, tWV500); double tWD550 = ModelShare.windDirCalc(tWU550, tWV550);
			double tWS500 = ModelShare.windSpdCalc(tWU500, tWV500); double tWS550 = ModelShare.windSpdCalc(tWU550, tWV550);
			double tWD600 = ModelShare.windDirCalc(tWU600, tWV600); double tWD650 = ModelShare.windDirCalc(tWU650, tWV650);
			double tWS600 = ModelShare.windSpdCalc(tWU600, tWV600); double tWS650 = ModelShare.windSpdCalc(tWU650, tWV650);
			double tWD700 = ModelShare.windDirCalc(tWU700, tWV700); double tWD750 = ModelShare.windDirCalc(tWU750, tWV750);
			double tWS700 = ModelShare.windSpdCalc(tWU700, tWV700); double tWS750 = ModelShare.windSpdCalc(tWU750, tWV750);
			double tWD800 = ModelShare.windDirCalc(tWU800, tWV800); double tWD850 = ModelShare.windDirCalc(tWU850, tWV850);
			double tWS800 = ModelShare.windSpdCalc(tWU800, tWV800); double tWS850 = ModelShare.windSpdCalc(tWU850, tWV850);
			double tWD900 = ModelShare.windDirCalc(tWU900, tWV900); double tWD950 = ModelShare.windDirCalc(tWU950, tWV950);
			double tWS900 = ModelShare.windSpdCalc(tWU900, tWV900); double tWS950 = ModelShare.windSpdCalc(tWU950, tWV950);
			double tWD1000 = ModelShare.windDirCalc(tWU1000, tWV1000); double tWD0 = ModelShare.windDirCalc(tWU0, tWV0); 
			double tWS1000 = ModelShare.windSpdCalc(tWU1000, tWV1000); double tWS0 = ModelShare.windSpdCalc(tWU0, tWV0);
	
			if(tTC100 != 0.0001) { hrrrJSON.put("T100_"+tFHour, tTC100); }
			if(tTC150 != 0.0001) { hrrrJSON.put("T150_"+tFHour, tTC150); }
			if(tTC200 != 0.0001) { hrrrJSON.put("T200_"+tFHour, tTC200); }
			if(tTC250 != 0.0001) { hrrrJSON.put("T250_"+tFHour, tTC250); }
			if(tTC300 != 0.0001) { hrrrJSON.put("T300_"+tFHour, tTC300); }
			if(tTC350 != 0.0001) { hrrrJSON.put("T350_"+tFHour, tTC350); }
			if(tTC400 != 0.0001) { hrrrJSON.put("T400_"+tFHour, tTC400); }
			if(tTC450 != 0.0001) { hrrrJSON.put("T450_"+tFHour, tTC450); }
			if(tTC500 != 0.0001) { hrrrJSON.put("T500_"+tFHour, tTC500); }
			if(tTC550 != 0.0001) { hrrrJSON.put("T550_"+tFHour, tTC550); }
			if(tTC600 != 0.0001) { hrrrJSON.put("T600_"+tFHour, tTC600); }
			if(tTC650 != 0.0001) { hrrrJSON.put("T650_"+tFHour, tTC650); }
			if(tTC700 != 0.0001) { hrrrJSON.put("T700_"+tFHour, tTC700); }
			if(tTC750 != 0.0001) { hrrrJSON.put("T750_"+tFHour, tTC750); }
			if(tTC800 != 0.0001) { hrrrJSON.put("T800_"+tFHour, tTC800); }
			if(tTC850 != 0.0001) { hrrrJSON.put("T850_"+tFHour, tTC850); }
			if(tTC900 != 0.0001) { hrrrJSON.put("T900_"+tFHour, tTC900); }
			if(tTC950 != 0.0001) { hrrrJSON.put("T950_"+tFHour, tTC950); }
			if(tTC1000 != 0.0001) { hrrrJSON.put("T1000_"+tFHour, tTC1000); }
			if(tTC0 != 0.0001) { hrrrJSON.put("T0_"+tFHour, tTC0); }
			
			if(tTD100 != 0.0001) { hrrrJSON.put("D100_"+tFHour, tTD100); }
			if(tTD150 != 0.0001) { hrrrJSON.put("D150_"+tFHour, tTD150); }
			if(tTD200 != 0.0001) { hrrrJSON.put("D200_"+tFHour, tTD200); }
			if(tTD250 != 0.0001) { hrrrJSON.put("D250_"+tFHour, tTD250); }
			if(tTD300 != 0.0001) { hrrrJSON.put("D300_"+tFHour, tTD300); }
			if(tTD350 != 0.0001) { hrrrJSON.put("D350_"+tFHour, tTD350); }
			if(tTD400 != 0.0001) { hrrrJSON.put("D400_"+tFHour, tTD400); }
			if(tTD450 != 0.0001) { hrrrJSON.put("D450_"+tFHour, tTD450); }
			if(tTD500 != 0.0001) { hrrrJSON.put("D500_"+tFHour, tTD500); }
			if(tTD550 != 0.0001) { hrrrJSON.put("D550_"+tFHour, tTD550); }
			if(tTD600 != 0.0001) { hrrrJSON.put("D600_"+tFHour, tTD600); }
			if(tTD650 != 0.0001) { hrrrJSON.put("D650_"+tFHour, tTD650); }
			if(tTD700 != 0.0001) { hrrrJSON.put("D700_"+tFHour, tTD700); }
			if(tTD750 != 0.0001) { hrrrJSON.put("D750_"+tFHour, tTD750); }
			if(tTD800 != 0.0001) { hrrrJSON.put("D800_"+tFHour, tTD800); }
			if(tTD850 != 0.0001) { hrrrJSON.put("D850_"+tFHour, tTD850); }
			if(tTD900 != 0.0001) { hrrrJSON.put("D900_"+tFHour, tTD900); }
			if(tTD950 != 0.0001) { hrrrJSON.put("D950_"+tFHour, tTD950); }
			if(tTD1000 != 0.0001) { hrrrJSON.put("D1000_"+tFHour, tTD1000); }
			if(tTD0 != 0.0001) { hrrrJSON.put("D0_"+tFHour, tTD0); }
			
			if(tWD100 != 0.0001) { hrrrJSON.put("WD100_"+tFHour, tWD100); }
			if(tWD150 != 0.0001) { hrrrJSON.put("WD150_"+tFHour, tWD150); }
			if(tWD200 != 0.0001) { hrrrJSON.put("WD200_"+tFHour, tWD200); }
			if(tWD250 != 0.0001) { hrrrJSON.put("WD250_"+tFHour, tWD250); }
			if(tWD300 != 0.0001) { hrrrJSON.put("WD300_"+tFHour, tWD300); }
			if(tWD350 != 0.0001) { hrrrJSON.put("WD350_"+tFHour, tWD350); }
			if(tWD400 != 0.0001) { hrrrJSON.put("WD400_"+tFHour, tWD400); }
			if(tWD450 != 0.0001) { hrrrJSON.put("WD450_"+tFHour, tWD450); }
			if(tWD500 != 0.0001) { hrrrJSON.put("WD500_"+tFHour, tWD500); }
			if(tWD550 != 0.0001) { hrrrJSON.put("WD550_"+tFHour, tWD550); }
			if(tWD600 != 0.0001) { hrrrJSON.put("WD600_"+tFHour, tWD600); }
			if(tWD650 != 0.0001) { hrrrJSON.put("WD650_"+tFHour, tWD650); }
			if(tWD700 != 0.0001) { hrrrJSON.put("WD700_"+tFHour, tWD700); }
			if(tWD750 != 0.0001) { hrrrJSON.put("WD750_"+tFHour, tWD750); }
			if(tWD800 != 0.0001) { hrrrJSON.put("WD800_"+tFHour, tWD800); }
			if(tWD850 != 0.0001) { hrrrJSON.put("WD850_"+tFHour, tWD850); }
			if(tWD900 != 0.0001) { hrrrJSON.put("WD900_"+tFHour, tWD900); }
			if(tWD950 != 0.0001) { hrrrJSON.put("WD950_"+tFHour, tWD950); }
			if(tWD1000 != 0.0001) { hrrrJSON.put("WD1000_"+tFHour, tWD1000); }
			if(tWD0 != 0.0001) { hrrrJSON.put("WD0_"+tFHour, tWD0); }
			
			if(tWS100 != 0.0001) { hrrrJSON.put("WS100_"+tFHour, tWS100); }
			if(tWS150 != 0.0001) { hrrrJSON.put("WS150_"+tFHour, tWS150); }
			if(tWS200 != 0.0001) { hrrrJSON.put("WS200_"+tFHour, tWS200); }
			if(tWS250 != 0.0001) { hrrrJSON.put("WS250_"+tFHour, tWS250); }
			if(tWS300 != 0.0001) { hrrrJSON.put("WS300_"+tFHour, tWS300); }
			if(tWS350 != 0.0001) { hrrrJSON.put("WS350_"+tFHour, tWS350); }
			if(tWS400 != 0.0001) { hrrrJSON.put("WS400_"+tFHour, tWS400); }
			if(tWS450 != 0.0001) { hrrrJSON.put("WS450_"+tFHour, tWS450); }
			if(tWS500 != 0.0001) { hrrrJSON.put("WS500_"+tFHour, tWS500); }
			if(tWS550 != 0.0001) { hrrrJSON.put("WS550_"+tFHour, tWS550); }
			if(tWS600 != 0.0001) { hrrrJSON.put("WS600_"+tFHour, tWS600); }
			if(tWS650 != 0.0001) { hrrrJSON.put("WS650_"+tFHour, tWS650); }
			if(tWS700 != 0.0001) { hrrrJSON.put("WS700_"+tFHour, tWS700); }
			if(tWS750 != 0.0001) { hrrrJSON.put("WS750_"+tFHour, tWS750); }
			if(tWS800 != 0.0001) { hrrrJSON.put("WS800_"+tFHour, tWS800); }
			if(tWS850 != 0.0001) { hrrrJSON.put("WS850_"+tFHour, tWS850); }
			if(tWS900 != 0.0001) { hrrrJSON.put("WS900_"+tFHour, tWS900); }
			if(tWS950 != 0.0001) { hrrrJSON.put("WS950_"+tFHour, tWS950); }
			if(tWS1000 != 0.0001) { hrrrJSON.put("WS1000_"+tFHour, tWS1000); }
			
			if(tWS0 != 0.0001) { hrrrJSON.put("WS0_"+tFHour, tWS0); }
			if(tCIN != 0.0001) { hrrrJSON.put("CIN_"+tFHour, tCIN); }
			if(tLI != 0.0001) { hrrrJSON.put("LI_"+tFHour, tLI); }
			if(tPRATE != 0.0001) { hrrrJSON.put("PRATE_"+tFHour, tPRATE*0.03937); }
			if(tPWAT != 0.0001) { hrrrJSON.put("PWAT_"+tFHour, tPRATE*0.03937); }
			if(tHGT500 != 0.0001) { hrrrJSON.put("HGT500_"+tFHour, tHGT500); }
			
			StumpJunk.runProcess("(echo \"run "+helpers.getPath()+"/ModelData.gs "+modelName+" "+tFHour4D+" "+getDate+" "+getHour+" "+xml2Path.getPath()+"\" | "+appPath.getPath()+"/grads -blc \"open "+tFXCtlFile.getPath()+"\")");
		
			tFHGlob.delete();
		
		}
			
		String thisJSONstring = hrrrJSON.toString();
		thisJSONstring = thisJSONstring.substring(0, thisJSONstring.length()-1)+",";
		try { StumpJunk.varToFile(thisJSONstring, jsonOutFile, true); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
		System.out.println(" -> Completed: "+modelName+" - Round: "+round);
			
	}
	
}
