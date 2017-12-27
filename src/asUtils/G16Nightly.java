/*
by Anthony Stump
Created: 7 SEP 2017
Updated: 27 DEC 2017
*/

package asUtils;

import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import asUtils.Shares.MyDBConnector;
import java.sql.*;
import java.nio.file.*;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class G16Nightly {

	public static void main(String[] args) {

                JunkyBeans junkyBeans = new JunkyBeans();
		final DateTime dtYesterday = new DateTime().minusDays(1);
		final DateTimeFormatter dtFormat = DateTimeFormat.forPattern("yyMMdd");
		final String dateStamp = dtFormat.print(dtYesterday);
		final String g16Base = junkyBeans.getWebRoot().toString()+"/Get/G16";

		System.out.println(dateStamp);

		String getRadarSetSQL = "SELECT Sector FROM WxObs.GOES16SectorsRDO WHERE Active=1 ORDER BY Sector ASC;";

		try (
			Connection conn = MyDBConnector.getMyConnection();
			Statement stmt = conn.createStatement();
			ResultSet resultSet = stmt.executeQuery(getRadarSetSQL);
		) {		
			while (resultSet.next()) {

				String thisSector = resultSet.getString("Sector");
				Path tG16Arch = Paths.get(g16Base+"/"+thisSector+"/Archive");
				Path tMemTmpA = Paths.get(junkyBeans.getRamDrive().toString()+"/G16MP4/Archive/"+thisSector);
				Path tArchDay = Paths.get(g16Base+"/ArchiveDay/"+thisSector);
				Path tListing = Paths.get(tMemTmpA+"/Listing.txt");

				StumpJunk.deleteDir(tMemTmpA.toFile());
				Files.createDirectories(tMemTmpA);
				Files.createDirectories(tArchDay);
				if (Files.exists(tListing)) { Files.delete(tListing); }

				StumpJunk.runProcess("mv "+tG16Arch+"/*.mp4 "+tMemTmpA);
				StumpJunk.runProcess("bash "+junkyBeans.getHelpers().toString()+"/Sequence.sh "+tMemTmpA+"/ mp4");

				String fileListingString = "";	

				List<String> aOfFiles = StumpJunk.fileSorter(tMemTmpA, "*.mp4");

				for (String thisFileStr : aOfFiles) {
					fileListingString += "file '"+thisFileStr+"'\n";
				}

				StumpJunk.varToFile(fileListingString, tListing.toFile(), false);

				System.out.println("Creating MP4 file...");
				StumpJunk.runProcess("ffmpeg -threads 8 -safe 0 -f concat -i "+tListing.toString()+" -c copy "+tArchDay.toString()+"/"+dateStamp+"A.mp4");
				StumpJunk.runProcess("(ls "+tArchDay.toString()+"/*.mp4 -t | head -n 31; ls "+tArchDay.toString()+"/*.mp4)|sort|uniq -u|xargs rm");
				StumpJunk.runProcess("chown -R "+junkyBeans.getWebUser()+" "+tArchDay.toString());
				System.out.println(thisSector+" completed!");

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

}
