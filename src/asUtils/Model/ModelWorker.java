/*
by Anthony Stump
Created: 22 Sep 2017
Updated: 27 Dec 2017
*/

package asUtils.Model;

public class ModelWorker {
	
	public static void main(String args[]) {
            
		
		final String getHour = args[0];
		final String round = args[1];
		boolean int6h = false;
		boolean int6hO = false;
		boolean int12h = false;
	
		if(getHour.equals("00") || getHour.equals("06") || getHour.equals("12") || getHour.equals("18")) { int6h = true; }
		if(getHour.equals("03") || getHour.equals("09") || getHour.equals("15") || getHour.equals("21")) { int6hO = true; }
		if(getHour.equals("00") || getHour.equals("12")) { int12h = true; }

		final String[] hrrrArgs = { getHour, round }; ModelWorkerHRRR.main(hrrrArgs);
		
		if(int6h) {
			
			final String[] gfsArgs = { getHour, round }; ModelWorkerGFS.main(gfsArgs);
			final String[] namArgs = { getHour, round }; ModelWorkerNAM.main(namArgs);
			
			if(int12h) {
				final String[] hrwaArgs = { getHour, round }; ModelWorkerHRWA.main(hrwaArgs);
				final String[] hrwnArgs = { getHour, round }; ModelWorkerHRWN.main(hrwnArgs);
				final String[] cmcArgs = { getHour, round }; ModelWorkerCMC.main(cmcArgs);
			}
			
		}

		if(int6hO) {

			final String[] srfaArgs = { getHour, round }; ModelWorkerSRFA.main(srfaArgs);
			final String[] srfnArgs = { getHour, round }; ModelWorkerSRFN.main(srfnArgs);

		}			
		
	}
	
}
