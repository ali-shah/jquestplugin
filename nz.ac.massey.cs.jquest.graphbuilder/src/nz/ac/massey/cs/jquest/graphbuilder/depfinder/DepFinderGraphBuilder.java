package nz.ac.massey.cs.jquest.graphbuilder.depfinder;


import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import edu.uci.ics.jung.graph.DirectedGraph;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
import nz.ac.massey.cs.jquest.graphbuilder.GraphBuilder;

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

}
