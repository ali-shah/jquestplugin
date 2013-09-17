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

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
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

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * The content provider class is responsible for providing objects to the
 * view. It can wrap existing objects in adapters or simply return objects
 * as-is. These objects may be sensitive to the current input of the view,
 * or ignore it and always show the same content (like Task List, for
 * example).
 */
class QueryViewContentProvider implements AbstractContentProvider {
	private static DirectedGraph<TypeNode, TypeRef> g = null;
	private static DirectedGraph<TypeNode, TypeRef> pg = null;
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
//	private ResultCollector<TypeNode, TypeRef> registry;
	private QueryResults registry = null;
	private Map<TypeNode,Integer> ordered = new HashMap<TypeNode, Integer>();
	private MotifInstance<TypeNode, TypeRef> currentInstance = null;
	private VisualizationForm form;
	private QueryView view;
	
	
	
	
	public QueryViewContentProvider(IJavaElement[] selectedItems, ElementChangedListener l2, boolean showIncoming, boolean showOutgoing, boolean external) {
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
		Object[] typenodes = null;
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
			String m = "No dependency found between " + srcNode.getName() + " and " + tarNode.getName() +
					". \nDo you want to search between " + tarNode.getName() + " and " + srcNode.getName();
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
			process(tarNode.getFullname(), srcNode.getFullname());
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
//		if(selections == null) return new Object[]{};
//		else {
//			selectedProject = selections[0].getJavaProject().getProject();
//			validateOrAddGraph();
//		}
//		if(selections[0].getElementType() == IJavaElement.COMPILATION_UNIT) {
//			String src = ((ICompilationUnit)selections[0]).getTypes()[0].getFullyQualifiedName();
//			String tar = ((ICompilationUnit)selections[1]).getTypes()[0].getFullyQualifiedName();
//			process(src, tar);
//			
//		} else if(selection.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
//			String packageName = ((IPackageFragment) selection).getElementName();
//			if(pg == null) { 
//				if(h == null) h = new GraphBuilderHandler();	
//				pg = h.loadPackageGraph(g, new NullProgressMonitor());
//			}
//			tarNode = Utils.getNode(pg, packageName);
//			srcNodeName = packageName;
//			srcNode = tarNode;
//			
//		} else {
//			return new Object[]{};
//		}

		return getNodes();
		
	}
	
	public void processQuery(Motif<TypeNode, TypeRef> motif) {
		validateOrAddGraph();
		registry = null;
		registry = query(g, motif);
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
		srcNode = Utils.getNode(g, src);
		srcNodeName = src;
		tarNodeName = tar;
		tarNode = Utils.getNode(g, tar);
		if(tarNode == null || srcNode == null) {
//			displayMessage();
			return;
		}
		String adhocQuery = "motif adhoc \n" +
				  "select src, tar \n" +
				  "where \"src.fullname=='" + srcNode.getFullname() + "'\" and \"tar.fullname=='" + tarNode.getFullname() +"'\" \n" +
				  "connected by uses(src>tar) find all \n" +
				  "where \"uses.type=='uses'\"" +
				  "group by \"src\"";
				  		
		Motif<TypeNode, TypeRef> m = loadMotif(new ByteArrayInputStream(adhocQuery.getBytes()));
		registry = null;
		registry = query(g,m);
		this.form.setRegistry(registry);
		
//		this.form.setNextInstanceEnabled(registry.hasNextMajorInstance() || registry.hasNextMinorInstance());
		
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
		if(currentInstance == null) return new Object[]{};
		Set<TypeNode> nodes = currentInstance.getVertices();
		return nodes.toArray();
	}
	private Object[] getNodes(TypeNode selectedNode) {
		Object[] inNodes = new Object[selectedNode.getInEdges().size()];
		int i = 0;
		Iterator<TypeRef> iter = selectedNode.getInEdges().iterator();
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
		TypeNode selected = (TypeNode) entity;
		List<TypeRef> edges = new ArrayList<TypeRef>();//currentInstance.getPath("uses").getEdges();
		for(String role : currentInstance.getMotif().getPathRoles()) {
			edges.addAll(currentInstance.getPath(role).getEdges());
		}
		ordered.clear();
		int i = 1;
		for (TypeRef e : edges) {
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
	  
	  public static QueryResults query(DirectedGraph<TypeNode, TypeRef> g,
				Motif<TypeNode, TypeRef> motif) {
//			String outfolder = "";
			MultiThreadedGQLImpl<TypeNode, TypeRef> engine = new MultiThreadedGQLImpl<TypeNode, TypeRef>();
			PathFinder<TypeNode, TypeRef> pFinder = new BreadthFirstPathFinder<TypeNode, TypeRef>(true);

			final QueryResults registry = new QueryResults();

			engine.query(new JungAdapter<TypeNode, TypeRef>(g), motif, registry,
						ComputationMode.ALL_INSTANCES, pFinder);
			return registry;
		}

	  private Motif<TypeNode, TypeRef> loadMotif(InputStream s) {
		  MotifReader<TypeNode, TypeRef> motifReader = new DefaultMotifReader<TypeNode, TypeRef>();
		  Motif<TypeNode, TypeRef> motif = null;
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
	}
