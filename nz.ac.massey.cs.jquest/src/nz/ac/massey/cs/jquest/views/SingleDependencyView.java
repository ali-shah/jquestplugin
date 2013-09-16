package nz.ac.massey.cs.jquest.views;

import java.util.ArrayList;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.jquest.PDEVizImages;
import nz.ac.massey.cs.jquest.actions.ASTViewImages;
import nz.ac.massey.cs.jquest.utils.Utils;
import org.eclipse.zest.layouts.algorithms.*;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import org.eclipse.jface.viewers.*;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

public class SingleDependencyView extends ViewPart implements IZoomableWorkbenchPart{
	
	protected GraphViewer viewer;
	protected IJavaElement selection;
	protected static ElementChangedListener l = null;
	protected static IProject selectedProject = null;
	protected static IJavaProject ijp = null;
	protected static ViewContentProvider currentProvider = null;
	protected IDoubleClickListener listener = null;

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
		
		refreshAction = new Action() {
			public void run() {
				performRefresh();
				
			}
		};

		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh");
		refreshAction.setEnabled(false);
		ASTViewImages.setImageDescriptors(refreshAction, ASTViewImages.REFRESH);
	}
	
	protected void performRefresh() {
		if(l.hasProjectModified()) {
			selectionChanged(selection);
			refreshAction.setEnabled(false);
		}
	}
	public void createPartControl(Composite parent) {
//		PackageExplorerPart part= PackageExplorerPart.getFromActivePerspective();
//		IResource resource = null /*any IResource to be selected in the explorer*/;
//		part.selectAndReveal(resource);
		l = new ElementChangedListener(this,selectedProject);
		JavaCore.addElementChangedListener(l);
		
		//create form now
		toolKit = new FormToolkit(parent.getDisplay());
		visualizationForm = new VisualizationForm(parent, toolKit, this);
		viewer = visualizationForm.getGraphViewer();
//		form = visualizationForm.getForm();
//		managedForm = visualizationForm.getManagedForm();
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED | ZestStyles.CONNECTIONS_DASH);
		viewer.setLayoutAlgorithm(new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING, new LayoutAlgorithm[] { new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING) }));
//		viewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
		FontData fontData = Display.getCurrent().getSystemFont().getFontData()[0];
		fontData.height = 42;

		searchFont = new Font(Display.getCurrent(), fontData);
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

		visualizationForm.getSearchBox().addModifyListener(new ModifyListener() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			public void modifyText(ModifyEvent e) {
				String textString = visualizationForm.getSearchBox().getText();
				HashMap figureListing = new HashMap();
				ArrayList list = new ArrayList();
				Iterator iterator = viewer.getGraphControl().getNodes().iterator();
				while (iterator.hasNext()) {
					GraphItem item = (GraphItem) iterator.next();
					figureListing.put(item.getText(), item);
				}
				iterator = figureListing.keySet().iterator();
				if (textString.length() > 0) {
					while (iterator.hasNext()) {
						String string = (String) iterator.next();
						if (string.toLowerCase().indexOf(textString.toLowerCase()) >= 0) {
							list.add(figureListing.get(string));
						}
					}
				}
				viewer.getGraphControl().setSelection((GraphItem[]) list.toArray(new GraphItem[list.size()]));
			}

		});
		toolbarZoomContributionViewItem = new ZoomContributionViewItem(this);
		makeActions();
		fillToolBar();
	}
	
	public void setLayout(LayoutAlgorithm algo) {
		viewer.setLayoutAlgorithm(algo, true);
		
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
//		Object[] elements = contentProvider.getElements(selectedItem);
		this.selection = (IJavaElement) selectedItem;
		ViewContentProvider p = new ViewContentProvider(selection,l, visualizationForm.getIncoming().getSelection(), 
				visualizationForm.getOutgoing().getSelection(), visualizationForm.getExternal().getSelection());
		currentProvider = p;
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(null);
		displayInCenter();
		showSelectedNode();
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
	
	protected FormToolkit toolKit = null;
//	protected ScrolledForm form = null;
//	protected ManagedForm managedForm = null;
//	protected Action focusDialogAction;
//	protected Action focusDialogActionToolbar;
//	protected Action showCalleesAction;
//	protected Action showCallersAction;
//	protected Action focusAction;
//	protected Action pinAction;
//	protected Action unPinAction;
	protected Action refreshAction;
	protected Action historyAction;
	protected Action forwardAction;
//	protected Action screenshotAction;
	protected Stack historyStack;
	protected Stack forwardStack;
//	protected Object currentNode = null;
	protected ViewLabelProvider currentLabelProvider;
	protected AbstractContentProvider contentProvider;
//	protected Object pinnedNode = null;
//	protected ZoomContributionViewItem contextZoomContributionViewItem;
	protected ZoomContributionViewItem toolbarZoomContributionViewItem;
	protected VisualizationForm visualizationForm;
	protected Font searchFont;



	public void showDependencies(boolean incoming, boolean outgoing, boolean external) {
		ViewContentProvider p = new ViewContentProvider(selection,l, incoming, outgoing, external);
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ViewLabelProvider());	
		viewer.setInput(null);
		showSelectedNode();
		
	}

	protected void showSelectedNode() {
		Iterator iter = viewer.getGraphControl().getNodes().iterator();
		GraphItem selected = null;
		while(iter.hasNext()){
			GraphItem i = (GraphItem) iter.next();
			String selectedNodeName = Utils.removeTrailingDot(currentProvider.getSelectedNode().getFullname());
			if(i.getText().equals(selectedNodeName)) {
				selected = i;
				break;
			}
		}
		if(selected != null) {
			viewer.getGraphControl().setSelection(new GraphItem[]{selected});
		}
		
	}
	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}
	
	public void projectUpdated() {
		refreshAction.setEnabled(true);
	}
	
}