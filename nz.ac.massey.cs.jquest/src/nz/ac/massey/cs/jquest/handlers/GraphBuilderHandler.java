package nz.ac.massey.cs.jquest.handlers;

//import nz.ac.massey.cs.gql4jung.TypeNode;
//import nz.ac.massey.cs.gql4jung.TypeRef;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.graphbuilder.GraphBuilder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import edu.uci.ics.jung.graph.DirectedGraph;


public class GraphBuilderHandler {
  private static final String IGRAPHBUILDER_ID = 
      "nz.ac.massey.cs.jquest.graphbuilder";
private IExtensionPoint point;
  
  public DirectedGraph<TypeNode, Dependency> loadGraph(IProject p, IProgressMonitor m){
	  
	  DirectedGraph<TypeNode, Dependency> g = null;
	  getOrAddExtensionPoint();
	  if(point == null) return null;
	  IConfigurationElement[] config = point.getConfigurationElements();
	  try {
	      for (IConfigurationElement e : config) {
	        final Object o = e.createExecutableExtension("class");
	        if (o instanceof GraphBuilder) {
	          g = executeExtension(o,p,m);
	        }
	      }
	    } catch (CoreException ex) {
	      System.out.println(ex.getMessage());
	    }
	  return g;
  }

  private void getOrAddExtensionPoint() {
	  if (point != null) return;
	  IExtensionRegistry registry = Platform.getExtensionRegistry();
	  IExtensionPoint point = registry.getExtensionPoint(IGRAPHBUILDER_ID);
	  this.point = point;
}

public DirectedGraph<TypeNode, Dependency> loadPackageGraph(DirectedGraph<TypeNode, Dependency> g, IProgressMonitor m){
	  DirectedGraph<TypeNode, Dependency> pg = null;
	  getOrAddExtensionPoint();
	  if(point == null) return null;
	  IConfigurationElement[] config = point.getConfigurationElements();
	  try {
	      for (IConfigurationElement e : config) {
	        final Object o = e.createExecutableExtension("class");
	        if (o instanceof GraphBuilder) {
	          pg = ((GraphBuilder) o).buildPackageGraph(g, m);
	        }
	      }
	    } catch (CoreException ex) {
	      System.out.println(ex.getMessage());
	    }
	  return pg;
  }
  private DirectedGraph<TypeNode, Dependency> executeExtension(final Object o, final IProject p, final IProgressMonitor m) {
	  return ((GraphBuilder) o).buildGraph(p, m);
  }
} 
