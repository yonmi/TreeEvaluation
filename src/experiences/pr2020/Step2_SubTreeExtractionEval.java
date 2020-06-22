package experiences.pr2020;

import java.io.File;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import datastructure.Tree;
import evaluation.bricks.Eval;
import evaluation.bricks.Eval.EvalType;
import evaluation.datastructure.STree;
import evaluation.datastructure.SegReference;
import standard.sequential.BPT;
import utils.Log;

public class Step2_SubTreeExtractionEval {

	static int currentBptToTreat = 1;
	
	public static void main(String[] args) {

		//String dataName = "weizmann1obj";
		String dataName = "grabcut";
		//String dataName = "voc2012";
		String mainDir = "xp//PR2020//DATA//"+ dataName;
		String imgDir = mainDir +"//img";
		String gtDir = mainDir +"//groundTruth";

//		EvalType evalType = EvalType.INTRINSIC;
//		EvalType evalType = EvalType.PONT_TUSET;
		EvalType evalType = EvalType.EXTRINSIC;
		
		boolean success = false;
		try {
			
			System.out.println("[Step2] Starging SubTree extraxtions!");
			
			File folder = new File(imgDir);
			File[] listOfFiles = folder.listFiles();
			int nbImages = listOfFiles.length;

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					
					File file = listOfFiles[i];

					String imgName = file.getName();
					String gtPath = gtDir +"//segref_("+ imgName +")";
					
					System.out.println((i+1) +"/"+ nbImages +" "+ imgName +" ... ");
					extractAndEval(mainDir, imgName, gtPath, nbImages, evalType);
				}
			}

			success = true;

		}catch(Exception e) {

			e.printStackTrace();
		}

		if(success)	System.out.println("[Step2] All SubTree extractions and evaluations finished!");
	}
	
	public static void extractAndEval(String mainDir, String imgName, String gtPath, int nbImages, EvalType evalType) {
		
		int gti = 0;
		
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
		//metricsName.add(ominmaxName);
		//metricsName.add(omseName);
		//metricsName.add(owsdmName);
		//metricsName.add(ocolcontminmaxName);
		//metricsName.add(ocolcontmseName);
		//metricsName.add(ocolcontwsdmName);
		//metricsName.add(omselabName);
		
		int nbBpts = nbImages * metricsName.size();

		/* Get segments of reference */
		boolean visu = false;
		TreeMap<Integer, SegReference> segRefs = SegReference.getSegReferences(gtPath, 0.0, visu);

		String resDir = mainDir +"//res";
		new File(resDir).mkdir();
		resDir = resDir +"//"+ evalType.toString();
		new File(resDir).mkdir();
		String resPath = resDir +"//"+ FilenameUtils.removeExtension(imgName) +".csv";

		PrintWriter writer = null;
		try {

			if(evalType.equals(EvalType.INTRINSIC)) {
				
				writer = new PrintWriter(resPath, "UTF-8");
				writer.println(";"
						+ " granularity; "
						+ " discordance;"
						+ " li;"
						+ " lp;"
						+ " ui;"
						+ " bpp;"
						+ " bii;"
						+ " bpi;"
						+ ";"
						+ " C1;"
						+ " C2;"
						+ " Q1;"
						+ " Q2;"
						+ " Q3;"
						+ " Q4;"
						+ " Q5;"
						+ ";"
						+ "sizeRoot;"
						+ "sizeGt;"
						+ "sizeLeaves;"
						+ "sizePureLeaves;"
						+ "sizePureNodes;"
						+ "Np");
				
			}else if(evalType.equals(EvalType.PONT_TUSET)) {
				
				writer = new PrintWriter(resPath, "UTF-8");
				writer.println(";Size gt;");
				
			}

			for(String nameOfMetric: metricsName) {
				
				if(evalType.equals(EvalType.EXTRINSIC)) {
					
					String newDirPath = resDir +"//"+ FilenameUtils.removeExtension(imgName);
					new File(newDirPath).mkdir();
					String newResPath = newDirPath +"//"+ FilenameUtils.removeExtension(imgName) +"_"+ nameOfMetric +".csv";
					writer = new PrintWriter(newResPath, "UTF-8");
				}

				String bptName = FilenameUtils.removeExtension(imgName);
				String bptDir = mainDir +"//bpt//"+ nameOfMetric;
				String bptPath = bptDir +"//"+ bptName +".h5";

				String streeDirParent = mainDir +"//stree";
				new File(streeDirParent).mkdir();
				String streeDirImg = streeDirParent +"//"+ nameOfMetric;
				new File(streeDirImg).mkdir();
				String streeDir = streeDirImg +"//"+ bptName;
				new File(streeDir);

				/* Load bpt */
				Log.show = true;
				Tree bpt = new BPT(bptPath);

				/* Extract sub trees */
				Log.println("SubTree", "Extractions");
				for(Entry<Integer, SegReference> entry: segRefs.entrySet()){

					Log.println("SubTree", "("+ currentBptToTreat +"/"+ nbBpts +")");
					currentBptToTreat++;
					
					/* each segment of reference */
					SegReference gt = entry.getValue();			

					/* Create the subtree */
					STree st = new STree(gti, bpt, gt);
					gti++;
					//st.index = gti++;

					/* Creating some image illustrations and save them */
					int width = gt.imgGtWidth;
					int height = gt.imgGtHeight;
					st.illustrateParams(streeDir, bptName, width, height);

					/* Evaluate the subtree */
					Eval res = st.eval(evalType);
					res.saveEvalResults(writer, nameOfMetric, false);
					res.printResults();
				}
				Log.println("SubTree", "Extraction Done!");

				System.out.println("|"+ nameOfMetric +" OK\n");
			}		
			writer.close();
			System.gc();

		}catch(Exception e) {

			e.printStackTrace();
		}
	}
}
