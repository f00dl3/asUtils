/*
by Anthony Stump
Created 18 Dec 2017
Updated 27 Dec 2017
*/


package asUtils.Shares;

public class SDUtilsVars {

        JunkyBeans junkyBeans = new JunkyBeans();
        
	private String build;
	private String cachePath = junkyBeans.getUserHome().toString()+"/.cache";
        private String codexPath = junkyBeans.getUserHome().toString()+"/src/codex";
	private String updated;
        private String usbBackupPath = junkyBeans.getUserHome().toString()+"/home/astump/USB-Back/";

	public String getBuild() { return build; }
	public String getCachePath() { return cachePath; }
        public String getCodexPath() { return codexPath; }
	public String getUpdated() { return updated; }
        public String getUsbBackupPath() { return usbBackupPath; }

	public void setBuild(String newBuild) { build = newBuild; }
	public void setUpdated(String newUpdated) { updated = newUpdated; }

}
