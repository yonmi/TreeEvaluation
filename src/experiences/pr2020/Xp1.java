/****************************************************************************
* Copyright AGAT-Team (2014)						       
* 									    
* Contributors:								
* J.F. Randrianasoa							    
* K. Kurtz								    
* E. Desjardin								    
* N. Passat								    
* 									    
* This software is a computer program whose purpose is to [describe	    
* functionalities and technical features of your software].		    
* 									    
* This software is governed by the CeCILL-B license under French law and    
* abiding by the rules of distribution of free software.  You can  use,     
* modify and/ or redistribute the software under the terms of the CeCILL-B  
* license as circulated by CEA, CNRS and INRIA at the following URL	    
* "http://www.cecill.info". 						    
* 									    
* As a counterpart to the access to the source code and  rights to copy,    
* modify and redistribute granted by the license, users are provided only   
* with a limited warranty  and the software's author,  the holder of the    
* economic rights,  and the successive licensors  have only  limited	    
* liability. 								    
* 									    
* In this respect, the user's attention is drawn to the risks associated    
* with loading,  using,  modifying and/or developing or reproducing the     
* software by the user in light of its specific status of free software,    
* that may mean  that it is complicated to manipulate,  and  that  also	   
* therefore means  that it is reserved for developers  and  experienced     
* professionals having in-depth computer knowledge. Users are therefore     
* encouraged to load and test the software's suitability as regards their   
* requirements in conditions enabling the security of their systems and/or  
* data to be ensured and,  more generally, to use and operate it in the     
* same conditions as regards security. 					    
*								            
* The fact that you are presently reading this means that you have had	    
* knowledge of the CeCILL-B license and that you accept its terms.          
* 									   		
* The full license is in the file LICENSE, distributed with this software.  
*****************************************************************************/

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
