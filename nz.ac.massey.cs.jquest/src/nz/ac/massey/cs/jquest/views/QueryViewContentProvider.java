package nz.ac.massey.cs.jquest.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
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
class QueryViewContentProvider implements IGraphEntityContentProvider {
	private static DirectedGraph<TypeNode, TypeRef> g = null;
	private static DirectedGraph<TypeNode, TypeRef> pg = null;
//	private GraphViewer viewer;
	private IJavaElement selection;
	private static ElementChangedListener l = null;
	private static String srcNodeName = null;
	private static TypeNode srcNode = null;
	private static IProject selectedProject = null;
	private GraphBuilderHandler h;
	private boolean showIncoming = true;
	private boolean showOutgoing = true;
	private boolean showExternal = true;
	private IJavaElement[] selections;
	private String tarNodeName;
	private ResultCollector<TypeNode, TypeRef> registry; 
	
	
	
	
	
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
		return typenodes;			
	  }
	  
//	private void displayMessage() {
//		MessageBox mb = new MessageBox(getSite().getWorkbenchWindow().getShell(),SWT.ICON_ERROR);
//		mb.setMessage("An error has occured. Close and restart the view.");
//		mb.setText("Status");
//		mb.open();
//	}

	private Object[] getTypeNodesFromSelection(Object inputElement) {
		TypeNode selectedNode = (TypeNode) inputElement;
		srcNode = selectedNode;
		srcNodeName = Utils.removeTrailingDot(selectedNode.getFullname());
		return getNodes(selectedNode);
	}

	private Object[] getTypeNodes(Object inputElement) throws JavaModelException {
		if(selections == null) return new Object[]{};
		else {
			selectedProject = selections[0].getJavaProject().getProject();
			validateOrAddGraph();
		}
		TypeNode srcNode = null;
		TypeNode tarNode = null;
		if(selections[0].getElementType() == IJavaElement.COMPILATION_UNIT) {
			String src = ((ICompilationUnit)selections[0]).getTypes()[0].getFullyQualifiedName();
			tarNode = Utils.getNode(g, src);
			srcNodeName = src;
			srcNode = tarNode;
			
			String tar = ((ICompilationUnit)selections[1]).getTypes()[0].getFullyQualifiedName();
			tarNodeName = tar;
			tarNode = Utils.getNode(g, tar);
			
		} else if(selection.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			String packageName = ((IPackageFragment) selection).getElementName();
			if(pg == null) { 
				if(h == null) h = new GraphBuilderHandler();	
				pg = h.loadPackageGraph(g, new NullProgressMonitor());
			}
			tarNode = Utils.getNode(pg, packageName);
			srcNodeName = packageName;
			srcNode = tarNode;
			
		} else {
			return new Object[]{};
		}
		if(tarNode == null || srcNode == null) {
//			displayMessage();
			return new Object[]{};
		}
		return getNodes(srcNode, tarNode);
		
		
	}
	private Object[] getNodes(TypeNode srcNode2, TypeNode tarNode) {
		String adhocQuery = "motif adhoc \n" +
"select src, tar \n" +
"where \"src.name=='" + srcNode2.getFullname() + "'\" and \"tar.name=='" + tarNode.getFullname() +"'\" \n" +
"connected by uses(src>tar) \n" +
"group by \"src\"";
		
		Motif<TypeNode, TypeRef> m = loadMotif(new ByteArrayInputStream(adhocQuery.getBytes()));
		this.registry = query(g,m);
		List<MotifInstance<TypeNode, TypeRef>> list = registry.getInstances();
//		for(MotifInstance<TypeNode, TypeRef> mi : list) {
		Set<TypeNode> nodes = list.get(0).getVertices();
//		}
		Object[] toReturn = nodes.toArray();
		return toReturn;
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
		  TypeNode n = (TypeNode) entity;
		  Iterator<TypeRef> iter = n.getOutEdges().iterator();
		  Object[] outNodes = new Object[n.getOutEdges().size()];
		  int i = 0;
		  String fullname = Utils.removeTrailingDot(n.getFullname());
		  if(fullname.equals(srcNodeName)) {
			  while(iter.hasNext()) {
				  Object end = iter.next().getEnd();
				  TypeNode node = (TypeNode) end;
				  if(!showExternal){
					  if(node.getContainer().equals(srcNode.getContainer())){
						  outNodes[i++] = end;	  
					  }  
				  } else {
					  outNodes[i++] = end;	  
				  }
			  }
		  } else {
			  while(iter.hasNext()) {
				  Object end = iter.next().getEnd();
				  String fullname1 = Utils.removeTrailingDot(((TypeNode) end).getFullname());
//				  if(fullname1.equals(srcNodeName)) {
					  outNodes[i++] = end;
//				  }
			  }
		  }
	      return  outNodes; 
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
	  
	  public static ResultCollector<TypeNode, TypeRef> query(DirectedGraph<TypeNode, TypeRef> g,
				Motif<TypeNode, TypeRef> motif) {
//			String outfolder = "";
			MultiThreadedGQLImpl<TypeNode, TypeRef> engine = new MultiThreadedGQLImpl<TypeNode, TypeRef>();
			PathFinder<TypeNode, TypeRef> pFinder = new BreadthFirstPathFinder<TypeNode, TypeRef>(true);

			final ResultCollector<TypeNode, TypeRef> registry = new ResultCollector<TypeNode, TypeRef>();

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
	}
