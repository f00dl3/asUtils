/*
by Anthony Stump
Created: 14 Aug 2017
Updated: 26 Dec 2017
*/

package asUtils;

import asUtils.Feed.G16Fetch;
import asUtils.Feed.GetSPC;
import asUtils.Feed.KCScout;
import asUtils.Feed.NWSWarnings;
import asUtils.Feed.Reddit;
import asUtils.Feed.cWazey;
import asUtils.Shares.StumpJunk;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import asUtils.Shares.MyDBConnector;
import asUtils.Shares.JunkyBeans;
import asUtils.Shares.Mailer;

public class Feeds {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
                
		final File ramDrive = junkyBeans.getRamDrive();
		final String mysqlShare = junkyBeans.getMySqlShare().toString();
		final String ramTemp = ramDrive.getPath()+"/rssXMLFeedsJ";
		final File ramTempF = new File(ramTemp);
		final String spcFeedBase = "http://www.spc.noaa.gov/products/";

		DateFormat dateFormat = new SimpleDateFormat("yyyy");
		Date date = new Date();
		String tYear = dateFormat.format(date);
		String freq = args[0];

		ramTempF.mkdirs();

		if (freq.equals("TwoMinute")) {

			final String eqFeedURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.csv";
			
			final File eqcsvFile = new File(mysqlShare+"/ANSSQuakes.csv");
			final File spcMDFile = new File(mysqlShare+"/spcmdrss.xml");
			final File spcWWFile = new File(mysqlShare+"/spcwwrss.xml");
		
			String getSPCArgs[] = { "Empty" }; GetSPC.main(getSPCArgs);
			/* String G16Args[] = { "Empty" }; G16Fetch.main(G16Args); */
			String NWSWarnArgs[] = { "Empty" }; NWSWarnings.main(NWSWarnArgs);
			String cWazeyArgs[] = { "Empty" }; cWazey.main(cWazeyArgs);
			
			StumpJunk.jsoupOutFile(eqFeedURL, eqcsvFile);
			StumpJunk.jsoupOutFile(spcFeedBase+"spcmdrss.xml", spcMDFile);
			StumpJunk.jsoupOutFile(spcFeedBase+"spcwwrss.xml", spcWWFile);			
			
			StumpJunk.runProcess("mysqlimport --fields-terminated-by=, --fields-enclosed-by=\\\" --verbose --replace WxObs "+mysqlShare+"/ANSSQuakes.csv");

                        String kcScoutSQL = KCScout.getScoutSQL();
                        String mailSQL = Mailer.mailForSQL();
			String cleanQuakeSQL = "DELETE FROM WxObs.ANSSQuakes WHERE time='time';";
			String loadSPCmdSQL = "LOAD DATA INFILE '"+mysqlShare+"/spcmdrss.xml' IGNORE INTO TABLE WxObs.SPCMesoscale CHARACTER SET 'utf8' LINES STARTING BY '<item>' TERMINATED BY '</item>' (@tmp) SET title = CONCAT(ExtractValue(@tmp, '//title'),' "+tYear+"'), description = ExtractValue(@tmp, '//description'), pubDate = ExtractValue(@tmp, '//pubDate');";
			String loadSPCwwSQL = "LOAD DATA INFILE '"+mysqlShare+"/spcwwrss.xml' IGNORE INTO TABLE WxObs.SPCWatches CHARACTER SET 'utf8' LINES STARTING BY '<item>' TERMINATED BY '</item>' (@tmp) SET title = CONCAT(ExtractValue(@tmp, '//title'),' "+tYear+"'), description = ExtractValue(@tmp, '//description'), pubDate = ExtractValue(@tmp, '//pubDate');";
			String cleanSPCmdSQL = "DELETE FROM WxObs.SPCMesoscale WHERE title LIKE 'SPC - No MDs are in effect as of %';";
			String cleanSPCwwSQL = "DELETE FROM WxObs.SPCWatches WHERE title LIKE 'SPC - No watches are valid as %';";
			
			try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();) {
				stmt.executeUpdate(kcScoutSQL);
				stmt.executeUpdate(cleanQuakeSQL);
				stmt.executeUpdate(loadSPCmdSQL);
				stmt.executeUpdate(loadSPCwwSQL);
				stmt.executeUpdate(cleanSPCmdSQL);
				stmt.executeUpdate(cleanSPCwwSQL);
                                if(StumpJunk.isSet(mailSQL)) { stmt.executeUpdate(mailSQL); }
			}
			catch (Exception e) { e.printStackTrace(); }

			eqcsvFile.delete();
			spcMDFile.delete();
			spcWWFile.delete();
			
		}
		
		if (freq.equals("Hour")) {
			
			final String getFeedsSQL = "SELECT Name, LinkURL FROM Feeds.RSSSources WHERE Frequency='H' AND Active=1 AND Reddit=0;";
			
			try (
				Connection conn = MyDBConnector.getMyConnection();
				Statement stmt = conn.createStatement();
				ResultSet resultSet = stmt.executeQuery(getFeedsSQL);
			) {		
				while (resultSet.next()) {
					
					String thisLinkName = resultSet.getString("Name");
					String thisLinkURL = resultSet.getString("LinkURL");
					String thisFeedFileStr = "NewsFeed"+thisLinkName+".xml";
					File thisFeedFile = new File(ramTemp+"/"+thisFeedFileStr);
					File thisFeedDestFile = new File(mysqlShare+"/"+thisFeedFileStr);

					System.out.println("Fetching: "+thisLinkName+" ("+thisLinkURL+")");
					StumpJunk.jsoupOutFile(thisLinkURL, thisFeedFile);
					StumpJunk.sedFileReplace(ramTemp+"/"+thisFeedFileStr, "<!\\[CDATA\\[", "");
					StumpJunk.sedFileReplace(ramTemp+"/"+thisFeedFileStr, "\\]\\]", "");
					StumpJunk.moveFile(ramTemp+"/"+thisFeedFileStr, mysqlShare+"/"+thisFeedFileStr);

					String thisFeedUpSQL = "LOAD XML LOCAL INFILE '"+mysqlShare+"/"+thisFeedFileStr+"' IGNORE INTO TABLE Feeds.RSSFeeds CHARACTER SET 'utf8' ROWS IDENTIFIED BY '<item>';";
					try ( Statement subStmt = conn.createStatement();) { subStmt.executeUpdate(thisFeedUpSQL); }

					thisFeedFile.delete();
					thisFeedDestFile.delete();
				}
			}
			catch (Exception e) { e.printStackTrace(); }
			
                        String nhcBase = "http://www.nhc.noaa.gov/";
			File spcOutFileSrc = new File(mysqlShare+"/spcacrss.xml");
			File nhcIAtFileSrc = new File(ramTemp+"/index-at.xml");
			File nhcIEpFileSrc = new File(ramTemp+"/index-ep.xml");
			File nhcDAtFileSrc = new File(ramTemp+"/TWDAT.xml");
			File nhcDEpFileSrc = new File(ramTemp+"/TWDEP.xml");
			StumpJunk.jsoupOutFile(spcFeedBase+"spcacrss.xml", spcOutFileSrc);		
			StumpJunk.jsoupOutFile(nhcBase+"index-at.xml", nhcIAtFileSrc);
			StumpJunk.jsoupOutFile(nhcBase+"index-ep.xml", nhcIEpFileSrc);
			StumpJunk.jsoupOutFile(nhcBase+"xml/TWDAT.xml", nhcDAtFileSrc);
			StumpJunk.jsoupOutFile(nhcBase+"xml/TWDEP.xml", nhcDEpFileSrc);
			StumpJunk.runProcess("iconv -f ISO-8859-1 -t UTF-8 "+ramTemp+"/index-at.xml > "+mysqlShare+"/index-at.xml");
			StumpJunk.runProcess("iconv -f ISO-8859-1 -t UTF-8 "+ramTemp+"/index-ep.xml > "+mysqlShare+"/index-ep.xml");
			StumpJunk.runProcess("iconv -f ISO-8859-1 -t UTF-8 "+ramTemp+"/TWDAT.xml > "+mysqlShare+"/TWDAT.xml");
			StumpJunk.runProcess("iconv -f ISO-8859-1 -t UTF-8 "+ramTemp+"/TWDEP.xml > "+mysqlShare+"/TWDEP.xml");
			String spcOutSQL = "LOAD DATA INFILE '"+mysqlShare+"/spcacrss.xml' IGNORE INTO TABLE WxObs.SPCOutlooks CHARACTER SET 'utf8' LINES STARTING BY '<item>' TERMINATED BY '</item>' (@tmp) SET title = ExtractValue(@tmp, '//title'), description = ExtractValue(@tmp, '//description'), pubDate = ExtractValue(@tmp, '//pubDate');";
			String nhcIAtSQL = "LOAD DATA INFILE '"+mysqlShare+"/index-at.xml' IGNORE INTO TABLE WxObs.NHCFeeds CHARACTER SET 'utf8' LINES STARTING BY '<item>' TERMINATED BY '</item>' (@tmp) SET guid = ExtractValue(@tmp, '//guid'), title = ExtractValue(@tmp, '//title'), link = ExtractValue(@tmp, '//link'), description = ExtractValue(@tmp, '//description'), pubDate = ExtractValue(@tmp, '//pubDate');";
			String nhcIEpSQL = "LOAD DATA INFILE '"+mysqlShare+"/index-ep.xml' IGNORE INTO TABLE WxObs.NHCFeeds CHARACTER SET 'utf8' LINES STARTING BY '<item>' TERMINATED BY '</item>' (@tmp) SET guid = ExtractValue(@tmp, '//guid'), title = ExtractValue(@tmp, '//title'), link = ExtractValue(@tmp, '//link'), description = ExtractValue(@tmp, '//description'), pubDate = ExtractValue(@tmp, '//pubDate');";
			String nhcDAtSQL = "LOAD DATA INFILE '"+mysqlShare+"/TWDAT.xml' IGNORE INTO TABLE WxObs.NHCFeeds CHARACTER SET 'utf8' LINES STARTING BY '<item>' TERMINATED BY '</item>' (@tmp) SET guid = ExtractValue(@tmp, '//guid'), title = ExtractValue(@tmp, '//title'), link = ExtractValue(@tmp, '//link'), description = ExtractValue(@tmp, '//description'), pubDate = ExtractValue(@tmp, '//pubDate');";
			String nhcDEpSQL = "LOAD DATA INFILE '"+mysqlShare+"/TWDEP.xml' IGNORE INTO TABLE WxObs.NHCFeeds CHARACTER SET 'utf8' LINES STARTING BY '<item>' TERMINATED BY '</item>' (@tmp) SET guid = ExtractValue(@tmp, '//guid'), title = ExtractValue(@tmp, '//title'), link = ExtractValue(@tmp, '//link'), description = ExtractValue(@tmp, '//description'), pubDate = ExtractValue(@tmp, '//pubDate');";
			try ( Connection conn2 = MyDBConnector.getMyConnection(); Statement stmt2 = conn2.createStatement();) {
				stmt2.executeUpdate(spcOutSQL);
				stmt2.executeUpdate(nhcIAtSQL);
				stmt2.executeUpdate(nhcIEpSQL);
				stmt2.executeUpdate(nhcDAtSQL);
				stmt2.executeUpdate(nhcDEpSQL);
			} catch (Exception e) { e.printStackTrace(); }
			new File(mysqlShare+"/spcacrss.xml").delete();
			new File(mysqlShare+"/index-at.xml").delete();
			new File(mysqlShare+"/index-ep.xml").delete();
			new File(mysqlShare+"/TWDAP.xml").delete();
			new File(mysqlShare+"/TWDEP.xml").delete();
			
			String[] redditArgs = { "Empty" }; Reddit.main(redditArgs);			

		}

	}

}
