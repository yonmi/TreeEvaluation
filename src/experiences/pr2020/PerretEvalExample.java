package experiences.pr2020;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import datastructure.Tree;
import evaluation.PerretEval;
import evaluation.bricks.Eval;
import evaluation.bricks.Eval.EvalType;
import evaluation.datastructure.STree;
import evaluation.datastructure.SegReference;
import standard.sequential.BPT;
import ui.ImFrame;
import utils.ImTool;
import utils.Log;

/**
 * Object detection measure from BPT
 * 
 * Implementation from B. Perret, J. Cousty, S.J.F. Guimarães, D.S. Maia. Evaluation of hierarchical watersheds. 
 * IEEE Transactions on Image Processing, 27(4): 1676-1688, 2018. doi:10.1109/TIP.2017.2779604. 
 * 
 * @author ckurtz
 *
 */

public class PerretEvalExample {

	static int currentBptToTreat = 1;
	
	public static void main(String[] args) {

		//String dataName = "weizmann1obj";
		String dataName = "grabcut";
		//String dataName = "voc2012";
		String mainDir = "xp//PR2020//DATA//"+ dataName;
		String imgDir = mainDir +"//img";
		String gtDir = mainDir +"//groundTruth";
		String markersDir = mainDir +"//gtEroded";
		
		boolean success = false;
		try {
			
			System.out.println("[Step1] Starting Perret Eval!");
			
			File folder = new File(imgDir);
			File[] listOfFiles = folder.listFiles();
			int nbImages = listOfFiles.length;

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					
					File file = listOfFiles[i];

					String imgName = file.getName();
					String gtPath = gtDir +"//segref_("+ imgName +")";
					
					System.out.println((i+1) +"/"+ nbImages +" "+ imgName +" ... ");
					extractAndEval(mainDir, imgName, gtPath, markersDir, nbImages);
				}
			}

			success = true;

		}catch(Exception e) {

			e.printStackTrace();
		}

		if(success)	System.out.println("[Step1] All Perret evaluations finished!");
	}
	
	public static void extractAndEval(String mainDir, String imgName, String gtPath, String markersPath, int nbImages) {
		
		String orandomName = "orandom";
		String ominmaxName = "ominmax";
		String omseName = "omse";
		String owsdmName = "owsdm";
		String ocolcontminmaxName = "ocolcontminmax";
		String ocolcontmseName = "ocolcontmse";
		String ocolcontwsdmName = "ocolcontwsdm";
		String omselabName ="omselab";

		Set<String> metricsName = new HashSet<String>();
		metricsName.add(orandomName);
		metricsName.add(ominmaxName);
		metricsName.add(omseName);
		metricsName.add(owsdmName);
		metricsName.add(ocolcontminmaxName);
		metricsName.add(ocolcontmseName);
		metricsName.add(ocolcontwsdmName);
		metricsName.add(omselabName);
		
		//Range of the erosion level Min-Max
		int sizesMarkersRangeMinMax[] = {0,21};
		
		int nbBpts = nbImages * metricsName.size();

		/* Get segments of reference */
		boolean visu = false;
		TreeMap<Integer, SegReference> segRefs = SegReference.getSegReferences(gtPath, 0.0, visu);

		String resDir = mainDir +"//resPerret";
		new File(resDir).mkdir();
		String resPath1 = resDir +"//"+ imgName +"_FM.csv";
		String resPath2 = resDir +"//"+ imgName +"_CAR.csv";

		PrintWriter writer1;
		PrintWriter writer2;
		
		try {

			writer1 = new PrintWriter(resPath1, "UTF-8");
			writer2 = new PrintWriter(resPath2, "UTF-8");
			
			String header1=";";
			for(int markerSize=sizesMarkersRangeMinMax[0];markerSize<=sizesMarkersRangeMinMax[1];markerSize++) {
				header1+="FM_ErosionLevel("+markerSize+")";
				if(markerSize<sizesMarkersRangeMinMax[1])header1+=";";
			}
			writer1.println(header1);
			
			String header2=";";
			for(int markerSize=sizesMarkersRangeMinMax[0];markerSize<=sizesMarkersRangeMinMax[1];markerSize++) {
				header2+="Cardinality_ErosionLevel("+markerSize+")";
				if(markerSize<sizesMarkersRangeMinMax[1])header2+=";";
			}
			writer2.println(header2);
			
			//For each BPT
			for(String nameOfMetric: metricsName) {

				writer1.print(nameOfMetric+";");
				writer2.print(nameOfMetric+";");
				
				String bptName = FilenameUtils.removeExtension(imgName);
				String bptDir = mainDir +"//bpt//"+ nameOfMetric;
				String bptPath = bptDir +"//"+ bptName +".h5";

				/* Load bpt */
				Log.show = false;
				Tree bpt = new BPT(bptPath);
				
				System.out.println("Number of segments of reference = "+segRefs.size());
				
				//For each Segment of reference
				for(Entry<Integer, SegReference> entry: segRefs.entrySet()){

					Log.println("PerretEval", "("+ currentBptToTreat +"/"+ nbBpts +")");
					currentBptToTreat++;
					
					/* each segment of reference */
					SegReference gt = entry.getValue();			

					//For each size of eroded image
					for(int markerSize=sizesMarkersRangeMinMax[0];markerSize<=sizesMarkersRangeMinMax[1];markerSize++) {
						
						//Load marker Image
						String imageMarkerPathPath = markersPath+"//"+bptName+"_"+markerSize+".png";
						BufferedImage markersImage= ImTool.read(imageMarkerPathPath);
						
						//Segment the tree from Perret
						BufferedImage segmentedImage= PerretEval.segment(bpt,markersImage);
						//ImTool.show(segmentedImage, ImFrame.IMAGE_STD_SIZE,"segmentedImage MarkerLevel("+markerSize+")");
						
						//Reconstruction of GT for FM computation
						BufferedImage gtImage = new BufferedImage(markersImage.getWidth(), markersImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
						for(Point p:gt.gtPoints) {
							gtImage.setRGB(p.x, p.y, ImTool.getRGBValueFrom(255 , 255, 255));
						}
						//ImTool.show(gtImage, ImFrame.IMAGE_STD_SIZE, "GT image");
						
						//Compute the Fmeasure
						double fm=0.0;
						int cardinality = 0;
						if(segmentedImage!=null) {
							fm=PerretEval.FMeasure(segmentedImage,gtImage);
							cardinality=PerretEval.computeBestCutCardinality(bpt);
						}
						
						System.out.println("Erosion Level = "+markerSize+" || FM = "+STree.decimal3(fm)+" || Cardinality = "+cardinality);
						writer1.print(STree.decimal3(fm)+"");
						writer2.print(cardinality+"");
						
						if(markerSize<sizesMarkersRangeMinMax[1]) {
							writer1.print(";");
							writer2.print(";");
						}
					}
					writer1.println("");
					writer2.println("");
				
				}
				System.out.println("|"+ nameOfMetric +" OK\n");
			}		
		
			writer1.close();
			writer2.close();
			System.gc();

		}catch(Exception e) {

			e.printStackTrace();
		}
	}
}
