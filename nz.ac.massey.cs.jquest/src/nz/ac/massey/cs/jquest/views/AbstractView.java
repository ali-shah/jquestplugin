package nz.ac.massey.cs.jquest.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.utils.Utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.EntityConnectionData;
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
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;

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
	protected Action showSource = null;


	public void createPartControl(Composite parent) {
		l = new ElementChangedListener(this,selectedProject);
		JavaCore.addElementChangedListener(l);
		
		toolKit = new FormToolkit(parent.getDisplay());
		visualizationForm = new VisualizationForm(parent, toolKit, this);
		viewer = visualizationForm.getGraphViewer();
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED | ZestStyles.CONNECTIONS_DASH);
		viewer.setLayoutAlgorithm(new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING));
		FontData fontData = Display.getCurrent().getSystemFont().getFontData()[0];
		fontData.height = 42;

		searchFont = new Font(Display.getCurrent(), fontData);
	
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
		
		hookContextMenu();
	}
	
	/**
	 * Creates the context menu for this view.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		fillContextMenu(menuMgr);

		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AbstractView.this.fillContextMenu(manager);

			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);

	}
	
	/**
	 * Add the items to the context menu
	 * 
	 * @param manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator());
		IStructuredSelection s = (IStructuredSelection) viewer.getSelection();
		if (s.size() > 0) {
			makeShowSourceAction(s.getFirstElement());
			manager.add(showSource);
			System.out.println();
			
		}
	}
	private void makeShowSourceAction(final Object sel) {
		
		showSource = new Action(){
			public void run() {
				if(sel instanceof TypeNode) {
					TypeNode tn = (TypeNode) sel;
					showSourceCode(tn.getFullname());
				} else if(sel instanceof EntityConnectionData) {
					EntityConnectionData rel = (EntityConnectionData) sel;
					TypeNode tn = (TypeNode) rel.source;
					TypeNode target = (TypeNode) rel.dest;
					IEditorPart editor = showSourceCode(tn.getFullname());
					highlightFirstDependency(editor, target);
				} else if(sel instanceof Dependency) {
					Dependency rel = (Dependency) sel;
					TypeNode tn = rel.getStart();
					TypeNode target = rel.getEnd();
					IEditorPart editor = showSourceCode(tn.getFullname());
					highlightFirstDependency(editor, target);
				}
			}

			
		};
		showSource.setText("Show Source Code");
		showSource.setEnabled(true);
	}
	private void highlightFirstDependency(IEditorPart editor,
			TypeNode target) {
		
		IDocumentProvider provider = ((AbstractTextEditor) editor).getDocumentProvider();
		IDocument doc = provider.getDocument(editor.getEditorInput());
		FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(doc);
		try {
			IRegion r = finder.find(0, target.getName(), true, true, true,false);
			if(r == null) {
				//try with full name;
				r = finder.find(0, target.getFullname(), true, true, true,false);
			}
			goToLine(editor, r);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
	}
	public IEditorPart showSourceCode(String sourceName) {
		IEditorPart editor = null;
		try {
			if(ijp == null) {
				ijp = JavaCore.create(selectedProject);
			}
			IType t = ijp.findType(sourceName);
			if(t instanceof BinaryType) {
				displayMessage("This is a Binary class. Select a Source class");
				return null;
			}
			IFile f = (IFile) t.getResource();
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//				IJavaElement sourceJavaElement = JavaCore.create(f);
				IEditorDescriptor desc = PlatformUI.getWorkbench().
				        getEditorRegistry().getDefaultEditor(f.getName());
				editor = page.openEditor(new FileEditorInput(f), desc.getId());
//				editor = JavaUI.openInEditor(sourceJavaElement);
				
			} catch (PartInitException e) {
				displayMessage("The requested class could not be found in the source folder");
			}
		} catch (NullPointerException e) {
			displayMessage("The requested class could not be found in the source folder");
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		return editor;
	}
	private static void goToLine(IEditorPart editorPart,IRegion lineInfo) {
		if (!(editorPart instanceof ITextEditor)) {
			return;
		}
		ITextEditor editor = (ITextEditor) editorPart;
		if (lineInfo != null) {
			editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
		}

		}
	private static void goToLine(IEditorPart editorPart, int lineNumber) {
		  if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
		    return;
		  }
		  ITextEditor editor = (ITextEditor) editorPart;
		  IDocument document = editor.getDocumentProvider().getDocument(
		    editor.getEditorInput());
		  if (document != null) {
		    IRegion lineInfo = null;
		    try {
		      // line count internaly starts with 0, and not with 1 like in
		      // GUI
		      lineInfo = document.getLineInformation(lineNumber - 1);
		    } catch (BadLocationException e) {
		      // ignored because line number may not really exist in document,
		      // we guess this...
		    }
		    if (lineInfo != null) {
		      editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
		    }
		  }
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
	
	private void displayMessage(String msg) {
		MessageBox mb = new MessageBox(getSite().getWorkbenchWindow()
				.getShell(), SWT.ICON_ERROR);
		mb.setText("Status");
		mb.setMessage(msg);
		mb.open();
	}
}
