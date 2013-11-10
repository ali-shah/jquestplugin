package nz.ac.massey.cs.jquest.views;

import java.util.Iterator;
import java.util.Stack;

import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.PDEVizImages;
import nz.ac.massey.cs.jquest.actions.ASTViewImages;

import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphNode;

public class SingleDependencyView extends AbstractView {
	
	protected static ViewContentProvider currentProvider = null;
	protected IDoubleClickListener listener = null;
	protected FormToolkit toolKit = null;
	protected Action refreshAction;
	protected Action historyAction;
	protected Action forwardAction;
	protected Stack historyStack;
	protected Stack forwardStack;
	protected ZestLabelProvider currentLabelProvider;
	protected ViewContentProvider contentProvider;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	protected void makeActions() {
		historyAction = new Action() {
			public void run() {
				if (historyStack.size() > 0) {
					Object o = historyStack.pop();
					forwardStack.push(currentProvider.getSelectedNode());
					forwardAction.setEnabled(true);
					focusOn(o, false);
					if (historyStack.size() <= 0) {
						historyAction.setEnabled(false);
					}
				}
			}
		};
		// @tag action : History action
		historyAction.setText("Back");
		historyAction.setToolTipText("Previous");
		historyAction.setEnabled(false);
		historyAction.setImageDescriptor(PDEVizImages.DESC_BACKWARD_ENABLED);

		forwardAction = new Action() {
			public void run() {
				if (forwardStack.size() > 0) {
					Object o = forwardStack.pop();
					focusOn(o, true);
					if (forwardStack.size() <= 0) {
						forwardAction.setEnabled(false);
						System.out.println();
						
					}
				}
			}
		};

		forwardAction.setText("Forward");
		forwardAction.setToolTipText("Go forward");
		forwardAction.setEnabled(false);
		forwardAction.setImageDescriptor(PDEVizImages.DESC_FORWARD_ENABLED);
		
		if(this instanceof SingleDependencyView){
			refreshAction = new Action() {
				public void run() {
					performRefresh();
					
				}
			};

			refreshAction.setText("Refresh");
			refreshAction.setToolTipText("Refresh");
			refreshAction.setEnabled(false);
			ASTViewImages.setImageDescriptors(refreshAction, ASTViewImages.REFRESH);
		} else {
			
		}
		
	}
	
	public void performRefresh() {
		if(l.hasProjectModified()) {
			selectionChanged(selection);
			refreshAction.setEnabled(false);
		}
	}
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		listener = new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object selectedElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if(ijp == null) ijp =  JavaCore.create(selectedProject);
				focusOn(selectedElement, true);
				forwardStack.clear();
				forwardAction.setEnabled(false);
			}
			
		};
		viewer.addDoubleClickListener(listener);

		makeActions();
		fillToolBar();
	}
	
	
	protected void focusOn(Object selectedElement, boolean recordHistory) {
		Object currentNode = currentProvider.getSelectedNode();
		if (currentNode != null && recordHistory && currentNode != selectedElement) {
			historyStack.push(currentNode);
			historyAction.setEnabled(true);
		}
		IJavaElement selectedJavaElement = createJavaSelection(selectedElement);
		SingleDependencyView.this.selectionChanged(selectedJavaElement);
	}
	protected IJavaElement createJavaSelection(Object selectedElement) {
		IJavaElement selectedJavaElement = null;
		if (selectedElement instanceof TypeNode) {
			TypeNode selNode = (TypeNode) selectedElement;
			boolean isPackage = false;
			if(selNode.getName().equals("")) isPackage = true;
			String selTypeName = selNode.getFullname();
			
			try {
				if (isPackage) {
					String packageName = selNode.getNamespace();
					IPackageFragment selectedIpf = null;
					for(IPackageFragment ipf : ijp.getPackageFragments()) {
						if(!ipf.isReadOnly() && ipf.getElementName().equals(packageName)){
							selectedIpf = ipf;	
							break;
						}
					}
					if(selectedIpf == null) return null;
					selectedJavaElement = selectedIpf;
				} else {
					IType t = null;
					t = ijp.findType(selTypeName);
					if(t == null) return null;
					selectedJavaElement =  t.getCompilationUnit();
					if(selectedJavaElement == null) return null;
				}
				
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return selectedJavaElement;
	}
	
	protected void fillToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(toolbarZoomContributionViewItem);

		fillLocalToolBar(bars.getToolBarManager());

	}

	/**
	 * Add the actions to the tool bar
	 * 
	 * @param toolBarManager
	 */
	protected void fillLocalToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(refreshAction);
		toolBarManager.add(historyAction);
		toolBarManager.add(forwardAction);
	}

	

	public void createControls(IJavaElement e) {
		if(l.getSelectedProject() == null) l.setProject(selectedProject);
		ijp =  JavaCore.create(selectedProject);
		historyStack = new Stack();
		forwardStack = new Stack();
		this.selectionChanged(e);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	

	public static void setProject(IProject project) {
		
		selectedProject  = project;
		
	}


	public void setSelectedElement(IJavaElement selection2) {
		this.selection  = selection2;
		selectedProject = selection2.getJavaProject().getProject();
	}
	/**
	 * Handle the select changed. This will update the view whenever a selection
	 * occurs.
	 * 
	 * @param selectedItem
	 */
	protected void selectionChanged(Object selectedItem) {
		if(selectedItem == null) return;
		this.selection = (IJavaElement) selectedItem;
		ViewContentProvider p = new ViewContentProvider(selection,l, visualizationForm.getIncoming().getSelection(), 
				visualizationForm.getOutgoing().getSelection(), visualizationForm.getExternal().getSelection());
		currentProvider = p;
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ZestLabelProvider());
		viewer.setInput(null);
		displayInCenter();
		showSelectedNode(currentProvider.getSelectedNode());
	}
	
	protected void displayInCenter() {
		Iterator nodes = viewer.getGraphControl().getNodes().iterator();
		if (viewer.getGraphControl().getNodes().size() > 0) {
			visualizationForm.enableSearchBox(true);
		} else {
			visualizationForm.enableSearchBox(false);
		}
		visualizationForm.enableSearchBox(true);
		Graph graph = viewer.getGraphControl();
		Dimension centre = new Dimension(graph.getBounds().width / 2, graph.getBounds().height / 2);
		while (nodes.hasNext()) {
			GraphNode node = (GraphNode) nodes.next();
			if (node.getLocation().x <= 1 && node.getLocation().y <= 1) {
				node.setLocation(centre.width, centre.height);
			}
		}
	}
	

	public void showDependencies(boolean incoming, boolean outgoing, boolean external) {
		currentProvider = new ViewContentProvider(selection,l, incoming, outgoing, external);
		viewer.setContentProvider(currentProvider);
		viewer.setLabelProvider(new ZestLabelProvider());	
		viewer.setInput(null);
		showSelectedNode(currentProvider.getSelectedNode());
		
	}

	public void projectUpdated() {
		refreshAction.setEnabled(true);
	}

	public void toggleName(boolean selection2) {
		ZestLabelProvider label = new ZestLabelProvider();
		label.setToggleName(selection2);
		viewer.setContentProvider(currentProvider);
		viewer.setLabelProvider(label);	
		viewer.setInput(null);
		showSelectedNode(currentProvider.getSelectedNode());
		
	}
	
}