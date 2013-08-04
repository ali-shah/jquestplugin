package nz.ac.massey.cs.jquest.handlers;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
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
  
  public DirectedGraph<TypeNode, TypeRef> loadGraph(IProject p, IProgressMonitor m){
	  
	  DirectedGraph<TypeNode, TypeRef> g = null;
	  IExtensionRegistry registry = Platform.getExtensionRegistry();
	  IExtensionPoint point = registry.getExtensionPoint(IGRAPHBUILDER_ID);
	  if (point == null) return null;
	  IConfigurationElement[] config = point.getConfigurationElements();
	  try {
	      for (IConfigurationElement e : config) {
//	        System.out.println("Evaluating extension");
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

  private DirectedGraph<TypeNode, TypeRef> executeExtension(final Object o, final IProject p, final IProgressMonitor m) {
	  return ((GraphBuilder) o).buildGraph(p, m);
  }
} 
