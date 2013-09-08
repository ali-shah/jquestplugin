package nz.ac.massey.cs.jquest.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class QueryVisualizationForm extends VisualizationForm {
	

	QueryVisualizationForm(Composite parent, FormToolkit toolkit,
			QueryView view) {
		super(parent, toolkit, view);
		
	}
	protected void createControlsSection(Composite parent) {
		super.createControlsSection(parent);
//		Composite headClient = new Composite(controlComposite, SWT.NULL);
//		headClient.setLayout(new GridLayout(2, false));
		
	}
}
