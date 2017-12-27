/*
by Anthony Stump
Created: 30 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import asUtils.GPSParse;
import asUtils.Shares.JunkyBeans;

public class GPSBulk {

	public static void main(String[] args) {

                JunkyBeans junkyBeans = new JunkyBeans();
		final String archFlag = "no";
		final File dropLocation = new File(junkyBeans.getDesktopPath().toString());
		final File[] dirList = dropLocation.listFiles();

		String thisTrace = null;

		if (dirList != null) {
			for (File child : dirList) {
				String childPath = child.getPath();
				if(childPath.contains(".csv")) {
					Pattern p = Pattern.compile("Desktop/(.*).csv");
					Matcher m = p.matcher(childPath);
					if (m.find()) {
						thisTrace = m.group(1);
						System.out.println(" --> Processing: "+thisTrace);
						String gpsProcArgs[] = { thisTrace, archFlag };
						GPSParse.main(gpsProcArgs);
					}
				}
			}
		}

	}

}
