/*
by Anthony Stump
Created: 12 Oct 2017
Updated: 27 Dec 2017
*/

package asUtils.Model;

import asUtils.Shares.StumpJunk;
import asUtils.Shares.JunkyBeans;
import asUtils.Model.ModelBeans;
import java.io.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ModelImageOps {

	public static void main(String args[]) {

                JunkyBeans junkyBeans = new JunkyBeans();
                ModelBeans modelBeans = new ModelBeans();
            
		final DateTime tDateTime = new DateTime(DateTimeZone.UTC).minusHours(4);
		final DateTimeFormatter getDateFormat = DateTimeFormat.forPattern("yyyyMMdd");
		final String getDate = getDateFormat.print(tDateTime);
		final File xml2Path = modelBeans.getXml2Path();
		final String wwwOutBase = junkyBeans.getWebRoot().toString()+"/G2Out";
		final String stdRes = modelBeans.getImageResStd();
		final File focusMP4 = new File(xml2Path.getPath()+"/FOCUS_Loop.mp4");
		final File helpers = junkyBeans.getHelpers();
		final File hrrrMP4 = new File(xml2Path.getPath()+"/HRRR_Loop.mp4");
		final File memHRRR = new File(xml2Path.getPath()+"/tmpic/HRRR");
		final File memHRRRFocus = new File(xml2Path.getPath()+"/tmpic/FOCUS");
		final File wwwOutArchive = new File(wwwOutBase+"/MergedJ/Archive");
		final File wwwOutLoops = new File(wwwOutBase+"/MergedJ/Loops");
		final File wwwOutImages = new File(wwwOutBase+"/MergedJ/Images");
		final String getHour = args[0];
		final String modelRunString = getDate+"_"+getHour+"Z";

		wwwOutArchive.mkdirs();
		wwwOutLoops.mkdirs();
		wwwOutImages.mkdirs();
		memHRRR.mkdirs();
		memHRRRFocus.mkdirs();
		hrrrMP4.delete();
		focusMP4.delete();

		Thread io1a = new Thread(() -> { StumpJunk.runProcess("rm "+xml2Path.getPath()+"/*js2tmp*"); });
		Thread io1b = new Thread(() -> { StumpJunk.runProcess("mv "+xml2Path.getPath()+"/*HRRR_* "+memHRRR.getPath()); });
		Thread io1c = new Thread(() -> { StumpJunk.runProcess("mv "+xml2Path.getPath()+"/FOCUS* "+memHRRRFocus.getPath()); });
		Thread io1Pool[] = { io1a, io1b, io1c }; 
		for (Thread thread : io1Pool) { thread.start(); }
		for (int i = 0; i < io1Pool.length; i++) { try { io1Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

		StumpJunk.runProcess("rm "+wwwOutImages.getPath()+"/*.png");
		StumpJunk.runProcess("mogrify -format png -gravity center -crop "+stdRes+"+0+0 "+memHRRR.getPath()+"/*.png");
		StumpJunk.runProcess("cv "+memHRRR.getPath()+"/*.png "+wwwOutImages.getPath());

		Thread io2a = new Thread(() -> { StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+memHRRR.getPath()+" png"); });
		Thread io2b = new Thread(() -> { StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+memHRRRFocus.getPath()+" png"); });
		Thread io2Pool[] = { io2a, io2b }; 
		for (Thread thread : io2Pool) { thread.start(); }
		for (int i = 0; i < io2Pool.length; i++) { try { io2Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

		Thread io3a = new Thread(() -> { StumpJunk.runProcess("ffmpeg -threads 8 -r 10 -i "+memHRRR.getPath()+"/%05d.png -vcodec libx264 -pix_fmt yuv420p "+hrrrMP4.getPath()); });
		Thread io3b = new Thread(() -> { StumpJunk.runProcess("ffmpeg -threads 8 -r 10 -i "+memHRRRFocus.getPath()+"/%05d.png -vcodec libx264 -pix_fmt yuv420p "+focusMP4.getPath()); });
		Thread io3Pool[] = { io3a, io3b }; 
		for (Thread thread : io3Pool) { thread.start(); }
		for (int i = 0; i < io3Pool.length; i++) { try { io3Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

		if(getHour.equals("00") || getHour.equals("06") || getHour.equals("12") || getHour.equals("18")) {

			final File wwwOutImages4 = new File(wwwOutBase+"/MergedJ/Images4");
			final File memGFS = new File(xml2Path.getPath()+"/tmpic/GFS");
			final File memNAM = new File(xml2Path.getPath()+"/tmpic/NAM");
			final File namMP4 = new File(xml2Path.getPath()+"/NAM_Loop.mp4");
			final File gfsMP4 = new File(xml2Path.getPath()+"/GFS_Loop.mp4");

			memGFS.mkdirs();
			memNAM.mkdirs();
			wwwOutImages4.mkdirs();
			
			gfsMP4.delete();
			namMP4.delete();
			
			Thread io4a = new Thread(() -> { StumpJunk.runProcess("mv "+xml2Path.getPath()+"/*NAM_* "+memNAM.getPath()); });
			Thread io4b = new Thread(() -> { StumpJunk.runProcess("mv "+xml2Path.getPath()+"/*GFS_* "+memGFS.getPath()); });
			Thread io4Pool[] = { io4a, io4b }; 
			for (Thread thread : io4Pool) { thread.start(); }
			for (int i = 0; i < io4Pool.length; i++) { try { io4Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

			StumpJunk.runProcess("rm "+wwwOutImages4.getPath()+"/*.png");
			StumpJunk.runProcess("mogrify -format png -gravity center -crop "+stdRes+"+0+0 "+memNAM.getPath()+"/*.png");
			StumpJunk.runProcess("mogrify -format png -gravity center -crop "+stdRes+"+0+0 "+memGFS.getPath()+"/*.png");

			Thread io5a = new Thread(() -> { StumpJunk.runProcess("cp "+memNAM.getPath()+"/*.png "+wwwOutImages4.getPath()); });
			Thread io5b = new Thread(() -> { StumpJunk.runProcess("cp "+memGFS.getPath()+"/*.png "+wwwOutImages4.getPath()); });
			Thread io5Pool[] = { io5a, io5b }; 
			for (Thread thread : io5Pool) { thread.start(); }
			for (int i = 0; i < io5Pool.length; i++) { try { io5Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

			Thread io6a = new Thread(() -> { StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+memNAM.getPath()+" png"); });
			Thread io6b = new Thread(() -> { StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+memGFS.getPath()+" png"); });
			Thread io6Pool[] = { io6a, io6b }; 
			for (Thread thread : io6Pool) { thread.start(); }
			for (int i = 0; i < io6Pool.length; i++) { try { io6Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

			Thread io7a = new Thread(() -> { StumpJunk.runProcess("ffmpeg -threads 8 -r 10 -i "+memNAM.getPath()+"/%05d.png -vcodec libx264 -pix_fmt yuv420p "+namMP4.getPath()); });
			Thread io7b = new Thread(() -> { StumpJunk.runProcess("ffmpeg -threads 8 -r 10 -i "+memGFS.getPath()+"/%05d.png -vcodec libx264 -pix_fmt yuv420p "+gfsMP4.getPath()); });
			Thread io7Pool[] = { io7a, io7b }; 
			for (Thread thread : io7Pool) { thread.start(); }
			for (int i = 0; i < io7Pool.length; i++) { try { io7Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

			if(getHour.equals("00") || getHour.equals("12")) {

				final File memCMC = new File(xml2Path.getPath()+"/tmpic/CMC");	
				final File memHRWA = new File(xml2Path.getPath()+"/tmpic/HRWA");
				final File memHRWN = new File(xml2Path.getPath()+"/tmpic/HRWN");
				final File cmcMP4 = new File(xml2Path.getPath()+"/CMC_Loop.mp4");
				final File hrwaMP4 = new File(xml2Path.getPath()+"/HRWA_Loop.mp4");
				final File hrwnMP4 = new File(xml2Path.getPath()+"/HRWN_Loop.mp4");

				memCMC.mkdirs();
				memHRWA.mkdirs();
				memHRWN.mkdirs();
				
				cmcMP4.delete();
				hrwaMP4.delete();
				hrwnMP4.delete();

				Thread io8a = new Thread(() -> {  StumpJunk.runProcess("mv "+xml2Path.getPath()+"/*CMC_* "+memCMC.getPath()); });
				Thread io8b = new Thread(() -> { StumpJunk.runProcess("mv "+xml2Path.getPath()+"/*HRWA_* "+memHRWA.getPath()); });
				Thread io8c = new Thread(() -> { StumpJunk.runProcess("mv "+xml2Path.getPath()+"/*HRWN_* "+memHRWN.getPath()); });
				Thread io8Pool[] = { io8a, io8b, io8c }; 
				for (Thread thread : io8Pool) { thread.start(); }
				for (int i = 0; i < io8Pool.length; i++) { try { io8Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }
								
				StumpJunk.runProcess("mogrify -format png -gravity center -crop "+stdRes+"+0+0 "+memCMC.getPath()+"/*.png");
				StumpJunk.runProcess("mogrify -format png -gravity center -crop "+stdRes+"+0+0 "+memHRWA.getPath()+"/*.png");
				StumpJunk.runProcess("mogrify -format png -gravity center -crop "+stdRes+"+0+0 "+memHRWN.getPath()+"/*.png");
				
				Thread io9a = new Thread(() -> { StumpJunk.runProcess("cp "+memCMC.getPath()+"/*.png "+wwwOutImages4.getPath()); });
				Thread io9b = new Thread(() -> { StumpJunk.runProcess("cp "+memHRWA.getPath()+"/*.png "+wwwOutImages4.getPath()); });
				Thread io9c = new Thread(() -> { StumpJunk.runProcess("cp "+memHRWN.getPath()+"/*.png "+wwwOutImages4.getPath()); });
				Thread io9Pool[] = { io9a, io9b, io9c }; 
				for (Thread thread : io9Pool) { thread.start(); }
				for (int i = 0; i < io9Pool.length; i++) { try { io9Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }
			
				Thread io10a = new Thread(() -> { StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+memCMC.getPath()+" png"); });
				Thread io10b = new Thread(() -> { StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+memHRWA.getPath()+" png"); });
				Thread io10c = new Thread(() -> { StumpJunk.runProcess("bash "+helpers.getPath()+"/Sequence.sh "+memHRWN.getPath()+" png"); });
				Thread io10Pool[] = { io10a, io10b, io10c }; 
				for (Thread thread : io10Pool) { thread.start(); }
				for (int i = 0; i < io10Pool.length; i++) { try { io10Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }
							
				Thread io11a = new Thread(() -> { StumpJunk.runProcess("ffmpeg -threads 8 -r 10 -i "+memCMC.getPath()+"/%05d.png -vcodec libx264 -pix_fmt yuv420p "+cmcMP4.getPath()); });
				Thread io11b = new Thread(() -> { StumpJunk.runProcess("ffmpeg -threads 8 -r 10 -i "+memHRWA.getPath()+"/%05d.png -vcodec libx264 -pix_fmt yuv420p "+hrwaMP4.getPath()); });
				Thread io11c = new Thread(() -> { StumpJunk.runProcess("ffmpeg -threads 8 -r 10 -i "+memHRWN.getPath()+"/%05d.png -vcodec libx264 -pix_fmt yuv420p "+hrwnMP4.getPath()); });
				Thread io11Pool[] = { io11a, io11b, io11c }; 
				for (Thread thread : io11Pool) { thread.start(); }
				for (int i = 0; i < io11Pool.length; i++) { try { io11Pool[i].join(); } catch (InterruptedException nx) { nx.printStackTrace(); } }

			}

		}

		StumpJunk.runProcess("rm "+wwwOutLoops.getPath()+"/*");
		StumpJunk.runProcess("rm "+xml2Path.getPath()+"/*.png");
		StumpJunk.runProcess("mv "+xml2Path.getPath()+"/*.mp4 "+wwwOutLoops.getPath());
		StumpJunk.runProcess("zip -9rv "+wwwOutArchive.getPath()+"/"+modelRunString+".zip "+wwwOutLoops.getPath()+"/*.mp4");
		StumpJunk.runProcess("chown www-data "+wwwOutArchive.getPath()+"/"+modelRunString+".zip");
		StumpJunk.runProcess("(ls "+wwwOutArchive.getPath()+"/*.zip -t | head -n 12; ls "+wwwOutArchive.getPath()+"/*.zip)|sort|uniq -u|xargs rm");

	}

}
