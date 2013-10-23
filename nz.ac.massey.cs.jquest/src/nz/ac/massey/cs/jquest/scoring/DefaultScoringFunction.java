package nz.ac.massey.cs.jquest.scoring;

import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.Path;
import nz.ac.massey.cs.jdg.Dependency;

public class DefaultScoringFunction implements ScoringFunction {

	@Override
	public int getEdgeScore(Motif<TypeNode,Dependency> motif, String pathRole,Path<TypeNode,Dependency> path,Dependency edge) {
		return 1;
	}
	
	@Override
	public String toString() {
		return "simple scoring function (score is always 1)";
	}

}
