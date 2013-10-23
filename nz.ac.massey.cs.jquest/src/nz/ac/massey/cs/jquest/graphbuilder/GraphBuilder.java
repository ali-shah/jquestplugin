package nz.ac.massey.cs.jquest.graphbuilder;


//import nz.ac.massey.cs.gql4jung.TypeNode;
//import nz.ac.massey.cs.gql4jung.TypeRef;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.ics.jung.graph.DirectedGraph;

public interface GraphBuilder {
	
	public DirectedGraph<TypeNode,Dependency>  buildGraph(IProject p, IProgressMonitor monitor);

	public DirectedGraph<TypeNode, Dependency> buildPackageGraph(
			DirectedGraph<TypeNode, Dependency> g, IProgressMonitor m);

}
