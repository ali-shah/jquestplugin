package nz.ac.massey.cs.jquest.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;

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
	
	public static Set<TypeRef> findLargestByIntRanking(Collection<TypeRef> coll,Function<TypeRef,Integer> ranks) {
		Map<TypeRef,Integer> edgeRanks = new HashMap<TypeRef, Integer>();
		for(TypeRef e : coll){
			int r = ranks.apply(e);
			if(r != 0) edgeRanks.put(e, r);
		}
		Map<TypeRef,Double> sortedEdgeRanks = sortByValue(edgeRanks);
		
		return sortedEdgeRanks.keySet();
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Map<TypeRef, Double> sortByValue(Map<TypeRef, Integer> edgeRanks) {
	     List list = new LinkedList(edgeRanks.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return - ((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	}
}
