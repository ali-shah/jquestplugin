package nz.ac.massey.cs.jquest.views;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
import nz.ac.massey.cs.jquest.handlers.GraphBuilderHandler;
import nz.ac.massey.cs.jquest.utils.Utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import edu.uci.ics.jung.graph.DirectedGraph;

public class SingleDependencyView extends ViewPart implements  ISelectionListener{
	
	private static DirectedGraph<TypeNode, TypeRef> g = null;
	private GraphViewer viewer;
	private static ElementChangedListener l = new ElementChangedListener();
	private static String selectedClassName = null;
	private static IProject selectedProject = null;

	/**
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */
	class ViewContentProvider implements IGraphEntityContentProvider {
		  
		public Object[] getElements(Object parent) {
			Object[] typenodes = getTypeNodes(selectedProject);
			return typenodes;			
		  }
		  
		private Object[] getTypeNodes(final IProject p) {
			if (g == null || l.projectHasChanged()) {
				try {
					IWorkbench wb = PlatformUI.getWorkbench();
					IProgressService ps = wb.getProgressService();
					ps.busyCursorWhile(new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							GraphBuilderHandler h = new GraphBuilderHandler();
							g = h.loadGraph(p, monitor);
							l.reset();
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			TypeNode selectedNode = Utils.getNode(g,selectedClassName);
			Object[] inNodes = new Object[selectedNode.getInEdges().size()];
			int i = 0;
			Iterator<TypeRef> iter = selectedNode.getInEdges().iterator();
			while (iter.hasNext()) {
			  inNodes[i++] = iter.next().getStart();
			}
			Object[] typenodes = new Object[inNodes.length+1];
			typenodes[0] = selectedNode;
			i = 1;
			for(Object node : inNodes) {
				typenodes[i++] = node;
			}

			return typenodes;
		}

		public Object[] getConnectedTo(Object entity) {
			  TypeNode n = (TypeNode) entity;
			  Iterator<TypeRef> iter = n.getOutEdges().iterator();
			  Object[] outNodes = new Object[n.getOutEdges().size()];
			  int i = 0;
			  
			  if(n.getFullname().equals(selectedClassName)) {
				  while(iter.hasNext()) {
					  Object end = iter.next().getEnd();
					  outNodes[i++] = end;
				  }
			  } else {
				  while(iter.hasNext()) {
					  Object end = iter.next().getEnd();
					  if(((TypeNode) end).getFullname().equals(selectedClassName)) {
						  outNodes[i++] = end;
					  }
				  }
			  }
		      return  outNodes; 
		  }
		 
		  public void dispose() { }
		  public void inputChanged(Viewer viewer1, Object oldInput, Object newInput) { 
			  viewer.refresh();
		  }
		}

	class ViewLabelProvider extends LabelProvider {
		  public String getText(Object element) {
		    if (!(element instanceof TypeNode))
		      return null;
		 
		    TypeNode project = (TypeNode) element;
		    return project.getFullname();
		  }
		 
		  public Image getImage(Object obj) {
		    return null;
		  }
		}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		getSite().getPage().addSelectionListener((ISelectionListener) this);
		viewer = new GraphViewer(parent, SWT.NONE);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING));
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED
				| ZestStyles.CONNECTIONS_DASH);
		viewer.setInput(null);
		JavaCore.addElementChangedListener(l);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public static void setSelection(String classname) {
		selectedClassName  = classname;
	}

	public static void setProject(IProject project) {
		
		selectedProject  = project;
		
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof ICompilationUnit) {
			ICompilationUnit lwUnit = (ICompilationUnit) ((IStructuredSelection) selection)
					.getFirstElement();
			try {
				selectedClassName = lwUnit.getTypes()[0].getFullyQualifiedName();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		viewer.refresh();
		
	}
}