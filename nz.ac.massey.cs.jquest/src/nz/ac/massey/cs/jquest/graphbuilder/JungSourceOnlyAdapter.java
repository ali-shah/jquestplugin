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
package nz.ac.massey.cs.jquest.graphbuilder;

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

	@Override
	public Iterator<Dependency> getOutEdges(TypeNode vertex) {
		return graphAdapter.getOutEdges(vertex);
	}

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


	@Override
	public int getVertexCount() throws UnsupportedOperationException {
		Iterator<TypeNode> iter = getVertices();
		int counter = 0;
		while(iter.hasNext()) {
			iter.next();
			counter ++;
		}
		return counter;
	}

	@Override
	public int getEdgeCount() throws UnsupportedOperationException {
		return graphAdapter.getEdgeCount();
	}

	@Override
	public void closeIterator(Iterator<?> iterator) {
		graphAdapter.closeIterator(iterator);
		
	}
	
}
