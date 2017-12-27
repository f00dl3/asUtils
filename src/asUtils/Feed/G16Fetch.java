/*
by Anthony Stump
Created: 7 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils.Feed;

import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class G16Fetch {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
            
		final String g16Base = junkyBeans.getWebRoot().toString()+"/Get/G16";
		final File objM01B02S05 = new File(g16Base+"/M01B02S05");
		final File objM01B02S05Latest = new File(objM01B02S05.getPath()+"/Latest");
		final File objCentPlains = new File(g16Base+"/CentPlains");
		final File objCentPlainsLatest = new File(objCentPlains.getPath()+"/Latest");
		final File objCentPlainsIR = new File(g16Base+"/CentPlainsIR");
		final File objCentPlainsIRLatest = new File(objCentPlainsIR.getPath()+"/Latest");
		final DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
		final Date date = new Date();
		final String imgTimestamp = dateFormat.format(date);
		final String baseURL = "http://rammb.cira.colostate.edu/ramsdis/online/images/latest/goes-16/";

		File fileM01B02S05 = new File(objM01B02S05.getPath()+"/"+imgTimestamp+".gif");
		File fileCentPlains = new File(objCentPlains.getPath()+"/"+imgTimestamp+".gif");
		File fileCentPlainsIR = new File(objCentPlainsIR.getPath()+"/"+imgTimestamp+".gif");

		objM01B02S05Latest.mkdirs();
		objCentPlainsLatest.mkdirs();
		objCentPlainsIRLatest.mkdirs();
	
		Thread s1a = new Thread(() -> { StumpJunk.jsoupOutBinary(baseURL+"mesoscale_01_band_02_sector_05.gif", fileM01B02S05, 5.0); });
		Thread s1b = new Thread(() -> { StumpJunk.jsoupOutBinary(baseURL+"central_plains_band_02.gif", fileCentPlains, 5.0); });
		Thread s1c = new Thread(() -> { StumpJunk.jsoupOutBinary(baseURL+"central_plains_band_13_minus_band_15.gif", fileCentPlainsIR, 5.0); });
		Thread downs[] = { s1a, s1b, s1c };
		for (Thread thread : downs) { thread.start(); }
		for (int i = 0; i < downs.length; i++) { try { downs[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

		Thread s2a = new Thread(() -> { try { StumpJunk.copyFile(fileM01B02S05.getPath(), objM01B02S05Latest.getPath()+"/Latest.gif"); } catch (IOException ix) { ix.printStackTrace(); }  });
		Thread s2b = new Thread(() -> { try { StumpJunk.copyFile(fileCentPlains.getPath(), objCentPlainsLatest.getPath()+"/Latest.gif"); } catch (IOException ix) { ix.printStackTrace(); }  });
		Thread s2c = new Thread(() -> { try { StumpJunk.copyFile(fileCentPlainsIR.getPath(), objCentPlainsIRLatest.getPath()+"/Latest.gif"); } catch (IOException ix) { ix.printStackTrace(); } });
		Thread s2d = new Thread(() -> { StumpJunk.runProcess("convert -delay 7 -loop 0 "+objCentPlains.getPath()+"/*.gif "+objCentPlainsLatest.getPath()+"/Loop.gif"); });
		Thread s2e = new Thread(() -> { StumpJunk.runProcess("convert -delay 7 -loop 0 "+objCentPlainsIR.getPath()+"/*.gif "+objCentPlainsIRLatest.getPath()+"/Loop.gif"); });
		Thread latest[] = { s2a, s2b, s2c, s2d, s2e };
		for (Thread thread : latest) { thread.start(); }
		for (int i = 0; i < latest.length; i++) { try { latest[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

	}

}
