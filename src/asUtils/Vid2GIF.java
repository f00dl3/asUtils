/*
by Anthony Stump
Created: 3 SEP 2017
Updated: 21 DEC 2017
*/

package asUtils;

import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import java.io.*;
import java.util.Scanner;

public class Vid2GIF {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
                
		final String sdCardPath = junkyBeans.getSdCardPath().toString();
		final String tempFolder = junkyBeans.getRamDrive().toString()+"/xTemp";
		final String dropPath = junkyBeans.getDesktopPath().toString();
		final String v2gVersion = "9.1 (Java)";
		final String v2gUpdated = "2017-12-21";

		final File tempFolderObj = new File(tempFolder);
		final File xOutObj = new File(sdCardPath+"/ASWebUI/Images/X");

		System.out.println("Vid2GIF Self-installing\nVersion "+v2gVersion+" (GIFVer 3)\nUpdated: "+v2gUpdated);

		Scanner inputReader = new Scanner(System.in);
	
		System.out.printf("File name: ");
		String getFileName = inputReader.nextLine();

		System.out.printf("File type: ");
		String getFileType = inputReader.nextLine();

		System.out.printf("Cut (HH:MM:SS): ");
		String getCutTime = inputReader.nextLine();

		inputReader.close();

		System.out.println("\n -> Parameters input: [ "+getFileName+" ], [ "+getFileType+" ], [ "+getCutTime+" ]");

		tempFolderObj.mkdirs();
		xOutObj.mkdirs();
		
		StumpJunk.runProcess("ffmpeg -ss "+getCutTime+" -i \""+dropPath+"/"+getFileName+"."+getFileType+"\" -pix_fmt rgb24 -r 10 -s 96x96 -t 00:00:03.000 "+tempFolder+"/"+getFileName+".%03d.png");
		StumpJunk.runProcess("convert -verbose -delay 5 -loop 0 "+tempFolder+"/"+getFileName+".*.png "+tempFolder+"/"+getFileName+".gif");
		StumpJunk.moveFile(tempFolder+"/"+getFileName+".gif", sdCardPath+"/ASWebUI/Images/X/"+getFileName+".pnx");
		StumpJunk.deleteDir(tempFolderObj);
		StumpJunk.runProcess("bash "+sdCardPath+"/ASWebUI/Install.sh");

		System.out.println("Done!");

	}

}
