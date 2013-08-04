package nz.ac.massey.cs.jquest.actions;

import nz.ac.massey.cs.jquest.views.SingleDependencyView;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
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

	@SuppressWarnings("null")
	public void run(IAction action) {
		String name = null;
		ICompilationUnit lwUnit = null;
		if (selection instanceof IStructuredSelection) {
			lwUnit = (ICompilationUnit) ((IStructuredSelection) selection)
					.getFirstElement();
			try {
				name = lwUnit.getTypes()[0].getFullyQualifiedName();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		IWorkbenchPage page = targetPart.getSite().getWorkbenchWindow().getActivePage();
		try {
			SingleDependencyView.setSelection(name);
			SingleDependencyView.setProject(lwUnit.getJavaProject().getProject());
			page.showView("nz.ac.massey.cs.jquest.SingleDependencyView");
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
