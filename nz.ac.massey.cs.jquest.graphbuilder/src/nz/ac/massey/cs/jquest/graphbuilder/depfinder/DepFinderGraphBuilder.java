package nz.ac.massey.cs.jquest.graphbuilder.depfinder;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.ac.massey.cs.jdg.DependencyType;
import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jquest.graphbuilder.GraphBuilder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class DepFinderGraphBuilder implements GraphBuilder {

	@Override
	public DirectedGraph<nz.ac.massey.cs.jdg.TypeNode, Dependency> buildGraph(IProject iProject,
			IProgressMonitor monitor) {
		DirectedGraph<TypeNode,Dependency> g = null;
		try {
			IPath wp = iProject.getWorkspace().getRoot().getLocation();
			IJavaProject ijp = JavaCore.create(iProject);
			IClasspathEntry[] icp = ijp.getRawClasspath();
			Object[] libEntries = new Object[icp.length];
			int i=0;
			for(IClasspathEntry e : icp) {
				if(e.getContentKind() == IPackageFragmentRoot.K_BINARY) {
					if(e.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						libEntries[i++] = (Object) e;
					}
				}
			}
			List<String> jarNames = new ArrayList<String> ();
			
			for(Object libEntry : libEntries) {
				IClasspathEntry e = (IClasspathEntry) libEntry;
				IPackageFragmentRoot[] libroots = ijp.findPackageFragmentRoots(e);
				
				for(IPackageFragmentRoot jarFile: libroots) {
					jarNames.add(jarFile.getPath().toOSString());
				}
			}
			String workspacePath = wp.toOSString();
			List<File> inputFoldersAndJars = new ArrayList<File>();
			for(String s : jarNames) {
				String fName = workspacePath + "/" + s;
				inputFoldersAndJars.add(new File(fName));
			}
			
			String outputFolder = ijp.getOutputLocation().toString();
			String binFolder = workspacePath + outputFolder;
			String projectPath = new File(binFolder).getAbsolutePath();
			File bin = new File(projectPath);
			inputFoldersAndJars.add(bin);
			
			g = Utils.loadGraph(inputFoldersAndJars, monitor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return g;
	}

//	@Override
//	public DirectedGraph<TypeNode, Dependency> buildPackageGraph(
//			DirectedGraph<TypeNode, Dependency> g, IProgressMonitor m) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public DirectedGraph<TypeNode, Dependency> buildPackageGraph(
			DirectedGraph<TypeNode, Dependency> g, IProgressMonitor m) {
		DirectedGraph<TypeNode, Dependency> pg = new DirectedSparseGraph<TypeNode, Dependency>();
		Set<String> namespaces = new HashSet<String>();
		Map<String, String> namespaceContainers = new HashMap<String, String>();
		for (TypeNode v : g.getVertices()) {
			String namespace = v.getNamespace();
			String container = v.getContainer();
			namespaceContainers.put(namespace, container);
			namespaces.add(namespace);
		}

		for (String namespace : namespaces) {
			TypeNode newV = new TypeNode("package");
			newV.setContainer(namespaceContainers.get(namespace));
			newV.setNamespace(namespace);
			pg.addVertex(newV);
		}

		for (Dependency e : g.getEdges()) {
			String srcNamespace = e.getStart().getNamespace();
			String tarNamespace = e.getEnd().getNamespace();
			if (srcNamespace.equals(tarNamespace))
				continue;
			TypeNode src = null;
			TypeNode tar = null;
			for (TypeNode ns : pg.getVertices()) {
				if (ns.getNamespace().equals(srcNamespace))
					src = ns;
			}
			for (TypeNode ns : pg.getVertices()) {
				if (ns.getNamespace().equals(tarNamespace))
					tar = ns;
			}
			String edge = srcNamespace + tarNamespace;
			Dependency newE = new Dependency(edge, src, tar);
			newE.setType(DependencyType.USES);
			if (!pg.containsEdge(newE))
				pg.addEdge(newE, src, tar);
		}
		return pg;
	}

}
