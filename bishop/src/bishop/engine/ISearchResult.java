package bishop.engine;

import bishop.base.MoveList;

public interface ISearchResult {
	public int getEvaluation();
	
	public MoveList getPrincipalVariation();

}
