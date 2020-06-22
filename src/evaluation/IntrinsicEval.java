package evaluation;

import java.io.PrintWriter;
import java.util.Map.Entry;

import datastructure.Node;
import evaluation.bricks.Eval;
import evaluation.bricks.EvalInterface;
import evaluation.datastructure.STree;
import evaluation.datastructure.STree.TypeN;
import utils.Log;


/**
 * A quality analysis can be carried out by directly observing
 * the BPT T with respect to the ground-truth G ⊂ Ω. In other words, the BPT
 * provides, by its inner structure and its spatial embedding in Ω, information
 * about its relevance and its ability to extract particular objects of interest.
 */
public class IntrinsicEval extends Eval implements EvalInterface {
	
	public String context = "INTRINSIC EVALUATION";
	
	public double cb1;
	public double cb2;
	public double qt1;
	public double qt2 = Double.NaN;
	public double qt3;
	public double qt4;
	private double qt5;
	public double ui_ref;
	public double bpp_ref;
	public double bii_ref;
	public double bpi_ref;

	/**
	 * Number of pixels contained in the root region.
	 */
	public double rootSize;

	/**
	 * Number of pixels contained in the GT region.
	 */
	public double gtSize;

	/**
	 * Total number of all pixels contained in all leaves.
	 */
	public double leavesSize;

	/**
	 * Total number of all pixels contained in all pure leaves.
	 */
	public double unionLp;

	/**
	 * Total number of all pixels contained in the largest pure node.
	 */
	public double largestPureNodeSize = -1;

	/**
	 * Total number of pixels contained in the union of all BPP having BPI fathers.
	 */
	public double unionNp;

	/**
	 * Number of BPP nodes having BPI fathers.
	 */
	public double np;

	@Override
	public void start(STree subTree) {
	
		this.subTree = subTree;
		
		Log.println(this.context, "Starting intrinsic evaluation");
		
		/* Defining the values of references defining the best merging */
		this.valRef();
	
		/* Combinatorial analysis */
		this.cb();
		this.qt();
		
		Log.println(this.context, "Done");
	}
	
	private void cb() {
		
		this.cb1 = (this.subTree.bpp + this.subTree.bii) / (this.subTree.ui + this.subTree.bpp + this.subTree.bii + this.subTree.bpi);
		this.cb2 = this.subTree.bpp / (this.subTree.bpp + this.subTree.bpi);
		
	}
	
	private void qt() {
		
		double ng = (double) this.subTree.root.getSize();
		double g = (double) this.subTree.gt.size();
		
		double unionL = 0.0;
		double unionLp = 0.0;

		for(int i=0; i < this.subTree.lpL.size(); ++i){
			
			unionL += (double) this.subTree.lpL.get(i).getSize();
			
		}
		unionLp = unionL;
		
		for(int i=0; i < this.subTree.liL.size(); ++i){
			
			unionL += (double) this.subTree.liL.get(i).getSize();
		}
		
		this.qt1 = ng - unionL;
		this.qt2 = (ng - g) / (unionL - g);
		
		double unionNp = 0.0;		
		double cardNp = 0.0;
		
		for(Entry<Node, TypeN> entry: this.subTree.regedit.entrySet()) {
			
			Node n = entry.getKey();
			TypeN type = entry.getValue();
			
			if(type.equals(TypeN.BPP) && (n.father == null || this.subTree.regedit.get(n.father).equals(TypeN.BPI))) {
				
				unionNp += n.getSize();
				cardNp++;
			}
		}
		
		this.qt3 = unionLp - unionNp;
		this.qt4 = (g - unionLp) / (g - unionNp);
		
		double cardLp = this.subTree.getLp();
		
		for(Node l: this.subTree.lpL) {
			
			if(l.father == null || this.subTree.regedit.get(l.father).equals(TypeN.BPI)) {
				
				cardNp++;
			}
		}
		
		this.qt5 = cardNp / cardLp;
		
		this.setRootSize(ng);
		this.setGtSize(g);
		this.setLeavesSize(unionL);
		this.setUnionLp(unionLp);
		this.setUnionNp(unionNp);
		this.np = cardNp;
	}
	
	public void setUnionNp(double unionNp) {
	
		this.unionNp = unionNp;
	}

	private void valRef() {

		if(this.subTree.getLi() == 0){
			
			this.ui_ref = 0.0;
			this.bpp_ref = this.subTree.getLp() - 1;
			this.bii_ref = 0.0;
			this.bpi_ref = 0.0;
			
		}else{
			
			this.ui_ref = 0;
			this.bpp_ref = this.subTree.getLp() - 1;
			this.bii_ref = this.subTree.getLi() - 1;
			this.bpi_ref = 1;
			
		}
	}
	
	private double getCb1() {
		
		return this.cb1;
		
	}

	private double getCb2() {
		
		return this.cb2;
		
	}

	private double getQ1() {
		
		return this.qt1;
		
	}

	private double getQ2() {
		
		return this.qt2;
		
	}

	private double getQ3() {
		
		return this.qt3;
		
	}

	private double getQ4() {
		
		return this.qt4;
		
	}
	
	@Override
	public void printResults()
	{
		System.out.println("[ST-"+ this.subTree.index +"] "
				   + " granularity: "+ STree.decimal3(this.subTree.getGranularity()) +" |"
				   + " discordance: "+ STree.decimal3(this.subTree.getDiscordance()) +" |"
				   + " li: "+ this.subTree.getLi() +" |"
				   + " lp: "+ this.subTree.getLp() +" |"
				   + " ui: "+ this.subTree.getUi() +" ("+ this.ui_ref +") |"
				   + " bpp: "+ this.subTree.getBpp() +" ("+ this.bpp_ref +") |"
				   + " bii: "+ this.subTree.getBii() +" ("+ this.bii_ref +") |"
				   + " bpi: "+ this.subTree.getBpi() +" ("+ this.bpi_ref +") |"
				   + "     "
				   + " C1: "+ this.getCb1() +"|"
				   + " C2: "+ this.getCb2() +"|"
				   + " Q1: "+ this.getQ1() +"|"
				   + " Q2: "+ this.getQ2() +"|"
				   + " Q3: "+ this.getQ3() +"|"
				   + " Q4: "+ this.getQ4() +"|"
				   + " Q5: "+ this.getQ5() +"|"
				   + "     "
				   + " sizeRoot: "+ this.getRootSize() +"|"
				   + " sizeGt: "+ this.getGtSize() +"|"
				   + " sizeLeaves: "+ this.getLeavesSize() +"|"
				   + " sizeUnionLp: "+ this.getUnionLp() +"|"
				   + " sizeUnionNp: "+ this.getUnionNp() +"|"
				   + " Np: "+ this.getNp());
	}

	public double getNp() {

		return this.np;
	}

	public double getUnionNp() {

		return this.unionNp;
	}

	@Override
	public void saveEvalResults(PrintWriter writer, String rowName, boolean showTitle) {
		
		if(showTitle)
		writer.println("[ST-"+ this.subTree.index +"];"
					   + " granularity; "
					   + " discordance;"
					   + " li;"
					   + " lp;"
					   + " ui ("+ this.ui_ref +");"
					   + " bpp ("+ this.bpp_ref +");"
					   + " bii ("+ this.bii_ref +");"
					   + " bpi ("+ this.bpi_ref +");"
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
					   + "sizeUnionLp;"
					   + "sizeUnionNp;"
					   + "Np");
		
		writer.print(rowName);
		writer.print(";"+ STree.decimal3(this.subTree.getGranularity()) + ";");
		writer.print(STree.decimal3(this.subTree.getDiscordance()) +";");
		writer.print(this.subTree.getLi() +";");
		writer.print(this.subTree.getLp() +";");
		writer.print(this.subTree.getUi() +";");
		writer.print(this.subTree.getBpp() +";");
		writer.print(this.subTree.getBii() +";");
		writer.print(this.subTree.getBpi() +";");
		writer.print(";");
		writer.print(STree.decimal3(this.getCb1()) +";");
		writer.print(STree.decimal3(this.getCb2()) +";");
		writer.print(STree.decimal3(this.getQ1()) +";");
		writer.print(STree.decimal3(this.getQ2()) +";");
		writer.print(STree.decimal3(this.getQ3()) +";");
		writer.print(STree.decimal3(this.getQ4()) +";");	
		writer.print(STree.decimal3(this.getQ5()) +";");	
		writer.print(";");
		writer.print(this.getRootSize() +";");
		writer.print(this.getGtSize() +";");
		writer.print(this.getLeavesSize() +";");
		writer.print(this.getUnionLp() +";");
		writer.print(this.getUnionNp() +";");
		writer.print(this.getNp());
		writer.println();
	}

	public double getRootSize() {
		return rootSize;
	}

	public void setRootSize(double rootSize) {
		this.rootSize = rootSize;
	}

	public double getGtSize() {
		return gtSize;
	}

	public void setGtSize(double gtSize) {
		this.gtSize = gtSize;
	}

	public double getLeavesSize() {
		return leavesSize;
	}

	public void setLeavesSize(double leavesSize) {
		this.leavesSize = leavesSize;
	}

	public double getUnionLp() {
		return unionLp;
	}

	public void setUnionLp(double pureLeavesSize) {
		this.unionLp = pureLeavesSize;
	}
	
	public void setLargestPureNodeSize(double pureNodesSize) {
		this.largestPureNodeSize = pureNodesSize;
	}
	
	public double getLargestPureNodeSize() {

		if(this.largestPureNodeSize > 0) {
			
			return this.largestPureNodeSize;			
		}else {
			
			return this.subTree.getLargestPureNode().getSize();
		}
	}

	public double getQ5() {
		return qt5;
	}

	public void setQt5(double qt5) {
		this.qt5 = qt5;
	}

}
