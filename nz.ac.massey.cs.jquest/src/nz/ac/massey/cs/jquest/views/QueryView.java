package nz.ac.massey.cs.jquest.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//import nz.ac.massey.cs.gql4jung.TypeNode;
//import nz.ac.massey.cs.gql4jung.Dependency;
import nz.ac.massey.cs.guery.ComputationMode;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.MotifInstance;
import nz.ac.massey.cs.guery.MotifReader;
import nz.ac.massey.cs.guery.PathFinder;
import nz.ac.massey.cs.guery.adapters.jung.JungAdapter;
import nz.ac.massey.cs.guery.impl.BreadthFirstPathFinder;
import nz.ac.massey.cs.guery.impl.MultiThreadedGQLImpl;
import nz.ac.massey.cs.guery.io.dsl.DefaultMotifReader;
import nz.ac.massey.cs.guery.util.ResultCollector;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.actions.ASTViewImages;
import nz.ac.massey.cs.jquest.utils.Utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.util.ClassFileReader;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.osgi.framework.Bundle;

import edu.uci.ics.jung.graph.DirectedGraph;

public class QueryView extends SingleDependencyView {
	
	private IJavaElement[] selections;
	protected QueryViewContentProvider p;
	private String selectedLibrary;
	private static String selectedMotif;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		viewer.removeDoubleClickListener(listener);
		visualizationForm.setQueryMode(true);
		refreshUpdate();
	}
	
	
	public void processCriticalDependencies(IProject prj, ComputationMode mode) {
		p = new QueryViewContentProvider(prj,l, visualizationForm, this);
		p.setQueryMode(visualizationForm.getQueryMode());
		currentProvider = p;
		Bundle bundle = Platform.getBundle("nz.ac.massey.cs.jquest");
		URL queriesFolder = BundleUtility.find(bundle,"queries/");
		String uri = null;
		try {
			uri = FileLocator.resolve(queriesFolder).getFile();
	        
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
		File qFolder = new File(uri);
		File[] queryFiles = new File[qFolder.listFiles().length];
		int i = 0;
		for(File f : qFolder.listFiles()) {
			if(f.getName().endsWith(".guery")) {
				queryFiles[i++] = f;
			}
		}
		List<Motif<TypeNode, Dependency>> motifs = loadMotifs(queryFiles);
		p.processCriticalDependencies(motifs);
		
	}

	public void refreshUpdate() {
		refreshAction.setEnabled(true);
	}
	@Override
	protected void performRefresh() {
		ComputationMode mode = visualizationForm.getQueryMode();
		if(selectedMotif != null && selectedMotif.equals("critical")) { 
			processCriticalDependencies(selectedProject, mode);
		} else if(selectedMotif != null && selectedMotif.equals("adhoc")) {
			if(selections==null) {
				//it means a library was selected
				processLibrary(selectedProject, selectedLibrary);
			} else {
				//this means two items were selected
				processAdhocQuery(selections);
			}
		} else if(selectedMotif != null && selectedProject != null){
			processAntipattern(selectedProject, selectedMotif, mode);
		} else {
			return;
		}
//		refreshAction.setEnabled(false);
	}
	public void processAntipattern(IProject prj2, String motif, ComputationMode mode) {
		selectedProject = prj2;
		selectedMotif = motif;
		p = new QueryViewContentProvider(prj2,l, visualizationForm, this);
		ComputationMode m = visualizationForm.getQueryMode();
		p.setQueryMode(m);
		currentProvider = p;
		Bundle bundle = Platform.getBundle("nz.ac.massey.cs.jquest");
		URL fileURL = BundleUtility.find(bundle,"queries/"+motif+".guery");
		String uri = null;
		try {
			uri = FileLocator.resolve(fileURL).getFile();
	        
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
		File[] queryFiles = new File[1];
		queryFiles[0] = new File(uri);
		List<Motif<TypeNode, Dependency>> motifs = loadMotifs(queryFiles);
		p.processQuery(motifs.iterator().next());
	}
	private List<Motif<TypeNode, Dependency>> loadMotifs(File[] queryFiles) {
		List<Motif<TypeNode, Dependency>> motifs = new ArrayList<Motif<TypeNode, Dependency>>();
		for (int i = 0; i < queryFiles.length; i++) {
			File f = queryFiles[i];
			if(f == null) break;
//			System.out.println("loading " + f.getAbsolutePath());
			Motif<TypeNode, Dependency> m;
			try {
				m = loadMotif(f.getAbsolutePath());
				if (m != null)
					motifs.add(m);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("could not load motif files");
				
			}
		}
		return motifs;
	}

	public void processAdhocQuery(IJavaElement[] selection) {
		this.selections = selection;
		selectedMotif = "adhoc";
		if(!validateSameProject()) {
			displayMessage("Choose dependencies from the same project.");
			return;
		}
		IProject prj = selections[0].getJavaProject().getProject();
		p = new QueryViewContentProvider(prj, selections,l, visualizationForm, this);
		p.setQueryMode(visualizationForm.getQueryMode());
		currentProvider = p; 
		
		String src = getFullname(selections[0]); 
		String tar = getFullname(selections[1]); 
		
		if(src == null || tar == null) {
			displayMessage("Invalid Selection");
			return;
		}
		
		String query = Utils.composeQuery(src, tar);
		p.processAdhocQuery(query, src, tar);
		
	}
	
	private void displayMessage(String msg) {
		MessageBox mb = new MessageBox(getSite().getWorkbenchWindow().getShell(),SWT.ICON_INFORMATION | SWT.OK);
		mb.setMessage(msg);
		mb.setText("Status");
		mb.open();
	}


	private boolean validateSameProject() {
		if(selections == null || selections.length < 2) return false;
		return selections[0].getJavaProject().getProject().equals(selections[1].getJavaProject().getProject());
	}


	private String getFullname(IJavaElement e) {
		String fullname = null;
		try{
			if(e instanceof ICompilationUnit) {
				fullname = ".fullname=='" + ((ICompilationUnit) e).getTypes()[0].getFullyQualifiedName() + "'";
				return fullname;
			} else if (e instanceof IClassFile) {
				IClassFile icf = (IClassFile) e;
				IClassFileReader r = new ClassFileReader(icf.getBytes(), IClassFileReader.ALL);
				char[] name = r.getClassName();
				String classname = String.valueOf(name);
				classname = classname.replace("/", ".");
				fullname = ".fullname=='" + classname + "'";
				return fullname;
			} else if(e instanceof IPackageFragment) {
				fullname = ".namespace=='" + e.getElementName() + "'";
				return fullname;
			} else if(e instanceof IPackageFragmentRoot) {
				fullname = ".container=='" + e.getElementName() + "'";
				return fullname;
			} else {
				return null;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return null;
	}

	public void setSelectionChanged(MotifInstance instance) {
		visualizationForm.updateActions();
		clearGraph(viewer.getGraphControl());
		viewer.getGraphControl().redraw();
		viewer.applyLayout();
		viewer.refresh(true);
		p.setCurrentInstance(instance);
		currentProvider = p;
		toggleName(visualizationForm.getClassNameOnly().getSelection());
//		viewer.setContentProvider(p);
//		viewer.setLabelProvider(new ViewLabelProvider());
//		viewer.setInput(null);
//		viewer.getGraphControl().redraw();
		
	}
	public void clearGraph( Graph g ) { 

		// remove all the connections 

		Object[] objects = g.getConnections().toArray() ; 
		for(int i = 0; i < objects.length; i++) {
			GraphConnection gc = (GraphConnection) objects[i];
			gc.dispose();
		}

		// remove all the nodes 

		objects = g.getNodes().toArray() ; 

		for(int i = 0; i < objects.length; i++) {
			GraphNode gc = (GraphNode) objects[i];
			gc.dispose();
		}	

		}
	public static Motif<TypeNode, Dependency> loadMotif(String name) throws Exception {
		MotifReader<TypeNode, Dependency> motifReader = new DefaultMotifReader<TypeNode, Dependency>();
		InputStream in = new FileInputStream(name);
		Motif<TypeNode, Dependency> motif = motifReader.read(in);
		in.close();
		return motif;
	}

	public void setSelectionChangedToCriticalDep(Dependency nextCritical) {
		visualizationForm.updateActions();
		clearGraph(viewer.getGraphControl());
		viewer.getGraphControl().redraw();
		viewer.applyLayout();
		viewer.refresh(true);
		p.setCurrentCriticalDep(nextCritical);
		viewer.setContentProvider(p);
		currentProvider = p;
		toggleName(visualizationForm.getClassNameOnly().getSelection());
//		viewer.setLabelProvider(new ViewLabelProvider());
//		viewer.setInput(null);
//		viewer.getGraphControl().redraw();
		
	}

	public void processLibrary(IProject prj, String nameLib) {
		// TODO Auto-generated method stub
		this.selectedLibrary = nameLib;
		selectedMotif = "adhoc";
		selectedProject = prj;
		p = new QueryViewContentProvider(prj, selections,l, visualizationForm, this);
		p.setQueryMode(visualizationForm.getQueryMode());
		p.processLibrary(nameLib);
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(null);
	}
}
