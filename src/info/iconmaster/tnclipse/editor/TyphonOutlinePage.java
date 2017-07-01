package info.iconmaster.tnclipse.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import info.iconmaster.tnclipse.TyphonIcons;
import info.iconmaster.tnclipse.nature.TyphonBuilder;
import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.compiler.TyphonSourceReader;
import info.iconmaster.typhon.language.Field;
import info.iconmaster.typhon.language.Function;
import info.iconmaster.typhon.language.Import;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.language.StaticInitBlock;
import info.iconmaster.typhon.language.TyphonLanguageEntity;
import info.iconmaster.typhon.types.EnumType;
import info.iconmaster.typhon.types.EnumType.EnumChoice;
import info.iconmaster.typhon.types.Type;
import info.iconmaster.typhon.types.UserType;

public class TyphonOutlinePage extends ContentOutlinePage {
	private TyphonEditor editor;
	private IEditorInput input;
	private Package parsedPackage;
	
	private List<TyphonLanguageEntity> getTyphonChildren(Package p) {
		List<TyphonLanguageEntity> a = new ArrayList<>();
		
		a.addAll(p.getSubpackges().stream().filter((pp)->(pp.getName() != null)).collect(Collectors.toList()));
		a.addAll(p.getImports());
		a.addAll(p.getFields());
		a.addAll(p.getFunctions());
		a.addAll(p.getTypes());
		a.addAll(p.getStaticInitBlocks());
		
		return a;
	}
	
	private static class EnumChoiceWithData extends TyphonLanguageEntity {
		EnumChoice choice;
		EnumType parent;
		
		public EnumChoiceWithData(EnumChoice choice, EnumType parent) {
			super(choice.tni, choice.source);
			
			this.choice = choice;
			this.parent = parent;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EnumChoiceWithData other = (EnumChoiceWithData) obj;
			if (choice == null) {
				if (other.choice != null)
					return false;
			} else if (!choice.equals(other.choice))
				return false;
			if (parent == null) {
				if (other.parent != null)
					return false;
			} else if (!parent.equals(other.parent))
				return false;
			return true;
		}
	}
	
	private class TyphonContentProvider implements ITreeContentProvider {
		
		
		@Override
		public Object[] getElements(Object unused) {
			if (parsedPackage == null) {
				return new Object[0];
			}
			
			return getTyphonChildren(parsedPackage).toArray();
		}

		@Override
		public Object[] getChildren(Object e) {
			if (e instanceof Package) {
				return getTyphonChildren(((Package)e)).toArray();
			} else if (e instanceof EnumType) {
				List<TyphonLanguageEntity> a = getTyphonChildren(((Type)e).getTypePackage());
				for (EnumChoice choice : ((EnumType)e).getChoices()) {
					a.add(new EnumChoiceWithData(choice, ((EnumType)e)));
				}
				return a.toArray();
			} else if (e instanceof Type) {
				return getTyphonChildren(((Type)e).getTypePackage()).toArray();
			} else {
				return new Object[0];
			}
		}

		@Override
		public Object getParent(Object e) {
			if (e instanceof Package) {
				Package p = ((Package) e).getParent();
				if (p == parsedPackage) {
					return input;
				}
				return p;
			} else if (e instanceof Type) {
				Package p = ((Type) e).getParent();
				if (p == parsedPackage) {
					return input;
				}
				return p;
			} else if (e instanceof EnumChoiceWithData) {
				return ((EnumChoiceWithData)e).parent;
			} else {
				return null;
			}
		}

		@Override
		public boolean hasChildren(Object e) {
			if (e instanceof Package) {
				return !getTyphonChildren((Package)e).isEmpty();
			} else if (e instanceof EnumType) {
				return !getTyphonChildren(((EnumType)e).getTypePackage()).isEmpty() && !((EnumType)e).getChoices().isEmpty();
			} else if (e instanceof Type) {
				return !getTyphonChildren(((Type)e).getTypePackage()).isEmpty();
			} else {
				return false;
			}
		}
	}
	
	private class TyphonLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object e) {
			if (e instanceof Package) {
				return TyphonIcons.ICON_PACKAGE;
			} else if (e instanceof Function) {
				return TyphonIcons.ICON_FUNC;
			} else if (e instanceof Field || e instanceof EnumChoiceWithData) {
				return TyphonIcons.ICON_FIELD;
			} else if (e instanceof EnumType) {
				return TyphonIcons.ICON_ENUM;
			} else if (e instanceof UserType) {
				return TyphonIcons.ICON_CLASS;
			} else if (e instanceof Import) {
				return TyphonIcons.ICON_IMPORT;
			} else {
				return null;
			}
		}
		
		@Override
		public String getText(Object e) {
			if (e instanceof Package) {
				return ((Package)e).getName();
			} else if (e instanceof Function) {
				return ((Function)e).getName();
			} else if (e instanceof Field) {
				return ((Field)e).getName();
			} else if (e instanceof EnumType) {
				return ((EnumType)e).getName();
			} else if (e instanceof UserType) {
				return ((UserType)e).getName();
			} else if (e instanceof Import.PackageImport) {
				return ((Import.PackageImport)e).getPackageName().stream().reduce("", (a,b)->a+"."+b).substring(1);
			} else if (e instanceof Import.RawImport) {
				return ((Import.RawImport)e).getImportData();
			} else if (e instanceof StaticInitBlock) {
				return "<static init>";
			} else if (e instanceof EnumChoiceWithData) {
				return ((EnumChoiceWithData)e).choice.getName();
			} else {
				return "ERROR";
			}
		}
		
		
	}
	
	public TyphonOutlinePage(TyphonEditor editor, IEditorInput input) {
		super();
		
		this.editor = editor;
		this.input = input;
		
		update();
	}
	
	public void update() {
		parsedPackage = null;
		
		TyphonInput tni = new TyphonInput();
		IDocument doc = editor.getDocumentProvider().getDocument(input);
		IFile file = null;
		
		// use the result of the Typhon builder if possible
		if (input instanceof IFileEditorInput) {
			file = ((IFileEditorInput) input).getFile();
		}
		
		try {
			if (file != null) {
				parsedPackage = (Package) file.getSessionProperty(TyphonBuilder.STORAGE_COMPILED_PACKAGE);
			}
		} catch (CoreException e) {
			// ignore; just fall back to parsing the document directly.
		}
		
		// if we don't have builder data, parse the contents of the document
		if (parsedPackage == null && doc != null) {
			parsedPackage = TyphonSourceReader.parseString(tni, doc.get());
		}
		
		// update the tree view
		TreeViewer viewer = getTreeViewer();

		if (viewer != null) {
			Control control= viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(input);
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new TyphonContentProvider());
		viewer.setLabelProvider(new TyphonLabelProvider());
		viewer.addSelectionChangedListener(this);
		
		if (input != null) {
			viewer.setInput(input);
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		
		TreeSelection sel = (TreeSelection) event.getSelection();
		if (sel.isEmpty() || parsedPackage == null) {
			editor.resetHighlightRange();
		} else {
			try {
				List<TyphonLanguageEntity> items = getTyphonChildren(parsedPackage);
				TyphonLanguageEntity e = (TyphonLanguageEntity) sel.getFirstElement();
				
				for (int i = 0; i < items.size(); i++) {
					TyphonLanguageEntity item = items.get(i);
					
					if (item.equals(e)) {
						editor.setHighlightRange(item.source.begin, item.source.end-item.source.begin+1, true);
						break;
					}
					
					if (item instanceof Package) {
						items.addAll(getTyphonChildren((Package)item));
					} else if (item instanceof EnumType) {
						items.addAll(getTyphonChildren(((EnumType)item).getTypePackage()));
						for (EnumChoice choice : ((EnumType)item).getChoices()) {
							items.add(new EnumChoiceWithData(choice, (EnumType)item));
						}
					} else if (item instanceof Type) {
						items.addAll(getTyphonChildren(((Type)item).getTypePackage()));
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				editor.resetHighlightRange();
			}
		}
	}
	
	public void setInput(IEditorInput input) {
		this.input = input;
		update();
	}
}
