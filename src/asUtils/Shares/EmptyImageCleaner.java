/*
by Anthony Stump
Created: 1 Nov 2017
*/

package asUtils.Shares;

import java.io.*;


public class EmptyImageCleaner {

	public static void main(String args[]) {

		final File imagePath = new File(args[0]);
		File[] imageListing = imagePath.listFiles();

		if (imageListing != null) {
			for (File image : imageListing) {

				String imageQuality = "";

				if(image.length() > 1024) { 
					imageQuality = "Good Image!";
				} else {
					imageQuality = "Empty Image!";
					image.delete();
					System.out.println("DELETING: " + image.getPath());
				}

			}

		} else {
			System.out.println(imagePath.getPath()+" is not a folder!");
		}

	}

}
