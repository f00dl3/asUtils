/*
by Anthony Stump
Created: 20 Dec 2017
Updated: 26 Dec 2017
 */

package asUtils.Shares;

public class SNMPBeans {
    
	private final double tA = 0.97;
	private final int multFact = 100;
        private int cpuLoad;
                
        public int getCpuLoad() { return cpuLoad; }
        public double getTa() { return tA; }
        public int getMultFact() { return multFact; }
        
        public void setCpuLoad(int newCpuLoad) { cpuLoad = newCpuLoad; }
                
}
