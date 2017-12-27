/* 
by Anthony Stump
Created: 10 Sep 2017
Updated: 26 Dec 2017
*/

package asUtils.Feed;

import asUtils.Shares.StumpJunk;
import asUtils.Shares.MyDBConnector;
import asUtils.Shares.JunkyBeans;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Scanner;

import org.json.*;


public class cWazey {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
                
		final File ramDrive = junkyBeans.getRamDrive();
		final String wazeFeedURL = "https://www.waze.com/rtserver/web/TGeoRSS?ma=600&mj=100&mu=100&left=-95.57315826416016&right=-93.53519439697266&bottom=38.73627888925287&top=39.15666429656342&_=1503589506358";
		final Path wazeJSON = Paths.get(ramDrive.toString()+"/Waze.json");

		StumpJunk.jsoupOutBinary(wazeFeedURL, wazeJSON.toFile(), 5.0);

		StumpJunk.sedFileReplace(wazeJSON.toString(), "\\n", "");
		StumpJunk.sedFileReplace(wazeJSON.toString(), " null\\,", "\\\"null\\\",");
			
		String wazeLoaded = "";
		Scanner wazeScanner = null;
		try { wazeScanner = new Scanner(wazeJSON.toFile()); while(wazeScanner.hasNext()) { wazeLoaded = wazeLoaded+wazeScanner.nextLine(); } }
		catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
		
		String wazeSQL = "INSERT IGNORE INTO Feeds.WazeFeed ("
			+ "id, uuid, country, nThumbsUp,"
			+ "reportRating, reliability, type, speed,"
			+ "reportMood, subType, street, additionalInfo,"
			+ "nComments, reportBy, reportDescription, wazeData,"
			+ "nearBy, pubMillis, longitude, latitude"
			+ ") VALUES";
		
		JSONObject wazeObj = new JSONObject(wazeLoaded);
		JSONArray alerts = wazeObj.getJSONArray("alerts");

		for (int i = 0; i < alerts.length(); i++) {

			JSONObject tJOAlert = alerts.getJSONObject(i);

			String tId = null;
			String tUuid = null;
			String tCountry = null;
			int tNThumbsUp = 0;
			int tReportRating = 0;
			int tReliability = 0;
			String tType = null;
			int tSpeed = 0;
			int tReportMood = 0;
			String tSubType = null;
			String tStreet = null;
			String tAdditionalInfo = null;
			int tNComments = 0;
			String tReportBy = null;
			String tReportDescription = null;
			String tWazeData = null;
			String tNearBy = null;
			long tPubMillis = 0;
			double tLatitude = 0.000000;
			double tLongitude = 0.000000;

			Object tObjLoc = tJOAlert.get("location");
			if(tObjLoc instanceof JSONObject) {
				JSONObject tJOLocation = tJOAlert.getJSONObject("location");
				tLongitude = tJOLocation.getDouble("x");
				tLatitude = tJOLocation.getDouble("y");
			}

			tId = tJOAlert.getString("id");
			tUuid = tJOAlert.getString("uuid");
			tCountry = StumpJunk.jsonSanitize(tJOAlert.getString("country"));
			tNThumbsUp = tJOAlert.getInt("nThumbsUp");
			tReportRating = tJOAlert.getInt("reportRating");
			tReliability = tJOAlert.getInt("reliability");
			tType = StumpJunk.jsonSanitize(tJOAlert.getString("type"));
			tSpeed = tJOAlert.getInt("speed");
			tReportMood = tJOAlert.getInt("reportMood");
			tSubType = StumpJunk.jsonSanitize(tJOAlert.getString("subtype"));
			if(tJOAlert.has("street")) { tStreet = StumpJunk.jsonSanitize(tJOAlert.getString("street")); }
			if(tJOAlert.has("additionalInfo")) { tAdditionalInfo = StumpJunk.jsonSanitize(tJOAlert.getString("additionalInfo")); }
			tNComments = tJOAlert.getInt("nComments");
			if(tJOAlert.has("reportBy")) { tReportBy = StumpJunk.jsonSanitize(tJOAlert.getString("reportBy")); }
			if(tJOAlert.has("reportDescription")) { tReportDescription = StumpJunk.jsonSanitize(tJOAlert.getString("reportDescription")); }
			tWazeData = tJOAlert.getString("wazeData");
			if(tJOAlert.has("nearBy")) { tNearBy = StumpJunk.jsonSanitize(tJOAlert.getString("nearBy")); }
			tPubMillis = tJOAlert.getLong("pubMillis");

			wazeSQL = wazeSQL+"("
				+ "'"+tId+"','"+tUuid+"','"+tCountry+"',"+tNThumbsUp+","
				+ tReportRating+","+tReliability+",'"+tType+"',"+tSpeed+","
				+ tReportMood+",'"+tSubType+"','"+tStreet+"','"+tAdditionalInfo+"',"
				+ tNComments+",'"+tReportBy+"','"+tReportDescription+"','"+tWazeData+"',"
				+ "'"+tNearBy+"',"+tPubMillis+","+tLatitude+","+tLongitude
				+ "),"; 

		}

		wazeSQL = (wazeSQL+";").replace(",;", ";");

		try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();) {
				stmt.executeUpdate(wazeSQL);
		} catch (Exception e) { e.printStackTrace(); }

		try { Files.delete(wazeJSON); } catch (IOException ix) { ix.printStackTrace(); }

	}

}
