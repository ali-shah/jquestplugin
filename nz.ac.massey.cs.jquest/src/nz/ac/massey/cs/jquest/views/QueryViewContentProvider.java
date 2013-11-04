package nz.ac.massey.cs.jquest.views;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.DependencyType;
import nz.ac.massey.cs.jdg.TypeNode;
//import nz.ac.massey.cs.gql4jung.Dependency;
import nz.ac.massey.cs.guery.ComputationMode;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.MotifInstance;
import nz.ac.massey.cs.guery.MotifReader;
import nz.ac.massey.cs.guery.MotifReaderException;
import nz.ac.massey.cs.guery.PathFinder;
import nz.ac.massey.cs.guery.adapters.jung.JungAdapter;
import nz.ac.massey.cs.guery.impl.BreadthFirstPathFinder;
import nz.ac.massey.cs.guery.impl.MultiThreadedGQLImpl;
import nz.ac.massey.cs.guery.io.dsl.DefaultMotifReader;
import nz.ac.massey.cs.guery.util.Cursor;
import nz.ac.massey.cs.guery.util.ResultCollector;
import nz.ac.massey.cs.jquest.handlers.GraphBuilderHandler;
import nz.ac.massey.cs.jquest.utils.Utils;
import nz.ac.massey.cs.jquest.views.QueryResults.QueryResultListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * The content provider class is responsible for providing objects to the
 * view. It can wrap existing objects in adapters or simply return objects
 * as-is. These objects may be sensitive to the current input of the view,
 * or ignore it and always show the same content (like Task List, for
 * example).
 */
class QueryViewContentProvider extends ViewContentProvider {
	private static DirectedGraph<TypeNode, Dependency> g = null;
	private static DirectedGraph<TypeNode, Dependency> pg = null;
//	private GraphViewer viewer;
	private IJavaElement selection;
	private static ElementChangedListener l = null;
	private static String srcNodeName = null;
	private static TypeNode srcNode = null;
	private static TypeNode tarNode = null;
	private static IProject selectedProject = null;
	private GraphBuilderHandler h;
	private boolean showIncoming = true;
	private boolean showOutgoing = true;
	private boolean showExternal = true;
	private IJavaElement[] selections;
	private String tarNodeName;
//	private ResultCollector<TypeNode, Dependency> registry;
	private QueryResults registry = null;
	private Map<TypeNode,Integer> ordered = new HashMap<TypeNode, Integer>();
	private MotifInstance<TypeNode, Dependency> currentInstance = null;
	private VisualizationForm form;
	private QueryView view;
	private boolean isPackage = false;
	private boolean isInCriticalDependenciesMode = false;
	private static Set<Dependency>  top100CriticalEdges;
	private Dependency currentCriticalEdge;
	private static ComputationMode queryMode;
	
	
	public QueryViewContentProvider(IJavaElement[] selectedItems, ElementChangedListener l2, boolean showIncoming, boolean showOutgoing, boolean external) {
		super();
		l = l2;
		this.showIncoming = showIncoming;
		this.showOutgoing = showOutgoing; 
		this.showExternal  = external; 
		this.selections =  selectedItems;
		selectedProject = selections[0].getJavaProject().getProject();		
	}
	public QueryViewContentProvider(Object selectedNode, Object selectedItem, ElementChangedListener l2, boolean showIncoming, boolean showOutgoing, boolean external) {
		l = l2;
		this.showIncoming = showIncoming;
		this.showOutgoing = showOutgoing; 
		this.showExternal  = external; 
		this.selection = (IJavaElement) selectedItem;
		selectedProject = selection.getJavaProject().getProject();	
		srcNode = (TypeNode) selectedNode;
	}
	public QueryViewContentProvider(IProject prj, IJavaElement[] selections2,
			ElementChangedListener l2, VisualizationForm f, QueryView queryView) {
		l = l2;
		this.showIncoming = f.getIncoming().getSelection();
		this.showOutgoing = f.getOutgoing().getSelection(); 
		this.showExternal  = f.getExternal().getSelection(); 
		this.selections =  selections2;
		selectedProject = prj;	
		this.form = f;
		this.view = queryView;
	}
	public QueryViewContentProvider(IProject prj, ElementChangedListener l2,
			VisualizationForm visualizationForm, QueryView queryView) {
		l=l2;
		selectedProject = prj;
		this.form = visualizationForm;
		this.view = queryView;
		
	}
	public Object[] getElements(Object inputElement) {
//		System.out.println("entered getElements()");
		Object[] typenodes = null;
		if(isInCriticalDependenciesMode) {
//			this.currentCriticalEdge = registry.getNextCritical();
			if(this.currentCriticalEdge == null) return new Object[]{};
			return new Object[]{currentCriticalEdge.getStart()};
		}
		try {
			if(inputElement != null && inputElement instanceof TypeNode) {
				return getTypeNodesFromSelection(inputElement);
			}
			typenodes = getTypeNodes(inputElement);
				
		} catch(Exception e) {
			e.printStackTrace();
			return new Object[]{}; //an empty array
		}
		if(typenodes.length == 0 && srcNode != null & tarNode != null ) {
			String m = "No dependency found between " + srcNodeName + " and " + tarNodeName +
					". \nDo you want to search between " + tarNodeName + " and " + srcNodeName;
			displayMessage(m);
		}
		return typenodes;			
	  }
	  
	private void displayMessage(String message) {
//		Display display = new Display();
//		final Shell shell = new Shell(display);
		MessageBox mb = new MessageBox(view.getSite().getWorkbenchWindow().getShell(),SWT.ICON_QUESTION | SWT.YES| SWT.NO);
		mb.setMessage(message);
		mb.setText("Status");
		int returnCode = mb.open();
		if(returnCode == 64) {
			process(tarNodeName, srcNodeName);
			view.viewer.setContentProvider(this);
			view.viewer.setLabelProvider(new ViewLabelProvider());
			view.viewer.setInput(null);
			
		} else {
			
		}
	}

	private Object[] getTypeNodesFromSelection(Object inputElement) {
		TypeNode selectedNode = (TypeNode) inputElement;
		srcNode = selectedNode;
		srcNodeName = Utils.removeTrailingDot(selectedNode.getFullname());
		return getNodes(selectedNode);
	}

	private Object[] getTypeNodes(Object inputElement) throws JavaModelException {
		return getNodes();
	}
	
	public void processCriticalDependencies(List<Motif<TypeNode, Dependency>> motifs) {
		isInCriticalDependenciesMode = true;
		validateOrAddGraph();
		registry = null;
		registry = queryCriticalDependencies(g, motifs, queryMode);
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
		}
//		top100CriticalEdges = edgesWithHighestRank;
		registry.setCriticalDeps(edgesWithHighestRank);
		this.currentCriticalEdge = registry.getNextCritical();
		this.form.setRegistry(registry);
		view.viewer.setContentProvider(this);
		view.viewer.setLabelProvider(new ViewLabelProvider());
		view.viewer.setInput(null);
	}
	
	public void processQuery(Motif<TypeNode, Dependency> motif) {
		validateOrAddGraph();
		registry = null;
		registry = query(g, motif, queryMode);
		this.form.setRegistry(registry);
		if(registry.getNumberOfInstances() == 0) {
			displayMessage();
			view.clearGraph(view.viewer.getGraphControl());
		} else {
			view.viewer.setContentProvider(this);
			view.viewer.setLabelProvider(new ViewLabelProvider());
			view.viewer.setInput(null);
		}
	}
	
	private void displayMessage() {
		MessageBox mb = new MessageBox(view.getSite().getWorkbenchWindow().getShell(),SWT.ICON_INFORMATION | SWT.OK);
		mb.setMessage("No instances found");
		mb.setText("Status");
		mb.open();
	}
	public void process(String src, String tar) {
		validateOrAddGraph();
		if(isPackage){
			srcNode = Utils.getNode(pg, src);
			tarNode = Utils.getNode(pg, tar);
			
		} else {
			srcNode = Utils.getNode(g, src);
			tarNode = Utils.getNode(g, tar);	
		}
		
		if(tarNode == null || srcNode == null) {
//			displayMessage();
			return;
		}
		srcNodeName = srcNode.getFullname();
		tarNodeName = tarNode.getFullname();
		String adhocQuery = "motif adhoc \n" +
				  "select src, tar \n" +
				  "where \"src.fullname=='" + srcNodeName + "'\" and \"tar.fullname=='" + tarNodeName +"'\" \n" +
				  "connected by uses(src>tar)\n" +
				  "where \"uses.hasType('USES')\"" +
				  "group by \"src\"";
		Motif<TypeNode, Dependency> m = loadMotif(new ByteArrayInputStream(adhocQuery.getBytes()));
		
		registry = null;
		System.out.println("starting querying");
		if(isPackage)registry = query(pg,m,queryMode);
		else registry = query(g,m,queryMode);
		System.out.println("finished querying");
		this.form.setRegistry(registry);
		
//		this.form.setNextInstanceEnabled(registry.hasNextMajorInstance() || registry.hasNextMinorInstance());
		
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
				  "connected by uses(src>tar)\n" +
				  "where \"uses.hasType('USES') || uses.hasType('EXTENDS') || uses.hasType('IMPLEMENTS')\"";
		Motif<TypeNode, Dependency> m = loadMotif(new ByteArrayInputStream(adhocQuery.getBytes()));
		registry = null;
		registry = query(g,m, queryMode);
		this.form.setRegistry(registry);
	}
	
	private Object[] getNodes() {
		if(currentInstance == null && registry.hasNextMajorInstance()) {
			Cursor c = registry.nextMajorInstance();
			currentInstance = registry.getInstance(c);
		} else if (currentInstance == null && registry.hasNextMinorInstance()) {
			Cursor c = registry.nextMinorInstance();
			currentInstance = registry.getInstance(c);
		}
		
//		c = registry.nextMinorInstance();
//		MotifInstance currentInstance1 = registry.getInstance(c);
//		c = registry.nextMinorInstance();
//		MotifInstance currentInstance2= registry.getInstance(c);
		if(currentInstance == null) {
			return new Object[]{};
		}
		Set<TypeNode> nodes = currentInstance.getVertices();
		return nodes.toArray();
	}
	private Object[] getNodes(TypeNode selectedNode) {
		Object[] inNodes = new Object[selectedNode.getInEdges().size()];
		int i = 0;
		Iterator<Dependency> iter = selectedNode.getInEdges().iterator();
		while (iter.hasNext()) {
		  inNodes[i++] = iter.next().getStart();
		}
		if(showIncoming && showOutgoing) {
			Object[] typenodes = new Object[inNodes.length+1];
			typenodes[0] = selectedNode;
			i = 1;
			for(Object node : inNodes) {
				typenodes[i++] = node;
			}
			return typenodes;
		} else if(showIncoming) {
			Object[] typenodes = new Object[inNodes.length];
			i = 0;
			for(Object node : inNodes) {
				typenodes[i++] = node;
			}
			return typenodes;
		} else if(showOutgoing) {
			return new Object[]{selectedNode};
		} else {
			return new Object[] {};
		}
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
		int i = 1;
		for (Dependency e : edges) {
			TypeNode start = e.getStart();
			TypeNode end = e.getEnd();
			if(start.getFullname().equals(selected.getFullname()) && !end.getFullname().equals(selected.getFullname())) {
				return new Object[]{end};
			}
//			if (!ordered.containsKey(start))
//				ordered.put(start, i++);
//			if (!ordered.containsKey(end))
//				ordered.put(end, i++);
		}
//		int pos = ordered.get(selected) + 1;
//		TypeNode toReturn = null;
//		for (Map.Entry<TypeNode, Integer> e : ordered.entrySet()) {
//			if (e.getValue() == pos) {
//				toReturn = e.getKey();
//			}
//
//		}
		return new Object[]{};
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
						pg = h.loadPackageGraph(g, monitor);
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
	  
	  public QueryResults queryCriticalDependencies(final DirectedGraph<TypeNode, Dependency> g,
				final List<Motif<TypeNode, Dependency>> motifs, final ComputationMode mode) {
		  final MultiThreadedGQLImpl<TypeNode, Dependency> engine = new MultiThreadedGQLImpl<TypeNode, Dependency>();
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
							engine.query(new JungAdapter<TypeNode, Dependency>(g), motif, registry,
									mode, pFinder);	
						}
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//			for (Motif<TypeNode, Dependency> motif : motifs) {
//				engine.query(new JungAdapter<TypeNode, Dependency>(g), motif, registry,
//						queryMode, pFinder);	
//			}
			
			return registry;
	  }
	  public static QueryResults query(final DirectedGraph<TypeNode, Dependency> g,
				final Motif<TypeNode, Dependency> motif, final ComputationMode mode) {
//			String outfolder = "";
			final MultiThreadedGQLImpl<TypeNode, Dependency> engine = new MultiThreadedGQLImpl<TypeNode, Dependency>();
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
						engine.query(new JungAdapter<TypeNode, Dependency>(g), motif, registry,
									mode, pFinder);
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
	public void setIsPackage(boolean f) {
		this.isPackage = f;
	}
	public void setCurrentCriticalDep(Dependency nextCritical) {
		this.currentCriticalEdge  = nextCritical;
		
	}
	public void setQueryMode(ComputationMode mode) {
		this.queryMode = mode;
	}
	
	
}
