package nz.ac.massey.cs.jquest.utils;

import edu.uci.ics.jung.graph.DirectedGraph;
import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;

public class Utils {

	public static TypeNode getNode(DirectedGraph<TypeNode, TypeRef>g, String fullyQualifiedName) {
	
		for(TypeNode tn : g.getVertices()) {
			if(tn.getFullname().equals(fullyQualifiedName))
				return tn;
		}
		return null;
	}
	
	
	
}
