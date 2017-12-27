/* 
by Anthony Stump
Created: 10 Sep 2017
Updated; 27 Dec 2017
*/

package asUtils;

import asUtils.Cams.CamBeans;
import asUtils.Shares.GDrive;
import asUtils.Shares.MyDBConnector;
import asUtils.Shares.StumpJunk;
import asUtils.Shares.JunkyBeans;
import java.io.*;
import java.sql.*;
import java.nio.file.*;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter; 

public class CamNightly {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
                CamBeans camBeans = new CamBeans();
                
		final File ramDrive = junkyBeans.getRamDrive();
		final File helpers = junkyBeans.getHelpers();
		final DateTime dtYesterday = new DateTime().minusDays(1);
		final DateTimeFormatter dtFormat = DateTimeFormat.forPattern("yyMMdd");
		final String getYesterday = dtFormat.print(dtYesterday);
		final Path camPath = Paths.get(camBeans.getCamWebRoot().toString());
		final Path sourceFolder = Paths.get(camPath.toString()+"/Archive");
		final Path unpackFolder = Paths.get(ramDrive.getPath()+"/mp4tmp");
		final Path cListing = Paths.get(unpackFolder.toString()+"/Listing.txt");
		final Path mp4OutFile = Paths.get(camPath+"/MP4/"+getYesterday+"J.mp4");

                try { GDrive.deleteChildItemsFromFolder("CloudCams"); } catch (IOException ix) { ix.printStackTrace(); }
                
		try { Files.createDirectories(unpackFolder); } catch (IOException ix) { ix.printStackTrace(); }

		StumpJunk.runProcess("mv "+sourceFolder.toString()+"/* "+unpackFolder.toString());
		StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+unpackFolder.toString()+"/ mp4");
		List<String> camFiles = StumpJunk.fileSorter(unpackFolder, "*.mp4");
		
		try { Files.delete(cListing); } catch (IOException ix) { ix.printStackTrace(); }

		for (String thisLoop : camFiles) {
			String fileListStr = "file '"+thisLoop+"'\n"; 
			try { StumpJunk.varToFile(fileListStr, cListing.toFile(), true); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
		}

		StumpJunk.runProcess("timeout --kill-after=120 120 ffmpeg -threads 8 -safe 0 -f concat -i "+cListing.toString()+" -c copy "+mp4OutFile.toString()+"  2> "+camPath.toString()+"/MakeMP4_Last.log");

/*		String camImgQtyS = "0";
		try { camImgQtyS = StumpJunk.runProcessOutVar("timeout --kill-after=120 120 ffprobe -v error -count_frames -select_streams v:0 -show_entries stream=nb_read_frames -of default=nokey=1:noprint_wrappers=1 "+mp4OutFile.toString()); } catch (IOException ix) { ix.printStackTrace(); }
		
		int camImgQty = Integer.parseInt(); */
		int camImgQty = 0;

		long camMP4Size = 0;
		try { camMP4Size = (Files.size(mp4OutFile)/1024); } catch (IOException ix) { ix.printStackTrace(); }

		String camLogSQL = "INSERT INTO Core.Log_CamsMP4 (Date,ImgCount,MP4Size) VALUES (CURDATE()-1,'"+camImgQty+"',"+camMP4Size+");";

		try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();) { stmt.executeUpdate(camLogSQL); }
		catch (Exception e) { e.printStackTrace(); }

		StumpJunk.runProcess("(ls "+camPath.toString()+"/MP4/*.mp4 -t | head -n 14; ls "+camPath.toString()+"/MP4/*.mp4)|sort|uniq -u|xargs rm");
		StumpJunk.runProcess("chown -R "+junkyBeans.getWebUser()+" "+camPath.toString()+"/MP4");

		StumpJunk.deleteDir(unpackFolder.toFile());
	}

}

