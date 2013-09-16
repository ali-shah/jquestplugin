package nz.ac.massey.cs.jquest.views;

import java.util.List;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
import nz.ac.massey.cs.guery.ComputationMode;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.MotifInstance;
import nz.ac.massey.cs.guery.PathFinder;
import nz.ac.massey.cs.guery.adapters.jung.JungAdapter;
import nz.ac.massey.cs.guery.impl.BreadthFirstPathFinder;
import nz.ac.massey.cs.guery.impl.MultiThreadedGQLImpl;
import nz.ac.massey.cs.guery.util.ResultCollector;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;

import edu.uci.ics.jung.graph.DirectedGraph;

public class QueryView extends SingleDependencyView {
	
	private IJavaElement[] selections;
	protected QueryViewContentProvider p;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		viewer.removeDoubleClickListener(listener);
		visualizationForm.setQueryMode(true);
	}

	public void setSelection(IJavaElement[] selection) {
		this.selections = selection;
		p = new QueryViewContentProvider(selections,l, visualizationForm, this);
		if(selections[0].getElementType() == IJavaElement.COMPILATION_UNIT) {
			try {
				String src = ((ICompilationUnit)selections[0]).getTypes()[0].getFullyQualifiedName();
				String tar = ((ICompilationUnit)selections[1]).getTypes()[0].getFullyQualifiedName();
				p.process(src, tar);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
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
	private void clearGraph( Graph g ) { 

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
	
}
