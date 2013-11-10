/**
 * Copyright 2009 Jens Dietrich Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software distributed under the 
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language governing permissions 
 * and limitations under the License.
 */

package nz.ac.massey.cs.jquest.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.guery.GroupByAggregation;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.MotifInstance;
import nz.ac.massey.cs.guery.MotifInstanceAggregation;
import nz.ac.massey.cs.guery.Path;
import nz.ac.massey.cs.guery.ResultListener;
import nz.ac.massey.cs.guery.util.Cursor;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jquest.scoring.DefaultScoringFunction;
/**
 * Utility class that listens to results computed by the GQL engine, 
 * and aggregates them using a MotifInstanceAggregation.
 * Supports cursor based access to results. This class can be used as model 
 * in user interfaces.
 * @author Jens Dietrich
 */
public class QueryResults implements ResultListener, Iterable {
	private List<MotifInstance<TypeNode, Dependency>> instances = new Vector<MotifInstance<TypeNode, Dependency>>();
	
	public QueryResults() {
		super();
	}
	
	public QueryResults(MotifInstanceAggregation aggregation) {
		super();
		this.aggregation = aggregation;
	}
	
	
//	public class Cursor {
//		public Cursor(int major, int minor) {
//			super();
//			this.major = major;
//			this.minor = minor;
//		}
//		public int major = -1;
//		public int minor = -1;
//	}
	
	private MotifInstanceAggregation aggregation = new GroupByAggregation();
	

	/*= new VertexGroupByDefinition() {
		@Override
		public Object getGroupIdentifier(MotifInstance instance) {
			return instance.toString();
		}
	};
	*/
	private LinkedHashMap<Object,List<MotifInstance>> results = new LinkedHashMap<Object,List<MotifInstance>>();
	private List<Object> keys = new ArrayList<Object>();
	private int majorCursor = -1;
	private int minorCursor = -1;
	private long lastupdate;
	private long minTimeBetweenEvents = 50;
	
	public interface QueryResultListener {
		public void resultsChanged(QueryResults source);
		public void progressMade(int progress,int total);
	} 
	private List<QueryResultListener> listeners = new Vector<QueryResultListener>();

	public long getMinTimeBetweenEvents() {
		return minTimeBetweenEvents;
	}
	public void setMinTimeBetweenEvents(long minTimeBetweenEvents) {
		this.minTimeBetweenEvents = minTimeBetweenEvents;
	}
	public void addListener(QueryResultListener l) {
		listeners.add(l);
	}
	public void removeListener(QueryResultListener l) {
		listeners.remove(l);
	}
	
	public synchronized void reset() {
		this.results.clear();
		this.keys.clear();
		majorCursor = -1;
		minorCursor = -1;
		
		// inform listeners
		callback();			
	}
	@Override
	public boolean found(MotifInstance instance) {
		this.instances.add(instance);
		Object key = aggregation.getGroupIdentifier(instance);
		List<MotifInstance> instances = results.get(key);
		if (instances==null) {
			instances = new ArrayList<MotifInstance>();
			results.put(key,instances);
			keys.add(key);
		}
		instances.add(instance);
		//added by ali
		Motif<TypeNode,Dependency> motif = instance.getMotif();
		for (String pathRole:motif.getPathRoles()) {
			Path<TypeNode,Dependency> path = instance.getPath(pathRole);
			for (Dependency e:path.getEdges()) {
				DefaultScoringFunction f = new DefaultScoringFunction();
				int score = f.getEdgeScore(motif, pathRole, path, e);
				register(e,motif.getName(),score);
			}
		}
		// inform listeners
		callback();	
		
		return true;
	}
	private void callback() {
		long t = System.currentTimeMillis();
		if (t-lastupdate > minTimeBetweenEvents) { 
			lastupdate = t;
			for (QueryResultListener l:this.listeners) {
				l.resultsChanged(this);
			}	
		} 
	}
	public synchronized int getNumberOfGroups() {
		return results.size();
	}
	public synchronized int getNumberOfInstances() {
		int s = 0;
		for (int i=0;i<results.size();i++) {
			s = s + getNumberOfInstances(i);
		}
		return s;
	}
	public synchronized int getNumberOfInstances(int groupIndex) {
		if (groupIndex==-1) return 0;
		Object key = null;
		try{
			key = keys.get(groupIndex);
		} catch(IndexOutOfBoundsException e) {
			return 0;
		}
		
		if (key==null) return 0;
		List<MotifInstance> instances =  results.get(key);
		return instances==null?0:instances.size();
	}
	@Override
	public void done() {
		// TODO Auto-generated method stub
		
	}
	
	public Cursor getCursor() {
		return new Cursor(this.majorCursor,this.minorCursor);
	}

	// cursor operations
	public synchronized Cursor setInitialCursor() {
		if (this.getNumberOfGroups()>0 && this.getNumberOfInstances(0)>0) {
			majorCursor=0;
			minorCursor=0;
		}
		return new Cursor(this.majorCursor,this.minorCursor);
	}
	// cursor operations
	public synchronized boolean hasNextMajorInstance() {
		return majorCursor<(this.getNumberOfGroups()-1) && this.getNumberOfInstances(majorCursor+1)>0;
	}
	public synchronized Cursor nextMajorInstance() {
		if (hasNextMajorInstance()) {
			majorCursor=majorCursor+1;
			minorCursor=0;
		}
		return new Cursor(this.majorCursor,this.minorCursor);
	}
	
	public List<MotifInstance<TypeNode, Dependency>> getInstances() {
		return instances;
	}
	public synchronized boolean hasPreviousMajorInstance() {
		return majorCursor>0 && this.getNumberOfInstances(majorCursor-1)>0;
	}
	public synchronized Cursor previousMajorInstance() {
		if (hasPreviousMajorInstance()) {
			majorCursor=majorCursor-1;
			minorCursor=0;
		}
		return new Cursor(this.majorCursor,this.minorCursor);
	}
	public synchronized boolean hasNextMinorInstance() {
		return minorCursor<(this.getNumberOfInstances(majorCursor)-1);
	}
	public synchronized Cursor nextMinorInstance() {
		if (hasNextMinorInstance()) {
			minorCursor=minorCursor+1;
		}
		return new Cursor(this.majorCursor,this.minorCursor);
	}
	public synchronized boolean hasPreviousMinorInstance() {
		return 0<minorCursor;
	}
	public synchronized Cursor previousMinorInstance() {
		if (hasPreviousMinorInstance()) {
			minorCursor=minorCursor-1;
		}
		return new Cursor(this.majorCursor,this.minorCursor);
	}
	
	public synchronized MotifInstance getInstance(Cursor cursor) {
		Object key = this.keys.get(cursor.major);
		return results.get(key).get(cursor.minor);
	}

	class Entry<K,V> implements  Map.Entry<K,V> {
		
		public Entry(K key, V value) {
			super();
			this.key = key;
			this.value = value;
		}

		K key;
		V value;
		@Override
		public K getKey() {
			return null;
		}

		@Override
		public V getValue() {
			return null;
		}

		@Override
		public V setValue(V arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	public synchronized Iterator<Map.Entry<Cursor,MotifInstance>> iterator() {
		// TODO use iterator with lazy initialisation
		Map<Cursor,MotifInstance> map = new LinkedHashMap<Cursor,MotifInstance>();
		for (int i=0;i<this.keys.size();i++) {
			List<MotifInstance> instances = this.results.get(keys.get(i));
			for (int j=0;j<instances.size();j++) {
				map.put(new Cursor(i,j),instances.get(j));
			}
		}		
		return map.entrySet().iterator();
	}
	public synchronized boolean hasResults() {
		return this.keys.size()>0;
	}

	@Override
	public void progressMade(int progress, int total) {
		// dispatch events
		for (QueryResultListener l:this.listeners) {
			l.progressMade(progress, total);
		}
		
	}

	public synchronized int getCount(String motif,Dependency edge) {
		Map<Dependency,Integer> map = edgeOccByMotif.get(motif);
		if (map==null) {
			return 0; // no counts available for this motif
		}
		Integer counter = map.get(edge);
		return counter==null?0:counter.intValue();
	}
			
	public synchronized int getCount(Dependency edge) {
		int total = 0;
		for (String motif:edgeOccByMotif.keySet()) {
			total = total+getCount(motif,edge);
		}
		
		// double check whether this and the sum for the counts for all patterns are consistent
		/*
		int checksum = getCount("awd",edge)+getCount("cd",edge)+getCount("deginh",edge)+getCount("stk",edge);
		if (total!=checksum) {
			System.err.println("Dependency ranks do not match for " + edge);
			System.err.println("Total is " + total + " but sum of pattern ranks is " + checksum);
		}
		*/
		
		return total;
	}

	public synchronized Map<String,Integer> getEdgeParticipation(Dependency e) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for (String motif:edgeOccByMotif.keySet()) {
			map.put(motif,this.getCount(motif,e));
		}	
		return map;
	}
	private synchronized void register(Dependency edge, String motif,int score) {
		String srcNamespace = edge.getStart().getNamespace();
		String tarNamespace = edge.getEnd().getNamespace();
		if(srcNamespace.equals(tarNamespace)) return;
		Map<Dependency,Integer> map = edgeOccByMotif.get(motif);
		if (map==null) {
			map = new HashMap<Dependency,Integer>();
			edgeOccByMotif.put(motif,map);
		}
		Integer counter = map.get(edge);
		int c = counter==null?0:counter.intValue();
		map.put(edge,c+score);
		
		
	}
	private Map<String,Map<Dependency,Integer>> edgeOccByMotif = new HashMap<String,Map<Dependency,Integer>>();


	private Set<Dependency> criticalDeps = null;
	private int nextCritical = 0;
	private int prevCritical = -1;
	public void setCriticalDeps(Set<Dependency> edgesWithHighestRank) {
		this.criticalDeps = edgesWithHighestRank;
	}
	public Dependency getNextCritical() {
		if(nextCritical >= criticalDeps.size()) return null;
		prevCritical = nextCritical; 
		majorCursor=majorCursor+1;
		minorCursor=0;
		return (Dependency) criticalDeps.toArray()[nextCritical++];
	}
	
	public Dependency getPrevCritical() {
		if(prevCritical < 0) return null;
		nextCritical = prevCritical + 1; 
		majorCursor=majorCursor-1;
		minorCursor=0;
		return (Dependency) criticalDeps.toArray()[prevCritical--];
	}
	public boolean hasNextCriticalDep() {
		if(criticalDeps == null) return false;
		if(nextCritical == criticalDeps.size()) {
			return false;
		}
		else {
//			majorCursor=majorCursor+1;
//			minorCursor=0;
			return true;
		}
//		return criticalDeps.size() > 0;
	}
	public boolean hasPrevCriticalDep() {
		if(criticalDeps == null) return false;
		if(prevCritical < 0) return false;
		else {
//			majorCursor=majorCursor-1;
//			minorCursor=0;
			return true;
		}
	}
}
