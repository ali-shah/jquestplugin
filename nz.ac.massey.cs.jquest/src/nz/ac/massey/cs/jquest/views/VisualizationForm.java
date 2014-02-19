/*******************************************************************************
 * Copyright 2005, 2012 CHISEL Group, University of Victoria, Victoria, BC,
 * Canada. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Chisel Group, University of Victoria IBM CAS, IBM Toronto
 * Lab
 ******************************************************************************/
package nz.ac.massey.cs.jquest.views;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import nz.ac.massey.cs.guery.ComputationMode;
import nz.ac.massey.cs.guery.MotifInstance;
import nz.ac.massey.cs.guery.util.Cursor;
import nz.ac.massey.cs.jdg.Dependency;
import nz.ac.massey.cs.jquest.PDEVizImages;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.CompositeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalShift;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

/**
 * This class encapsulates the process of creating the form view in the PDE
 * visualization tool.
 */
/* package */public class VisualizationForm {

	protected static final String SymbolicName_Match_Pattern = "Bundle Name Match Regexp";
	protected static final String SymbolicName_Exclude_Pattern = "Bundle Name Exclude Regexp";
	protected static final String Hide_Fragments = "Hide Fragments";
	/*
	 * These are all the strings used in the form. These can probably be
	 * abstracted for internationalization
	 */
	protected static String Plugin_Dependency_Analysis = "Program Dependency Analysis";
	protected static String Controls = "Options";
	protected static String Show_Dependency_Path = "Show Dependency Path";
	protected static String Version_Number = "Show Bundle Version Numbers";

	protected static String Show_Class_Name_Only = "Hide Package Names";
	protected static String Show_Incoming_Dependencies = "Show Incoming Dependencies";
	protected static String Show_Outgoing_Dependencies = "Show Outgoing Dependencies";
	protected static String Show_External_Dependencies = "Show External Dependencies";
	private static String Include_Variants = "Include Variants";
	
	/*
	 * These are strings and used to determine which radio button is selected
	 */
	public static String Show_All_Paths = "Show All Paths";
	public static String Show_Smart_Path = "Show Smart Path";
	public static String Show_Shortest_Path = "Show Shortest Path";
	private static ComputationMode queryMode = ComputationMode.CLASSES_NOT_REDUCED;

	/*
	 * Some parts of the form we may need access to
	 */
	protected ScrolledForm form;
	protected FormToolkit toolkit;
	protected ManagedForm managedForm;
	protected GraphViewer viewer;
	protected AbstractView view;

	/*
	 * Some buttons that we need to access in local methods
	 */
	protected Button showSmartPath = null;
	protected Button showShortestPath = null;
	protected Button showAllPaths = null;
	protected Button dependencyAnalysis = null;
	protected Button showVersionNumber = null;
	
	protected Button showNext = null;
	protected Button showPrevious = null;
	
	protected Button showClassNameOnly = null;
	protected Button showIncomingDependencies = null;
	protected Button showOutgoingDependencies = null;
	protected Button showExternalDependencies = null;
	protected Button includeVariants = null;
	
	protected String currentPathAnalysis = null;
	protected SashForm sash;
	protected Text searchBox;
	protected ToolItem cancelIcon;
	protected ToolItem masseyLogo; 
	protected Label searchLabel;
	protected Label layoutLabel;
	protected Text layoutBox;
	
	protected Composite controlComposite = null;
	private boolean isQueryView;
	private QueryResults registry;
	private Label resultsLabel;
	

	/**
	 * Creates the form.
	 * @param isQueryView 
	 * 
	 * @param toolKit
	 * @return
	 */
	VisualizationForm(Composite parent, FormToolkit toolkit, AbstractView abstractView) {
		this.toolkit = toolkit;
		this.view = abstractView;
		if(abstractView instanceof QueryView) {
			this.isQueryView = true;
		}
		form = this.toolkit.createScrolledForm(parent);
		managedForm = new ManagedForm(this.toolkit, this.form);
		createHeaderRegion(form);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 4;
		form.getBody().setLayout(layout);

		this.toolkit.decorateFormHeading(this.form.getForm());
		createSash(form.getBody());
	}

	public void setFocusedNodeName(String nodeName) {
		form.setText(Plugin_Dependency_Analysis + ": " + nodeName);
		searchBox.setText("");
		form.reflow(true);
	}

	/**
	 * Creates the header region of the form, with the search dialog, background
	 * and title.  It also sets up the error reporting
	 * @param form
	 */
	protected void createHeaderRegion(ScrolledForm form) {
		Composite headClient = new Composite(form.getForm().getHead(), SWT.NULL);
		headClient.setSize(800, 200);
		GridLayout glayout = new GridLayout();
//		glayout.marginHeight = 0;
//		glayout.marginWidth = 10;
//		glayout.horizontalSpacing = 10;
//		glayout.verticalSpacing = 0;
		glayout.numColumns = 6;
		headClient.setLayout(glayout);
		headClient.setBackgroundMode(SWT.INHERIT_NONE);
		GridData logo = new GridData();
		logo.widthHint = 500;
		Label logoLabel = new Label(headClient, SWT.NONE);
		logoLabel.setImage(PDEVizImages.get(PDEVizImages.IMG_MASSEY_LOGO));
		logoLabel.setData(logo);
		logoLabel.setSize(500, 100);
//		logoLabel.setText("<html><body><a href='http://www.google.com'></a></body></html>");
		
		searchLabel = new Label(headClient, SWT.NONE);
		searchLabel.setText("Search:");
		searchBox = toolkit.createText(headClient, "");
		GridData data = new GridData();
//		data.grabExcessVerticalSpace = true;
		data.horizontalAlignment = GridData.FILL;
//		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.END;
		data.widthHint = 300;
		searchBox.setLayoutData(data);
		ToolBar cancelBar = new ToolBar(headClient, SWT.FLAT );
		
		cancelIcon = new ToolItem(cancelBar, SWT.NONE);
		cancelIcon.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchBox.setText("");
			}
		});
		cancelIcon.setImage(PDEVizImages.get(PDEVizImages.IMG_SEARCH_CANCEL));
		toolkit.paintBordersFor(headClient);
		form.setHeadClient(headClient);
		searchBox.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (searchBox.getText().length() > 0) {
					cancelIcon.setEnabled(true);
				} else {
					cancelIcon.setEnabled(false);
				}
			}
		});
		cancelIcon.setEnabled(false);
		
		
		
//		ToolBar logoBar = new ToolBar(headClient, SWT.RIGHT_TO_LEFT );
//		logoBar.setOrientation(SWT.RIGHT_TO_LEFT);
//		logoBar.setSize(204, 42);
//		masseyLogo = new ToolItem(logoBar, SWT.RIGHT_TO_LEFT);
//		masseyLogo.setImage(PDEVizImages.get(PDEVizImages.IMG_MASSEY_LOGO));
		
		enableSearchBox(true);

		// Add a hyperlink listener for the messages
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
				String title = e.getLabel();
				Object href = e.getHref();
				if (href instanceof IMessage[] && ((IMessage[]) href).length > 1) {
					Point hl = ((Control) e.widget).toDisplay(0, 0);
					hl.x += 10;
					hl.y += 10;
					final Shell shell = new Shell(VisualizationForm.this.form.getShell(), SWT.ON_TOP | SWT.TOOL);
					shell.setImage(getImage(VisualizationForm.this.form.getMessageType()));
					shell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
					shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
					GridLayout layout = new GridLayout();
					layout.numColumns = 1;
					layout.verticalSpacing = 0;
					shell.setText(title);
					shell.setLayout(layout);
					Link link = new Link(shell, SWT.NONE);
					link.setText("<A>close</A>");
					GridData data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
					link.setLayoutData(data);
					link.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							shell.close();
						}
					});
					Group group = new Group(shell, SWT.NONE);
					data = new GridData(SWT.LEFT, SWT.TOP, true, true);
					group.setLayoutData(data);
					group.setLayout(layout);
					group.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
					FormText text = toolkit.createFormText(group, true);
					configureFormText(VisualizationForm.this.form.getForm(), text);
					if (href instanceof IMessage[]) {
						text.setText(createFormTextContent((IMessage[]) href), true, false);
					}

					shell.setLocation(hl);
					shell.pack();
					shell.open();
				} else if (href instanceof IMessage[]) {
					IMessage oneMessage = ((IMessage[]) href)[0];
//					ErrorReporting error = (ErrorReporting) oneMessage.getData();
//					if (error != null) {
//						error.handleError();
//					}
				}
			}
		});
	}

	protected void configureFormText(final Form form, FormText text) {
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				String is = (String) e.getHref();
				try {
					((FormText) e.widget).getShell().dispose();
					int index = Integer.parseInt(is);
					IMessage[] messages = form.getChildrenMessages();
					IMessage message = messages[index];
//					ErrorReporting error = (ErrorReporting) message.getData();
//					if (error != null) {
//						error.handleError();
//					}
				} catch (NumberFormatException ex) {
				}
			}
		});
		text.setImage("error", getImage(IMessageProvider.ERROR));
		text.setImage("warning", getImage(IMessageProvider.WARNING));
		text.setImage("info", getImage(IMessageProvider.INFORMATION));
	}

	String createFormTextContent(IMessage[] messages) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("<form>");
		for (int i = 0; i < messages.length; i++) {
			IMessage message = messages[i];
			pw.print("<li vspace=\"false\" style=\"image\" indent=\"16\" value=\"");
			switch (message.getMessageType()) {
			case IMessageProvider.ERROR:
				pw.print("error");
				break;
			case IMessageProvider.WARNING:
				pw.print("warning");
				break;
			case IMessageProvider.INFORMATION:
				pw.print("info");
				break;
			}
			pw.print("\"> <a href=\"");
			pw.print(i + "");
			pw.print("\">");
			if (message.getPrefix() != null) {
				pw.print(message.getPrefix());
			}
			pw.print(message.getMessage());
			pw.println("</a></li>");
		}
		pw.println("</form>");
		pw.flush();
		return sw.toString();
	}

	/**
	 * Creates the sashform to separate the graph from the controls.
	 * 
	 * @param parent
	 */
	protected void createSash(Composite parent) {
		sash = new SashForm(parent, SWT.NONE);
		this.toolkit.paintBordersFor(parent);

		createGraphSection(sash);
		createControlsSection(sash);
		sash.setWeights(new int[] { 10, 3 });
	}

	protected class MyGraphViewer extends GraphViewer {
		public MyGraphViewer(Composite parent, int style) {
			super(parent, style);
			Graph graph = new Graph(parent, style) {
				public Point computeSize(int hint, int hint2, boolean changed) {
					return new Point(0, 0);
				}
			};
			setControl(graph);
		}
	}

	/**
	 * Creates the section of the form where the graph is drawn
	 * 
	 * @param parent
	 */
	protected void createGraphSection(Composite parent) {
		Section section = this.toolkit.createSection(parent, Section.TITLE_BAR);
		viewer = new MyGraphViewer(section, SWT.NONE);
		section.setClient(viewer.getControl());
	}

	protected void setDependencyPath(boolean enabled) {
		if (showAllPaths.getEnabled() != enabled) {
			showAllPaths.setEnabled(enabled);
		}
		if (showSmartPath.getEnabled() != enabled) {
			showSmartPath.setEnabled(enabled);
		}
		if (showShortestPath.getEnabled() != enabled) {
			showShortestPath.setEnabled(enabled);
		}

		if (!enabled) {
			showAllPaths.setSelection(false);
			showSmartPath.setSelection(false);
			showShortestPath.setSelection(false);
		} else {
			if (currentPathAnalysis == Show_All_Paths) {
				showAllPaths.setSelection(true);
			} else if (currentPathAnalysis == Show_Smart_Path) {
				showSmartPath.setSelection(true);
			} else if (currentPathAnalysis == Show_Shortest_Path) {
				showShortestPath.setSelection(true);
			}
		}
	}

	/**
	 * Creates the section holding the analysis controls.
	 * 
	 * @param parent
	 */
	protected void createControlsSection(Composite parent) {
		
		Section controls = this.toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
		controls.setText(Controls);
		controlComposite = new Composite(controls, SWT.NONE) {
			public Point computeSize(int hint, int hint2, boolean changed) {
				return new Point(0, 0);
			}
		};
		this.toolkit.adapt(controlComposite);
		
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = glayout.marginHeight = 0;
		glayout.numColumns = 1;
		controlComposite.setLayout(glayout);
		showClassNameOnly = this.toolkit.createButton(controlComposite, Show_Class_Name_Only, SWT.CHECK);
		showClassNameOnly.setLayoutData(new GridData(SWT.FILL, SWT.None, true, false));
		showClassNameOnly.setSelection(false);
		showClassNameOnly.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				view.toggleName(showClassNameOnly.getSelection());
			}
		});
		
		showIncomingDependencies = this.toolkit.createButton(controlComposite, Show_Incoming_Dependencies, SWT.CHECK);
		showIncomingDependencies.setLayoutData(new GridData(SWT.FILL, SWT.None, true, false));
		showIncomingDependencies.setSelection(true);
		showIncomingDependencies.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(view instanceof SingleDependencyView){
					((SingleDependencyView) view).showDependencies(showIncomingDependencies.getSelection(), showOutgoingDependencies.getSelection(), showExternalDependencies.getSelection());
				}
			}
		});

		showOutgoingDependencies = this.toolkit.createButton(controlComposite, Show_Outgoing_Dependencies, SWT.CHECK);
		showOutgoingDependencies.setLayoutData(new GridData(SWT.FILL, SWT.None, true, false));
		showOutgoingDependencies.setSelection(true);
		showOutgoingDependencies.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(view instanceof SingleDependencyView){
					((SingleDependencyView) view).showDependencies(showIncomingDependencies.getSelection(), showOutgoingDependencies.getSelection(), showExternalDependencies.getSelection());
				}
			}
		});
		
		showExternalDependencies = this.toolkit.createButton(controlComposite, Show_External_Dependencies, SWT.CHECK);
		showExternalDependencies.setLayoutData(new GridData(SWT.FILL, SWT.None, true, false));
		showExternalDependencies.setSelection(true);
		showExternalDependencies.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(view instanceof SingleDependencyView){
					((SingleDependencyView) view).showDependencies(showIncomingDependencies.getSelection(), showOutgoingDependencies.getSelection(), showExternalDependencies.getSelection());
				}
				
			}
		});
		Composite headClient = new Composite(controlComposite, SWT.NULL);
		headClient.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		layoutLabel = new Label(headClient, SWT.NONE);
		layoutLabel.setText("Choose a Layout:");
		layoutLabel.setLayoutData(gridData);
		final Combo combo= new Combo(headClient, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData data = new GridData(SWT.LEFT, SWT.FILL, true, false);
		data.widthHint = 150;
		combo.setLayoutData(data);
		combo.setBounds(0, 0, 200, 200);
		combo.add("Tree Layout");
		combo.add("Composite Layout");
		combo.add("Spring Layout");
		combo.add("Radial Layout");
		combo.add("Grid Layout");
		
		combo.addSelectionListener(new SelectionAdapter(){
			 public void widgetSelected(SelectionEvent e) {
				 String text = combo.getText();
				 LayoutAlgorithm a = null;
				 if (text.equals("Tree Layout")) {
					a = new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
				 } else if(text.equals("Composite Layout")){
					a = new CompositeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING, new LayoutAlgorithm[] { new DirectedGraphLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), new HorizontalShift(LayoutStyles.NO_LAYOUT_NODE_RESIZING) });
				 } else if(text.equals("Spring Layout")) {
					a = new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
				 } else if(text.equals("Grid Layout")) {
					a = new GridLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
				 } else if(text.equals("Radial Layout")) {
					 a = new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING);
				 }
				 view.setLayout(a);
			 }
		});
		combo.select(2);
		
		if(isQueryView) {
			Composite queryButtons = new Composite(controlComposite, SWT.NULL);
			queryButtons.setLayout(new GridLayout(2, true));
			GridData gData = new GridData(SWT.LEFT, SWT.FILL, false, false);
			showPrevious = this.toolkit.createButton(queryButtons, "Prev", SWT.PUSH);
			showPrevious.setLayoutData(gData);
			showPrevious.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
			final QueryView qview = (QueryView) view;
			showPrevious.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if(registry == null) return;
					MotifInstance instance = null;
					 if (registry.hasPrevCriticalDep()) {
							Dependency prevCritical = registry.getPrevCritical();
							updateResultCounter();
							qview.setSelectionChangedToCriticalDep(prevCritical);
					 } else if(registry.hasPreviousMajorInstance()) {
						Cursor c = registry.previousMajorInstance();
						instance = registry.getInstance(c);
						updateResultCounter();
						qview.setSelectionChanged(instance);
					} 
					else if (registry.hasPreviousMinorInstance()) {
						Cursor c = registry.previousMinorInstance();
						instance = registry.getInstance(c);
						updateResultCounter();
						qview.setSelectionChanged(instance);
					}
				}
			});
			showNext = this.toolkit.createButton(queryButtons, "Next", SWT.PUSH);
			showNext.setLayoutData(new GridData(SWT.LEFT, SWT.None, false, false));
			showNext.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
			showNext.setAlignment(SWT.RIGHT);
			
			showNext.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					MotifInstance instance = null;
					if(registry == null) return;
					if (registry.hasNextCriticalDep()) {
						Dependency nextCritical = registry.getNextCritical();
						qview.setSelectionChangedToCriticalDep(nextCritical);
//						updateResultCounter();
					} else if(registry.hasNextMajorInstance()) {
						Cursor c = registry.nextMajorInstance();
						instance = registry.getInstance(c);
						updateResultCounter();
						qview.setSelectionChanged(instance);
					} else if (registry.hasNextMinorInstance()) {
						Cursor c = registry.nextMinorInstance();
						instance = registry.getInstance(c);
						updateResultCounter();
						qview.setSelectionChanged(instance);
					}  
				}
				
			});
//			createResultsCounter();
			createQueryModeSelectionControls();
			
			updateActions();
		}
		createHelpLink();
		controls.setClient(controlComposite);
	}
	private void createHelpLink() {
		Composite headClient = new Composite(controlComposite, SWT.NULL);
		headClient.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		Hyperlink l = this.toolkit.createHyperlink(headClient, "View Plugin Demo", SWT.NONE);
		l.setLayoutData(new GridData(SWT.FILL, SWT.None, true, false));
		l.addHyperlinkListener(new HyperlinkAdapter() {
			 
			 public void linkActivated(HyperlinkEvent e) {
					try {
		                //  Open default external browser 
		                PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL("http://www.youtube.com/watch?v=NQws7QqMhJ0"));
		              } 
		             catch (PartInitException ex) {
		                 ex.printStackTrace();
		            } 
		            catch (MalformedURLException ex) {
		                ex.printStackTrace();
		            }
			 }

		});
		
		Hyperlink l2 = this.toolkit.createHyperlink(headClient, "Massey Software Lab", SWT.NONE);
		l2.setLayoutData(gridData);
		l2.addHyperlinkListener(new HyperlinkAdapter() {
			 
			 public void linkActivated(HyperlinkEvent e) {
					try {
		                //  Open default external browser 
		                PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL("http://software-lab.massey.ac.nz/"));
		              } 
		             catch (PartInitException ex) {
		                 ex.printStackTrace();
		            } 
		            catch (MalformedURLException ex) {
		                ex.printStackTrace();
		            }
			 }

		});
	}

	private void updateResultCounter() {
//		Cursor c = registry.getCursor();
//		int majI = c.major+1; if(majI ==0) majI = 1;
//		int max = registry.getNumberOfGroups();
//		String s = "instance " + majI + "/" + max;
//		resultsLabel.setText("");
//		resultsLabel.setVisible(true);
		
	}

	private void createResultsCounter() {
		
		Composite headClient = new Composite(controlComposite, SWT.NULL);
		headClient.setLayout(new GridLayout(1, false));
		GridData gridData = new GridData();
		gridData.widthHint = 250;
//		gridData.horizontalSpan = 2;
		this.resultsLabel = new Label(headClient, SWT.LEFT);
//		resultsLabel.setSize(300, 30);
		resultsLabel.setLayoutData(gridData);
		resultsLabel.setText("");
		
	}

	private void createQueryModeSelectionControls() {
		Composite headClient = new Composite(controlComposite, SWT.NULL);
		headClient.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gridData.horizontalSpan = 2;
		includeVariants = this.toolkit.createButton(headClient, Include_Variants, SWT.CHECK);
		includeVariants.setLayoutData(new GridData(SWT.FILL, SWT.None, true, false));
		includeVariants.setSelection(false);
		includeVariants.setToolTipText("Reruns query to include variants of all instances. " +
				"The query execution time is slower in this mode.");
		includeVariants.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MessageBox mb = new MessageBox(view.getSite().getShell(), SWT.YES | SWT.NO);
				mb.setText("Status");
				mb.setMessage("Do you want to rerun the query");
				int r = mb.open();
				if(!includeVariants.getSelection()) {
					 queryMode = ComputationMode.CLASSES_NOT_REDUCED;
				} else {
					 queryMode = ComputationMode.ALL_INSTANCES;
				} 
				if(r == 64) {
					view.performRefresh();
				}
			}
		});
	}

	public void setQueryMode(boolean b) {
		this.isQueryView = b;
	}
	Button getDependencyAnalysis() {
		return dependencyAnalysis;
	}

	/**
	 * Gets the currentGraphViewern
	 * 
	 * @return
	 */
	public GraphViewer getGraphViewer() {
		return viewer;
	}

	/**
	 * Gets the form we created.
	 */
	public ScrolledForm getForm() {
		return form;
	}

	public ManagedForm getManagedForm() {
		return managedForm;
	}

	public Text getSearchBox() {
		return this.searchBox;
	}

	public void enableSearchBox(boolean enable) {
		this.searchLabel.setEnabled(enable);
		this.searchBox.setEnabled(enable);
	}

	protected Image getImage(int type) {
		switch (type) {
		case IMessageProvider.ERROR:
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		case IMessageProvider.WARNING:
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		case IMessageProvider.INFORMATION:
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		}
		return null;
	}
	
	public Button getIncoming() {
		return showIncomingDependencies;
	}
	public Button getOutgoing() {
		return showOutgoingDependencies;
	}
	public Button getExternal() {
		return showExternalDependencies;
	}

	public void setNextInstanceEnabled(boolean b) {
		showNext.setEnabled(b);
	}

	public void setRegistry(QueryResults registry) {
		this.registry = registry;
		updateActions();
		updateResultCounter();
		
	}
	
	public void updateActions() {
		
		if(registry != null) {
			int n = registry.getNumberOfInstances();
			if(n == 1) {
				showNext.setEnabled(false);
				showPrevious.setEnabled(false);
				return;
			}
			boolean hasNextB = registry.hasNextMajorInstance() || registry.hasNextMinorInstance() ||
					registry.hasNextCriticalDep();
			showNext.setEnabled(hasNextB);
			boolean hasPrev = registry.hasPreviousMajorInstance() || registry.hasPreviousMinorInstance() ||
					registry.hasPrevCriticalDep();
			showPrevious.setEnabled(hasPrev);
		} else {
			showNext.setEnabled(false);
			showPrevious.setEnabled(false);
		}
		showExternalDependencies.setEnabled(false);
		showIncomingDependencies.setEnabled(false);
		showOutgoingDependencies.setEnabled(false);
	}

	public Button getClassNameOnly() {
		return showClassNameOnly;
	}

	public ComputationMode getQueryMode() {
		return queryMode;
	}
}
