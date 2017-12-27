/*
by Anthony Stump
Created: 26 Dec 2017
*/

package asUtils.Feed;

import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class KCScout {
    
    public static String getScoutSQL() {
        
        JunkyBeans junkyBeans = new JunkyBeans();
                
        final String scoutURL = "http://www.kcscout.net/IncidentViewer.aspx";
        final File ramDrive = junkyBeans.getRamDrive();
        final String ramTemp = ramDrive.getPath()+"/rssXMLFeedsJ";
        final File scoutFile = new File(ramTemp+"/ScoutFeed.aspx");
        
        StumpJunk.jsoupOutFile(scoutURL, scoutFile);
        StumpJunk.sedFileReplace(ramTemp+"/ScoutFeed.aspx", "</div><br></br><div>", "\n");
			
        List<String> scoutType = new ArrayList<>();
        List<String> scoutLocation = new ArrayList<>();
        List<String> scoutDescription = new ArrayList<>();
        List<String> scoutStartTime = new ArrayList<>();

        Scanner scoutScanner = null; try {		
                scoutScanner = new Scanner(scoutFile);
                while(scoutScanner.hasNext()) {				
                        String line = scoutScanner.nextLine();
                        if(line.contains("Type : </b>")) {
                                line = line.replaceAll("<b>", "").replaceAll("<br />", " ").replaceAll("</div><div>", ",");
                                line = line.replaceAll("Type : </b>", "").replaceAll("Location : </b>", "").replaceAll("Description : </b>", "").replaceAll("Start Time : </b>", "");
                                line = line.replaceAll(" +<p><div>  "," ").replaceAll(" +", " ").replaceAll("</div><br></br></p>", "");
                                String[] lineTmp = line.split(",");
                                scoutType.add(lineTmp[0]);
                                scoutLocation.add(lineTmp[1]);
                                scoutDescription.add(lineTmp[2]);
                                scoutStartTime.add(lineTmp[3]);
                        }
                }
        }
        catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
        catch (ArrayIndexOutOfBoundsException aix) { aix.printStackTrace(); }

        String kcScoutSQL = "INSERT IGNORE INTO Feeds.KCScoutIncidents (KeyString, Type, Location, Description, StartTime) VALUES";
        int eNo = 0;
        for (String thisSType : scoutType) {
                kcScoutSQL = kcScoutSQL+" ('"+scoutStartTime.get(eNo)+thisSType+"','"+thisSType+"','"+scoutLocation.get(eNo)+"','"+scoutDescription.get(eNo)+"','"+scoutStartTime.get(eNo)+"'),";
                eNo++;
        }
        kcScoutSQL = kcScoutSQL+";";
        kcScoutSQL = kcScoutSQL.replaceAll("\\),;", "\\);");
		
        return kcScoutSQL;
        
    }
    
}
