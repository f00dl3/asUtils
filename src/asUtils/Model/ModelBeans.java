/*
By Anthony Stump
Created: 20 Dec 2017
*/

package asUtils.Model;

import java.io.File;
import asUtils.Shares.JunkyBeans;

public class ModelBeans {
    
        JunkyBeans junkyBeans = new JunkyBeans();
    
	final private File ramDrive = junkyBeans.getRamDrive();
        final private String imageResStd = "2904x1440";
       
	final private File xml2Path = new File(ramDrive.getPath()+"/modelsJ");
        final private double downloadTimeout = 15.0;
	final private File imgOutPath = new File(xml2Path.getPath()+"/tmpic");
	final private File pointDump = new File(xml2Path.getPath()+"/pointDump.txt");
        
        public double getDownloadTimeout() { return downloadTimeout; }
        public String getImageResStd() { return imageResStd; }
        public File getXml2Path() { return xml2Path; }
        public File getImgOutPath() { return imgOutPath; }
        public File getPointDump() { return pointDump; }

    
}
