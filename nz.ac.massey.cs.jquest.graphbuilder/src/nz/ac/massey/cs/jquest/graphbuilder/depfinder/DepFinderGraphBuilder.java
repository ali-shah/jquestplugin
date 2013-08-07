package nz.ac.massey.cs.jquest.graphbuilder.depfinder;


import java.io.File;
import java.util.HashSet;
import java.util.Set;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
import nz.ac.massey.cs.jquest.graphbuilder.GraphBuilder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class DepFinderGraphBuilder implements GraphBuilder {

	@Override
	public DirectedGraph<TypeNode,TypeRef> buildGraph(IProject iProject,
			IProgressMonitor monitor) {
		DirectedGraph<TypeNode,TypeRef> g = null;
		try {
			IPath wp = iProject.getWorkspace().getRoot().getLocation();
			String binFolder = wp.toOSString()
					+ iProject.getFullPath().toOSString() + "/bin/";
			String projectPath = new File(binFolder).getAbsolutePath();
			g = Utils.loadGraph(projectPath, monitor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return g;
	}

	@Override
	public DirectedGraph<TypeNode, TypeRef> buildPackageGraph(
			DirectedGraph<TypeNode, TypeRef> g, IProgressMonitor m) {
		DirectedGraph<TypeNode, TypeRef> pg = new DirectedSparseGraph<TypeNode, TypeRef>();
		Set<String> namespaces = new HashSet<String>();

		for (TypeNode v : g.getVertices()) {
			String namespace = v.getNamespace();
			namespaces.add(namespace);
		}

		for (String namespace : namespaces) {
			TypeNode newV = new TypeNode(namespace);
			newV.setName(namespace);
			pg.addVertex(newV);
		}

		for (TypeRef e : g.getEdges()) {
			String srcNamespace = e.getStart().getNamespace();
			String tarNamespace = e.getEnd().getNamespace();
			if (srcNamespace.equals(tarNamespace))
				continue;
			TypeNode src = null;
			TypeNode tar = null;
			for (TypeNode ns : pg.getVertices()) {
				if (ns.getName().equals(srcNamespace))
					src = ns;
			}
			for (TypeNode ns : pg.getVertices()) {
				if (ns.getName().equals(tarNamespace))
					tar = ns;
			}
			String edge = srcNamespace + tarNamespace;
			TypeRef newE = new TypeRef(edge, src, tar);
			if (!pg.containsEdge(newE))
				pg.addEdge(newE, src, tar);
		}
		return pg;
	}

}
