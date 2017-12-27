/*
by Anthony Stump
Created: 21 Dec 2017
Updated: 27 Dec 2017
 */

package asUtils.Shares;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileInputStream;

public class SSHTools {

    public static void sftpUpload(String user, String hostIP, int port, File localFullPath, File uploadFullPath, File hostKey) {

            System.out.println("Starting SSH transaction - PUT "+localFullPath.toString()
                    +" TO "+uploadFullPath.toString()+" on "+user+"@"+hostIP+":"+port
                    +" using key "+hostKey.toString());
            
            SSHVars sshVars = new SSHVars();
            sshVars.setHostIP(hostIP);
            sshVars.setLocalFullPath(localFullPath);
            sshVars.setUser(user);
            sshVars.setUploadFullPath(uploadFullPath);
            sshVars.setPort(port);

            Session session = null;
            Channel channel = null;
            ChannelSftp channelSftp = null;

            try {
                JSch jsch = new JSch();
                jsch.addIdentity(hostKey.toString());
                session = jsch.getSession(sshVars.getUser(), sshVars.getHostIP(), sshVars.getPort());
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                channel = session.openChannel("sftp");
                channel.connect();
                channelSftp = (ChannelSftp) channel;
                channelSftp.cd(sshVars.getUploadFullPath().getParent());
                channelSftp.put(new FileInputStream(sshVars.getLocalFullPath()), sshVars.getLocalFullPath().getName());
                channelSftp.exit();
                session.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

    }
    
    public static void sftpDownload(String user, String hostIP, int port, File localFullPath, File downloadFullPath, File hostKey) {

            System.out.println("Starting SSH transaction - GET "+downloadFullPath.toString()
                    +" from "+user+"@"+hostIP+":"+port
                    +" using key "+hostKey.toString());
            
            SSHVars sshVars = new SSHVars();
            sshVars.setHostIP(hostIP);
            sshVars.setLocalFullPath(localFullPath);
            sshVars.setUser(user);
            sshVars.setDownloadFullPath(downloadFullPath);
            sshVars.setPort(port);

            Session session = null;
            Channel channel = null;
            ChannelSftp channelSftp = null;

            try {
                JSch jsch = new JSch();
                jsch.addIdentity(hostKey.toString());
                session = jsch.getSession(sshVars.getUser(), sshVars.getHostIP(), sshVars.getPort());
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.connect();
                channel = session.openChannel("sftp");
                channel.connect();
                channelSftp = (ChannelSftp) channel;
                channelSftp.lcd(sshVars.getLocalFullPath().getParent());
                channelSftp.cd(sshVars.getDownloadFullPath().getParent());
                channelSftp.get(sshVars.getDownloadFullPath().getName(), sshVars.getLocalFullPath().getName(), null );
                channelSftp.exit();
                session.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

    }
    
}
