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
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;
//import nz.ac.massey.cs.gql4jung.Dependency;

public class Utils {

	public static TypeNode getNode(DirectedGraph<TypeNode, Dependency>g, String fullyQualifiedName) {
	
		Collection<TypeNode> vertices = g.getVertices();
		for(TypeNode tn : vertices) {
			String currentFullname = removeTrailingDot(tn.getFullname());
			if(currentFullname.equals(fullyQualifiedName))
				return tn;
		}
		return null;
	}

	public static String removeTrailingDot(String str) {
		if(str.endsWith(".null")) 
			str = str.substring(0, str.lastIndexOf(".null"));

//		if (str.length() > 0 && str.charAt(str.length()-1)=='.null") {
//		    str = str.substring(0, str.length()-1);
//		  }
		return str;
	}
	
	public static Set<Dependency> findLargestByIntRanking(Collection<Dependency> coll,Function<Dependency,Integer> ranks) {
		Map<Dependency,Integer> edgeRanks = new HashMap<Dependency, Integer>();
		for(Dependency e : coll){
			int r = ranks.apply(e);
			if(r != 0) edgeRanks.put(e, r);
		}
		Map<Dependency,Double> sortedEdgeRanks = sortByValue(edgeRanks);
		
		return sortedEdgeRanks.keySet();
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Map<Dependency, Double> sortByValue(Map<Dependency, Integer> edgeRanks) {
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

	public static String composeQuery(String src, String tar) {
		
		String adhocQuery = "motif adhoc \n" +
				  "select src, tar \n" +
				  "where \"src" + src + " and tar" + tar +"\" \n" +
				  "connected by uses(src>tar)[1,1]\n" +
				  "where \"uses.hasType('USES') || uses.hasType('EXTENDS') || uses.hasType('IMPLEMENTS')\" \n" +
				  "group by \"src\"";
		return adhocQuery;
	}
}
