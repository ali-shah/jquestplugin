package nz.ac.massey.cs.jquest.actions;

import nz.ac.massey.cs.guery.ComputationMode;
import nz.ac.massey.cs.jquest.views.QueryView;
import nz.ac.massey.cs.jquest.views.SingleDependencyView;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
		try {
			
			IWorkbenchPage page = targetPart.getSite().getWorkbenchWindow()
					.getActivePage();
			String id = action.getId();
			
			if(id.equals("nz.ac.massey.cs.jquest.actionLibrary")){
				if(selection instanceof IStructuredSelection) {
					QueryView qv = (QueryView) page.showView("nz.ac.massey.cs.jquest.QueryView");
					if(((IStructuredSelection) selection).size() == 1) {
						Object e = ((IStructuredSelection) selection).getFirstElement();
						if(e instanceof IPackageFragmentRoot) {
							IProject prj = ((IPackageFragmentRoot) e).getJavaProject().getProject();
							qv.processLibrary(prj, ((IPackageFragmentRoot) e).getElementName());
							return;
						}
					}
				}
			}
			
			if (id.equals("nz.ac.massey.cs.jquest.scdaction")) {
				if(selection instanceof IStructuredSelection) {
					QueryView qv = (QueryView) page.showView("nz.ac.massey.cs.jquest.QueryView");
					if(((IStructuredSelection) selection).size() == 1) {
						Object e = ((IStructuredSelection) selection).getFirstElement();
						if(e instanceof IJavaElement) {
							IProject prj = ((IJavaElement) e).getJavaProject().getProject();
							qv.processAntipattern(prj, "scd", ComputationMode.CLASSES_NOT_REDUCED);
							return;
						}
					}
				}
			} else if (id.equals("nz.ac.massey.cs.jquest.stkaction")) {
				if(selection instanceof IStructuredSelection) {
					QueryView qv = (QueryView) page.showView("nz.ac.massey.cs.jquest.QueryView");
					if(((IStructuredSelection) selection).size() == 1) {
						Object e = ((IStructuredSelection) selection).getFirstElement();
						if(e instanceof IJavaElement) {
							IProject prj = ((IJavaElement) e).getJavaProject().getProject();
							qv.processAntipattern(prj, "stk", ComputationMode.CLASSES_NOT_REDUCED);
							return;
						}
					}
				}

			} else if (id.equals("nz.ac.massey.cs.jquest.awdaction")) {
				if(selection instanceof IStructuredSelection) {
					QueryView qv = (QueryView) page.showView("nz.ac.massey.cs.jquest.QueryView");
					if(((IStructuredSelection) selection).size() == 1) {
						Object e = ((IStructuredSelection) selection).getFirstElement();
						if(e instanceof IJavaElement) {
							IProject prj = ((IJavaElement) e).getJavaProject().getProject();
							qv.processAntipattern(prj, "awd", ComputationMode.CLASSES_NOT_REDUCED);
							return;
						}
					}
				}

			} else if (id.equals("nz.ac.massey.cs.jquest.deginhaction")) {
				if(selection instanceof IStructuredSelection) {
					QueryView qv = (QueryView) page.showView("nz.ac.massey.cs.jquest.QueryView");
					if(((IStructuredSelection) selection).size() == 1) {
						Object e = ((IStructuredSelection) selection).getFirstElement();
						if(e instanceof IJavaElement) {
							IProject prj = ((IJavaElement) e).getJavaProject().getProject();
							qv.processAntipattern(prj, "deginh", ComputationMode.CLASSES_NOT_REDUCED);
							return;
						}
					}
				}

			}
			
			else if (id.equals("nz.ac.massey.cs.jquest.critical")) {
				if(selection instanceof IStructuredSelection) {
					QueryView qv = (QueryView) page.showView("nz.ac.massey.cs.jquest.QueryView");
					if(((IStructuredSelection) selection).size() == 1) {
						Object e = ((IStructuredSelection) selection).getFirstElement();
						if(e instanceof IJavaElement) {
							IProject prj = ((IJavaElement) e).getJavaProject().getProject();
							qv.processCriticalDependencies(prj, ComputationMode.CLASSES_NOT_REDUCED);
							return;
						}
					}
				}

			}
			
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
					qv.processAdhocQuery(selection);
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
