/*
by Anthony Stump
Created: 13 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils.Cams;

import asUtils.Shares.StumpJunk;
import java.io.*;
import java.nio.file.*;
import java.util.List;

import asUtils.Shares.JunkyBeans;

public class CamGrab {

	public static void main(String args[]) {
		
                JunkyBeans junkyBeans = new JunkyBeans();
            
		final File helpers = junkyBeans.getHelpers();
		final Path camPath = junkyBeans.getDesktopPath();
		final Path sourceFolder = Paths.get(junkyBeans.getWebRoot().toString()+"/Get/Cams/Archive");
		final Path unpackFolder = Paths.get(junkyBeans.getRamDrive().toString()+"/camPull");
		final Path cListing = Paths.get(unpackFolder.toString()+"/Listing.txt");
		final Path mp4OutFile = Paths.get(camPath+"/CamPull.mp4");

		try { Files.createDirectories(unpackFolder); } catch (IOException ix) { ix.printStackTrace(); }

		StumpJunk.runProcess("cp "+sourceFolder.toString()+"/* "+unpackFolder.toString());
		StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+unpackFolder.toString()+"/ mp4");
		List<String> camFiles = StumpJunk.fileSorter(unpackFolder, "*.mp4");
		
		try { Files.delete(cListing); } catch (IOException ix) { ix.printStackTrace(); }

		for (String thisLoop : camFiles) {
			String fileListStr = "file '"+thisLoop+"'\n"; 
			try { StumpJunk.varToFile(fileListStr, cListing.toFile(), true); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
		}

		StumpJunk.runProcess("ffmpeg -threads 8 -safe 0 -f concat -i "+cListing.toString()+" -c copy "+mp4OutFile.toString());

		StumpJunk.runProcess("chown "+junkyBeans.getUName()+" "+mp4OutFile.toString());

		StumpJunk.deleteDir(unpackFolder.toFile());
	}

}
