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
import java.awt.Window.Type;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeSet;

import datastructure.Node;
import datastructure.Node.TypeOfNode;
import ui.ImFrame;
import datastructure.Tree;
import utils.ImTool;

/**
 * Object detection measure from BPT
 * 
 * Implementation from B. Perret, J. Cousty, S.J.F. Guimarães, D.S. Maia. Evaluation of hierarchical watersheds. 
 * IEEE Transactions on Image Processing, 27(4): 1676-1688, 2018. doi:10.1109/TIP.2017.2779604. 
 * 
 * @author ckurtz
 *
 */


public class PerretEval {

	/**
	 * F-Measure from a segmented image and a GT
	 * @param seg
	 * @param GT
	 * @return FM
	 */
	public static double FMeasure(BufferedImage seg, BufferedImage GT) {
		
		int w = seg.getWidth();
		int h = seg.getHeight();
		
		int sizeObject = 0;
		int sizeGT = 0;
		
		double precision = 0.0;
		double recall = 0.0;
		double trueP=0.0;
		
		for(int y = 0; y < h; ++y) {	
			for(int x = 0; x < w; ++x) {
				double valSeg = seg.getRaster().getSampleDouble(x, y, 0);
				double valGT = GT.getRaster().getSampleDouble(x, y, 0);
				
				if(valSeg==255.0) sizeObject++;
				if(valGT==255.0) sizeGT++;	
				
				if(valSeg==255.0&&valGT==255.0)
					trueP=trueP+1.0;
			}
		}
		precision = trueP/sizeObject;
		recall= trueP/sizeGT;
		
		return 2.0 * ((precision*recall) / (precision+recall)) ;
		
	}
	/**
	 * Compute Best Cut cardinality
	 * @return
	 */
	public static int computeBestCutCardinality(Tree bpt) {
		/* Additional info : count the number of nodes in the best cut*/
		
		//Get the leaves from the tree
		TreeSet<Node> leaves = new TreeSet<Node>();
		for(Node n:bpt.getNodes()) {
			if (n.type==TypeOfNode.LEAF)
					leaves.add(n);
		}
		
		int cardinalityCut=0;
		int nbLeavesAsObject=0;
		TreeSet<Node> cut=new TreeSet<Node> ();
		for(Node n:leaves) {
			if(n.label==2) { 
				nbLeavesAsObject++;
			
				Node temp=n;
				while(temp.father!=null && temp.father.label==2) {
					temp=temp.father;
				}
				cut.add(temp);
			}
		}
	
		cardinalityCut= cut.size();
		//System.out.println("Number of leaves marked as object = "+nbLeavesAsObject);
		//System.out.println("cardinality of best cut = "+cardinalityCut);
		
		return cardinalityCut;
	}
	
	/**
	 * Object detection measure from BPT
	 * Bottom-up and top-down algorithm for marking BPT nodes
	 * 
	 * @param bpt
	 * @param markersImage
	 * @return SegmentedImage
	 */
	public static BufferedImage segment(Tree bpt, BufferedImage markersImage) {
		
		BufferedImage segmentedImage =null;
		
		//---Start by marking the leaves of the tree as Object, Background, Undefined
		//Label correspondence : 0 => Background, 1=> Undefined, 2=> Object
		
		//reset the label of the nodes as Undefined
		for(Node n:bpt.getNodes()) {
					n.label=1;
		}
		
		//Get the leaves from the tree
		TreeSet<Node> leaves = new TreeSet<Node>();
		for(Node n:bpt.getNodes()) {
			if (n.type==TypeOfNode.LEAF)
				leaves.add(n);
		}
		
		
		//Mark the leaves
		for(Node l:leaves) {
			
			//Find the label of the leaf in the markersImage
			TreeSet<Double> setValues=new TreeSet<Double>();
			for(Point p:l.getPixels()) {
				double valMarkerNVG = markersImage.getRaster().getSampleDouble(p.x, p.y, 0);
				setValues.add(valMarkerNVG);
			}
			//Check if pure leaves or not
			if(setValues.size()==1) {
				if(setValues.first().intValue()==0) {
					l.label = 0;
				}
				if(setValues.first().intValue()==127) {
					l.label = 1;
				}
				if(setValues.first().intValue()==255) {
					l.label = 2;
				}
			}else {
				//in not pure => unknown
				l.label = 1;
			}
		}
		//Check if at least one leave contains object or background
		boolean containsBackground=false;
		boolean containsObject=false;
		for(Node l:leaves) {
			if(l.label==0) {
				containsBackground=true;
			}
			if(l.label==2) {
				containsObject=true;
			}
		}
		if(containsBackground==false||containsObject==false) {
			return null;
		}
		
		
		//Print the labels of the leaves
		/*for(Node l:leaves) {
			System.out.println(l.name +"-->"+ l.label);
		}*/
		
		//Perret algorithm
		/*In the first step of the algorithm, the hierarchy is browsed
		from the leaves to the root. If the current node is labeled
		Background then its parent node intersects the background
		marker and is labeled Background. If the current node is
		labeled Object and its parent is not currently labeled then it
		can be labeled Object. 
		*/
		
		//Bottom up
		for(Node n:leaves) {
			//Propagate up in the tree the background
			if(n.label==0) {
				Node temp =n;
				while(temp.father!=null) {
					temp.label=0;
					temp = temp.father;
				}
			}
				
		}
		for(Node n:leaves) {
			//Propagate up in the tree the object
			if(n.label==2) {
				Node temp =n;
				while(n.label==2 && temp.father!=null&& temp.father.label==1) {
					temp.label=2;
					temp = temp.father;
				}
			}
				
		}
		//Top down
		/*In the second step, the tree is browsed
		from the root to the leaves and any non labeled node takes
		the label of its parent.
		*/
		Node r=bpt.getRoot();
		Stack<Node> fifo=new Stack<Node>();
		fifo.add(r);
		while(!fifo.isEmpty()) {
			Node n=fifo.pop();
			
			if(n.type!=TypeOfNode.ROOT) {
				if(n.label == 1)
					n.label=n.father.label;
			}
			
			if(n.leftNode!=null)
				fifo.add(n.leftNode);
			if(n.rightNode!=null)
				fifo.add(n.rightNode);
		}
		
		
		
		
		//Reconstruction of object in image : the labels of the leaves (the image pixels) give the segmentation result
		segmentedImage = new BufferedImage(markersImage.getWidth(), markersImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		for(Node n:leaves) {
			if(n.label == 0) {
				for(Point p:n.getPixels()) {
					segmentedImage.setRGB(p.x, p.y, ImTool.getRGBValueFrom(0 , 0, 0));
				}
			}
			if(n.label == 1) { // normally useless
				System.err.println("[PerretEval] Segmentation result contains unknown leaves...");
				for(Point p:n.getPixels()) {
					segmentedImage.setRGB(p.x, p.y, ImTool.getRGBValueFrom(127 , 127, 127));
				}
			}
			if(n.label == 2) {
				for(Point p:n.getPixels()) {
					segmentedImage.setRGB(p.x, p.y, ImTool.getRGBValueFrom(255, 255, 255));
				}
			}
				
		}
		return segmentedImage;
		
		
	
	}
	
}
