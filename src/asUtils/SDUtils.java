/*
by Anthony Stump
Created: 14 Aug 2017
Updated: 27 Dec 2017
*/

package asUtils;

import asUtils.Secure.JunkyPrivate;
import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import asUtils.Shares.SDUtilsVars;
import asUtils.Shares.MyDBConnector;

public class SDUtils {

	public static void main (String[] args) {

		SDUtilsVars sduVars = new SDUtilsVars();
                JunkyBeans junkyBeans = new JunkyBeans();
                JunkyPrivate junkyPrivate = new JunkyPrivate();
                
		sduVars.setBuild("SD Utils Java - Build 358");
		sduVars.setUpdated("27 Dec 2017 @ 10:21 CT");
                File usbDrivePath = junkyBeans.getSdCardPath();
                final String userHome = junkyBeans.getUserHome().toString();
                final String uName = junkyBeans.getUName();
                
		DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");
		Date date = new Date();
		String thisTimestamp = dateFormat.format(date);
		System.out.println(sduVars.getBuild()+"\n"+sduVars.getUpdated()+"\n"+usbDrivePath.toString()+"\nRun time: "+thisTimestamp+"\nby Anthony Stump\n\n");

		System.out.println("Requesting privilages and setting path...");
		StumpJunk.runProcess("sudo echo Done");

		System.setProperty("user.dir", usbDrivePath.toString());
		System.out.println("Installing ASWebUI off MicroSD card...");
		StumpJunk.runProcess("bash "+usbDrivePath.toString()+"/ASWebUI/Install.sh");

		System.out.println("Cleaning crap off this MicroSD card...");
		
		String[] pathsToDelete = {
			"/.mmsyscache/",
			"/.the.pdfviewer3/",
			"/.Trash-1000/",
			"/albumthumbs/",
			"/Android/data/",
			"/AppGame/",
			"/DCIM/.thumbnails/",
			"/LOST.DIR/",
			"/ppy_cross/",
			"/tmp/" };
		
		for (String thisPathString : pathsToDelete) {
			File thisFolder = new File(usbDrivePath.toString()+"/"+thisPathString);
			StumpJunk.deleteDir(thisFolder);
		}

		String[] filesToDelete = { ".bugsense", "tapcontext" };
		for (String thisFileString : filesToDelete) {
			File thisFile = new File(usbDrivePath.toString()+"/"+thisFileString);
			thisFile.delete();
		}
		
		System.out.println("Creating encrypted backup of MySQL critical databases...");
		System.setProperty("user.dir", usbDrivePath.toString()+"/");
		new File(sduVars.getCachePath()+"/SQLDumps").mkdirs();
		
		String[] sqlTasks = { "Core", "Feeds", "jspServ", "lahman2016", "mydiary", "net_snmp", "WebCal" };
		for (String task : sqlTasks) {
			StumpJunk.runProcess("sudo -i mysqldump "+task+" --result-file="+sduVars.getCachePath()+"/SQLDumps/"+task+".sql");
		}

		StumpJunk.runProcess("tar -zcvf \""+sduVars.getCachePath()+"/"+thisTimestamp+"-Struct.tar.gz\" "+sduVars.getCachePath()+"/SQLDumps/*");
		File sqlCacheFolder = new File(sduVars.getCachePath()+"/SQLDumps");
		StumpJunk.deleteDir(sqlCacheFolder);		
		StumpJunk.runProcess("(sudo -i -u "+uName+" gpg --output \""+usbDrivePath.toString()+"/[data]/Tools/SQL/Backup/"+thisTimestamp+"-Struct.tar.gz.gpg\" --encrypt --recipient "+junkyPrivate.getGmailUser()+" \""+sduVars.getCachePath()+"/"+thisTimestamp+"-Struct.tar.gz\")");
		File unEncDbBU = new File(sduVars.getCachePath()+"/"+thisTimestamp+"-Struct.tar.gz");
		unEncDbBU.delete();

		System.out.println("Backing up CODEX data...");
                File codexOutZip = new File(usbDrivePath.toString()+"/[data]/Tools/dev/codex.zip");
                File codexPath = new File(sduVars.getCodexPath());
		StumpJunk.zipThisFolder(codexPath, codexOutZip);

		System.out.println("Backing up all SD Card data...");
                File thisZip = new File(sduVars.getUsbBackupPath()+"/"+thisTimestamp+".zip");
                StumpJunk.zipThisFolder(usbDrivePath, thisZip);
		StumpJunk.runProcess("(ls "+userHome+"/USB-Back/* -t | head -n 4; ls "+userHome+"/USB-Back/*)|sort|uniq -u|xargs rm");

		System.out.println("Writing log into database...");
		String usbBackSizeKB = Long.toString(new File(userHome+"/USB-Back/"+thisTimestamp+".zip").length()/1024);
		String updateQuery = "INSERT INTO Core.Log_SDUtils (Date,Time,Notes,ZIPSize) VALUES (CURDATE(),CURTIME(),'Ran "+sduVars.getBuild()+" Modified "+sduVars.getUpdated()+"',"+usbBackSizeKB+");";		
		try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement(); ) { stmt.executeUpdate(updateQuery); } catch (Exception e) { e.printStackTrace(); }
		
	}

}
