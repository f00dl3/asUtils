/*
by Anthony Stump
Created: 31 Aug 2017
Updated: 26 Dec 2017
*/

package asUtils.Cams;

import asUtils.Shares.StumpJunk;
import asUtils.Secure.JunkyPrivate;
import asUtils.Shares.JunkyBeans;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class CamWorkerHF {

	public static void main(String args[]) {

                CamBeans camBeans = new CamBeans();
                JunkyBeans junkyBeans = new JunkyBeans();
                JunkyPrivate junkyPrivate = new JunkyPrivate();
                
		final int testVal = 1;

		final String camPath = args[0];
                final String capRes = camBeans.getCapRes();
		final double capWait = camBeans.getCapWait();
		final String instance = args[1];

		DateFormat dateOverlayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		DateFormat dateFileFormat = new SimpleDateFormat("yyMMdd-HHmmss-SSS");

		final String ipCam1 = junkyPrivate.getIpForCam1() + ":88";
		final String ipCam1Alt = junkyPrivate.getIpForCam1Alt() + ":88";
		final String ipCam2 = junkyPrivate.getIpForCam2() + ":88";
		final String piCam1 = junkyPrivate.getIpForRaspPi1();
		final String ipCamUser = junkyPrivate.getIpCamUser();
		final String ipCamPass = junkyPrivate.getIpCamPass();

		final File lastUPSStatus = new File(camPath+"/LUStatus.txt");
		final File lastCaseTemp = camBeans.getTfOutCase();
		final File lastCPUTemp = camBeans.getTfOutCPU();
		final File lastGarageTemp = camBeans.getTfOutGarage();

		File xWebC1File = new File(camPath+"/Xwebc1-temp.jpeg");
                File yWebC2File = new File(camPath+"/"+instance+"webc2-temp.jpeg");
		File yWebC3File = new File(camPath+"/"+instance+"webc3-temp.jpeg");
		File yWebC4File = new File(camPath+"/"+instance+"webc4-temp.jpeg");
		File webcYaFile = new File(camPath+"/webc"+instance+"a-temp.jpeg");
		File webcYbFile = new File(camPath+"/webc"+instance+"b-temp.jpeg");
		File webcYFile = new File(camPath+"/webc"+instance+"-temp.jpeg");
                
                while (testVal == testVal) {

			Date date = new Date();
			final String camTimestamp = dateOverlayFormat.format(date);
			final String fileTimestamp = dateFileFormat.format(date);

			yWebC2File.delete();
			yWebC3File.delete();
			yWebC4File.delete();
			webcYaFile.delete();
			webcYbFile.delete();

			Scanner caseScanner = null; int tempCase = 0; try { caseScanner = new Scanner(lastCaseTemp); while(caseScanner.hasNext()) { tempCase = Integer.parseInt(caseScanner.nextLine()); } } catch (FileNotFoundException e) { e.printStackTrace(); }
			Scanner cpuScanner = null; int tempCPU = 0; try { cpuScanner = new Scanner(lastCPUTemp); while(cpuScanner.hasNext()) { tempCPU = Integer.parseInt(cpuScanner.nextLine()); } } catch (FileNotFoundException e) { e.printStackTrace(); }
                        Scanner garageScanner = null; int tempGarage = 0; try { garageScanner = new Scanner(lastGarageTemp); while(garageScanner.hasNext()) { tempGarage = Integer.parseInt(garageScanner.nextLine()); } } catch (FileNotFoundException e) { e.printStackTrace(); }
			Scanner upsScanner = null; String upsStatus = null; try { upsScanner = new Scanner(lastUPSStatus); while(upsScanner.hasNext()) { upsStatus = upsScanner.nextLine(); } } catch (FileNotFoundException e) { e.printStackTrace(); }

			final String camCaption = junkyBeans.getApplicationName()+" Cams - "+camTimestamp+" -- IN "+tempCase+"F -- GA "+tempGarage+"F -- CPU "+tempCPU+"F -- "+upsStatus;

			Thread ca1a = new Thread(() -> { StumpJunk.runProcess("ffmpeg -i \"rtsp://"+piCam1+":8554/unicast\" -ss 00:00:00.2 -frames 1 "+yWebC4File.getPath()); });
			Thread ca1b = new Thread(() -> { StumpJunk.jsoupOutBinary("http://"+ipCam1+"/cgi-bin/CGIProxy.fcgi?cmd=snapPicture2&usr="+ipCamUser+"&pwd="+ipCamPass, yWebC3File, capWait); });
			Thread ca1c = new Thread(() -> { StumpJunk.jsoupOutBinary("http://"+ipCam2+"/cgi-bin/CGIProxy.fcgi?cmd=snapPicture2&usr="+ipCamUser+"&pwd="+ipCamPass, yWebC2File, capWait); });
			Thread thList1[] = { ca1a, ca1b, ca1c };
			for (Thread thread : thList1) { thread.start(); } 
			for (int i = 0; i < thList1.length; i++) { try { thList1[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }
		
			if(yWebC3File.length() == 0) { StumpJunk.jsoupOutBinary("http://"+ipCam1Alt+"/cgi-bin/CGIProxy.fcgi?cmd=snapPicture2&usr="+ipCamUser+"&pwd="+ipCamPass, yWebC3File, capWait); }

			if(!xWebC1File.exists()) { StumpJunk.runProcess("convert -size "+capRes+" -gravity center -annotate 0 \"Cam1 temporarily unavailable!\" -pointsize 48 -fill Yellow xc:navy "+xWebC1File.getPath()); }
			if(!yWebC2File.exists()) { StumpJunk.runProcess("convert -size "+capRes+" -gravity center -annotate 0 \"Cam2 temporarily unavailable!\" -pointsize 48 -fill Yellow xc:navy "+yWebC2File.getPath()); }
                        if(!yWebC3File.exists()) { StumpJunk.runProcess("convert -size "+capRes+" -gravity center -annotate 0 \"Cam3 temporarily unavailable!\" -pointsize 48 -fill Yellow xc:navy "+yWebC3File.getPath()); }
                        if(!yWebC4File.exists()) { StumpJunk.runProcess("convert -size "+capRes+" -gravity center -annotate 0 \"Cam4 temporarily unavailable!\" -pointsize 48 -fill Yellow xc:navy "+yWebC4File.getPath()); }

			String convertA = "convert \\( "+yWebC4File.getPath()+" -resize "+capRes+"! "+xWebC1File.getPath()+" -resize "+capRes+"! +append \\)"
				+ " -background Black -append "+webcYaFile.getPath();

			String convertB = "convert \\( "+yWebC2File.getPath()+" -resize "+capRes+"! "+yWebC3File.getPath()+" -resize "+capRes+"! +append \\)"
				+ " \\( -gravity south -background Black -pointsize 36 -fill Yellow label:\""+camCaption+"\" +append \\)"
				+ " -background Black -append "+webcYbFile.getPath();

			Thread ca2a = new Thread(() -> { StumpJunk.runProcess(convertA); });
			Thread ca2b = new Thread(() -> { StumpJunk.runProcess(convertB); });
			Thread thList2[] = { ca2a, ca2b };
			for (Thread thread : thList2) { thread.start(); } 
			for (int i = 0; i < thList2.length; i++) { try { thList2[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

			String convertC = "convert \\( "+webcYaFile.getPath()+" +append \\)"
				+ " \\( "+webcYbFile.getPath()+" +append \\)"
				+ " -background Black -append -resize "+camBeans.getFinalRes()+"! "+webcYFile.getPath();

			StumpJunk.runProcess(convertC);

			StumpJunk.moveFile(webcYFile.getPath(), camPath+"/PushTmp/"+fileTimestamp+".jpeg");

		}

	}

}
