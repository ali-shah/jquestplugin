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
package nz.ac.massey.cs.jquest.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.ac.massey.cs.guery.ComputationMode;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.MotifInstance;
import nz.ac.massey.cs.guery.MotifReader;
import nz.ac.massey.cs.guery.MotifReaderException;
import nz.ac.massey.cs.guery.PathFinder;
import nz.ac.massey.cs.guery.adapters.jung.JungAdapter;
import nz.ac.massey.cs.guery.impl.BreadthFirstPathFinder;
import nz.ac.massey.cs.guery.impl.GQLImpl;
import nz.ac.massey.cs.guery.io.dsl.DefaultMotifReader;
import nz.ac.massey.cs.guery.util.Cursor;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.graphbuilder.JungSourceOnlyAdapter;
import nz.ac.massey.cs.jquest.handlers.GraphBuilderHandler;
import nz.ac.massey.cs.jquest.utils.Utils;
import nz.ac.massey.cs.jquest.views.QueryResults.QueryResultListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.zest.core.viewers.IGraphContentProvider;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * The content provider class is responsible for providing objects to the
 * view. It can wrap existing objects in adapters or simply return objects
 * as-is. These objects may be sensitive to the current input of the view,
 * or ignore it and always show the same content (like Task List, for
 * example).
 */
class QVContentProvider implements IGraphContentProvider {
	private static DirectedGraph<TypeNode, Dependency> g = null;
	private static ElementChangedListener l = null;
	private static String srcNodeName = null;
	private static TypeNode srcNode = null;
	private static IProject selectedProject = null;
	private GraphBuilderHandler h;
	private IJavaElement[] selections;
	private static String tarNodeName;
	private static QueryResults registry = null;
	private Map<TypeNode,Integer> ordered = new HashMap<TypeNode, Integer>();
	private MotifInstance<TypeNode, Dependency> currentInstance = null;
	private VisualizationForm form;
	private QueryView view;
	private boolean isInCriticalDependenciesMode = false;
	private Dependency currentCriticalEdge;
	private static ComputationMode queryMode;
	
	public QVContentProvider(IProject prj,IJavaElement[] selections2, ElementChangedListener l2,
			VisualizationForm visualizationForm, QueryView queryView) {
		l=l2;
		this.selections =  selections2;
		selectedProject = prj;
		this.form = visualizationForm;
		this.view = queryView;
		
	}
	public Object[] getElements(Object inputElement) {
		Object[] dependencies = null;
		if(isInCriticalDependenciesMode) {
			if(this.currentCriticalEdge == null) return new Object[]{};
			return new Object[]{currentCriticalEdge};
		} else {
			dependencies = getEdges();
			if(dependencies.length == 0 && selections != null) {
				String m = "No dependency found between " + selections[0].getElementName() + " and " + selections[1].getElementName() +
						". \nDo you want to search between " + selections[1].getElementName() + " and " + selections[0].getElementName();
				displayMessage(m);
			}
			return dependencies;
		}	
	  }
	  
	private void displayMessage(String message) {
		MessageBox mb = new MessageBox(view.getSite().getWorkbenchWindow().getShell(),SWT.ICON_QUESTION | SWT.YES| SWT.NO);
		mb.setMessage(message);
		mb.setText("Status");
		int returnCode = mb.open();
		if(returnCode == 64) {
			String query = Utils.composeQuery(tarNodeName, srcNodeName);
			processAdhocQuery(query, tarNodeName, srcNodeName );
		} 
	}

	public void processCriticalDependencies(List<Motif<TypeNode, Dependency>> motifs) {
		isInCriticalDependenciesMode = true;
		validateOrAddGraph();
		registry = null;
		registry = query(g, motifs, queryMode, true);
		Set<Dependency> edgesWithHighestRank = Utils.findLargestByIntRanking(g.getEdges(),
				new Function<Dependency, Integer>() {
					@Override
					public Integer apply(Dependency e) {
						return registry.getCount(e);
					}
				});
		if(edgesWithHighestRank.size() == 0) {
			displayMessage();
			view.clearGraph(view.viewer.getGraphControl());
			return;
		}
		registry.setCriticalDeps(edgesWithHighestRank);
		this.currentCriticalEdge = registry.getNextCritical();
		this.form.setRegistry(registry);
		view.viewer.setContentProvider(this);
		view.viewer.setLabelProvider(new ZestLabelProvider());
		view.viewer.setInput(null);
	}
	
	public void processQuery(Motif<TypeNode, Dependency> motif) {
		validateOrAddGraph();
		registry = null;
		List<Motif<TypeNode, Dependency>> motifs = new ArrayList<Motif<TypeNode, Dependency>>();
		motifs.add(motif);
		registry = query(g, motifs, queryMode, true);
		this.form.setRegistry(registry);
		if(registry.getNumberOfInstances() == 0) {
			displayMessage();
			view.clearGraph(view.viewer.getGraphControl());
		} else {
			view.viewer.setContentProvider(this);
			view.viewer.setLabelProvider(new ZestLabelProvider());
			view.viewer.setInput(null);
		}
	}
	
	public void processAdhocQuery(String query, String src, String tar) {
		srcNodeName = src;
		tarNodeName = tar;
		validateOrAddGraph();
		Motif<TypeNode, Dependency> m = loadMotif(new ByteArrayInputStream(query.getBytes()));
		List<Motif<TypeNode, Dependency>> motifs = new ArrayList<Motif<TypeNode, Dependency>>();
		motifs.add(m);
		registry = null;
		registry = query(g, motifs, queryMode, false);
		this.form.setRegistry(registry);
		view.viewer.setContentProvider(this);
		view.viewer.setLabelProvider(new ZestLabelProvider());
		view.viewer.setInput(null);
	}
	
	private void displayMessage() {
		MessageBox mb = new MessageBox(view.getSite().getWorkbenchWindow().getShell(),SWT.ICON_INFORMATION | SWT.OK);
		mb.setMessage("No instances found");
		mb.setText("Status");
		mb.open();
	}
	
	public void processLibrary(String libName) {
		validateOrAddGraph();
		Set<String> containers = new HashSet<String>();
		for(TypeNode tn : g.getVertices()) {
			containers.add(tn.getContainer());
		}
		String srcContainer = "_src";
		String adhocQuery = "motif adhoc \n" +
				  "select src, tar \n" +
				  "where \"src.container=='" + srcContainer + "'\" and \"tar.container=='" + libName +"'\" \n" +
				  "connected by uses(src>tar)[1,1]\n" +
				  "where \"uses.hasType('USES') || uses.hasType('EXTENDS') || uses.hasType('IMPLEMENTS')\"";
		Motif<TypeNode, Dependency> m = loadMotif(new ByteArrayInputStream(adhocQuery.getBytes()));
		registry = null;
		List<Motif<TypeNode, Dependency>> motifs = new ArrayList<Motif<TypeNode, Dependency>>();
		motifs.add(m);
		registry = query(g, motifs, queryMode, false);
		this.form.setRegistry(registry);
	}
	
	private Object[] getEdges() {
		if(currentInstance == null && registry.hasNextMajorInstance()) {
			Cursor c = registry.nextMajorInstance();
			currentInstance = registry.getInstance(c);
		} else if (currentInstance == null && registry.hasNextMinorInstance()) {
			Cursor c = registry.nextMinorInstance();
			currentInstance = registry.getInstance(c);
		}
		if(currentInstance == null) {
			return new Object[]{};
		}
		List<Dependency> edges = new ArrayList<Dependency>();//currentInstance.getPath("uses").getEdges();
		for(String role : currentInstance.getMotif().getPathRoles()) {
			edges.addAll(currentInstance.getPath(role).getEdges());
		}
		return edges.toArray();
	}
	public Object[] getConnectedTo(Object entity) {
		if(isInCriticalDependenciesMode ) {
			return new Object[]{currentCriticalEdge.getEnd()};
		}
		TypeNode selected = (TypeNode) entity;
		List<Dependency> edges = new ArrayList<Dependency>();//currentInstance.getPath("uses").getEdges();
		for(String role : currentInstance.getMotif().getPathRoles()) {
			edges.addAll(currentInstance.getPath(role).getEdges());
		}
		ordered.clear();
		int i = 0;
		Object[] tmpDeps = new Object[edges.size()];
		for (Dependency e : edges) {
			TypeNode start = e.getStart();
			TypeNode end = e.getEnd();
			if(start.getFullname().equals(selected.getFullname()) && !end.getFullname().equals(selected.getFullname())) {
				tmpDeps[i++] = end;
			}
		}
		
		Object[] depsToReturn = new Object[i];
		for(int j=0; j<i; j++) {
			depsToReturn[j] = tmpDeps[j];
		}
		return depsToReturn;
	  }
	private void validateOrAddGraph() {
		if (g == null || l.hasProjectModified() || l.hasProjectChanged(selectedProject)) {
			try {
				IWorkbench wb = PlatformUI.getWorkbench();
				IProgressService ps = wb.getProgressService();
				ps.busyCursorWhile(new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						 h = new GraphBuilderHandler();
						g = h.loadGraph(selectedProject, monitor);
//						pg = h.loadPackageGraph(g, monitor);
						l.reset();
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	 
	  public void dispose() { }
	  public void inputChanged(Viewer viewer1, Object oldInput, Object newInput) { 
//		  this.viewer = viewer1;
		  
	  }
	  
	  public TypeNode getSelectedNode() {
		  return srcNode;
	  }
	  
	  public static QueryResults query(final DirectedGraph<TypeNode, Dependency> g,
				final List<Motif<TypeNode, Dependency>> motifs, final ComputationMode mode, final boolean sourceOnly) {
			final GQLImpl<TypeNode, Dependency> engine = new GQLImpl<TypeNode, Dependency>();
			final PathFinder<TypeNode, Dependency> pFinder = new BreadthFirstPathFinder<TypeNode, Dependency>(true);
			final QueryResults registry = new QueryResults();
			try {
				IWorkbench wb = PlatformUI.getWorkbench();
				IProgressService ps = wb.getProgressService();
				ps.busyCursorWhile(new IRunnableWithProgress() {
					public void run(final IProgressMonitor monitor)
							throws InvocationTargetException,
							InterruptedException {
						monitor.beginTask("executing query", g.getVertexCount());
						registry.addListener(new QueryResultListener(){

							@Override
							public void resultsChanged(QueryResults source) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void progressMade(int progress, int total) {
								monitor.worked(progress);
							}
							
						});
						for (Motif<TypeNode, Dependency> motif : motifs) {
							if(sourceOnly) {
								engine.query(new JungSourceOnlyAdapter(new JungAdapter<TypeNode, Dependency>(g)), motif, registry,
										mode, pFinder);	
							} else {
								engine.query(new JungAdapter<TypeNode, Dependency>(g), motif, registry,
										mode, pFinder);	
							}
						}
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			return registry;
		}

	  private Motif<TypeNode, Dependency> loadMotif(InputStream s) {
		  MotifReader<TypeNode, Dependency> motifReader = new DefaultMotifReader<TypeNode, Dependency>();
		  Motif<TypeNode, Dependency> motif = null;
		try {
			motif = motifReader.read(s);
			 s.close();
		} catch (MotifReaderException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		  return motif;
	  }
	public QueryResults getRegistry() {
		return registry;
	}
	public void setCurrentInstance(MotifInstance instance) {
		this.currentInstance = instance;
	}
//	public void setIsPackage(boolean f) {
//		this.isPackage = f;
//	}
	public void setCurrentCriticalDep(Dependency nextCritical) {
		this.currentCriticalEdge  = nextCritical;
		
	}
	public void setQueryMode(ComputationMode mode) {
		queryMode = mode;
	}
	@Override
	public Object getSource(Object rel) {
		if(rel instanceof Dependency) {
			return ((Dependency) rel).getStart();
		}
		return null;
	}

	@Override
	public Object getDestination(Object rel) {
		if(rel instanceof Dependency) {
			return ((Dependency) rel).getEnd();
		}
		return null;
	}
	
}
