package nz.ac.massey.cs.jquest.graphbuilder;


import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.ics.jung.graph.DirectedGraph;

public interface GraphBuilder {
	
	public DirectedGraph<TypeNode,TypeRef>  buildGraph(IProject p, IProgressMonitor monitor);

}
