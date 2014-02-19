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
package nz.ac.massey.cs.jquest.handlers;

import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.graphbuilder.GraphBuilder;
import nz.ac.massey.cs.jquest.graphquery.GraphQuery;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;

import edu.uci.ics.jung.graph.DirectedGraph;


public class GraphQueryHandler {
  private static final String IGRAPHBUILDER_ID = 
      "nz.ac.massey.cs.jquest.graphquery";
private IExtensionPoint point;
  
  public DirectedGraph<TypeNode, Dependency> loadGraph(IProject p, IProgressMonitor m){
	  
	  DirectedGraph<TypeNode, Dependency> g = null;
	  getOrAddExtensionPoint();
	  if(point == null) return null;
	  IConfigurationElement[] config = point.getConfigurationElements();
	  try {
	      for (IConfigurationElement e : config) {
	        final Object o = e.createExecutableExtension("query");
	        if (o instanceof GraphQuery) {
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

  private DirectedGraph<TypeNode, Dependency> executeExtension(final Object o, final IProject p, final IProgressMonitor m) {
	  return ((GraphBuilder) o).buildGraph(p, m);
  }
} 
