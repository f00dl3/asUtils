/*
by Anthony Stump
Created 7 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils.Feed;

import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

import asUtils.Shares.MyDBConnector;

public class Reddit {

	public static void main(String[] args) {
            
                JunkyBeans junkyBeans = new JunkyBeans();

		final String mysqlShare = junkyBeans.getMySqlShare().toString();
		final String ramTemp = junkyBeans.getRamDrive().toString()+"/rssXMLFeedsJ";
		DateFormat sqlCompareFormat = new SimpleDateFormat("yyyy-MM-dd HH");
		DateFormat hackFormat = new SimpleDateFormat("yyyyMMddHH");
		Date date = new Date();
		String sqlCompare = sqlCompareFormat.format(date);
		String hackTime = hackFormat.format(date);

		final String getRedditSQL = "SELECT Name, LinkURL FROM Feeds.RSSSources WHERE Frequency='H' AND Active=1 AND Reddit=1;";
		final String getImageLinksSQL = "SELECT id, GetTime, Content FROM Feeds.RedditFeeds WHERE GetTime LIKE '"+sqlCompare+"%';";
			
		try (
			Connection connR = MyDBConnector.getMyConnection();
			Statement stmtR = connR.createStatement();
			ResultSet resultSetR = stmtR.executeQuery(getRedditSQL);
		) {		
			while (resultSetR.next()) {
					
				String thisLinkName = resultSetR.getString("Name");
				String thisLinkURL = resultSetR.getString("LinkURL");
				String thisFeedFileStr = "Reddit"+thisLinkName+".rss";
				File thisFeedFile = new File(ramTemp+"/"+thisFeedFileStr);
				File thisFeedDestFile = new File(mysqlShare+"/"+thisFeedFileStr);
					
				System.out.println("Fetching: "+thisLinkName+" ("+thisLinkURL+")");
				StumpJunk.jsoupOutBinary(thisLinkURL, thisFeedFile, 15.0);
				StumpJunk.sedFileReplace(ramTemp+"/"+thisFeedFileStr, "<!\\[CDATA\\[", "");
				StumpJunk.sedFileReplace(ramTemp+"/"+thisFeedFileStr, "\\]\\]", "");
				StumpJunk.runProcess("iconv -f ISO-8859-1 -t UTF-8 '"+ramTemp+"/"+thisFeedFileStr+"' > '"+mysqlShare+"/"+thisFeedFileStr+"'");
					
				String thisRedditUpSQL = "LOAD XML LOCAL INFILE '"+mysqlShare+"/"+thisFeedFileStr+"' IGNORE INTO TABLE Feeds.RedditFeeds CHARACTER SET 'utf8' ROWS IDENTIFIED BY '<entry>';";
				try ( Statement subStmtR = connR.createStatement();) { subStmtR.executeUpdate(thisRedditUpSQL); }
					
				thisFeedFile.delete();
				thisFeedDestFile.delete();

			}
		} catch (Exception e) { e.printStackTrace(); }
			
		try (
			Connection connR = MyDBConnector.getMyConnection();
			Statement stmtR = connR.createStatement();
			ResultSet resultSetR = stmtR.executeQuery(getImageLinksSQL);
		) {		
			while (resultSetR.next()) {
				String thumbURL = null;
				String fullURL = null;
				String keyLookup = resultSetR.getString("id");
				String line = resultSetR.getString("content");
				if(line.contains("<img src=")) { Pattern p = Pattern.compile("<img src=\\\"(.*)\\\" alt"); Matcher m = p.matcher(line); if (m.find()) { thumbURL = m.group(1); } }
				if(line.contains("[link]")) { Pattern p = Pattern.compile("<span><a href=\\\"(.*)\\\">\\[link\\]"); Matcher m = p.matcher(line); if (m.find()) { fullURL = m.group(1); } }
				if (thumbURL != null && thumbURL.toLowerCase().contains("jpg")) {
					File thisTmpFolder = new File(ramTemp+"/Reddit");
					File thisThumbFile = new File(ramTemp+"/Reddit/"+keyLookup+"#"+thumbURL.substring(thumbURL.lastIndexOf("/")+1));
					thisTmpFolder.mkdirs();
					File thisFullFile = new File(ramTemp+"/Reddit/"+keyLookup+"#"+fullURL.substring(fullURL.lastIndexOf("/")+1));
					if (fullURL != null && (fullURL.toLowerCase().contains("jpg") || fullURL.toLowerCase().contains("png"))) {
						StumpJunk.jsoupOutBinary(fullURL, thisFullFile, 15.0);
					} else { 
						StumpJunk.jsoupOutBinary(fullURL, thisFullFile, 15.0);
						StumpJunk.jsoupOutBinary(thumbURL, thisThumbFile, 15.0);
						String nextDown = null;
						Scanner subScanner = null; try {		
							subScanner = new Scanner(thisFullFile);
							while(subScanner.hasNext()) {				
								String subLine = subScanner.nextLine();
								if(subLine.contains("secure_url")) {
									Pattern p = Pattern.compile("content=\\\"(.*)\\\""); Matcher m = p.matcher(subLine); if (m.find()) { nextDown = m.group(1); }
									System.out.println(nextDown);
									File subFile = new File(ramTemp+"/Reddit/"+keyLookup+"#"+nextDown.substring(nextDown.lastIndexOf("/")+1));
									StumpJunk.jsoupOutBinary(nextDown, subFile, 15.0);
									thisThumbFile.delete();
								}
								if(subLine.contains("mp4Url")) {
									nextDown = "https://giant.gfycat.com/"+fullURL.substring(fullURL.lastIndexOf("/")+1)+".mp4";
									System.out.println(nextDown);
									File subFile = new File(ramTemp+"/Reddit/"+keyLookup+"#"+nextDown.substring(nextDown.lastIndexOf("/")+1));
									StumpJunk.jsoupOutBinary(nextDown, subFile, 15.0);
									thisThumbFile.delete();
								}
							}
						} catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
						thisFullFile.delete();
					}
				} else {
					File thisFullFile = new File(ramTemp+"/Reddit/"+keyLookup+"#"+fullURL.substring(fullURL.lastIndexOf("/")+1));
					StumpJunk.jsoupOutBinary(fullURL, thisFullFile, 15.0);
					String nextDown = null;
					Scanner subScanner = null; try {		
						subScanner = new Scanner(thisFullFile);
						while(subScanner.hasNext()) {
							String subLine = subScanner.nextLine();
							if(subLine.contains("m4a: ")) {
								Pattern p = Pattern.compile("m4a: \\\"(.*)\\\""); Matcher m = p.matcher(subLine); if (m.find()) { nextDown = m.group(1); }
								System.out.println(nextDown);
								File subFile = new File(ramTemp+"/Reddit/"+keyLookup+"#"+nextDown.substring(nextDown.lastIndexOf("/")+1));
								StumpJunk.jsoupOutBinary(nextDown, subFile, 15.0);
								thisFullFile.delete();
							}
						}
					} catch (FileNotFoundException fnf) { fnf.printStackTrace(); }	
				}
			}
		
		} catch (Exception e) { e.printStackTrace(); }

		File redditFolder = new File(ramTemp+"/Reddit");
		if (redditFolder.isDirectory()) {
			File webOutFolder = new File(junkyBeans.getWebRoot().toString()+"/rOut");
                        File outZip = new File(webOutFolder+"/Reddit#"+hackTime+".zip");
			webOutFolder.mkdirs();
			StumpJunk.zipThisFolder(redditFolder, outZip);
			StumpJunk.deleteDir(redditFolder);
		}

	}

}
