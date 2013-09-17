package nz.ac.massey.cs.jquest.views;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.osgi.framework.Bundle;

import edu.uci.ics.jung.graph.DirectedGraph;

public class QueryView extends SingleDependencyView {
	
	private IJavaElement[] selections;
	protected QueryViewContentProvider p;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		viewer.removeDoubleClickListener(listener);
		visualizationForm.setQueryMode(true);
	}

	public void processAntipattern(IProject prj2, String motif) {
		p = new QueryViewContentProvider(prj2,l, visualizationForm, this);
		Bundle bundle = Platform.getBundle("nz.ac.massey.cs.jquest");
		URL fileURL = BundleUtility.find(bundle,"queries/"+motif+".guery");
		String uri = null;
		try {
			uri = FileLocator.resolve(fileURL).getFile();
//	        uri2 = FileLocator.resolve(fileURL2).getFile();
//	        uri3 = FileLocator.resolve(fileURL3).getFile();
//	        uri4 = FileLocator.resolve(fileURL4).getFile();
	        
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
		File[] queryFiles = new File[1];
		queryFiles[0] = new File(uri);
//		queryFiles[1] = new File(uri2);
//		queryFiles[2] = new File(uri3);
//		queryFiles[3] = new File(uri4);
		
		List<Motif<TypeNode, TypeRef>> motifs = new ArrayList<Motif<TypeNode, TypeRef>>();
		for (int i = 0; i < queryFiles.length; i++) {
			File f = queryFiles[i];
			Motif<TypeNode, TypeRef> m;
			try {
				m = loadMotif(f.getAbsolutePath());
				if (m != null)
					motifs.add(m);
			} catch (Exception e) {
				System.out.println("could not load motif files");
				
			}
		}
		p.processQuery(motifs.iterator().next());
//		viewer.setContentProvider(p);
//		viewer.setLabelProvider(new ViewLabelProvider());
//		viewer.setInput(null);
	}
	public void setSelection(IJavaElement[] selection) {
		this.selections = selection;
		IProject prj = selections[0].getJavaProject().getProject();
		p = new QueryViewContentProvider(prj, selections,l, visualizationForm, this);
		if(selections[0].getElementType() == IJavaElement.COMPILATION_UNIT &&
				selections[1].getElementType() == IJavaElement.COMPILATION_UNIT) {
			try {
				String src = ((ICompilationUnit)selections[0]).getTypes()[0].getFullyQualifiedName();
				String tar = ((ICompilationUnit)selections[1]).getTypes()[0].getFullyQualifiedName();
				p.process(src, tar);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		} else if(selections[0].getElementType() == IJavaElement.PACKAGE_FRAGMENT &&
				selections[1].getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
			
		}
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(null);
	}
	
	public void setSelectionChanged(MotifInstance instance) {
		visualizationForm.updateActions();
		clearGraph(viewer.getGraphControl());
		viewer.getGraphControl().redraw();
		viewer.applyLayout();
		viewer.refresh(true);
		p.setCurrentInstance(instance);
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(null);
		viewer.getGraphControl().redraw();
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
	public static Motif<TypeNode, TypeRef> loadMotif(String name) throws Exception {
		MotifReader<TypeNode, TypeRef> motifReader = new DefaultMotifReader<TypeNode, TypeRef>();
		InputStream in = new FileInputStream(name);
		Motif<TypeNode, TypeRef> motif = motifReader.read(in);
		in.close();
		return motif;
	}
}
