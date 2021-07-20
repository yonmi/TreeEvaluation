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

package evaluation.datastructure;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;

import java.util.TreeMap;

import datastructure.Node;
import datastructure.Node.TypeOfNode;
import datastructure.Tree;
import evaluation.bricks.Eval;
import evaluation.bricks.Eval.EvalType;
import evaluation.bricks.EvalFactory;
import utils.ImTool;

public class STree {

	public enum TypeN{
		NIL,
		LI,
		LP,
		UI,
		BPP,
		BII,
		BPI
	}
	public int index;
	public Tree bpt;
	public SegReference gt;
	public ArrayList<Node> liL;
	public ArrayList<Node> lpL;
	public TreeMap<Node, TypeN> regedit;
	public double bpi = 0.0;
	public double bii = 0.0;
	public double bpp = 0.0;
	public double ui = 0.0;
	public Node root;
	public double discordance = 1.0;
	public double granularity = 0.0;

	public STree(int gti, Tree bpt, SegReference gt) {

		this.index = gti;
		this.bpt = bpt;
		this.gt = gt;
		this.root = bpt.getRoot(); // initial root
		
		System.out.print("[ST-"+ gti +"] ");
		
		this.leavesIdentification();		
		this.extractFrom(this.lpL);
		this.extractFrom(this.liL);
		this.classifyN();
		
		System.out.println("Ok");
		
		/* Test the relevance of the evaluation by computing the granularity and the discordance */
		this.relevance();
	}

	public static String decimal3(double value) {
		
		return new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US)).format(value);
	}

	public Eval eval(EvalType evalType) {
	
		Eval eval = EvalFactory.prepareEval(evalType);
		eval.start(this);
		return eval;
	}

	public double getBii() {
		
		return this.bii;
		
	}

	public double getBpi() {
		
		return this.bpi;
		
	}

	public double getBpp() {
	
		return this.bpp;
		
	}

	public double getDiscordance() {
		
		return this.discordance;
		
	}
	
	public double getGranularity() {
		
		return this.granularity;
		
	}

	public int getLi() {
		
		return this.liL.size();
		
	}

	public int getLp() {
	
		return this.lpL.size();
	
	}
	
	public double getUi() {
		
		return this.ui;
		
	}

	/**
	 * Drawing: </br>
	 * <ul>
	 * <li> the GT points: white, 
	 * <li> the root of the sub tree: purple, 
	 * <li> the pure leaves: green, 
	 * <li> the largest pure node: blue, 
	 * <li> the impure leaves: red. 
	 * </ul>
	 * @param dir folder where the images will be stored.
	 * @param namePart core of the name of each image to save.
	 * @param width of the image .
	 * @param height of the image.
	 */
	public void illustrateParams(String dir, String namePart, int width, int height) {
	
		namePart = FilenameUtils.removeExtension(namePart);
		String pathPart = dir +"//"+ namePart;
		new File(dir).mkdir();
		
		/* the GT */
		BufferedImage gtIllustration = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		
		// white
		int r = 255;
		int g = 255;
		int b = 255;
		int rgb = ImTool.getRGBValueFrom(r, g, b); 
		for(Point p: this.gt.getPoints()) {
			
			gtIllustration.setRGB(p.x, p.y, rgb);
		}
		String gtIllustrationPath = pathPart +"_GT.png";
		ImTool.save(gtIllustration, gtIllustrationPath);
		
		/* the root */
		BufferedImage rootIllustration = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		
		// purple
		r = 200;
		g = 0;
		b = 220;
		rgb = ImTool.getRGBValueFrom(r, g, b); 
		for(Point p: this.root.getPixels()) {
			
			rootIllustration.setRGB(p.x, p.y, rgb);
		}
		String rootIllustrationPath = pathPart +"_ROOT.png";
		ImTool.save(rootIllustration, rootIllustrationPath);
		
		/* the leaves */
		BufferedImage leavesIllustration = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		
		// green
		r = 0;
		g = 220;
		b = 100;
		rgb = ImTool.getRGBValueFrom(r, g, b); 
		for(Node l: this.lpL) { // pure leaves
			for(Point p: l.getPixels()) {
				
				leavesIllustration.setRGB(p.x, p.y, rgb);
			}
		}
		
		/* the impure leaves */
		// red
		r = 220;
		g = 50;
		b = 0;
		rgb = ImTool.getRGBValueFrom(r, g, b); 
		for(Node l: this.liL) { // impure leaves
			for(Point p: l.getPixels()) { 
				
				leavesIllustration.setRGB(p.x, p.y, rgb);
			}
		}
		
		/* the largest pure node */
		// blue
		r = 0;
		g = 20;
		b = 250;
		rgb = ImTool.getRGBValueFrom(r, g, b);
		Node largestPureNode = this.getLargestPureNode();
		if(largestPureNode != null)
			for(Point p: largestPureNode.getPixels()) { 

				leavesIllustration.setRGB(p.x, p.y, rgb);
			}
		String leavesIllustrationPath = pathPart +"_Leaves.png";
		ImTool.save(leavesIllustration, leavesIllustrationPath);
		
	}

	/**
	 * Find the largest pure node in the sub tree.
	 * @return a Node
	 */
	public Node getLargestPureNode() {

		Node np = null;
		
		for(Entry<Node, TypeN> entry: this.regedit.entrySet()) {
			
			Node n = entry.getKey();
			TypeN type = entry.getValue();
			
			if(type.equals(TypeN.BPP)){
				
				if(np == null || (n.getSize() > np.getSize())) {
					
					np = n;
				}
			}
		}
		
		return np;
	}

	private void classifyN() {
		
		for(Entry<Node, TypeN> entry: regedit.entrySet()){
	
			Node n = entry.getKey();
	
			if(n.type != TypeOfNode.LEAF){
	
				TypeN tn = entry.getValue();
				Node nl = n.leftNode;
				Node nr = n.rightNode;
	
				if(this.regedit.containsKey(nl) && this.regedit.containsKey(nr)){
	
					TypeN tnl = this.regedit.get(nl);
					TypeN tnr = this.regedit.get(nr);
	
					if((tnl == TypeN.LP || tnl == TypeN.BPP) && (tnr == TypeN.LP || tnr == TypeN.BPP)){
	
						tn = TypeN.BPP;
						++ bpp;
	
					}else if((tnl == TypeN.LI || tnl == TypeN.UI || tnl == TypeN.BII || tnl == TypeN.BPI) && (tnr == TypeN.LI || tnr == TypeN.UI || tnr == TypeN.BII || tnr == TypeN.BPI)){
	
						tn = TypeN.BII;
						++ bii;
	
					}else{
	
						tn = TypeN.BPI;
						++ bpi;
	
					}
	
				}else{
	
					tn = TypeN.UI;
					++ ui;
	
				}
	
				this.regedit.put(n, tn);
	
			}
	
		}
	
	}

	private void extractFrom(ArrayList<Node> lL) {
		
		TypeN fType = TypeN.NIL;
		
		for(Node l: lL){
			
			Node f = l.father;
			
			while(f != null && !this.regedit.containsKey(f)){
				
				this.regedit.put(f, fType);
				
				if(f.getSize() >= this.gt.size()){ 
	
					ArrayList<Point> gtPoints = this.gt.getPoints();
					ArrayList<Point> fPoints = f.getPixels();
					
					if(fPoints.containsAll(gtPoints) && f.getSize() < this.root.getSize()){ // root choice
	
						this.root = f;
						f = null;
	
					}else f = f.father;
	
				}else f = f.father;
			
			}
							
		}
		
	}

	private void leavesIdentification() {
	
		this.liL = new ArrayList<Node>();
		this.lpL = new ArrayList<Node>();
		this.regedit = new TreeMap<Node, TypeN>();
	
		ArrayList<Point> gtPoints = this.gt.getPoints();
		int nbLBpt = this.bpt.getNbLeaves();
		for(int i=0; i < nbLBpt; ++i){
			
			Node l = this.bpt.getNode(i);
			ArrayList<Point> lPoints = l.getPixels();
			
			if(gtPoints.containsAll(lPoints)){
				
				this.lpL.add(l);
				this.regedit.put(l, TypeN.LP);
				
			}else{
				
				for(Point p: lPoints){
					
					if(gtPoints.contains(p)){
						
						this.liL.add(l);
						this.regedit.put(l, TypeN.LI);
						break;
						
					}
					
				}
				
			}
			
		}
		
	}

	private void relevance() {
	
		int g = this.gt.size();
		
		int lg = this.lpL.size() + this.liL.size();
		
		this.granularity = Math.pow(1 + Math.log10((double) g / (double) lg), -1);
		
		double sumMinInOut = 0;
		for(Node l:  this.liL){
	
			ArrayList<Point> pointsOut = new ArrayList<Point>();
			pointsOut.addAll(l.getPixels());
			pointsOut.removeAll(this.gt.getPoints());
	
			double pout = pointsOut.size();
			double pin = l.getSize() - pout;
	
			sumMinInOut += Math.min(pout, pin);
		}
	
		this.discordance = sumMinInOut / g;
	}
}
