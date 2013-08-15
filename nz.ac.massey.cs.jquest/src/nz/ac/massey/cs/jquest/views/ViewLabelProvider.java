package nz.ac.massey.cs.jquest.views;

import nz.ac.massey.cs.gql4jung.TypeNode;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

class ViewLabelProvider implements VisualizationLabelProvider {
	  public String getText(Object element) {
	    if (!(element instanceof TypeNode))
	      return null;
	 
	    TypeNode project = (TypeNode) element;
	    return project.getFullname();
	  }
	 
	  public Image getImage(Object obj) {
	    return null;
	  }

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurrentSelection(Object root, Object currentSelection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] getInterestingRelationships() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPinnedNode(Object pinnedNode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showVersionNumber(boolean show) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReverseBundleDependencies(
			boolean reverseBundleDependencies) {
		// TODO Auto-generated method stub
		
	}
	}