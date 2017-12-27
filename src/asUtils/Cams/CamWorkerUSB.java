/*
by Anthony Stump
Created: 31 Aug 2017
Updated: 27 Dec 2017
*/

package asUtils.Cams;

import asUtils.Shares.StumpJunk;
import java.io.*;


public class CamWorkerUSB {

	public static void main(String args[]) {
		
                CamBeans camBeans = new CamBeans();
		final String camPath = args[0];
		final int testVal = 1;
		final File usbCamFile = new File(camPath+"/Xwebc1-temp.jpeg");
		final File usbTempFile = new File(camPath+"/Xwebc1-temp-A.jpeg");
		while (testVal == testVal) {

			String usbDevice = null;
			try { usbDevice = StumpJunk.runProcessOutVar("ls "+camBeans.getUsbs().toString()+"*"); } catch (IOException ix) { ix.printStackTrace(); }
			usbDevice = usbDevice.replaceAll("\\n", "");
			StumpJunk.runProcess("timeout --kill-after=5 5 ffmpeg -f video4linux2 -s "+camBeans.getCapRes()+" -i "+usbDevice+" -ss 00:00:00.2 -frames 1 "+usbTempFile.getPath());
			if(usbCamFile.exists()) { usbCamFile.delete(); }
			if(usbTempFile.exists()) { StumpJunk.moveFile(camPath+"/Xwebc1-temp-A.jpeg", usbCamFile.getPath()); } else { System.out.println("USB issues!"); }

		}

	}

}
