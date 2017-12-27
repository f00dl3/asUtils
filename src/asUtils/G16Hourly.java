/*
by Anthony Stump
Created: 7 SEP 2017
Updated: 27 DEC 2017
*/

package asUtils;

import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class G16Hourly {

	public static void main(String[] args) {
            
                JunkyBeans junkyBeans = new JunkyBeans();

		final String animName = args[0];
		final DateFormat dateFormat = new SimpleDateFormat("yyMMddHH");
		final Date date = new Date();
		final String mp4Timestamp = dateFormat.format(date);
		final String g16Source = junkyBeans.getWebRoot().toString()+"/Get/G16/"+animName;
		final File memTemp = new File(junkyBeans.getRamDrive().toString()+"/G16MP4/"+animName);
		final File g16Archive = new File(g16Source+"/Archive");

		memTemp.mkdirs();
		g16Archive.mkdirs();

		StumpJunk.runProcess("mv "+g16Source+"/*.gif "+memTemp+"/");
		StumpJunk.runProcess("bash "+junkyBeans.getHelpers().toString()+"/Sequence.sh "+memTemp+"/ gif");
		StumpJunk.runProcess("mogrify -format jpg "+memTemp+"/*.gif");
		StumpJunk.runProcess("ffmpeg -threads 8 -framerate 24 -i "+memTemp+"/%05d.jpg -vf \"scale=trunc(iw/2)*2:trunc(ih/2)*2\" -vcodec libx264 -pix_fmt yuv420p "+g16Archive.getPath()+"/H"+mp4Timestamp+".mp4");
		StumpJunk.runProcess("chown -R "+junkyBeans.getWebUser()+" "+g16Archive.getPath());
		StumpJunk.deleteDir(memTemp);

	}

}
