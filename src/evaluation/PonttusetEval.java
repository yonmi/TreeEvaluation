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

import datastructure.Node;
import datastructure.Node.TypeOfNode;
import evaluation.bricks.Eval;
import evaluation.bricks.EvalInterface;
import evaluation.datastructure.STree;
import evaluation.datastructure.STree.TypeN;
import utils.Log;


public class PonttusetEval extends Eval implements EvalInterface {

	public String context = "PONT-TUSET EVALUATION";
	
	public Node root;
	public STree subTree;
	
	@Override
	public void start(STree subTree) {
	
		this.root = subTree.root;
		this.subTree = subTree;
		this.treat(this.root, subTree);
		
		Log.println(this.context, "Done");
	}
	
	public void treat(Node n, STree subTree) {
		
		int vectorLength = subTree.getLi() + subTree.getLp() + 10;
		n.vector = new Integer[vectorLength]; 
		
		if(n.type.equals(TypeOfNode.LEAF)) {
			
			n.optRacine = true;
			
			n.vector[0] = new Integer(0);
			
			ArrayList<Point> gtPoints = subTree.gt.getPoints();
			ArrayList<Point> nPoints = n.getPixels();
			
			int value = 0;
			for(Point p: nPoints) {
				
				if(gtPoints.contains(p)) {
					
					value++;
					
				}else {
					
					value--;
				}
			}
			
			n.vector[1] = new Integer(value);
			
		}else {
			
			TypeN typeN = subTree.regedit.get(n);
			if(typeN.equals(TypeN.UI)) { /* Unaire */
				
				Node child = n.leftNode;
				Node outChild = n.rightNode;
				if(!subTree.regedit.containsKey(child) && !subTree.liL.contains(child) && !subTree.lpL.contains(child)) {
					
					child = n.rightNode;
					outChild = n.leftNode;
				}
				
				treat(child, subTree);
				n.optRacine = false;
				
				n.vector[0] = 0;
				n.vector[1] = child.vector[1];
				
				int i = 1;
				while(i < child.vector.length && child.vector[i] != null) {
					
					if((i+1) < n.vector.length) {
						
						if(child.vector[i+1] != null){
							
							if(child.vector[i] - outChild.getSize() > child.vector[i+1]) {
								
								n.vector[i+1] = child.vector[i] - outChild.getSize();
								
							}else {
								
								n.vector[i+1] = child.vector[i+1];						
							}
						}else {
							
							n.vector[i+1] = child.vector[i] - outChild.getSize();
						}
					}
					
					i++;
				}
				
			}else { /* Binaires */
				
				Node left = n.leftNode;
				Node right = n.rightNode;
				
				treat(left, subTree);
				treat(right, subTree);
				
				int p = n.vector.length - 1;
				n.vector[0] = new Integer(0);
				
				if(left.optRacine == false || right.optRacine == false) {
					
					n.optRacine = false;
					
					for(int i = 1; i <= 2; ++i) {

						Integer valMax = null;
						Integer valRight = right.vector[i];
						if(valRight != null) {
							
							valMax = left.vector[0] + valRight;
						}
						
						for(int j = 1; j <= i; ++j) {
							
							Integer currentVal = null;
							Integer leftVal = left.vector[j];
							Integer rightVal = right.vector[i-j];
							
							if(leftVal != null && rightVal != null) {
								
								currentVal = leftVal + rightVal;

								if(valMax == null) {
									
									valMax = currentVal;									
								}else {
									
									if(currentVal != null && currentVal > valMax) {
										
										valMax = currentVal;
									}									
								}
							}
						}
						
						n.vector[i] = valMax;
					}
					
				}else {
					
					/* Coupes de taille 1 */
					int valMax01 = left.vector[0] + right.vector[1];
					int valMax11 = left.vector[1] + right.vector[1];
					int valMax10 = left.vector[1] + right.vector[0];
					
					if(valMax11 > valMax01 && valMax11 > valMax10) {
						
						n.optRacine = true;
						n.vector[1] = valMax11;
						
					}else if(valMax01 > valMax10) {
						
						n.optRacine = false;
						n.vector[1] = valMax01;
						
					}else {
						
						n.optRacine = false;
						n.vector[1] = valMax10;
					}
					
					if(n.vector[1] == null) {
						
						System.out.println("opt racine: vrai vrai");
					}
					
					/* Coupes de taille 2 */
					Integer valMax20 = null;
					Integer leftVal = left.vector[2];
					if(leftVal != null) {

						valMax20 = leftVal + right.vector[0];
					}

					Integer valMax02 = null;
					Integer rightVal = right.vector[2];
					if(rightVal != null) {

						valMax02 = left.vector[0] + rightVal;
					}

					n.vector[2] = null;
					
					if(valMax02 == null) {
						
						n.vector[2] = valMax20;
						
					}else if(valMax20 == null) {
						
						n.vector[2] = valMax02;
						
					}else {
						
						if(valMax02 > valMax20) {
							
							n.vector[2] = valMax02;
							
						}else {
							
							n.vector[2] = valMax20;						
						}						
					}
				}
				
				for(int i = 3; i <= p; ++i) {

					Integer valMax = null;
					if(right.vector[i] != null) {
						
						valMax = left.vector[0] + right.vector[i];
					}
					
					for(int j = 1; j <= i; ++j) {
						
						Integer currentVal = null;
						Integer leftVal = left.vector[j];
						Integer rightVal = right.vector[i-j];
						if(leftVal != null && rightVal != null) {

							currentVal = left.vector[j] + right.vector[i-j];
						}
							
						if(valMax == null) {
							
							valMax = currentVal;	
							
						}else {
							
							if(currentVal != null && currentVal > valMax) {
								
								valMax = currentVal;
							}
						}
					}
					
					n.vector[i] = valMax;
				}
			}
		}
		
/*		System.out.print("node: "+ n.name);
		if(n.leftNode != null) {
			
			System.out.print(" left: "+ n.leftNode.name +" ");
		}
		
		if(n.rightNode != null) {
			
			System.out.print(" right: "+ n.rightNode.name +" ");
		}
		
		for(int i=0; i < n.vector.length; ++i) {
			
			System.out.print(n.vector[i] +"  ");
		}
		System.out.println("");*/
	}
	
	@Override
	public void printResults()
	{
		System.out.print(this.subTree.gt.size() + "| ");
		for(int i=0; i < this.root.vector.length; ++i) {
			
			System.out.print(this.root.vector[i] +" ");
		}
	}
	
	@Override
	public void saveEvalResults(PrintWriter writer, String rowName, boolean showTitle) {
		
		writer.print(rowName +";"+ this.subTree.gt.size() +";");
		for(int i=1; i < this.root.vector.length; ++i) {
			
			writer.print(";");
			writer.print(this.root.vector[i] +" ");
		}
		writer.println(); 
	}
}
