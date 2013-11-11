package nz.ac.massey.cs.jquest.views;

import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jdg.DependencyType;
import nz.ac.massey.cs.jdg.TypeNode;
import nz.ac.massey.cs.jquest.utils.Utils;

import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.zest.core.viewers.EntityConnectionData;

public class ZestLabelProvider extends LabelProvider implements IContentProvider {
	private boolean showClassname = false;
	public Image getImage(Object element) {
		if (element.getClass() == EntityConnectionData.class) {
			return null;
		}

		if (element instanceof TypeNode) {
			TypeNode tn = (TypeNode) element;
			if (tn.getName() == null)
				return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_PACKAGE);
			else {
				if (tn.isAbstract()) {
					return JavaPluginImages
							.get(JavaPluginImages.IMG_OBJS_INTERFACE);
				} else {
					return JavaPluginImages
							.get(JavaPluginImages.IMG_OBJS_CLASS);
				}
			}

		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof TypeNode) {
			TypeNode node = (TypeNode) element;
		    String name = null;
		    if(showClassname) name = node.getName();
		    else name = node.getFullname();
		    return Utils.removeTrailingDot(name);
		} else if (element instanceof Dependency) {
			Dependency d = (Dependency) element;
			if (d.hasType(DependencyType.USES)) {
				return "uses";
			} else if (d.hasType(DependencyType.EXTENDS)) {
				return "extends";
			} else if (d.hasType(DependencyType.IMPLEMENTS)) {
				return "implements";
			} else {
				return "";
			}
		} else {
			return "";
		}
	}
	
	public void setToggleName(boolean selection2) {
		showClassname = selection2;
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		
	}
}