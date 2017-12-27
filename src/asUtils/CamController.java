/*
by Anthony Stump
Created: 7 Sep 2017
Updated: 26 Dec 2017
*/

package asUtils;

import asUtils.Cams.CamBeans;
import asUtils.Cams.CamWorkerHF;
import asUtils.Cams.CamWorkerUSB;
import asUtils.Shares.JunkyBeans;
import asUtils.Secure.JunkyPrivate;
import asUtils.Shares.Mailer;
import asUtils.Shares.StumpJunk;
import java.io.*;

public class CamController {

	public static void main(String args[]) {

                CamBeans camBeans = new CamBeans();
                JunkyBeans junkyBeans = new JunkyBeans();
                JunkyPrivate junkyPrivate = new JunkyPrivate();

		final String myCell = junkyPrivate.getSmsAddress();
		final String myGmail = junkyPrivate.getGmailUser();
		final String thisSubject = "asUtils.CamController started up!";
		final String thisMessage = "This means a system reboot likely occurred!";
		final File camPath = camBeans.getCamPath();
		final File pushTemp = camBeans.getPushTemp();
                final File sysLog = new File("/var/log/syslog");
                final File packedLog = new File(junkyBeans.getRamDrive()+"/syslog.zip");
        
                StumpJunk.zipThisFile(sysLog, packedLog);
		Thread cm1 = new Thread(() -> { Mailer.sendMail(myCell, thisSubject, thisMessage, null); });
                Thread cm2 = new Thread(() -> { Mailer.sendMail(myGmail, thisSubject, thisMessage, packedLog); });
                Thread mailers[] = { cm1, cm2 };
                for (Thread thread : mailers) { thread.start(); }
                for (int i = 0; i < mailers.length; i++) { try { mailers[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }
                packedLog.delete();
                
		final String[] usbArg = { camPath.getPath() };
		final String[] hf1Arg = { camPath.getPath(), "X" };
		final String[] hf2Arg = { camPath.getPath(), "Y" };
		final String[] hf3Arg = { camPath.getPath(), "Z" };
		final String[] hf4Arg = { camPath.getPath(), "A" };
		int tester = 1;

		if (!pushTemp.exists()) {
			camPath.mkdirs();
			pushTemp.mkdirs();
		}

		while (tester == tester) {
			Thread cc1 = new Thread(() -> { CamWorkerUSB.main(usbArg); });
			Thread cc2 = new Thread(() -> { CamWorkerHF.main(hf1Arg); });
			Thread cc3 = new Thread(() -> { CamWorkerHF.main(hf2Arg); });
			Thread cc4 = new Thread(() -> { CamWorkerHF.main(hf3Arg); });
			Thread cams[] = { cc1, cc2, cc3, cc4 };
			for (Thread thread : cams) { thread.start(); } 
			for (int i = 0; i < cams.length; i++) {
                            try {
                                long sleepPeriod = (long) i * 200;
                                cams[i].sleep(sleepPeriod);
                                cams[i].join();
                            } catch (InterruptedException nx) { nx.printStackTrace(); }
                        }
		}
                
	}

}
