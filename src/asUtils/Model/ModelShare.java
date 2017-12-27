/*
by Anthony Stump
Created: 4 Oct 2017
Updated: 27 Dec 2017
*/

package asUtils.Model;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import asUtils.Shares.JunkyBeans;
import asUtils.Model.ModelBeans;
import asUtils.Shares.StumpJunk;
import asUtils.Shares.MyDBConnector;

public class ModelShare {	
	
        public static JunkyBeans junkyBeans = new JunkyBeans();
        public static ModelBeans modelBeans = new ModelBeans();
    
	final public static double windDirCalc(double tWUin, double tWVin) { return 57.29578*(Math.atan2(tWUin, tWVin))+180; }
	final public static double windSpdCalc(double tWUin, double tWVin) { return Math.sqrt(tWUin*tWUin+tWVin*tWVin)*1.944; }
	final public static double calcSLCL(double tTCin, double tRHin) { return (20+(tTCin/5))*(100-tRHin); }
	final public static double calcDwpt(double tTCin, double tRHin) { return tTCin-(100-tRHin)/5; }
        
	public static String pointInputAsString(String tStation) {
		final String pointsSQL = "SELECT SUBSTRING(Point, 2, CHAR_LENGTH(Point)-2) AS Coords FROM WxObs.Stations WHERE Station='"+tStation+"' ORDER BY Station DESC;";
		List<String> pointInputArray = new ArrayList<String>();
		try (
			Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();
			ResultSet resultSetPIA = stmt.executeQuery(pointsSQL);
		) { while (resultSetPIA.next()) { pointInputArray.add(resultSetPIA.getString("Coords")); } }
		catch (Exception e) { e.printStackTrace(); }
		String thisGeo = null;
		String pointInputString = "";
		for (String point : pointInputArray) {
			thisGeo = point.replace(",", " ");
			pointInputString += "-lon "+thisGeo+" ";
		}
		return pointInputString;
	}

	public static String filters(String whichOne) {
		
                final File helpers = junkyBeans.getHelpers();
		File filtFile = null;
		String filtData = null;
		Scanner filtScanner = null;

		switch(whichOne) { 
		
			case "g2f": 
				filtFile = new File(helpers.getPath()+"/g2Filters.txt");
				try { filtScanner = new Scanner(filtFile); while(filtScanner.hasNext()) { filtData = filtScanner.nextLine(); } }
				catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
				break;
		
			case "g2fd": 
				filtFile = new File(helpers.getPath()+"/g2FiltersD.txt");
				filtScanner = null; try { filtScanner = new Scanner(filtFile); while(filtScanner.hasNext()) { filtData = filtScanner.nextLine(); } }
				catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
				break;
		
			case "g2fr": 
				filtFile = new File(helpers.getPath()+"/g2FiltersR.txt");
				filtScanner = null; try { filtScanner = new Scanner(filtFile); while(filtScanner.hasNext()) { filtData = filtScanner.nextLine(); } }
				catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
				break;

		}

		return filtData;

	}

	public static String jsonMerge(String modelName) {
                final File xml2Path = modelBeans.getXml2Path();
		String thisJSON = null;
		try { thisJSON = StumpJunk.runProcessOutVar("cat "+xml2Path.getPath()+"/"+modelName+"Out*.json"); } catch (IOException ix) { ix.printStackTrace(); }
		thisJSON = thisJSON.replace("\n","").replace(",}", "}").replace("{,","").replace("{","").replace("}","");
		thisJSON = ("{"+thisJSON+"}").replace(",}","}");
		return thisJSON;
	}
		
}
