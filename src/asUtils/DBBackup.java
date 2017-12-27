/*
by Anthony Stump
Created 24 Dec 2017
Updated 25 Dec 2017
 */

package asUtils;

import asUtils.Secure.GDriveAttribs;
import asUtils.Secure.JunkyPrivate;
import asUtils.Shares.GDrive;
import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DBBackup {
    
    public static void main(String[] args) {
           
        GDriveAttribs gDriveAttribs = new GDriveAttribs();
        JunkyBeans junkyBeans = new JunkyBeans();
        JunkyPrivate junkyPrivate = new JunkyPrivate();
        DateFormat dateFormat = new SimpleDateFormat("yyMMdd");
	Date date = new Date();
        
        final String gdParentFolder = gDriveAttribs.getDbBackupFolder();
        final String gpgKeyRecipient = junkyPrivate.getGpgKeyRecipient();
        final String backupType = args[0];
        final String uName = junkyBeans.getUName();
        final File dbbPath = junkyBeans.getDbBackupPath();
        final int keepCount = 2;
        File backupFinalFile = null;
  
	final String today = dateFormat.format(date);
        
        if(!dbbPath.exists()) { dbbPath.mkdirs(); }
        
        if(backupType == "Full") {
            System.out.println("Performing a Full Encrypted Compressed MySQL backup");
            backupFinalFile = new File(dbbPath.toString()+"/FullBackup-"+today+".sql.gz.gpg");
            StumpJunk.runProcess("(mysqldump --all-databases | gzip | sudo -H -u astump gpg --output "+backupFinalFile.toString()+" --encrypt --recipient "+gpgKeyRecipient+" -)");
            StumpJunk.runProcess("chown "+uName+" "+backupFinalFile.toString());
            StumpJunk.runProcess("((ls "+dbbPath.toString()+"/FullBackup-??????.sql.gz.gpg -t | head -n "+keepCount+"; ls "+dbbPath.toString()+"/FullBackup-??????.sql.gz.gpg)|sort|uniq -u|xargs rm)");
        } else {
            System.out.println("Performing a WxObs Encrypted Compressed MySQL backup");
            backupFinalFile = new File(dbbPath.toString()+"/WxObs-"+today+".sql.gz.gpg");
            StumpJunk.runProcess("(mysqldump WxObs | gzip | sudo -H -u astump gpg --output "+backupFinalFile.toString()+" --encrypt --recipient "+gpgKeyRecipient+" -)");
            StumpJunk.runProcess("chown "+uName+" "+backupFinalFile.toString());
            StumpJunk.runProcess("((ls "+dbbPath.toString()+"/WxObs-??????.sql.gz.gpg -t | head -n "+keepCount+"; ls "+dbbPath.toString()+"/WxObs-??????.sql.gz.gpg)|sort|uniq -u|xargs rm)");
        }
        
        StumpJunk.runProcess("chown -R "+dbbPath.toString());

        if(backupFinalFile != null) {
            try { GDrive.deleteChildItemsFromFolder("DBBackup"); } catch (IOException ix) { ix.printStackTrace(); }
            try { GDrive.uploadFile(backupFinalFile, "application/zip", gdParentFolder); } catch (IOException ix) { ix.printStackTrace(); }
        } else {
            System.out.println("Backup failed, skipping upload.");
        }
        
    }
}
