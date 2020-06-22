package experiences.pr2020;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import datastructure.Tree;
import evaluation.bricks.Eval;
import evaluation.bricks.Eval.EvalType;
import evaluation.datastructure.STree;
import evaluation.datastructure.SegReference;
import standard.sequential.BPT;
import utils.Log;

public class IntrinsicEvalExample {

	public static void main(String[] args) {

		String dataName = "weizmann1obj";
		//String dataName = "grabcut";
		//String dataName = "voc2012";
		String mainDir = "xp//PR2020//DATA//"+ dataName;
		String gtDir = mainDir +"//groundTruth";

		int nbImages = 1;
		int iTest = 3083;
		//int iTest = 4730;

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

		boolean success = false;
		try {

			int gti = 0;

			System.out.println("[Step2] Starging SubTree extraxtions!");
			for(int i = 0; i < nbImages; ++i) {


				String imgName = "img_"+ iTest +"_modif.png";
				String gtPath = gtDir +"//segref_("+ imgName +")";

				/* Get segments of reference */
				boolean visu = false;
				TreeMap<Integer, SegReference> segRefs = SegReference.getSegReferences(gtPath, 0.0, visu);

				

				String resPath = "xp//PR2020//Data//weizmann1obj//res//"+ FilenameUtils.removeExtension(imgName) +"_IN.csv";
				PrintWriter writer;
				try {

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
							+ "sizePureNodes"
							+ "Np");



					System.out.println((i+1) +"/"+ nbImages +" "+ imgName +" ... ");
					for(String nameOfMetric: metricsName) {

						String bptName = "img_"+ iTest +"_modif";
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

							/* each segment of reference */
							SegReference gt = entry.getValue();			

							/* Create the subtree */
							STree st = new STree(gti, bpt, gt);
							st.index = gti++;
							
							/* Creating some image illustrations and save them */
							int width = gt.imgGtWidth;
							int height = gt.imgGtHeight;
							st.illustrateParams(streeDir, bptName, width, height);

							/* Evaluate the subtree */
							Eval res = st.eval(EvalType.INTRINSIC);
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

			success = true;

		}catch(Exception e) {

			e.printStackTrace();
		}

		if(success)	System.out.println("[Step2] All SubTree extractions and intrinsic evaluations finished!");
	}
}
