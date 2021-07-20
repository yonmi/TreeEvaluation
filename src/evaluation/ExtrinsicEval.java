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

package evaluation;

import java.awt.Point;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import datastructure.Node;
import evaluation.bricks.Eval;
import evaluation.bricks.EvalInterface;
import evaluation.datastructure.STree;
import evaluation.datastructure.STree.TypeN;
import utils.Log;

/**
 * Beyond the intrinsic quality criteria provided by its very structure, the relevance of a BPT
 * is also related to its ability to provide good segmentation results.
 * This notion of goodness is both application-dependent and user-dependent. In
 * particular, evaluating the quality of a BPT requires not only to define a ground-
 * truth, but also a metric for comparing this ground-truth to the actual segmentation
 * results obtained from the BPT. 
 * 
 * The core idea is to find a cut C b ⊂ N within the BPT T forming a segmented region S b = ∪ C b ⊂ Ω 
 * that best matches the ground-truth example G according to the similarity metric Λ chosen by the user.
 */
public class ExtrinsicEval extends Eval implements EvalInterface {

	public String context = "EXTRINSIC EVALUATION";
	
	public STree subTree;
	public ArrayList<Point> gtPoints;
	public Node root;
	
	public float[] diceTab;
	
	@Override
	public void start(STree subTree) {
	
		this.subTree = subTree;
		this.gtPoints = this.subTree.gt.getPoints();
		this.root = this.subTree.root;
		
		this.pruneSubTree();
		this.init();
		this.trajectory(100, 10000, 50);
		
		Log.println(this.context, "Done");
	}
	
	private void trajectory(int sizeTrajectory, int nbTrajectory, int sizeMax) {
		
		this.diceTab = new float[sizeMax+1];
		
		ArrayList<Node> defNode = new ArrayList<Node>();
		defNode.addAll(this.subTree.lpL);
		defNode.addAll(this.subTree.liL);
		
		for(Entry<Node, TypeN> entry: this.subTree.regedit.entrySet()) {
			
			Node n = entry.getKey();
			TypeN typeN = entry.getValue();
			
			if(typeN.equals(TypeN.UI)) {
				
				defNode.add(n);
			}
		}
		
		for(int k = 1; k <= nbTrajectory; ++k) {
			
			this.initSegSeed();

			this.treat(this.root);
			float diceScore = this.dice();
			int sizeCut = this.sizeCut();
			
			if(sizeCut < this.diceTab.length) {
				
				if(this.diceTab[sizeCut] < diceScore) {
					
					this.diceTab[sizeCut] = diceScore;
				}
			}
//			System.out.println("dice: "+ diceScore +" sizeCut: "+ sizeCut);
			
			for(int i = 1; i <= sizeTrajectory; ++i) {
				
				int randomI = ThreadLocalRandom.current().nextInt(0, defNode.size());
				Node n = defNode.get(randomI);
				n.s = !n.s;
				n.optRacine = !n.optRacine;

				this.treat(this.root);
				float diceScore2 = this.dice();
				int sizeCut2 = this.sizeCut();
				
				if(sizeCut2 < this.diceTab.length) {
					
					if(this.diceTab[sizeCut2] < diceScore2) {

						this.diceTab[sizeCut2] = diceScore2;
					}
				}
//				System.out.println("dice: "+ diceScore2 +" sizeCut: "+ sizeCut2);
			}	
			
			System.gc();
		}
	}

	private int sizeCut() {

		return this.root.d[1][0];
	}

	private float dice() {

		return  ((float) (2.0 * this.root.d[1][1]) / (float) (this.root.d[1][2] + this.gtPoints.size()));
	}

	private void treat(Node n) {

		if(!this.subTree.lpL.contains(n) && !this.subTree.liL.contains(n)) {
			
			TypeN typeN = this.subTree.regedit.get(n);
			
			if(typeN.equals(TypeN.UI)) {
				
				if(n.s == false) {
					
					Node child = n.leftNode;
					if(!subTree.regedit.containsKey(child) && !subTree.liL.contains(child) && !subTree.lpL.contains(child)) {
						
						child = n.rightNode;
					}
					
					this.treat(child);
					n.optRacine = false;
					
					n.d[1][0] = child.d[1][0];
					n.d[1][1] = child.d[1][1];
					n.d[1][2] = child.d[1][2];
				}
			}else {
				
				n.s = false;
				n.optRacine = false;
				
				Node left = n.leftNode;
				Node right = n.rightNode;
				this.treat(left);
				this.treat(right);
				
				int el = 0;
				int er = 0;
				
				if(left.s == false) {
					
					el = 1;
				}
				
				if(right.s == false) {
					
					er = 1;
				}
				
				n.d[1][0] = left.d[el][0] + right.d[er][0];
				n.d[1][1] = left.d[el][1] + right.d[er][1];
				n.d[1][2] = left.d[el][2] + right.d[er][2];
				
				if(left.optRacine == true && right.optRacine == true) {
					
					n.d[1][0] = 1;
					n.optRacine = true;
				}
			}
		}
	}

	private void initSegSeed() {

		for(Node lp: this.subTree.lpL) {
			
			lp.s = true;
			lp.optRacine = true;
			
//			System.out.println("lp ligne0: "+ lp.d[0][0] + " "+ lp.d[0][1] +" "+ lp.d[0][2]);
		}
		
		for(Node li: this.subTree.liL) {
	
			int nbvp = li.d[0][1];
			int nbfp = li.d[0][2] - li.d[0][1];
			if(nbvp > nbfp) {
				
				li.s = true;
				li.optRacine = true;
				
			}else {
				
				li.s = false;
				li.optRacine = false;
			}

//			System.out.println("li ligne0: "+ li.d[0][0] + " "+ li.d[0][1] +" "+ li.d[0][2]);
		}
		
		for(Entry<Node, TypeN> entry: this.subTree.regedit.entrySet()) {
			
			Node n = entry.getKey();
			TypeN typeN = entry.getValue();
			
			if(typeN.equals(TypeN.UI)) {
					
				int nbvp = n.d[0][1];
				int nbfp = n.d[0][2] - n.d[0][1];
				if(nbvp > nbfp) {
					
					n.s = true;
					n.optRacine = true;
				}else {
					
					n.s = false;
					n.optRacine = false;
				}
			}
			
//			System.out.println("ui ligne0: "+ n.d[0][0] + " "+ n.d[0][1] +" "+ n.d[0][2]);

		}
	}

	private void init() {

		for(Node lp: this.subTree.lpL) {
			
			lp.d[0][0] = 1;
			lp.d[0][1] = lp.getSize();
			lp.d[0][2] = lp.getSize();
		}
		
		for(Node li: this.subTree.liL) {
			
			ArrayList<Point> lPoints = li.getPixels();
			
			int nbvp = 0;
			for(int i = 0; i < lPoints.size(); ++i) {
				
				Point currentPoint = lPoints.get(i);
				if(this.gtPoints.contains(currentPoint)) {
					
					nbvp++;
				}
			}
			
			li.d[0][0] = 1;
			li.d[0][1] = nbvp;
			li.d[0][2] = li.getSize();
		}
		
		for(Entry<Node, TypeN> entry: this.subTree.regedit.entrySet()) {
			
			Node n = entry.getKey();
			TypeN typeN = entry.getValue();
			
			if(typeN.equals(TypeN.UI)) {
					
				ArrayList<Point> nPoints = n.getPixels();

				int nbvp = 0;
				for(int i = 0; i < nPoints.size(); ++i) {

					Point currentPoint = nPoints.get(i);
					if(this.gtPoints.contains(currentPoint)) {

						nbvp++;
					}
				}

				n.d[0][0] = 1;
				n.d[0][1] = nbvp;
				n.d[0][2] = n.getSize();
			}
		}
	}

	private void pruneSubTree() {

		ArrayList<Node> tmp = new ArrayList<Node>();
		
		for(Node l: this.subTree.lpL) {
			
			Node parent = l.father;
			
			if(parent != null) {
				
				TypeN typeNParent = this.subTree.regedit.get(parent);
				if(typeNParent.equals(TypeN.BPP)){
					
					tmp.add(l);
				}
			}
		}
		this.subTree.lpL.removeAll(tmp);
		
//		tmp.clear();
		for(Entry<Node, TypeN> entry: this.subTree.regedit.entrySet()) {
			
			Node n = entry.getKey();
			TypeN typeN = entry.getValue();
			Node parent = n.father;
			
			if(parent != null && !n.equals(this.root)) {
				
				TypeN typeNParent = this.subTree.regedit.get(parent);
//				System.out.println("parent: "+ parent.name +" found: "+ this.subTree.regedit.containsKey(parent) +" type:"+ typeNParent);
				if(typeNParent.equals(TypeN.BPP)){
					
					tmp.add(n);
					
				}else if(typeN.equals(TypeN.BPP)){
					
					tmp.add(n);
					this.subTree.lpL.add(n);
				}
			}
		}
		
		for(Node n: tmp) {
			
			this.subTree.regedit.remove(n);
		}		
	}
	
	@Override
	public void printResults()
	{
		for(int i = 0; i < this.diceTab.length; ++i) {
			
			System.out.println("taille: "+ i +" dice: "+ this.diceTab[i]);
		}
	}
	
	@Override
	public void saveEvalResults(PrintWriter writer, String rowName, boolean showTitle) {
		
		for(int i = 0; i < this.diceTab.length; ++i) {
			
			writer.println("("+ i +",;"+ this.diceTab[i] +";)");
		}
		writer.close();
	}
}
