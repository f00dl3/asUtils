/*
by Anthony Stump
Created: 13 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils.xs;

import asUtils.Shares.StumpJunk;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import asUtils.Shares.MyDBConnector;
import asUtils.Shares.EmptyImageCleaner;
import asUtils.Shares.JunkyBeans;

public class xsImageOp {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
                
		final int iCyc = 192;
		final String xsTmp = args[0];
		final String gradsOut = xsTmp+"/grib2/iOut";
		final String gVarsSQL = "SELECT gVar FROM WxObs.gradsOutType WHERE Active=1;";
		final String gVarsHSQL = "SELECT gVar FROM WxObs.gradsOutType WHERE Active=1 AND HighRes=1;";
		final String gVarsLSQL = "SELECT gVar FROM WxObs.gradsOutType WHERE Active=1 AND HighRes=0;";
		final String wwwOut = junkyBeans.getWebRoot().toString()+"/G2Out";
		final File gradsOutObj = new File(xsTmp+"/grib2/iOut");
		final File wwwOutObj = new File(wwwOut+"/xsOut");
		List<String> gVars = new ArrayList<>();
		List<String> gVarsH = new ArrayList<>();
		List<String> gVarsL = new ArrayList<>();
		
		StumpJunk.runProcess("cp -Rv "+gradsOutObj.getPath()+"/* "+wwwOutObj.getPath());

		try (
			Connection conn1 = MyDBConnector.getMyConnection(); Statement stmt1 = conn1.createStatement();
			ResultSet resultSetGVars = stmt1.executeQuery(gVarsSQL);
		) {
			while (resultSetGVars.next()) { gVars.add(resultSetGVars.getString("gVar")); }
		}
		catch (Exception e) { e.printStackTrace(); }

		try (
			Connection conn2 = MyDBConnector.getMyConnection(); Statement stmt2 = conn2.createStatement();
			ResultSet resultSetGVarsH = stmt2.executeQuery(gVarsHSQL);
		) {
			while (resultSetGVarsH.next()) { gVarsH.add(resultSetGVarsH.getString("gVar")); }
		}
		catch (Exception e) { e.printStackTrace(); }

		try (
			Connection conn3 = MyDBConnector.getMyConnection(); Statement stmt3 = conn3.createStatement();
			ResultSet resultSetGVarsL = stmt3.executeQuery(gVarsLSQL);
		) {
			while (resultSetGVarsL.next()) { gVarsL.add(resultSetGVarsL.getString("gVar")); }
		}
		catch (Exception e) { e.printStackTrace(); }

		for (String gVar : gVarsL) { StumpJunk.runProcess("(ls "+wwwOut+"/xsOut/"+gVar+"/*.png -t | head -n "+iCyc+"; ls "+wwwOut+"/xsOut/"+gVar+"/*.png)|sort|uniq -u|xargs rm"); }
		for (String gVar : gVarsH) { StumpJunk.runProcess("(ls "+wwwOut+"/xsOut/"+gVar+"/*.png -t | head -n 12; ls "+wwwOut+"/xsOut/"+gVar+"/*.png)|sort|uniq -u|xargs rm"); }

		StumpJunk.runProcess("cp -R "+wwwOut+"/xsOut/* "+gradsOut+"/");

		for (String gVar : gVarsL) {
			String thisPathString = gradsOut+"/"+gVar;
			String[] imageCleanerArgs = { thisPathString };
			EmptyImageCleaner.main(imageCleanerArgs);
			StumpJunk.runProcess("bash "+junkyBeans.getHelpers().toString()+"/Sequence.sh "+thisPathString+" png");
			StumpJunk.runProcess("ffmpeg -threads 8 -r 10 -i "+thisPathString+"/%05d.png -vcodec libx264 -pix_fmt yuv420p "+xsTmp+"/_HRRRLoop_"+gVar+".mp4");
		}

		StumpJunk.runProcess("mv "+xsTmp+"/_HRRRLoop_* "+wwwOut+"/");
		StumpJunk.runProcess("chown -R "+junkyBeans.getWebUser()+" "+wwwOut+"/");
		StumpJunk.deleteDir(gradsOutObj);

	}

}
