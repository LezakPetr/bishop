package bishop.engine;

import bishop.base.MoveList;

public interface ISearchResult {
	public NodeEvaluation getNodeEvaluation();
	
	public MoveList getPrincipalVariation();

}
