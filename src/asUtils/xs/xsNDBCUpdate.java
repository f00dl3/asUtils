/*
by Anthony Stump
Created: 24 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils.xs;

import asUtils.Shares.StumpJunk;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import asUtils.Shares.MyDBConnector;

public class xsNDBCUpdate {

	public static String getAttributeValue(String attr, Element tElement) throws NullPointerException {
		String nVal = tElement.getAttributes().getNamedItem(attr).getNodeValue();
		if(nVal == null) { return null; }
		return nVal;
	}

	public static void main(String args[]) {

		final String xsTmp = args[0];

		final String activeStationURL = "http://www.ndbc.noaa.gov/activestations.xml";
		final File activeFile = new File(xsTmp+"/activestations.xml");

		StumpJunk.jsoupOutBinary(activeStationURL, activeFile, 15.0);

		List<String> wxStations = new ArrayList<>();
		final String getStationListSQL = "SELECT Station FROM WxObs.Stations;";
		String addStationTestSQL = "INSERT IGNORE INTO WxObs.Stations (Station, Point, City, State, Active, Priority, Region, DataSource, Frequency) VALUES";

		try (
			Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();
			ResultSet resultSetStations = stmt.executeQuery(getStationListSQL);
		) { while (resultSetStations.next()) { wxStations.add(resultSetStations.getString("Station")); } }
		catch (Exception e) { e.printStackTrace(); }

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDoc = builder.parse(activeFile);
			NodeList nodeList = xmlDoc.getElementsByTagName("station");
	
			if (nodeList != null) {
				for(int i=0; i < nodeList.getLength(); i++) {
					Node thisData = nodeList.item(i);
					if(thisData.getNodeType() == Node.ELEMENT_NODE) {

						int tPri = 6;
						Element tElement = (Element) thisData;	
						String thisStation = null;
						String latitude = null;
						String longitude = null;
						String name = null;

						try { thisStation = getAttributeValue("id", tElement); } catch (NullPointerException npx) { npx.printStackTrace(); }
						try { latitude = getAttributeValue("lat", tElement); } catch (NullPointerException npx) { npx.printStackTrace(); }
						try { longitude = getAttributeValue("lon", tElement); } catch (NullPointerException npx) { npx.printStackTrace(); }
						try { name = getAttributeValue("name", tElement).replace("\'","\\\'").replace(" +", " "); } catch (NullPointerException npx) { npx.printStackTrace(); }

						thisStation = thisStation.toUpperCase();
						if (thisStation.substring(0,1).matches("\\d")) { thisStation = "B"+thisStation; tPri = 6; } else { tPri = 7; }

						if(!wxStations.contains(thisStation) && latitude != null && longitude != null) {
							addStationTestSQL = addStationTestSQL+" ('"+thisStation+"','["+longitude+","+latitude+"]','"+name+"',Null,1,"+tPri+",'BH2','NWS',60),";
						}
					}
				}
			}

		}

		catch (SAXException sex) { sex.printStackTrace(); }
		catch (ParserConfigurationException pcx) { pcx.printStackTrace(); }
		catch (IOException iox) { iox.printStackTrace(); }

		addStationTestSQL = (addStationTestSQL+";").replace(",;",";");

		System.out.println(addStationTestSQL);

		try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();) { stmt.executeUpdate(addStationTestSQL); }
		catch (SQLException se) { se.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }
		
	}

}
