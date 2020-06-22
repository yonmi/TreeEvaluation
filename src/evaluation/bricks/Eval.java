package evaluation.bricks;

import java.io.PrintWriter;

import evaluation.datastructure.STree;
import lang.Strings;

/**
 * 
 * Parent of all evaluation classes.
 * (!) All evaluation classes must implement the interface {@link EvalInterface} and override all its methods.
 * 
 */
public class Eval implements EvalInterface{
	
	public static final String CONTEXT = Strings.EVALUATION;
	
	public STree subTree;
	
	/**
	 *
	 * Each evaluation class must be associated to a precise type.
	 * The type will help the factory to build the right evaluation object.
	 *
	 */
	public static enum EvalType{
		
		INTRINSIC,
		PONT_TUSET,
		EXTRINSIC
	}
	
	@Override
	public void start(STree subTree) {
		
		System.err.println(CONTEXT +"[WARNING] the method 'agat.evaluation.bricks.EvalInterface.start(BufferedImage groundTruth)' is not implemented!");
		System.exit(0);
	}

	@Override
	public void saveEvalResults(PrintWriter writer, String rowName, boolean showTitle) {

		System.err.println(CONTEXT +"[WARNING] the method 'agat.evaluation.bricks.EvalInterface.saveIntrinsicEvalResults(PrintWriter writer)' is not implemented!");
		System.exit(0);
	}

	@Override
	public void printResults() {
		
		System.err.println(CONTEXT +"[WARNING] the method 'agat.evaluation.bricks.EvalInterface.printIntrinsicEvalResults()' is not implemented!");
		System.exit(0);
	}
}
