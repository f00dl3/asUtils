/*
by Anthony Stump
Created: 20 DEC 2017
Updated: 21 DEC 2017
*/

package asUtils.Shares;

import java.io.File;

public class SSHVars {
    
    private File downloadFullPath;
    private String hostIP;
    private File localFullPath;
    private int port;
    private File uploadFullPath;
    private String user;
    private File userHome;
    
    public File getDownloadFullPath() { return downloadFullPath; }
    public String getHostIP() { return hostIP; }
    public File getLocalFullPath() { return localFullPath; }
    public int getPort() { return port; }
    public File getUploadFullPath() { return uploadFullPath; }
    public String getUser() { return user; }
    public File getUserHome() { return userHome; }

    public void setDownloadFullPath(File newDownloadFullPath) { downloadFullPath = newDownloadFullPath; }
    public void setHostIP(String newHostIP) { hostIP = newHostIP; }
    public void setLocalFullPath(File newLocalFullPath) { localFullPath = newLocalFullPath; }
    public void setPort(int newPort) { port = newPort; };
    public void setUploadFullPath(File newUploadFullPath) { uploadFullPath = newUploadFullPath; }
    public void setUser(String newUser) { user = newUser; }
    public void setUserHome(File newUserHome) { userHome = newUserHome; }

}
