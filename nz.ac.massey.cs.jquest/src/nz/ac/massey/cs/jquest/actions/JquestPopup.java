package nz.ac.massey.cs.jquest.actions;

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
			SingleDependencyView sdv = (SingleDependencyView) page.showView("nz.ac.massey.cs.jquest.SingleDependencyView");
			if(selection instanceof IStructuredSelection) {
				IJavaElement e = (IJavaElement) ((IStructuredSelection) selection).getFirstElement();
				if(e == null) return;
				sdv.setSelectedElement(e);
				sdv.createControls(e);
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
