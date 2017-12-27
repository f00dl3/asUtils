/*
by Anthony Stump
Created: 4 Oct 2017
Updated: 27 Dec 2017
*/

package asUtils.Model;

import asUtils.Shares.StumpJunk;
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

import asUtils.Shares.MyDBConnector;
import asUtils.Shares.JunkyBeans;

public class ModelWorkerCMC {
	
	public static void main(String args[]) {
		
                JunkyBeans junkyBeans = new JunkyBeans();
                ModelBeans modelBeans = new ModelBeans();
            
		final String getHour = args[0];
		final String round = args[1];
		final DateTime tDateTime = new DateTime(DateTimeZone.UTC).minusHours(4);
		final DateTimeFormatter getDateFormat = DateTimeFormat.forPattern("yyyyMMdd");
		final String getDate = getDateFormat.print(tDateTime);
		final File appPath = junkyBeans.getAppShareSys();
		final File xml2Path = modelBeans.getXml2Path();
		final File helpers = junkyBeans.getHelpers();
		final String pointInputAsString = ModelShare.pointInputAsString("KOJC");
		final File jsonOutFile = new File(xml2Path+"/cmcOut_"+round+".json");
		final String modelName = "CMC";
		final String cmcBase = "http://dd.weather.gc.ca/model_gem_global/25km/grib2/lat_lon/"+getHour;
		final String cFHq = "SELECT FHour FROM WxObs.GFSFHA WHERE DoGet=1 AND CMC=1 AND Round="+round+";";
		final String vCMCq = "SELECT VarName FROM WxObs.CMCModelVarsV2 WHERE HeightLoop=0;";
		final String vlCMCq = "SELECT VarName FROM WxObs.CMCModelVarsV2 WHERE HeightLoop=1;";
		final String heightQ = "SELECT HeightMb FROM WxObs.ModelHeightLevels WHERE CMC=1 ORDER BY HeightMB DESC;";

		int gribSpot = 3;
		
		JSONObject cmcJSON = new JSONObject();
		
		List<String> cFHList = new ArrayList<>();
		List<String> vCMC = new ArrayList<>();
		List<String> vlCMC = new ArrayList<>();
		List<String> heightList = new ArrayList<>();
		
		try (
			Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();
			ResultSet resultSetCFH = stmt.executeQuery(cFHq);
		) { while (resultSetCFH.next()) { cFHList.add(resultSetCFH.getString("FHour")); } }
		catch (Exception e) { e.printStackTrace(); }

		try (
			Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();
			ResultSet resultSetVcmc = stmt.executeQuery(vCMCq);
		) { while (resultSetVcmc.next()) { vCMC.add(resultSetVcmc.getString("VarName")); } }
		catch (Exception e) { e.printStackTrace(); }

		try (
			Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();
			ResultSet resultSetVLcmc = stmt.executeQuery(vlCMCq);
		) { while (resultSetVLcmc.next()) { vlCMC.add(resultSetVLcmc.getString("VarName")); } }
		catch (Exception e) { e.printStackTrace(); }
				
		try (
			Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();
			ResultSet resultSetHeights = stmt.executeQuery(heightQ);
		) { while (resultSetHeights.next()) { heightList.add(resultSetHeights.getString("HeightMb")); } }
		catch (Exception e) { e.printStackTrace(); }

		for (String tFHourStr : cFHList) {
				
			int tFHour = Integer.parseInt(tFHourStr);
			String tFHour2D = String.format("%02d", tFHour);
			String tFHour3D = String.format("%03d", tFHour);
			String tFHour4D = String.format("%04d", tFHour);
			File sounding = new File(xml2Path.getPath()+"/outCMC_"+tFHour4D+".csv");
			File tFHGlob = new File(xml2Path.getPath()+"/fc"+tFHour4D);
			File tFHCtlFile = new File(tFHGlob.getPath()+".ctl");

			for(String tVar : vlCMC) {
				for(String tHeight : heightList) {
					String thisFHData = cmcBase+"/"+tFHour3D+"/CMC_glb_"+tVar+"_"+tHeight+"_latlon.24x.24_"+getDate+getHour+"_P"+tFHour3D+".grib2";
					File thisFHFile = new File(tFHGlob.getPath()+"_"+tVar+"_"+tHeight+".part");
					System.out.println(thisFHData);
					StumpJunk.jsoupOutBinary(thisFHData, thisFHFile, modelBeans.getDownloadTimeout());
				}
			}
			
			for(String tVar : vCMC) {
				String thisFHData = cmcBase+"/"+tFHour3D+"/CMC_glb_"+tVar+"_latlon.24x.24_"+getDate+getHour+"_P"+tFHour3D+".grib2";
				File thisFHFile = new File(tFHGlob.getPath()+"_"+tVar+"_SFC_0.part");
				System.out.println(thisFHData);
				StumpJunk.jsoupOutBinary(thisFHData, thisFHFile, modelBeans.getDownloadTimeout());
			}
			
			StumpJunk.runProcess("(cat "+tFHGlob.getPath()+"*.part > "+tFHGlob.getPath()+")");
			StumpJunk.runProcess("rm "+tFHGlob.getPath()+"*.part");
			
			try { StumpJunk.runProcessOutFile("\""+appPath.getPath()+"/wgrib2\" "+tFHGlob.getPath()+" "+pointInputAsString, sounding, false); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
			StumpJunk.sedFileReplace(sounding.getPath(), ":lon", ",lon");
			try { StumpJunk.runProcessOutFile("\""+appPath.getPath()+"/g2ctl\" "+tFHGlob.getPath(), tFHCtlFile, false); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
			StumpJunk.runProcess("\""+appPath.getPath()+"/gribmap\" -v -i "+tFHCtlFile.getPath());
			
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
			
			double tPRATE = 0.001; double tHGT500 = 0.001;
		
			Scanner cmcScanner = null; try {		
				cmcScanner = new Scanner(sounding);
				while(cmcScanner.hasNext()) {
					String line = cmcScanner.nextLine();
					
					if(line.startsWith("24:")) { String[] lineTmp = line.split(","); try { tTC100 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("25:")) { String[] lineTmp = line.split(","); try { tTC150 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("26:")) { String[] lineTmp = line.split(","); try { tTC200 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("27:")) { String[] lineTmp = line.split(","); try { tTC250 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("28:")) { String[] lineTmp = line.split(","); try { tTC300 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("29:")) { String[] lineTmp = line.split(","); try { tTC350 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("30:")) { String[] lineTmp = line.split(","); try { tTC400 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("31:")) { String[] lineTmp = line.split(","); try { tTC450 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("32:")) { String[] lineTmp = line.split(","); try { tTC500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("33:")) { String[] lineTmp = line.split(","); try { tTC550 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("34:")) { String[] lineTmp = line.split(","); try { tTC600 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("35:")) { String[] lineTmp = line.split(","); try { tTC650 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("36:")) { String[] lineTmp = line.split(","); try { tTC700 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("37:")) { String[] lineTmp = line.split(","); try { tTC750 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("38:")) { String[] lineTmp = line.split(","); try { tTC800 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("39:")) { String[] lineTmp = line.split(","); try { tTC850 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("40:")) { String[] lineTmp = line.split(","); try { tTC900 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("41:")) { String[] lineTmp = line.split(","); try { tTC950 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("23:")) { String[] lineTmp = line.split(","); try { tTC1000 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("42:")) { String[] lineTmp = line.split(","); try { tTC0 = Double.parseDouble(lineTmp[gribSpot].replace("val=", ""))-273.15; } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
			
					if(line.startsWith("44:")) { String[] lineTmp = line.split(","); try { tWU100 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("45:")) { String[] lineTmp = line.split(","); try { tWU150 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("46:")) { String[] lineTmp = line.split(","); try { tWU200 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("47:")) { String[] lineTmp = line.split(","); try { tWU250 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("48:")) { String[] lineTmp = line.split(","); try { tWU300 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("49:")) { String[] lineTmp = line.split(","); try { tWU350 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("50:")) { String[] lineTmp = line.split(","); try { tWU400 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("51:")) { String[] lineTmp = line.split(","); try { tWU450 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("52:")) { String[] lineTmp = line.split(","); try { tWU500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("53:")) { String[] lineTmp = line.split(","); try { tWU550 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("54:")) { String[] lineTmp = line.split(","); try { tWU600 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("55:")) { String[] lineTmp = line.split(","); try { tWU650 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("56:")) { String[] lineTmp = line.split(","); try { tWU700 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("57:")) { String[] lineTmp = line.split(","); try { tWU750 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("58:")) { String[] lineTmp = line.split(","); try { tWU800 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("59:")) { String[] lineTmp = line.split(","); try { tWU850 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("60:")) { String[] lineTmp = line.split(","); try { tWU900 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("61:")) { String[] lineTmp = line.split(","); try { tWU950 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("43:")) { String[] lineTmp = line.split(","); try { tWU1000 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("62:")) { String[] lineTmp = line.split(","); try { tWU0 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }

					if(line.startsWith("64:")) { String[] lineTmp = line.split(","); try { tWV100 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("65:")) { String[] lineTmp = line.split(","); try { tWV150 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("66:")) { String[] lineTmp = line.split(","); try { tWV200 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("67:")) { String[] lineTmp = line.split(","); try { tWV250 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("68:")) { String[] lineTmp = line.split(","); try { tWV300 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("69:")) { String[] lineTmp = line.split(","); try { tWV350 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("70:")) { String[] lineTmp = line.split(","); try { tWV400 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("71:")) { String[] lineTmp = line.split(","); try { tWV450 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("72:")) { String[] lineTmp = line.split(","); try { tWV500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("73:")) { String[] lineTmp = line.split(","); try { tWV550 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("74:")) { String[] lineTmp = line.split(","); try { tWV600 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("75:")) { String[] lineTmp = line.split(","); try { tWV650 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("76:")) { String[] lineTmp = line.split(","); try { tWV700 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("77:")) { String[] lineTmp = line.split(","); try { tWV750 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("78:")) { String[] lineTmp = line.split(","); try { tWV800 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("79:")) { String[] lineTmp = line.split(","); try { tWV850 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("80:")) { String[] lineTmp = line.split(","); try { tWV900 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("81:")) { String[] lineTmp = line.split(","); try { tWV950 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("63:")) { String[] lineTmp = line.split(","); try { tWV1000 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("82:")) { String[] lineTmp = line.split(","); try { tWV0 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
		
					if(line.startsWith("1:")) { String[] lineTmp = line.split(","); try { tPRATE = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }
					if(line.startsWith("12:")) { String[] lineTmp = line.split(","); try { tHGT500 = Double.parseDouble(lineTmp[gribSpot].replace("val=", "")); } catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); } }

				}
				
			}
			catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
						
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

			if(tTC100 != 0.0001) { cmcJSON.put("T100_"+tFHour, tTC100); }
			if(tTC150 != 0.0001) { cmcJSON.put("T150_"+tFHour, tTC150); }
			if(tTC200 != 0.0001) { cmcJSON.put("T200_"+tFHour, tTC200); }
			if(tTC250 != 0.0001) { cmcJSON.put("T250_"+tFHour, tTC250); }
			if(tTC300 != 0.0001) { cmcJSON.put("T300_"+tFHour, tTC300); }
			if(tTC350 != 0.0001) { cmcJSON.put("T350_"+tFHour, tTC350); }
			if(tTC400 != 0.0001) { cmcJSON.put("T400_"+tFHour, tTC400); }
			if(tTC450 != 0.0001) { cmcJSON.put("T450_"+tFHour, tTC450); }
			if(tTC500 != 0.0001) { cmcJSON.put("T500_"+tFHour, tTC500); }
			if(tTC550 != 0.0001) { cmcJSON.put("T550_"+tFHour, tTC550); }
			if(tTC600 != 0.0001) { cmcJSON.put("T600_"+tFHour, tTC600); }
			if(tTC650 != 0.0001) { cmcJSON.put("T650_"+tFHour, tTC650); }
			if(tTC700 != 0.0001) { cmcJSON.put("T700_"+tFHour, tTC700); }
			if(tTC750 != 0.0001) { cmcJSON.put("T750_"+tFHour, tTC750); }
			if(tTC800 != 0.0001) { cmcJSON.put("T800_"+tFHour, tTC800); }
			if(tTC850 != 0.0001) { cmcJSON.put("T850_"+tFHour, tTC850); }
			if(tTC900 != 0.0001) { cmcJSON.put("T900_"+tFHour, tTC900); }
			if(tTC950 != 0.0001) { cmcJSON.put("T950_"+tFHour, tTC950); }
			if(tTC1000 != 0.0001) { cmcJSON.put("T1000_"+tFHour, tTC1000); }
			if(tTC0 != 0.0001) { cmcJSON.put("T0_"+tFHour, tTC0); }
					
			if(tWD100 != 0.0001) { cmcJSON.put("WD100_"+tFHour, tWD100); }
			if(tWD150 != 0.0001) { cmcJSON.put("WD150_"+tFHour, tWD150); }
			if(tWD200 != 0.0001) { cmcJSON.put("WD200_"+tFHour, tWD200); }
			if(tWD250 != 0.0001) { cmcJSON.put("WD250_"+tFHour, tWD250); }
			if(tWD300 != 0.0001) { cmcJSON.put("WD300_"+tFHour, tWD300); }
			if(tWD350 != 0.0001) { cmcJSON.put("WD350_"+tFHour, tWD350); }
			if(tWD400 != 0.0001) { cmcJSON.put("WD400_"+tFHour, tWD400); }
			if(tWD450 != 0.0001) { cmcJSON.put("WD450_"+tFHour, tWD450); }
			if(tWD500 != 0.0001) { cmcJSON.put("WD500_"+tFHour, tWD500); }
			if(tWD550 != 0.0001) { cmcJSON.put("WD550_"+tFHour, tWD550); }
			if(tWD600 != 0.0001) { cmcJSON.put("WD600_"+tFHour, tWD600); }
			if(tWD650 != 0.0001) { cmcJSON.put("WD650_"+tFHour, tWD650); }
			if(tWD700 != 0.0001) { cmcJSON.put("WD700_"+tFHour, tWD700); }
			if(tWD750 != 0.0001) { cmcJSON.put("WD750_"+tFHour, tWD750); }
			if(tWD800 != 0.0001) { cmcJSON.put("WD800_"+tFHour, tWD800); }
			if(tWD850 != 0.0001) { cmcJSON.put("WD850_"+tFHour, tWD850); }
			if(tWD900 != 0.0001) { cmcJSON.put("WD900_"+tFHour, tWD900); }
			if(tWD950 != 0.0001) { cmcJSON.put("WD950_"+tFHour, tWD950); }
			if(tWD1000 != 0.0001) { cmcJSON.put("WD1000_"+tFHour, tWD1000); }
			if(tWD0 != 0.0001) { cmcJSON.put("WD0_"+tFHour, tWD0); }
			
			if(tWS100 != 0.0001) { cmcJSON.put("WS100_"+tFHour, tWS100); }
			if(tWS150 != 0.0001) { cmcJSON.put("WS150_"+tFHour, tWS150); }
			if(tWS200 != 0.0001) { cmcJSON.put("WS200_"+tFHour, tWS200); }
			if(tWS250 != 0.0001) { cmcJSON.put("WS250_"+tFHour, tWS250); }
			if(tWS300 != 0.0001) { cmcJSON.put("WS300_"+tFHour, tWS300); }
			if(tWS350 != 0.0001) { cmcJSON.put("WS350_"+tFHour, tWS350); }
			if(tWS400 != 0.0001) { cmcJSON.put("WS400_"+tFHour, tWS400); }
			if(tWS450 != 0.0001) { cmcJSON.put("WS450_"+tFHour, tWS450); }
			if(tWS500 != 0.0001) { cmcJSON.put("WS500_"+tFHour, tWS500); }
			if(tWS550 != 0.0001) { cmcJSON.put("WS550_"+tFHour, tWS550); }
			if(tWS600 != 0.0001) { cmcJSON.put("WS600_"+tFHour, tWS600); }
			if(tWS650 != 0.0001) { cmcJSON.put("WS650_"+tFHour, tWS650); }
			if(tWS700 != 0.0001) { cmcJSON.put("WS700_"+tFHour, tWS700); }
			if(tWS750 != 0.0001) { cmcJSON.put("WS750_"+tFHour, tWS750); }
			if(tWS800 != 0.0001) { cmcJSON.put("WS800_"+tFHour, tWS800); }
			if(tWS850 != 0.0001) { cmcJSON.put("WS850_"+tFHour, tWS850); }
			if(tWS900 != 0.0001) { cmcJSON.put("WS900_"+tFHour, tWS900); }
			if(tWS950 != 0.0001) { cmcJSON.put("WS950_"+tFHour, tWS950); }
			if(tWS1000 != 0.0001) { cmcJSON.put("WS1000_"+tFHour, tWS1000); }
			
			if(tPRATE != 0.0001) { cmcJSON.put("PRATE_"+tFHour, tPRATE*0.03937); }
			if(tHGT500 != 0.0001) { cmcJSON.put("HGT500_"+tFHour, tHGT500); }
			
			StumpJunk.runProcess("(echo \"run "+helpers.getPath()+"/ModelData.gs "+modelName+" "+tFHour4D+" "+getDate+" "+getHour+" "+xml2Path.getPath()+"\" | "+appPath.getPath()+"/grads -blc \"open "+tFHCtlFile.getPath()+"\")");
			
			tFHGlob.delete();
			
		}

		String thisJSONstring = cmcJSON.toString();
		thisJSONstring = thisJSONstring.substring(0, thisJSONstring.length()-1)+",";
		try { StumpJunk.varToFile(thisJSONstring, jsonOutFile, true); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
		System.out.println(" -> Completed: "+modelName+" - Round: "+round);

	}
}
