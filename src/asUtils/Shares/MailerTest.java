/*
by Anthony Stump
Created: 17 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils.Shares;

import asUtils.Secure.JunkyPrivate;
import java.io.File;


public class MailerTest {

	public static void main(String[] args) {

                JunkyPrivate junkyPrivate = new JunkyPrivate();
                JunkyBeans junkyBeans = new JunkyBeans();
                
                final File sysLog = new File("/var/log/syslog");
                final File packedLog = new File(junkyBeans.getRamDrive().toString()+"/syslog.zip");
		final String thisSubject = junkyBeans.getApplicationName()+".Shares.MailerTest";
		final String myGmail = junkyPrivate.getGmailUser();
		final String thisMessage = "This is a Java test message.";
                
		System.out.println(thisSubject+" - send message with attachment to gmail.");
                
                StumpJunk.zipThisFile(sysLog, packedLog);
		Mailer.sendMail(myGmail, thisSubject, thisMessage, packedLog);

	}

}
