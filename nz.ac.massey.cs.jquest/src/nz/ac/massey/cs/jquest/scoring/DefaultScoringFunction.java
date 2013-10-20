package nz.ac.massey.cs.jquest.scoring;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.Path;

public class DefaultScoringFunction implements ScoringFunction {

	@Override
	public int getEdgeScore(Motif<TypeNode,TypeRef> motif, String pathRole,Path<TypeNode,TypeRef> path,TypeRef e) {
		return 1;
	}
	
	@Override
	public String toString() {
		return "simple scoring function (score is always 1)";
	}

}
