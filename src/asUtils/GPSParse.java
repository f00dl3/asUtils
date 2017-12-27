/* 
by Anthony Stump
Created: 4 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils;

import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import asUtils.Shares.MyDBConnector;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.*;


public class GPSParse {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
		final String dropLocation = junkyBeans.getDesktopPath().toString();
                final String appName = junkyBeans.getApplicationName();
		final Path gpsInFile = Paths.get(dropLocation+"/"+args[0]+".csv");
		final String thisDate = args[0].substring(0, 10);
		final String bicycle = junkyBeans.getBicycle();
		final String legacyFlag = args[1];
		String fullGPSjson = "";
		String geoJSONtrace = "";

		System.out.println(appName+".GPSParse\nFile: "+gpsInFile.toString());

		String line = "";
	
		List<Double> intSpeeds = new ArrayList<>();
		List<Double> miles = new ArrayList<>();
		List<Double> wattPowers = new ArrayList<>();
		List<Integer> cadences = new ArrayList<>();
		List<Integer> heartRates = new ArrayList<>();
		List<Integer> logNos = new ArrayList<>();
		List<Integer> trackedSeconds = new ArrayList<>();

		try (BufferedReader br = Files.newBufferedReader(gpsInFile)) {
			String firstLine = br.readLine();
			while ((line = br.readLine()) != null) {
				String[] thisLine = line.split(";");

				String logNo = thisLine[0];
				logNos.add(Integer.parseInt(logNo));

				double latitude = 0.0;
				double longitude = 0.0;

				JSONObject gpsLog = new JSONObject();
				JSONObject gpsData = new JSONObject();
				gpsLog.put(logNo, gpsData);

				if(StumpJunk.isSetNotZero(thisLine[1])) { double altFt = StumpJunk.meters2Feet(Integer.parseInt(thisLine[1])/1000); gpsData.put("AltitudeFt", altFt); }
				if(StumpJunk.isSetNotZero(thisLine[2])) { double altDiffDownFt = StumpJunk.meters2Feet(Integer.parseInt(thisLine[2]) / 1000); gpsData.put("AltDiffDownFt", altDiffDownFt); }
				if(StumpJunk.isSetNotZero(thisLine[3])) { double altDiffUpFt = StumpJunk.meters2Feet(Integer.parseInt(thisLine[3]) / 1000); gpsData.put("AltDiffUpFt", altDiffUpFt); }
				if(StumpJunk.isSetNotZero(thisLine[4])) { int cadence = Integer.parseInt(thisLine[4]); gpsData.put("Cadence", cadence); cadences.add(cadence); }
				if(StumpJunk.isSetNotZero(thisLine[5])) { double kCal = Double.parseDouble(thisLine[5]); gpsData.put("kcal", kCal); }
				if(StumpJunk.isSetNotZero(thisLine[6])) { double distAbsMi = (Double.parseDouble(thisLine[6])/1000)*0.621; gpsData.put("DistTotMiles", distAbsMi); miles.add(distAbsMi); }
				if(StumpJunk.isSetNotZero(thisLine[7])) { double distInt = Double.parseDouble(thisLine[7]); gpsData.put("DistIntMeters", distInt); }
				if(StumpJunk.isSetNotZero(thisLine[8])) { double distIntDown = Double.parseDouble(thisLine[8]); gpsData.put("DistIntDownFt", StumpJunk.meters2Feet(distIntDown)); }
				if(StumpJunk.isSetNotZero(thisLine[9])) { double distIntUp = Double.parseDouble(thisLine[9]); gpsData.put("DistIntUpFt", StumpJunk.meters2Feet(distIntUp)); }
				if(StumpJunk.isSetNotZero(thisLine[10])) { int heartrate = Integer.parseInt(thisLine[10]); gpsData.put("HeartRate", heartrate); heartRates.add(heartrate); }
				if(StumpJunk.isSetNotZero(thisLine[11])) { double incline = Double.parseDouble(thisLine[11]); gpsData.put("Incline", incline); }
				if(StumpJunk.isSetNotZero(thisLine[12])) { int iZone = Integer.parseInt(thisLine[12]); gpsData.put("IntensityZone", iZone); }
				if(StumpJunk.isSetNotZero(thisLine[13])) { latitude = Double.parseDouble(thisLine[13]); gpsData.put("Latitude", latitude);  }
				if(StumpJunk.isSetNotZero(thisLine[14])) { longitude = Double.parseDouble(thisLine[14]); gpsData.put("Longitude", longitude); }
				if(StumpJunk.isSetNotZero(thisLine[15])) { double pHRMax = Double.parseDouble(thisLine[15]); gpsData.put("PercentHRMax", pHRMax); }
				if(StumpJunk.isSetNotZero(thisLine[16])) { double powerWatts = Double.parseDouble(thisLine[16]); gpsData.put("PowerWatts", powerWatts); wattPowers.add(powerWatts); }
				if(StumpJunk.isSetNotZero(thisLine[17])) { double pWRatio = Double.parseDouble(thisLine[17]); gpsData.put("PowerWeightRatio", pWRatio); }
				if(StumpJunk.isSetNotZero(thisLine[18])) { double riseRate = Double.parseDouble(thisLine[18]); gpsData.put("RiseRate", riseRate); }
				if(StumpJunk.isSetNotZero(thisLine[19])) { double speedMPH = Double.parseDouble(thisLine[19])*2.237; gpsData.put("SpeedMPH", speedMPH); intSpeeds.add(speedMPH); }
				if(StumpJunk.isSetNotZero(thisLine[20])) { String speedRef = thisLine[20]; gpsData.put("SpeedSource", speedRef); }
				if(StumpJunk.isSetNotZero(thisLine[21])) { int speedTime = Integer.parseInt(thisLine[21]); gpsData.put("SpeedTime", speedTime); }
				if(StumpJunk.isSetNotZero(thisLine[22])) { int targetZone = Integer.parseInt(thisLine[22]); gpsData.put("TargetZone", targetZone); }
				if(StumpJunk.isSetNotZero(thisLine[23])) { double tempF = StumpJunk.tempC2F(Double.parseDouble(thisLine[23])); gpsData.put("TemperatureF", tempF); }
				if(StumpJunk.isSetNotZero(thisLine[24])) { int trainTime = Integer.parseInt(thisLine[24]); gpsData.put("TrainingTime", trainTime); }
				if(StumpJunk.isSetNotZero(thisLine[25])) { int trainTimeTot = Integer.parseInt(thisLine[25]); gpsData.put("TrainingTimeTotalSec", trainTimeTot); trackedSeconds.add(trainTimeTot); }
				if(StumpJunk.isSetNotZero(thisLine[26])) { int trainTimeDown = Integer.parseInt(thisLine[26]); gpsData.put("TrainingTimeDownhillSec", trainTimeDown); }
				if(StumpJunk.isSetNotZero(thisLine[27])) { int trainTimeUp = Integer.parseInt(thisLine[27]); gpsData.put("TrainingTimeUphillSec", trainTimeUp); }
				if(StumpJunk.isSetNotZero(thisLine[28])) { int workKJ = Integer.parseInt(thisLine[28]); gpsData.put("WorkKJ", workKJ); }
				if(StumpJunk.isSetNotZero(thisLine[29])) { int powerZone = Integer.parseInt(thisLine[29]); gpsData.put("PowerZone", powerZone); }

				geoJSONtrace += "["+longitude+","+latitude+"],";				

				String thisJSONstring = gpsLog.toString().substring(1);
				thisJSONstring = thisJSONstring.substring(0, thisJSONstring.length()-1)+",";
				if(thisJSONstring.equals("\""+logNo+"\":{},")) {
					System.out.println("Empty JSON!");
				} else {
					fullGPSjson += thisJSONstring;
				}

			}

		}
		catch (IOException ix) { ix.printStackTrace(); }
		
		fullGPSjson = ("{"+fullGPSjson+"}").replace(",}", "}");
		geoJSONtrace = ("["+geoJSONtrace+"]").replace(",]","]");

		double avgCadence = 0.0;
		double avgHeart = 0.0;
		double avgSpeed = 0.0;
		double avgPower = 0.0;
		double maxPower = 0.0;
		double maxSpeed = 0.0;
		double trackedDistance = 0.0;

		int maxCadence = 0;
		int maxHeart = 0;
		int trackedTime = 0;

		String activityType = null;
		String activityDataField = null;
		String geoJSONField = null;
		String hrAvgField = null; String hrMaxField = null;
		String speedAvgField = null; String speedMaxField = null;

		String activityCode = args[0].substring(11);


		try { avgHeart = (StumpJunk.sumListInteger(heartRates) / Collections.max(logNos)); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
		try { avgSpeed = (StumpJunk.sumListDouble(intSpeeds) / Collections.max(logNos)); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
		try { maxHeart = Collections.max(heartRates); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
		try { maxSpeed = Collections.max(intSpeeds); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
		try { trackedDistance = Collections.max(miles); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
		try { trackedTime = Collections.max(trackedSeconds)/100; } catch (NoSuchElementException nse) { nse.printStackTrace(); }

		switch(activityCode) {

			case "C":
				activityType = "Cycling";
				activityDataField = "gpsLogCyc";
				geoJSONField = "CycGeoJSON";
				speedAvgField = "CycSpeedAvg";
				speedMaxField = "CycSpeedMax";
				hrAvgField = "CycHeartAvg";
				hrMaxField = "CycHeartMax";
				try { maxCadence = Collections.max(cadences); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
				try { avgCadence = (StumpJunk.sumListInteger(cadences) / Collections.max(logNos)); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
				try { maxPower = Collections.max(wattPowers); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
				try { avgPower = (StumpJunk.sumListDouble(wattPowers) / Collections.max(logNos)); } catch (NoSuchElementException nse) { nse.printStackTrace(); }
				break;

			case "D":
				activityType = "Cycling";
				activityDataField = "gpsLogCyc2";
				geoJSONField = "AltGeoJSON";
				break;

			case "R":
				activityType = "RunWalk";
				activityDataField = "gpsLogRun";
				geoJSONField = "RunGeoJSON";
				speedAvgField = "RunSpeedAvg";
				speedMaxField = "RunSpeedMax";
				hrAvgField = "RunHeartAvg";
				hrMaxField = "RunHeartMax";
				break;

			case "S":
				activityType = "RunWalk";
				activityDataField = "gpsLogRun2";
				geoJSONField = "AltGeoJSON";
				break;


		}	
	
		String gpsQuery = null;		

		if (legacyFlag.equals("yes")) {

			gpsQuery = "UPDATE Core.Fitness SET "+activityDataField+"='"+fullGPSjson+"' WHERE Date='"+thisDate+"';";

		} else {
		
			gpsQuery = "UPDATE Core.Fitness SET "+activityType+" = CASE WHEN "+activityType+" IS NULL THEN "+trackedDistance+" ELSE "+activityType+"+"+trackedDistance+" END, "+activityDataField+"='"+fullGPSjson+"', "+geoJSONField+"='"+geoJSONtrace+"'";
			if(activityType.equals("RunWalk")) { gpsQuery += ", TrackedTime="+trackedTime+", TrackedDist="+trackedDistance; }
			if(StumpJunk.isSetNotZero(speedAvgField)) { gpsQuery += ", "+speedAvgField+"="+avgSpeed; }
			if(StumpJunk.isSetNotZero(speedMaxField)) { gpsQuery += ", "+speedMaxField+"="+maxSpeed; }
			if(StumpJunk.isSetNotZero(hrAvgField)) { gpsQuery += ", "+hrAvgField+"="+avgHeart; }
			if(StumpJunk.isSetNotZero(hrMaxField)) { gpsQuery += ", "+hrMaxField+"="+maxHeart; }
			if(maxCadence > 0) { gpsQuery += ", CycCadMax="+maxCadence; }
			if(avgCadence > 0) { gpsQuery += ", CycCadAvg="+avgCadence; }
			if(maxPower > 0) { gpsQuery += ", CycPowerMax="+maxPower; }
			if(avgPower > 0) { gpsQuery += ", CycPowerAvg="+avgPower; }
			if(activityType.equals("Cycling")) { gpsQuery+= ", Bicycle='"+bicycle+"'"; }
			gpsQuery += " WHERE Date='"+thisDate+"';";

		}

		System.out.println(gpsQuery);
		try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();) { stmt.executeUpdate(gpsQuery); }
		catch (SQLException se) { se.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }

	}

}
