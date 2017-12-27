/*
by Anthony Stump
Created: 11 Sep 2017
Updated: 25 Dec 2017
Concept - not funtional
*/

package asUtils;

import asUtils.Shares.Mailer;
import asUtils.Shares.MyDBConnector;
import java.io.*;
import java.sql.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter; 


public class AlertMe {

	public static void main(String[] args) {

		final DateTime dtEnd = new DateTime();
		final DateTime dtStart = new DateTime().minusDays(365);
		final DateTimeFormatter sqlTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		final String beginTime = sqlTime.print(dtStart);
		final String endTime = sqlTime.print(dtEnd);

		List<String> subscribedUsers = new ArrayList<>();

	 	final String getSubsSQL = "SELECT Destination FROM Core.AlertEndpoints;";
		try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement(); ResultSet resultSubs = stmt.executeQuery(getSubsSQL)) {
	
			while (resultSubs.next()) {

				String alertScope = resultSubs.getString("MessageLevel");
				String sameCode = resultSubs.getString("Areas");
				String destinationAddress = resultSubs.getString("Destination");

				String getActWarnSQL = "SELECT GetTime, title, summary, testCond FROM ("
					+ "	SELECT"
					+ "		lw.capVersion, lw.id, lw.published, lw.updated, lw.title, lw.summary,"
					+ "		lw.cappolygon, lw.cap12polygon, lw.capgeocode, lw.capparameter, lw.capevent,"
					+ "		lwc.ColorRGB, lwc.ColorHEX, lwc.ExtendDisplayTime, lwc.ShowIt, lw.GetTime,"
					+ "		CASE"
					+ "			WHEN lw.capgeocode IS NOT NULL THEN REPLACE(REPLACE(REPLACE(substring_index(substring_index(lw.capgeocode, 'FIPS6', -1), 'UGC', 1),'\r\n\t ',' '),'  ',''),' ',',')"
					+ "			ELSE ''"
					+ "		END as FIPSCodes,"
					+ "		lw.cap12same, lw.cap12ugc, lw.cap12vtec, CONVERT_TZ(STR_TO_DATE(SUBSTRING(lw.capexpires,1,19),'%Y-%m-%dT%H:%i:%s'),SUBSTRING(lw.capexpires,20,5),'-05:00') as testCond"
					+ "	FROM WxObs.LiveWarnings lw"
					+ "	LEFT JOIN WxObs.LiveWarningColors lwc ON lw.capevent = lwc.WarnType"
					+ "	WHERE "
					+ "		CASE WHEN lwc.ExtendDisplayTime = 0 THEN"
					+ "			(CONVERT_TZ(STR_TO_DATE(SUBSTRING(lw.published,1,19),'%Y-%m-%dT%H:%i:%s'),SUBSTRING(lw.published,20,5),'-05:00') BETWEEN '"+beginTime+"' AND '"+endTime+"'"
					+ "			OR CONVERT_TZ(STR_TO_DATE(SUBSTRING(lw.updated,1,19),'%Y-%m-%dT%H:%i:%s'),SUBSTRING(lw.updated,20,5),'-05:00') BETWEEN '"+beginTime+"' AND '"+endTime+"')"
					+ "			AND CONVERT_TZ(STR_TO_DATE(SUBSTRING(lw.capexpires,1,19),'%Y-%m-%dT%H:%i:%s'),SUBSTRING(lw.capexpires,20,5),'-05:00') > '"+beginTime+"'"
					+ "		ELSE"
					+ "			CONVERT_TZ(STR_TO_DATE(SUBSTRING(lw.capexpires,1,19),'%Y-%m-%dT%H:%i:%s'),SUBSTRING(lw.capexpires,20,5),'-05:00') >= '"+beginTime+"'"
					+ "		END"
					+ "		AND lw.title IS NOT NULL"
					+ "		AND ( lwc.AlertScope LIKE '%"+alertScope+"%' OR lwc.AlertScope IS NULL)"
					+ "		AND ( lw.capgeocode REGEXP '"+sameCode+"' OR lw.cap12same REGEXP '"+sameCode+"' )"
					+ "	ORDER BY lw.published DESC"
					+ ") as lwm"
					+ " GROUP BY "
					+ "	CASE WHEN cap12polygon IS NOT NULL THEN CONCAT(capevent,cap12polygon) END,"
					+ "	CASE WHEN cappolygon IS NOT NULL THEN CONCAT(capevent,cappolygon) END,"
					+ "	CASE WHEN cappolygon IS NULL AND cap12polygon IS NULL THEN CONCAT(capevent,FIPSCodes,cap12same) END"
					+ " ORDER BY GetTime DESC"
					+ " LIMIT 1;";

				System.out.println(getActWarnSQL);

				try ( Connection connS = MyDBConnector.getMyConnection(); Statement stmtS = conn.createStatement(); ResultSet resultAlerts = stmtS.executeQuery(getActWarnSQL); ) {

					while (resultAlerts.next()) {
						String tExpireTime = resultAlerts.getString("testCond");
						String tTitle = resultAlerts.getString("title");
						String tSummary = resultAlerts.getString("summary");
						System.out.println("Alert!\nTitle: "+tTitle+"\nExpires: "+tExpireTime+"\nSummary: "+tSummary);
						Mailer.sendMail(destinationAddress, "asUtils.AlertMe: "+tTitle, tSummary, null);
					}

				} catch (Exception e) { e.printStackTrace(); }

			}

		} catch (Exception e) { e.printStackTrace(); }

	}

}

