package nz.ac.massey.cs.jquest.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.utils.Utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;

public abstract class AbstractView extends ViewPart implements IZoomableWorkbenchPart {
	protected GraphViewer viewer;
	protected IJavaElement selection;
	protected static ElementChangedListener l = null;
	protected static IProject selectedProject = null;
	protected static IJavaProject ijp = null;
	protected VisualizationForm visualizationForm;
	protected IDoubleClickListener listener = null;
	protected FormToolkit toolKit = null;
	protected Font searchFont;
	protected ZoomContributionViewItem toolbarZoomContributionViewItem;


	
	public void createPartControl(Composite parent) {
//		PackageExplorerPart part= PackageExplorerPart.getFromActivePerspective();
//		IResource resource = null /*any IResource to be selected in the explorer*/;
//		part.selectAndReveal(resource);
		l = new ElementChangedListener(this,selectedProject);
		JavaCore.addElementChangedListener(l);
		
		toolKit = new FormToolkit(parent.getDisplay());
		visualizationForm = new VisualizationForm(parent, toolKit, this);
		viewer = visualizationForm.getGraphViewer();
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED | ZestStyles.CONNECTIONS_DASH);
		viewer.setLayoutAlgorithm(new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING, new LayoutAlgorithm[] { new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING) }));
		FontData fontData = Display.getCurrent().getSystemFont().getFontData()[0];
		fontData.height = 42;

		searchFont = new Font(Display.getCurrent(), fontData);
	
//		viewer.addDoubleClickListener(listener);

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
	}
	
	
	@Override
	public AbstractZoomableViewer getZoomableViewer() {
		return viewer;
	}

	protected void showSelectedNode(TypeNode sel) {
		Iterator iter = viewer.getGraphControl().getNodes().iterator();
		GraphItem selected = null;
		while(iter.hasNext()){
			GraphItem i = (GraphItem) iter.next();
			if(sel == null) return;
			String selectedNodeName = Utils.removeTrailingDot(sel.getFullname());
			if(i.getText().equals(selectedNodeName)) {
				selected = i;
				break;
			}
		}
		if(selected != null) {
			viewer.getGraphControl().setSelection(new GraphItem[]{selected});
		}
		
	}
	public abstract void toggleName(boolean selection2);
	public void setLayout(LayoutAlgorithm algo) {
		viewer.setLayoutAlgorithm(algo, true);
		
	}


	public abstract void projectUpdated();


	public abstract void performRefresh();
}
