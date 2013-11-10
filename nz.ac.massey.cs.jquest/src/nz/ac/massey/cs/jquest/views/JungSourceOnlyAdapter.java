package nz.ac.massey.cs.jquest.views;

import java.util.Iterator;

import nz.ac.massey.cs.guery.AbstractGraphAdapter;
import nz.ac.massey.cs.guery.GraphAdapter;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;

import com.google.common.base.Predicate;

public class JungSourceOnlyAdapter extends AbstractGraphAdapter<TypeNode, Dependency> {
	
	private GraphAdapter<TypeNode, Dependency> graphAdapter;

	public JungSourceOnlyAdapter(GraphAdapter<TypeNode, Dependency> graphAdapter) {
		this.graphAdapter = graphAdapter;
	}

	@Override
	public Iterator<Dependency> getInEdges(TypeNode vertex) {
		return graphAdapter.getInEdges(vertex);
	}

//	@Override
//	public Iterator<Dependency> getInEdges(TypeNode vertex,
//			Predicate<? super Dependency> filter) {
//		return graphAdapter.getInEdges(vertex, filter);
//	}

	@Override
	public Iterator<Dependency> getOutEdges(TypeNode vertex) {
		return graphAdapter.getOutEdges(vertex);
	}

//	@Override
//	public Iterator<Dependency> getOutEdges(TypeNode vertex,
//			Predicate<? super Dependency> filter) {
//		return graphAdapter.getOutEdges(vertex, filter);
//	}

	@Override
	public TypeNode getStart(Dependency edge) {
		return graphAdapter.getStart(edge);
	}

	@Override
	public TypeNode getEnd(Dependency edge) {
		return graphAdapter.getEnd(edge);
	}

	@Override
	public Iterator<Dependency> getEdges() {
		Predicate<Dependency> filter = new Predicate<Dependency>() {
			@Override
			public boolean apply(Dependency e) {
				return e.getStart().getContainer().equals("_src") && 
						e.getEnd().getContainer().equals("_src");
			}	
		};
		Iterator<Dependency> edges = graphAdapter.getEdges(filter);
		return edges;
	}

//	@Override
//	public Iterator<Dependency> getEdges(Predicate<? super Dependency> filter) {
//		return graphAdapter.getEdges(filter);
//	}

	@Override
	public Iterator<TypeNode> getVertices() {
		Predicate<TypeNode> filter = new Predicate<TypeNode>() {

			@Override
			public boolean apply(TypeNode node) {
				return node.getContainer().equals("_src");
			}
			
		};
		return graphAdapter.getVertices(filter);
	}

//	@Override
//	public Iterator<TypeNode> getVertices(
//			Comparator<? super TypeNode> comparator) {
//		if (comparator==null) return getVertices();
////		TreeSet<TypeNode> sorted = new TreeSet<TypeNode>(comparator);
////		Iterator<TypeNode> unsorted = getVertices();
////		while (unsorted.hasNext()) {
////			sorted.add(unsorted.next());
////		}
////		closeIterator(unsorted);
////		return sorted.iterator();
////		Iterator<TypeNode> iter = graphAdapter.getVertices(comparator);
////		Set<String> containers = new HashSet<String>();
////		while(iter.hasNext()) {
////			TypeNode tn = iter.next();
////			containers.add(tn.getContainer());
////		}
//		return graphAdapter.getVertices(comparator);
//	}

//	@Override
//	public Iterator<TypeNode> getVertices(Predicate<? super TypeNode> filter) {
//		return graphAdapter.getVertices(filter);
//	}

	@Override
	public int getVertexCount() throws UnsupportedOperationException {
		Iterator<TypeNode> iter = getVertices();
		int counter = 0;
		while(iter.hasNext()) {
			iter.next();
			counter ++;
		}
		return counter;
//		return graphAdapter.getVertexCount();
	}

	@Override
	public int getEdgeCount() throws UnsupportedOperationException {
		return graphAdapter.getEdgeCount();//TODO
	}

	@Override
	public void closeIterator(Iterator<?> iterator) {
		graphAdapter.closeIterator(iterator);
		
	}
	
}
