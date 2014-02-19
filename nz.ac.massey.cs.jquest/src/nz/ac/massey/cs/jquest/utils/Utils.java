/*
 * Copyright 2014 Ali Shah Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.gnu.org/licenses/agpl.html Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
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
				  "connected by uses(src>tar)[1,1] find all\n" +
				  "where \"uses.hasType('USES') || uses.hasType('EXTENDS') || uses.hasType('IMPLEMENTS')\"" ;
//				  "group by \"src\"";
		return adhocQuery;
	}
}
