package nz.ac.massey.cs.jquest.scoring;

import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;
//import nz.ac.massey.cs.gql4jung.Dependency;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.Path;

/**
 * Function used to compute the score for an edge.
 * This can be used to associates weights with certain motifs and path roles within motifs.
 * @author jens dietrich
 *
 */
public interface ScoringFunction {

	int getEdgeScore(Motif<TypeNode,Dependency> motif,String pathRole,Path<TypeNode,Dependency> path,Dependency e);
}
