package nz.ac.massey.cs.jquest.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;
//import nz.ac.massey.cs.gql4jung.Dependency;
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
class ViewContentProvider implements AbstractContentProvider  {
	private static DirectedGraph<TypeNode, Dependency> g = null;
	private static DirectedGraph<TypeNode, Dependency> pg = null;
//	private GraphViewer viewer;
	private IJavaElement selection;
	private static ElementChangedListener l = null;
	private static String selectedNodeName = null;
	private static TypeNode baseNode = null;
	private static IProject selectedProject = null;
	private GraphBuilderHandler h;
	private boolean showClassNameOnly = false;
	private boolean showIncoming = true;
	private boolean showOutgoing = true;
	private boolean showExternal = true; 
	
	
	
	public ViewContentProvider(Object selectedItem, ElementChangedListener l2, boolean showIncoming, boolean showOutgoing, boolean external) {
		l = l2;
		this.showIncoming = showIncoming;
		this.showOutgoing = showOutgoing; 
		this.showExternal  = external; 
		this.selection = (IJavaElement) selectedItem;
		selectedProject = selection.getJavaProject().getProject();		
	}
	public ViewContentProvider(Object selectedNode, Object selectedItem, ElementChangedListener l2, boolean showIncoming, boolean showOutgoing, boolean external) {
		l = l2;
		this.showIncoming = showIncoming;
		this.showOutgoing = showOutgoing; 
		this.showExternal  = external; 
		this.selection = (IJavaElement) selectedItem;
		selectedProject = selection.getJavaProject().getProject();	
		baseNode = (TypeNode) selectedNode;
	}
	public ViewContentProvider() {
		// TODO Auto-generated constructor stub
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
		baseNode = selectedNode;
		selectedNodeName = Utils.removeTrailingDot(selectedNode.getFullname());
		return getNodes(selectedNode);
//		if(selectedNode.getId().equals("package")){
//			
//		}
	}

	private Object[] getTypeNodes(Object inputElement) throws JavaModelException {
		if(selection == null) return new Object[]{};
		else {
			selectedProject = selection.getJavaProject().getProject();
			validateOrAddGraph();
		}
//		if(inputElement == null) return new Object[]{};
//		if(inputElement instanceof IJavaElement) {
//			selection = (IJavaElement) inputElement;
//			selectedProject = selection.getJavaProject().getProject();
//			validateOrAddGraph();
//		}
		TypeNode selectedNode = null;
		if(selection.getElementType() == IJavaElement.COMPILATION_UNIT) {
			String classname = ((ICompilationUnit)selection).getTypes()[0].getFullyQualifiedName();
			selectedNode = Utils.getNode(g, classname);
			selectedNodeName = classname;
			baseNode = selectedNode;
		} else if(selection.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			String packageName = ((IPackageFragment) selection).getElementName();
			if(pg == null) { 
				if(h == null) h = new GraphBuilderHandler();	
				pg = h.loadPackageGraph(g, new NullProgressMonitor());
			}
			selectedNode = Utils.getNode(pg, packageName);
			selectedNodeName = packageName;
			baseNode = selectedNode;
			
		} else {
			return new Object[]{};
		}
		if(selectedNode == null) {
//			displayMessage();
			return new Object[]{};
		}
		return getNodes(selectedNode);
		
		
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
		  TypeNode n = (TypeNode) entity;
		  Iterator<Dependency> iter = n.getOutEdges().iterator();
		  Object[] outNodes = new Object[n.getOutEdges().size()];
		  int i = 0;
		  String fullname = Utils.removeTrailingDot(n.getFullname());
		  if(fullname.equals(selectedNodeName)) {
			  while(iter.hasNext()) {
				  Object end = iter.next().getEnd();
				  TypeNode node = (TypeNode) end;
				  if(!showExternal){
					  if(node.getContainer().equals(baseNode.getContainer())){
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
				  if(fullname1.equals(selectedNodeName)) {
					  outNodes[i++] = end;
				  }
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
		  return baseNode;
	  }
	}
