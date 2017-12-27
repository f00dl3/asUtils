/*
by Anhony Stump
Created: 14 Aug 2017
Updated: 27 Dec 2017
*/

package asUtils;

import asUtils.Secure.JunkyPrivate;
import asUtils.Shares.JunkyBeans;
import asUtils.Shares.StumpJunk;
import java.io.*;
import java.sql.*;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import asUtils.Shares.MyDBConnector;

public class GetDaily {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
                JunkyPrivate junkyPrivate = new JunkyPrivate();
                
		final String cf6DataURL = "http://w2.weather.gov/climate/getclimate.php?wfo=eax&pil=CF6&sid=mci";
		final File cf6File = new File(junkyBeans.getRamDrive().toString()+"/cf6.txt");
		final int daysBack = Integer.parseInt(args[0]);
		final DateTime tdt = new DateTime().minusDays(daysBack);
		final DateTimeFormatter tdtf = DateTimeFormat.forPattern("dd");
		final DateTimeFormatter sqlf = DateTimeFormat.forPattern("yyyy-MM-dd");
		
		String dom = tdtf.print(tdt);
		String dSQL = sqlf.print(tdt);
		String domSp = dom.replaceAll("\\G0", " ");
		
		StumpJunk.jsoupOutBinary(cf6DataURL, cf6File, 30.0);
		
		String processedLine = null;
		
		int tHigh = 0;
		int tLow = 0;
		int tAverage = 0;
		int tDFNorm = 0;
		int hdd = 0;
		int cdd = 0;
		int pFlag = 0;
		double liquid = 0.0;
		double snow = 0.0;
		double sDepth = 0.0;
		double wAvg = 0;
		int wMax = 0;
		int clouds = 0;
		String weather = null;
		
		Scanner cf6FileScanner = null; try {		
			cf6FileScanner = new Scanner(cf6File);
			while(cf6FileScanner.hasNext()) {				
				String line = cf6FileScanner.nextLine();
				if(line.startsWith(domSp)) {
					processedLine = line.replaceFirst(domSp, dSQL).replaceAll("       ", ",-").replaceAll(" +", ",");
					String[] lineTmp = processedLine.split(",");
					tHigh = Integer.parseInt(lineTmp[1]);
					tLow = Integer.parseInt(lineTmp[2]);
					tAverage = Integer.parseInt(lineTmp[3]);
					tDFNorm = Integer.parseInt(lineTmp[4]);
					hdd = Integer.parseInt(lineTmp[5]);
					cdd = Integer.parseInt(lineTmp[6]);
					liquid = Double.parseDouble(lineTmp[7].replaceAll("T","0.01").replaceAll("M","0.00"));
					if (liquid != 0.00) { pFlag = 1; }
					snow = Double.parseDouble(lineTmp[8].replaceAll("T","0.1").replaceAll("M","0.0"));
					sDepth = Double.parseDouble(lineTmp[9].replaceAll("T","0.1").replaceAll("M","0.0"));
					wAvg = Double.parseDouble(lineTmp[10]);
					wMax = Integer.parseInt(lineTmp[11]);
					clouds = Integer.parseInt(lineTmp[15]);
					weather = lineTmp[16].replaceAll("-","");
				}
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		
		System.out.println(processedLine+"\n\n");
		
		String cf6SQLQuery = "INSERT IGNORE INTO WxObs.CF6MCI ("
			+ "Date, High, Low, Average, DFNorm, HDD, CDD, PFlag,"
			+ " Liquid, Snow, SDepth, WAvg, WMax, Clouds, Weather, Auto"
			+ ") VALUES ("
			+ "'"+dSQL+"',"+tHigh+","+tLow+","+tAverage+","+tDFNorm+","+hdd+","+cdd+","+pFlag+","
			+ liquid+","+snow+","+sDepth+","+wAvg+","+wMax+","+clouds+",'"+weather+"',1"
			+ ");";
			
		System.out.println(cf6SQLQuery);
		try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();) { stmt.executeUpdate(cf6SQLQuery); }
		catch (Exception e) { e.printStackTrace(); }
		
		String anwPrepSQLQuery = "SET @runtot := "+junkyPrivate.getMortBeginningBalance()+";";
		
		String autoNetWorthSQLQuery = "REPLACE INTO FB_ENWT ("
			+ "AsOf, AsLiq, AsFix, Life, Credits, Debts, Auto, AsLiqCA, AsLiqNV,"
			+ " AsFixHM, AsFixAU, AsFixDF, AsFixFT, AsFixEL, AsFixJC, AsFixKT, AsFixMD, AsFixTL,"
			+ " AsFixPT, AsFixUN, AsFixTR"
			+ ") VALUES ("
			+ "current_date,(SELECT SUM("
			+ "(SELECT FORMAT(SUM(Value)/1000,1) FROM FB_Assets WHERE Category IN ('NV','CA')) +"
			+ "(SELECT FORMAT(SUM(Credit-Debit)/1000,1) FROM FB_CFCK01 WHERE Date <= current_date) +"
			+ "(SELECT FORMAT(SUM(Credit-Debit)/1000,1) FROM FB_CFSV59 WHERE Date <= current_date))),"
			+ "(SELECT FORMAT(SUM(Value)/1000,1) FROM FB_Assets WHERE Type = 'F'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'LI'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'CR'),"
			+ "(SELECT FORMAT(MIN(@runtot := @runtot + (@runtot * (("+junkyPrivate.getMortIntRate()+"/12)/100)) - (Extra + "+junkyPrivate.getMortBaseMonthly()+"))/1000,1) AS MBal FROM FB_WFML35 WHERE DueDate < current_date + interval '30' day),1,"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'CA'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'NV'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'HM'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'AU'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'DF'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'FT'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'EL'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'JC'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'KT'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'MD'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'TL'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'PT'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'UN'),"
			+ "(SELECT FORMAT((SUM(Value)/1000),1) FROM FB_Assets WHERE Category = 'TR')"
			+ ");";
		
			try ( Connection conn = MyDBConnector.getMyConnection(); Statement stmt = conn.createStatement();) {
				stmt.executeUpdate(anwPrepSQLQuery);
				stmt.executeUpdate(autoNetWorthSQLQuery);
			}
			catch (Exception e) { e.printStackTrace(); }
			
	}

}
