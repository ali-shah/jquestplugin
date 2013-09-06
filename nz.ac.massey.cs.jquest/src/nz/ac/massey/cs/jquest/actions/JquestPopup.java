package nz.ac.massey.cs.jquest.actions;

import nz.ac.massey.cs.jquest.views.QueryView;
import nz.ac.massey.cs.jquest.views.SingleDependencyView;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

public class JquestPopup implements IObjectActionDelegate {
	private ISelection selection;
	private IWorkbenchPart targetPart;

	public void run(IAction action) {
		IWorkbenchPage page = targetPart.getSite().getWorkbenchWindow().getActivePage();
		try {
			
			if(selection instanceof IStructuredSelection) {
				if(((IStructuredSelection) selection).size() == 2) {
					QueryView qv = (QueryView) page.showView("nz.ac.massey.cs.jquest.QueryView");
					Object[] selectedElements =  ((IStructuredSelection) selection).toArray();
					IJavaElement[] selection = new IJavaElement[2];
					int i=0;
					for(Object s : selectedElements) {
						if(s instanceof IJavaElement) {
							selection[i++] = (IJavaElement) s;
						}
					}
					qv.setSelection(selection);
					System.out.println();
				} else if (((IStructuredSelection) selection).size() == 1) {
					SingleDependencyView sdv = (SingleDependencyView) page.showView("nz.ac.massey.cs.jquest.SingleDependencyView");
					IJavaElement e = (IJavaElement) ((IStructuredSelection) selection).getFirstElement();
					if(e == null) return;
					sdv.setSelectedElement(e);
					sdv.createControls(e);
				} else {
					//display message
				}
				
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

}
