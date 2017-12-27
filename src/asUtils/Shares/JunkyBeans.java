/*
By Anthony Stump
Created: 19 Dec 2017
Updated: 27 Dec 2017
 */

package asUtils.Shares;

import java.io.File;
import java.nio.file.*;

public class JunkyBeans {
    
        private final String applicationName = "asUtils";
        private final int applicationMajorVersion = 4;
        private final File appShareSys = new File("/usr/local/bin");
        private final String bicycle = "A16";
        private final int downloadTimeout = 10;
        private final String gmailSmtpServer = "smtp.gmail.com";
        private final File mySqlShare = new File("/var/lib/mysql-files");
	private final File ramDrive = new File("/dev/shm");
	private final File helpers = new File(ramDrive.getPath()+"/asUtils/helpers");
        private final File sdCardPath = new File("/media/astump/PHONE");
        private final File sshKeyFolder = new File("/root/.ssh");
        private final File tomcatWebapps = new File("/var/lib/tomcat8/webapps");
        private final String uName = "astump";
        private final File userHome = new File("/home/astump");
        private final File webRoot = new File("/var/www");
        private final String webUser = "www-data";
        
        private final Path desktopPath = Paths.get(userHome+"/Desktop");
        private final File dbBackupPath = new File(userHome+"/Scripts/dbBackup");
        
        public String getApplicationName() { return applicationName; }
        public int getApplicationMajorVersion() { return applicationMajorVersion; }
        public File getAppShareSys() { return appShareSys; }
        public String getBicycle() { return bicycle; }
        public File getDbBackupPath() { return dbBackupPath; }
        public Path getDesktopPath() { return desktopPath; }
        public int getDownloadTimeout() { return downloadTimeout; }
        public String getGmailSmtpServer() { return gmailSmtpServer; }
        public File getHelpers() { return helpers; }
        public File getMySqlShare() { return mySqlShare; }
        public File getRamDrive() { return ramDrive; }
        public File getSdCardPath() { return sdCardPath; }
        public File getSshKeyFolder() { return sshKeyFolder; }
        public File getTomcatWebapps() { return tomcatWebapps; }
        public String getUName() { return uName; }
        public File getUserHome() { return userHome; }
        public File getWebRoot() { return webRoot; }
        public String getWebUser() { return webUser; }
        
}
