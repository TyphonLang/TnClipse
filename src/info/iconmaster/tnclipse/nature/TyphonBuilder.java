package info.iconmaster.tnclipse.nature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

import info.iconmaster.typhon.TyphonInput;
import info.iconmaster.typhon.compiler.TyphonCompiler;
import info.iconmaster.typhon.errors.TyphonError;
import info.iconmaster.typhon.linker.TyphonLinker;
import info.iconmaster.typhon.model.Function;
import info.iconmaster.typhon.model.MemberAccess;
import info.iconmaster.typhon.model.Package;
import info.iconmaster.typhon.model.TyphonModelReader;
import info.iconmaster.typhon.model.libs.CorePackage;
import info.iconmaster.typhon.types.TemplateType;
import info.iconmaster.typhon.types.TypeRef;
import info.iconmaster.typhon.types.TyphonAnnotChecker;
import info.iconmaster.typhon.types.TyphonTypeResolver;

public class TyphonBuilder extends IncrementalProjectBuilder {

	/**
	 * ID of this builder.
	 */
	public static final String ID = "info.iconmaster.tnclipse.typhon";
	
	public static final QualifiedName STORAGE_TNI = new QualifiedName("info.iconmaster.tnclipse", "tni");
	public static final QualifiedName STORAGE_COMPILED_PACKAGE = new QualifiedName("info.iconmaster.tnclipse", "package");
	
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				//incrementalBuild(delta, monitor);
				fullBuild(monitor);
			}
		}
		return null;
	}
	
	private void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			TyphonInput tni = new TyphonInput();
			
			getProject().accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {
					resource.setSessionProperty(STORAGE_TNI, tni);
					if (resource instanceof IFile)
						buildResource((IFile)resource, tni);
					return true;
				}
			});
		} catch (CoreException e) {
		}
	}
	
//	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
//		delta.accept(new IResourceDeltaVisitor() {
//			@Override
//			public boolean visit(IResourceDelta delta) throws CoreException {
//				IResource resource = delta.getResource();
//				if (resource instanceof IFile && delta.getKind() != IResourceDelta.REMOVED) {
//					buildResource((IFile)resource);
//				}
//				return true;
//			}
//		});
//	}
	
	private void buildResource(IFile file, TyphonInput tni) {
		if ("tn".equals(file.getFileExtension())) {
			try {
				// clear the file
				file.setSessionProperty(STORAGE_COMPILED_PACKAGE, null);
				file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
				tni.errors.clear();
				
				// repopulate the file
				Package p = TyphonModelReader.parseFile(tni, new File(file.getLocationURI()));
				TyphonLinker.link(p);
				TyphonTypeResolver.resolve(p);
				TyphonAnnotChecker.check(p);
				TyphonCompiler.compile(p);
				
				file.setSessionProperty(STORAGE_COMPILED_PACKAGE, p);
				
				for (TyphonError error : tni.errors) {
					IMarker marker = file.createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.MESSAGE, error.getMessage());
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.CHAR_START, error.source.begin);
					marker.setAttribute(IMarker.CHAR_END, error.source.end+1);
					marker.setAttribute(IMarker.LOCATION, error.source.begin+"-"+error.source.end);
				}
			} catch (IOException e) {
				// ignore; if we can't read it, we can't parse it
			} catch (CoreException e) {
				// TODO
			}
		}
	}
	
	public static List<Package> getPackagesInProject(IProject project) {
		try {
			TyphonInput tni = (TyphonInput) project.getSessionProperty(STORAGE_TNI);
			if (tni == null) {
				return null;
			}
			
			List<Package> ps = new ArrayList<>();
			
			project.accept(new IResourceVisitor() {
				@Override
				public boolean visit(IResource resource) throws CoreException {
					Package p = (Package) resource.getSessionProperty(STORAGE_COMPILED_PACKAGE);
					if (p != null) {
						ps.add(p);
					}
					
					return true;
				}
			});
			
			return ps;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Function> getMainFunctions(List<Package> ps) {
		List<Function> result = new ArrayList<>();
		
		for (Package p : ps) {
			for (Function f : p.getFunctions()) {
				if (f.hasAnnot(f.tni.corePackage.ANNOT_MAIN)) {
					result.add(f);
				}
			}
			
			result.addAll(getMainFunctions(p.getSubpackges()));
		}
		
		return result;
	}
	
	public static String toIdentifierString(Function f) {
		List<String> parentage = new ArrayList<>();
		MemberAccess member = f.getMemberParent();
		while (member != null) {
			if (member instanceof Package && !(member instanceof CorePackage) && member.getName() != null) {
				parentage.add(0, member.getName());
			}
			
			member = member.getMemberParent();
		}
		
		Optional<String> result = parentage.stream().reduce((a,b)->a+"."+b);
		if (result.isPresent()) {
			return result.get()+"."+f.getName();
		} else {
			return f.getName();
		}
	}
	
	public static List<Function> fromIdentifierString(TyphonInput tni, String s) {
		HashMap<TemplateType, TypeRef> map = new HashMap<>();
		List<MemberAccess> result = new ArrayList<>(); result.addAll(tni.corePackage.getCoreSubpackages());
		String[] names = s.split("\\.");
		
		for (String name : names) {
			List<MemberAccess> newResult = new ArrayList<>();
			
			for (MemberAccess member : result) {
				newResult.addAll(member.getMembers(name, map));
			}
			
			result = newResult;
		}
		
		return (List) result.stream().filter(e->e instanceof Function).collect(Collectors.toList());
	}
}
