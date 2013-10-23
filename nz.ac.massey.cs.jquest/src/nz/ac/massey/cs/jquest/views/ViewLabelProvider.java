package nz.ac.massey.cs.jquest.views;

import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.utils.Utils;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.zest.core.viewers.EntityConnectionData;

class ViewLabelProvider implements VisualizationLabelProvider {
	  private boolean showClassname = false;

	public String getText(Object element) {
	    if (!(element instanceof TypeNode))
	      return null;
	 
	    TypeNode node = (TypeNode) element;
	    String name = null;
	    if(showClassname) name = node.getName();
	    else name = node.getFullname();
	    return Utils.removeTrailingDot(name);
	  }
	 
	  public Image getImage(Object element) {
		if (element.getClass() == EntityConnectionData.class) {
			return null;
		}
			
		if (element instanceof TypeNode) {
			TypeNode tn = (TypeNode) element;
			if(tn.getName()== null)
				return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_PACKAGE);
			else {
				if(tn.isAbstract()) {
					return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_INTERFACE);
				} else {
					return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_CLASS);
				}
			}
				
		}
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

	public void setToggleName(boolean selection2) {
		showClassname = selection2;
		
	}
	}