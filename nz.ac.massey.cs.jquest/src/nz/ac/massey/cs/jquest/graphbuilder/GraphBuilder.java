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
