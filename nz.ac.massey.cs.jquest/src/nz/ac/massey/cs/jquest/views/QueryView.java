package nz.ac.massey.cs.jquest.views;

import java.util.List;

import nz.ac.massey.cs.gql4jung.TypeNode;
import nz.ac.massey.cs.gql4jung.TypeRef;
import nz.ac.massey.cs.guery.ComputationMode;
import nz.ac.massey.cs.guery.Motif;
import nz.ac.massey.cs.guery.PathFinder;
import nz.ac.massey.cs.guery.adapters.jung.JungAdapter;
import nz.ac.massey.cs.guery.impl.BreadthFirstPathFinder;
import nz.ac.massey.cs.guery.impl.MultiThreadedGQLImpl;
import nz.ac.massey.cs.guery.util.ResultCollector;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.widgets.Composite;

import edu.uci.ics.jung.graph.DirectedGraph;

public class QueryView extends SingleDependencyView {
	
	
	private IJavaElement[] selections;


	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
	}
	
	
	

	public void setSelection(IJavaElement[] selection) {
		this.selections = selection;
		QueryViewContentProvider p = new QueryViewContentProvider(selections,l, visualizationForm.getIncoming().getSelection(), 
				visualizationForm.getOutgoing().getSelection(), visualizationForm.getExternal().getSelection());
//		currentProvider = p;
		viewer.setContentProvider(p);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(null);
		
	}
	
}
