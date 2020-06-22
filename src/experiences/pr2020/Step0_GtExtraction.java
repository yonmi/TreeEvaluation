package experiences.pr2020;

import java.io.File;

import evaluation.datastructure.SegReference;

public class Step0_GtExtraction {

	public static void main(String[] args) {

		System.out.println("[Step0] Starging GT extraxtions!");

		//String dataName = "weizmann1obj";
		String dataName = "grabcut";
		//String dataName = "voc2012";
		String mainDir = "xp//PR2020//DATA//"+ dataName;
		String gtDir = mainDir +"//groundTruth";

		boolean success = false;
		try {

			File folder = new File(gtDir);
			File[] listOfFiles = folder.listFiles();
			int nbImages = listOfFiles.length;

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					
					File file = listOfFiles[i];
					String gtName = file.getName();
					System.out.println("\n"+ (i+1) +"/"+ nbImages +" "+ gtName +" ... ");

					/* Extraction */
					extractGT(file.getPath());
				}
			}
			
			success = true;

		}catch(Exception e) {
			
			e.printStackTrace();
		}
		
		if(success)	System.out.println("[Step0] All extractions finished!");
	}
	
	public static void extractGT(String gtPath) {

		double alpha = 0; // for the distance map and the mu map. 
		boolean savingCrops = true;

		SegReference.extractSegmentsOfReference(gtPath, alpha, savingCrops);
		SegReference.nbSegs = 0;

		System.out.println("| Ok");
		System.gc();
	}
}
