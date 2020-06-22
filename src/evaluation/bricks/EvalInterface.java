package evaluation.bricks;

import java.io.PrintWriter;

import evaluation.datastructure.STree;

/**
 * 
 * Interface containing all methods that all evaluation classes should implement.
 *
 */
public interface EvalInterface {
	
	/** 
	 * Performs an evaluation on the sub tree.
	 * @param subTree structure to assess.
	 */ 
	public void start(STree subTree);
	
	public void printResults();

	public void saveEvalResults(PrintWriter writer, String columnName, boolean showTitle);
}
