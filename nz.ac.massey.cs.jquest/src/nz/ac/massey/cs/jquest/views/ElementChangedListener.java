package nz.ac.massey.cs.jquest.views;


import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

public class ElementChangedListener implements IElementChangedListener {

	private boolean projectHasChanged = false;

	public void elementChanged(ElementChangedEvent event) {
		IJavaElementDelta javaElementDelta = event.getDelta();
		processDelta(javaElementDelta);
	}

	private void processDelta(IJavaElementDelta delta) {
		IJavaElement javaElement = delta.getElement();
		switch(javaElement.getElementType()) {
		case IJavaElement.JAVA_MODEL:
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
		case IJavaElement.PACKAGE_FRAGMENT:
			IJavaElementDelta[] affectedChildren = delta.getAffectedChildren();
			for(IJavaElementDelta affectedChild : affectedChildren) {
				processDelta(affectedChild);
			}
			break;
		case IJavaElement.COMPILATION_UNIT:
			if(delta.getKind() == IJavaElementDelta.ADDED) {
				projectHasChanged = true;
			}
			else if(delta.getKind() == IJavaElementDelta.REMOVED) {
				projectHasChanged = true;
			}
			else if(delta.getKind() == IJavaElementDelta.CHANGED) {
				projectHasChanged = true;
//				if((delta.getFlags() & IJavaElementDelta.F_FINE_GRAINED) != 0) {
//				}
			}
		}
	}

	public boolean projectHasChanged() {
		return projectHasChanged;
	}
	public void reset() {
		projectHasChanged = false;
	}
}
