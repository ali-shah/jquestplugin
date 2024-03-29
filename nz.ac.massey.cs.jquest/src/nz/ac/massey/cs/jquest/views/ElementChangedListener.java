/*
 * Copyright 2014 Ali Shah Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.gnu.org/licenses/agpl.html Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package nz.ac.massey.cs.jquest.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

public class ElementChangedListener implements IElementChangedListener {

	private IProject selectedProject = null;
	private boolean projectHasChanged = false;
	private AbstractView singleView = null;

	public ElementChangedListener(AbstractView abstractView, IProject selectedProject) {
		this.selectedProject = selectedProject;
		this.singleView  = abstractView; 
	}

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
			ICompilationUnit cu = (ICompilationUnit)javaElement;
			if(selectedProject == null) return;
			if(!cu.getJavaProject().getProject().getName().equals(selectedProject.getName())) {
				return;
			}
			if(delta.getKind() == IJavaElementDelta.ADDED) {
				projectHasChanged = true;
				singleView.projectUpdated();
			}
			else if(delta.getKind() == IJavaElementDelta.REMOVED) {
				projectHasChanged = true;
				singleView.projectUpdated();
			}
			else if(delta.getKind() == IJavaElementDelta.CHANGED) {
				projectHasChanged = true;
				singleView.projectUpdated();
//				if((delta.getFlags() & IJavaElementDelta.F_FINE_GRAINED) != 0) {
//				}
			}
		}
	}

	public boolean hasProjectChanged(IProject p) {
		if(selectedProject == null) {
			selectedProject = p;
			return true;
		}
		if(selectedProject.getName().equals(p.getName())){
			return false;
		} else {
			selectedProject = p;
			return true;
		}
	}
	public boolean hasProjectModified() {
		return projectHasChanged;
	}
	public void reset() {
		projectHasChanged = false;
	}

	public IProject getSelectedProject() {
		return selectedProject;
	}

	public void setProject(IProject selectedProject2) {
		this.selectedProject = selectedProject2;
		
	}
}
