/*
by Anthony Stump
Models core class

Created: 22 Sep 2017
Updated: 27 Dec 2017
Status: Production

Completed: GFS, CMC, HRRR, HRWA, HRWN, NAM, SRFA, SRFN
*/


package asUtils;

import asUtils.Shares.StumpJunk;
import java.io.*;
import java.sql.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import asUtils.Shares.MyDBConnector;
import asUtils.Model.ModelBeans;
import asUtils.Model.ModelImageOps;
import asUtils.Model.ModelShare;
import asUtils.Model.ModelWorker;

public class Models {

	public static void main(String args[]) {

                ModelBeans modelBeans = new ModelBeans();
                
		final DateTime tDateTime = new DateTime(DateTimeZone.UTC).minusHours(4);
		final DateTimeFormatter getDateFormat = DateTimeFormat.forPattern("yyyyMMdd");
		final String getHour = args[0];
		final String getDate = getDateFormat.print(tDateTime);
		final String modelRunString = getDate+"_"+getHour+"Z";
		final File imgOutPath = modelBeans.getImgOutPath();
		final File xml2Path = modelBeans.getXml2Path();
		final File mSQLDebugDumpFile = new File(xml2Path.getPath()+"/mSQLDebugDump.json");

		StumpJunk.deleteDir(xml2Path);
		imgOutPath.mkdirs();
		
		final String[] mw01Args = { getHour, "1" };
		final String[] mw02Args = { getHour, "2" };
		final String[] mw03Args = { getHour, "3" };
		final String[] mw04Args = { getHour, "4" };
		final String[] mw05Args = { getHour, "5" };
		final String[] mw06Args = { getHour, "6" };
		final String[] mw07Args = { getHour, "7" };
		final String[] mw08Args = { getHour, "8" };
		final String[] mw09Args = { getHour, "9" };
		final String[] mw10Args = { getHour, "10" };
		final String[] mw11Args = { getHour, "11" };
		final String[] mw12Args = { getHour, "12" };
		final String[] mw13Args = { getHour, "13" };
		final String[] mw14Args = { getHour, "14" };
		final String[] mw15Args = { getHour, "15" };
		final String[] mw16Args = { getHour, "16" };
		final String[] mw17Args = { getHour, "17" };
		final String[] mw18Args = { getHour, "18" };
		final String[] mw19Args = { getHour, "19" };
		final String[] mw20Args = { getHour, "20" };
		final String[] mw21Args = { getHour, "21" };
		final String[] mw22Args = { getHour, "22" };
		final String[] mw23Args = { getHour, "23" };
		final String[] mw24Args = { getHour, "24" };
		final String[] mw25Args = { getHour, "25" };
		final String[] mw26Args = { getHour, "26" };
		final String[] mw27Args = { getHour, "27" };
		
		Thread mw01 = new Thread(() -> { ModelWorker.main(mw01Args); });
		Thread mw02 = new Thread(() -> { ModelWorker.main(mw02Args); });
		Thread mw03 = new Thread(() -> { ModelWorker.main(mw03Args); });
		Thread mw04 = new Thread(() -> { ModelWorker.main(mw04Args); });
		Thread mw05 = new Thread(() -> { ModelWorker.main(mw05Args); });
		Thread mw06 = new Thread(() -> { ModelWorker.main(mw06Args); });
		Thread mw07 = new Thread(() -> { ModelWorker.main(mw07Args); });
		Thread mw08 = new Thread(() -> { ModelWorker.main(mw08Args); });
		Thread mw09 = new Thread(() -> { ModelWorker.main(mw09Args); });
		Thread mw10 = new Thread(() -> { ModelWorker.main(mw10Args); });
		Thread mw11 = new Thread(() -> { ModelWorker.main(mw11Args); });
		Thread mw12 = new Thread(() -> { ModelWorker.main(mw12Args); });
		Thread mw13 = new Thread(() -> { ModelWorker.main(mw13Args); });
		Thread mw14 = new Thread(() -> { ModelWorker.main(mw14Args); });
		Thread mw15 = new Thread(() -> { ModelWorker.main(mw15Args); });
		Thread mw16 = new Thread(() -> { ModelWorker.main(mw16Args); });
		Thread mw17 = new Thread(() -> { ModelWorker.main(mw17Args); });
		Thread mw18 = new Thread(() -> { ModelWorker.main(mw18Args); });
		Thread mw19 = new Thread(() -> { ModelWorker.main(mw19Args); });
		Thread mw20 = new Thread(() -> { ModelWorker.main(mw20Args); });
		Thread mw21 = new Thread(() -> { ModelWorker.main(mw21Args); });
		Thread mw22 = new Thread(() -> { ModelWorker.main(mw22Args); });
		Thread mw23 = new Thread(() -> { ModelWorker.main(mw23Args); });
		Thread mw24 = new Thread(() -> { ModelWorker.main(mw24Args); });
		Thread mw25 = new Thread(() -> { ModelWorker.main(mw25Args); });
		Thread mw26 = new Thread(() -> { ModelWorker.main(mw26Args); });
		Thread mw27 = new Thread(() -> { ModelWorker.main(mw27Args); });
		Thread mwPool[] = { mw01, mw02, mw03, mw04, mw05, mw06, mw07, mw08, mw09, mw10, mw11, mw12, mw13, mw14, mw15, mw16, mw17, mw18, mw19, mw20, mw21, mw22, mw23, mw24, mw25, mw26, mw27 }; 
		for (Thread thread : mwPool) { thread.start(); }
		for (int i = 0; i < mwPool.length; i++) { try { mwPool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }
		
		final String[] imgOpArgs = { getHour }; ModelImageOps.main(imgOpArgs);
		
		String mSQLIndex = null;
		String mSQLQuery = null;
		
		String hrrrJSONString = ModelShare.jsonMerge("hrrr");
		
		if(getHour.equals("03") || getHour.equals("09") || getHour.equals("15") || getHour.equals("21")) {
			String srfaJSONString = ModelShare.jsonMerge("srfa");
			String srfnJSONString = ModelShare.jsonMerge("srfn");
			mSQLIndex = "INSERT INTO WxObs.MOS_Index (RunString, GFS, NAM4KM, RAP, CMC, HRRR, HRWA, HRWN, SRFA, SRFN) VALUES ('"+modelRunString+"',0,0,0,0,1,0,0,1,1);";
			mSQLQuery = "INSERT INTO WxObs.KOJC_MFMD (RunString, HRRR, SRFA, SRFN) VALUES ('"+modelRunString+"','"+hrrrJSONString+"','"+srfaJSONString+"','"+srfnJSONString+"');";
		} else if(getHour.equals("00") || getHour.equals("06") || getHour.equals("12") || getHour.equals("18")) {
			String gfsJSONString = ModelShare.jsonMerge("gfs");
			String namJSONString = ModelShare.jsonMerge("nam");
			if(getHour.equals("00") || getHour.equals("12")) {
				String cmcJSONString = ModelShare.jsonMerge("cmc");
				String hrwaJSONString = ModelShare.jsonMerge("hrwa");
				String hrwnJSONString = ModelShare.jsonMerge("hrwn");
				mSQLIndex = "INSERT INTO WxObs.MOS_Index (RunString, GFS, NAM4KM, RAP, CMC, HRRR, HRWA, HRWN, SRFA, SRFN) VALUES ('"+modelRunString+"',1,1,0,1,1,1,1,0,0);";
				mSQLQuery = "INSERT INTO WxObs.KOJC_MFMD (RunString, GFS, NAM, CMC, HRRR, HRWA, HRWN) VALUES ('"+modelRunString+"','"+gfsJSONString+"','"+namJSONString+"','"+cmcJSONString+"','"+hrrrJSONString+"','"+hrwaJSONString+"','"+hrwnJSONString+"');";
			} else {
				mSQLIndex = "INSERT INTO WxObs.MOS_Index (RunString, GFS, NAM4KM, RAP, CMC, HRRR, HRWA, HRWN, SRFA, SRFN) VALUES ('"+modelRunString+"',1,1,0,0,1,0,0,0,0);";
				mSQLQuery = "INSERT INTO WxObs.KOJC_MFMD (RunString, GFS, NAM, HRRR) VALUES ('"+modelRunString+"','"+gfsJSONString+"','"+namJSONString+"','"+hrrrJSONString+"');";
			}
		} else {
			mSQLIndex = "INSERT INTO WxObs.MOS_Index (RunString, GFS, NAM4KM, RAP, CMC, HRRR, HRWA, HRWN, SRFA, SRFN) VALUES ('"+modelRunString+"',0,0,0,0,1,0,0,0,0);";
			mSQLQuery = "INSERT INTO WxObs.KOJC_MFMD (RunString, HRRR) VALUES ('"+modelRunString+"','"+hrrrJSONString+"');";
		}
		
		String mSQLDumper = mSQLIndex + "\n" + mSQLQuery;
		
		try { StumpJunk.varToFile(mSQLDumper, mSQLDebugDumpFile, false); } catch (FileNotFoundException fnf) { fnf.printStackTrace(); }
		
		try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();) { stmt.executeUpdate(mSQLIndex); }
		catch (SQLException se) { se.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }
		
		try ( Connection conn2 = MyDBConnector.getMyConnection(); Statement stmt2 = conn2.createStatement();) { stmt2.executeUpdate(mSQLQuery); }
		catch (SQLException se) { se.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }
		
	}

}
