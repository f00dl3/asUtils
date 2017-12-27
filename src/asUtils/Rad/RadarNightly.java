/*
by Anthony Stump
Created: 10 Sep 2017
Concept only - not in use.
*/

package asUtils.Rad;

import java.nio.file.*;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class RadarNightly {

	public static void main(String args[]) {

		final DateTime dtYesterday = new DateTime().minusDays(1);
		final DateTimeFormatter dtFormat = DateTimeFormat.forPattern("yyMMdd");
		final String getYesterday = dtFormat.print(dtYesterday);
		final Path ramTmp = Paths.get("/dev/shm");
		final Path radPath = Paths.get("/var/www/Get/Radar");
		final Path radTmp = Paths.get(ramTmp.toString()+"/Radar");


	}

}

/*
radList=($(echo "SELECT Site FROM WxObs.RadarList WHERE Active=1 ORDER BY Site ASC;" | mysql -N))
jClassPath="-cp "$ramTemp"/jASUtils"

mkdir -p $radTmp
for thisRad in ${radList[@]}; do
  mkdir -p $radTmp/$thisRad
	mv $radPath/$thisRad/Archive/*.gif $radTmp/$thisRad
done
zip -9rv $radPath/MP4/$getYesterday.Archived.zip $radTmp
(ls $radPath/MP4/* -t | head -n 14; ls $radPath/MP4/*)|sort|uniq -u|xargs rm
chown -R www-data $radPath/MP4/
rm -fr $radTmp
*/
