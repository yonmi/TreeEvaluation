package experiences.pr2020;

import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map.Entry;

import evaluation.bricks.Eval;
import evaluation.bricks.Eval.EvalType;
import evaluation.datastructure.STree;
import evaluation.datastructure.SegReference;
import standard.sequential.BPT;
import ui.ImFrame;
import utils.ImTool;
import utils.Log;

/**
 * For Pattern Recognition review. \n
 * 
 * First experiment on the evaluation of the quality of a BPT structure.
 * 
 *
 */
public class Xp1 {

	public static void main(String[] args) {
		
		Log.show = true;
		
		/* Create a tree */
		//String path = "xp//examples//3x3.png";
		//String path = "xp//examples//img-100-100.png";
		String path = "xp//PR2020//DATA//weizmann1obj//img//img_3083_modif.png";
		BufferedImage image = ImTool.read(path);
		ImTool.show(image, ImFrame.IMAGE_STD_SIZE);
		
		String presegPath = "xp//PR2020//DATA//weizmann1obj//slic//img_3083_modif.tif";
		BufferedImage presegImg = ImTool.read(presegPath);
		
		BPT tree = new BPT(image);
		tree.setPreSegImage(presegImg);
		tree.grow();
		
		/* Extract the segments of reference from a ground truth image */
		//String gtImgPath = "xp//examples//img-100-100-gt.png";
		String gtImgPath = "xp//PR2020//DATA//weizmann1obj//groundTruth//img_3083_modif.png";
		double alpha = 0.0;
		boolean generateImgCrops = true; // creates and saves a crop for each segment of reference
		TreeMap<Integer, SegReference> segReferences = SegReference.extractSegmentsOfReference(gtImgPath, alpha, generateImgCrops);
		
		/* Visualizing the segments if wanted */
		SegReference.showBoundingBoxes(segReferences, image, ImFrame.IMAGE_STD_SIZE, "Bounding boxes"); // not necessary

		/* Extract sub trees */
		Log.println("SubTree", "Extractions");
		STree extractedSubTrees[] = new STree[segReferences.size()];
		int gti = 0;
		for(Entry<Integer, SegReference> entry: segReferences.entrySet()){

			/* each segment of reference */
			SegReference gt = entry.getValue();			

			/* Create the subtree */
			STree st = new STree(gti, tree, gt);
			st.index = gti++;
			extractedSubTrees[st.index] = st;
		}
		Log.println("SubTree", "Extraction Done!");
		
		/* Evaluate the sub trees */
		ArrayList<Eval> evalRes = new ArrayList<Eval>();
		for(int i = 0; i < extractedSubTrees.length; ++i) {
			
			STree st = extractedSubTrees[i];
			evalRes.add(st.eval(EvalType.INTRINSIC));
		}

		/* Saving the results into a file */
		String resPath = "xp//PR2020//DATA//weizmann1obj//res//img_3083_modif.csv";
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
					+ "sizePureNodes;"
					+ "Np");
			
			System.out.println("\nSaving the results into file: \""+ resPath +"\"");
			for(Eval res: evalRes) {

				res.saveEvalResults(writer, "tree-1",false);
			}
			writer.close();
			
		}catch(Exception e) {

			e.printStackTrace();
		}
		
		/* Print the intrinsic evaluations results */
		System.out.println("\nIntrinsic evaluation results: ");
		for(Eval res: evalRes) {
			
			res.printResults();
		}
	}
}
