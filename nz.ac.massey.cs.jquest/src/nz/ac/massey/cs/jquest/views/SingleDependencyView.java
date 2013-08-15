package nz.ac.massey.cs.jquest.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

@SuppressWarnings("rawtypes, unchecked")
public class SingleDependencyView extends ViewPart{
	
	private GraphViewer viewer;
	private IJavaElement selection;
	private static ElementChangedListener l = null;
	private static IProject selectedProject = null;


	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	
	public void createPartControl(Composite parent) {
		
		l = new ElementChangedListener(selectedProject);
		JavaCore.addElementChangedListener(l);
		//create form now
		toolKit = new FormToolkit(parent.getDisplay());
		visualizationForm = new VisualizationForm(parent, toolKit, this);
		viewer = visualizationForm.getGraphViewer();
//		form = visualizationForm.getForm();
//		managedForm = visualizationForm.getManagedForm();
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED | ZestStyles.CONNECTIONS_DASH);
//		viewer.setLayoutAlgorithm(new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING, new LayoutAlgorithm[] { new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING) }));
		viewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
		FontData fontData = Display.getCurrent().getSystemFont().getFontData()[0];
		fontData.height = 42;

		searchFont = new Font(Display.getCurrent(), fontData);
//		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
//
//			public void selectionChanged(SelectionChangedEvent event) {
//				Object selectedElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
//				if (selectedElement instanceof EntityConnectionData) {
//					return;
//				}
//				SingleDependencyView.this.selectionChanged(selectedElement);
//			}
//		});


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
	}
	
	public void createControls(IJavaElement e) {
		if(l.getSelectedProject() == null) l.setProject(selectedProject);
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

//	@Override
//	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
//		if (selection instanceof ICompilationUnit) {
//			ICompilationUnit lwUnit = (ICompilationUnit) ((IStructuredSelection) selection)
//					.getFirstElement();
//			try {
//				selectedNodeName = lwUnit.getTypes()[0].getFullyQualifiedName();
//			} catch (JavaModelException e) {
//				e.printStackTrace();
//			}
//		}
//		viewer.refresh();
//		
//	}

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
	private void selectionChanged(Object selectedItem) {
//		Object[] elements = contentProvider.getElements(selectedItem);
		this.selection = (IJavaElement) selectedItem;
		ViewContentProvider p = new ViewContentProvider(selection,l, true, true);
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(null);
		showSelectedNode(p);
	}
	
	private FormToolkit toolKit = null;
//	private ScrolledForm form = null;
//	private ManagedForm managedForm = null;
//	private Action focusDialogAction;
//	private Action focusDialogActionToolbar;
//	private Action showCalleesAction;
//	private Action showCallersAction;
//	private Action focusAction;
//	private Action pinAction;
//	private Action unPinAction;
//	private Action historyAction;
//	private Action forwardAction;
//	private Action screenshotAction;
//	private Stack historyStack;
//	private Stack forwardStack;
//	private Object currentNode = null;
	protected ViewLabelProvider currentLabelProvider;
	protected ViewContentProvider contentProvider;
//	protected Object pinnedNode = null;
//	private ZoomContributionViewItem contextZoomContributionViewItem;
//	private ZoomContributionViewItem toolbarZoomContributionViewItem;
	private VisualizationForm visualizationForm;
	private Font searchFont;



	public void showDependencies(boolean incoming, boolean outgoing) {
		ViewContentProvider p = new ViewContentProvider(selection,l, incoming, outgoing);
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ViewLabelProvider());	
		viewer.setInput(null);
		showSelectedNode(p);
		
	}

	private void showSelectedNode(ViewContentProvider p) {
		Iterator iter = viewer.getGraphControl().getNodes().iterator();
		GraphItem selected = null;
		while(iter.hasNext()){
			GraphItem i = (GraphItem) iter.next();
			if(i.getText().equals(p.getSelectedNode().getFullname())) {
				selected = i;
				break;
			}
		}
		if(selected != null) {
			viewer.getGraphControl().setSelection(new GraphItem[]{selected});
		}
		
	}
}