package evaluation.bricks;

import evaluation.ExtrinsicEval;
import evaluation.IntrinsicEval;
import evaluation.PonttusetEval;
import evaluation.bricks.Eval.EvalType;

/**
 * 
 * Factory building the right evaluation objects.
 * <br>
 * <ul>
 * <li> INTRINSIC
 * <li> EXTRINSIC
 * </ul>
 * Each coded evaluation class should figure in this class as a choice.
 *
 */
public class EvalFactory {

	/**
	 * Chooses and builds the right evaluation object according to the type.
	 * 
	 * @param evalType specifying the kind of evaluation object to create; should not be null.
	 * 
	 * <br>
	 * <ul>
	 * <li> INTRINSIC
	 * <li> EXTRINSIC
	 * </ul>
	 * 
	 * @throws NullPointerException if evalType is null
	 */
	public static Eval prepareEval(EvalType evalType) {

		switch(evalType){
		
			case INTRINSIC: return new IntrinsicEval();
			case PONT_TUSET: return new PonttusetEval();
			case EXTRINSIC: return new ExtrinsicEval();
			default: return new IntrinsicEval();
		}
	}
}
