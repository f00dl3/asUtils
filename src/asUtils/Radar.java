/*
by Anthony Stump
Created: 30 Aug 2017
Updated: 27 Dec 2017
*/

package asUtils;

import asUtils.Shares.StumpJunk;

import asUtils.Rad.RadarWorker;
import asUtils.Shares.JunkyBeans;

public class Radar {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
		final String radPath = junkyBeans.getWebRoot().toString()+"/Get/Radar";

		Thread r01 = new Thread(() -> { RadarWorker.main("1"); });
		Thread r02 = new Thread(() -> { RadarWorker.main("2"); });
		Thread r03 = new Thread(() -> { RadarWorker.main("3"); });
		Thread r04 = new Thread(() -> { RadarWorker.main("4"); });
		Thread r05 = new Thread(() -> { RadarWorker.main("5"); });
/*		Thread r06 = new Thread(() -> { RadarWorker.main("6"); });
		Thread r07 = new Thread(() -> { RadarWorker.main("7"); });
		Thread r08 = new Thread(() -> { RadarWorker.main("8"); }); */
		Thread rList[] = { r01, r02, r03, r04, r05 /* r06, r07, r08 */ };
		for (Thread thread : rList) { thread.start(); }
		for (int i = 0; i < rList.length; i++) { try { rList[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }
	
		StumpJunk.runProcess("chown "+junkyBeans.getWebUser()+" "+radPath);
			
	}

}
