package nz.ac.massey.cs.jquest.utils;

import edu.uci.ics.jung.graph.DirectedGraph;
import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;

public class Utils {

	public static TypeNode getNode(DirectedGraph<TypeNode, TypeRef>g, String fullyQualifiedName) {
	
		for(TypeNode tn : g.getVertices()) {
			String currentFullname = removeTrailingDot(tn.getFullname());
			if(currentFullname.equals(fullyQualifiedName))
				return tn;
		}
		return null;
	}

	public static String removeTrailingDot(String str) {
		if (str.length() > 0 && str.charAt(str.length()-1)=='.') {
		    str = str.substring(0, str.length()-1);
		  }
		return str;
	}
	
	
	
}
