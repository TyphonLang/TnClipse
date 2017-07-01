package info.iconmaster.tnclipse.editor;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import info.iconmaster.tnclipse.TyphonIcons;
import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.compiler.TyphonSourceReader;
import info.iconmaster.typhon.language.Field;
import info.iconmaster.typhon.language.Function;
import info.iconmaster.typhon.language.Import;
import info.iconmaster.typhon.language.Package;
import info.iconmaster.typhon.language.StaticInitBlock;
import info.iconmaster.typhon.language.TyphonLanguageEntity;
import info.iconmaster.typhon.types.EnumType;
import info.iconmaster.typhon.types.UserType;

public class TyphonOutlinePage extends ContentOutlinePage {
	private TyphonEditor editor;
	private IEditorInput input;
	private Package parsedPackage;
	
	private List<TyphonLanguageEntity> getTyphonChildren(Package p) {
		List<TyphonLanguageEntity> a = new ArrayList<>();
		
		a.addAll(p.getSubpackges());
		a.addAll(p.getImports());
		a.addAll(p.getFields());
		a.addAll(p.getFunctions());
		a.addAll(p.getTypes());
		a.addAll(p.getStaticInitBlocks());
		
		return a;
	}
	
	private class TyphonContentProvider implements ITreeContentProvider {
		@Override
		public Object[] getElements(Object unused) {
			return getTyphonChildren(parsedPackage).toArray();
		}

		@Override
		public Object[] getChildren(Object e) {
			if (e instanceof Package) {
				return getTyphonChildren(((Package)e)).toArray();
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
			} else {
				return null;
			}
		}

		@Override
		public boolean hasChildren(Object element) {
			return element instanceof Package;
		}
	}
	
	private class TyphonLabelProvider extends LabelProvider {
		@Override
		public Image getImage(Object e) {
			if (e instanceof Package) {
				return TyphonIcons.ICON_PACKAGE;
			} else if (e instanceof Function) {
				return TyphonIcons.ICON_FUNC;
			} else if (e instanceof Field) {
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
			} else if (e instanceof UserType) {
				return ((UserType)e).getName();
			} else if (e instanceof Import.PackageImport) {
				return ((Import.PackageImport)e).getPackageName().stream().reduce("", (a,b)->a+"."+b).substring(1);
			} else if (e instanceof Import.RawImport) {
				return ((Import.RawImport)e).getImportData();
			} else if (e instanceof StaticInitBlock) {
				return "<static init>";
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
		TyphonInput tni = new TyphonInput();
		IDocument doc = editor.getDocumentProvider().getDocument(input);
		if (doc != null) {
			parsedPackage = TyphonSourceReader.parseString(tni, doc.get());
		} else {
			parsedPackage = null;
		}
		
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
					
					if (item == e) {
						editor.setHighlightRange(item.source.begin, item.source.end-item.source.begin, true);
						break;
					}
					
					if (item instanceof Package) {
						items.addAll(getTyphonChildren((Package)item));
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
