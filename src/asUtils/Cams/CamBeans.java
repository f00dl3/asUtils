/*
by Anthony Stump
Created: 20 Dec 2017
Updated: 27 Dec 2017
 */

package asUtils.Cams;

import asUtils.Shares.JunkyBeans;
import java.io.File;

public class CamBeans {
    
    JunkyBeans junkyBeans = new JunkyBeans();

    final private File camPath = new File(junkyBeans.getRamDrive().toString()+"/GetCamsJ");
    final private File camWebRoot = new File(junkyBeans.getWebRoot().toString()+"/Get/Cams");
    final private String capRes = "954x540";
    final private double capWait = 0.01;
    final private String finalRes = "1090x810";
    final private int frameRate = 12;
    final private File usbs = new File("/dev/video");
    final private File tfOutCase = new File(camPath.toString()+"/tfOutCase.txt");
    final private File tfOutCPU = new File(camPath.toString()+"/tfOutCPU.txt");
    final private File tfOutGarage = new File(camPath.toString()+"/tfOutGarage.txt");
    final private File dumpTemp = new File(camPath.getPath()+"/DumpTmp");
    final private File pushTemp = new File(camPath.getPath()+"/PushTmp");   
    
    public File getCamPath() { return camPath; }
    public File getCamWebRoot() { return camWebRoot; }
    public String getCapRes() { return capRes; }
    public double getCapWait() { return capWait; }
    public File getDumpTemp() { return dumpTemp; }
    public String getFinalRes() { return finalRes; }
    public int getFrameRate() { return frameRate; }
    public File getPushTemp() { return pushTemp; }
    public File getUsbs() { return usbs; }
    public File getTfOutCase() { return tfOutCase; }
    public File getTfOutCPU() { return tfOutCPU; }
    public File getTfOutGarage() { return tfOutGarage; }
    
    
}
