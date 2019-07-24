package io.github.jorelali.commandapi.safereflection;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic.Kind;

import com.google.auto.service.AutoService;

@AutoService(Processor.class)
public class SafeReflectionProcessor extends AbstractProcessor {

	private Messager messager;
	private List<String> errorReport;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		messager = processingEnv.getMessager();
		errorReport = new ArrayList<>();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotations = new HashSet<String>();
		annotations.add(SafeReflection.class.getCanonicalName());
		annotations.add(SafeReflections.class.getCanonicalName());
		return annotations;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		//Handle single annotations of SafeReflection
		roundEnv.getElementsAnnotatedWith(SafeReflection.class).stream().forEach(element -> {
			processSafeReflection(element.getAnnotation(SafeReflection.class));
		});
		
		//Handle multiple annotations of SafeReflection
		roundEnv.getElementsAnnotatedWith(SafeReflections.class).stream().forEach(element -> {
			Arrays.stream(element.getAnnotation(SafeReflections.class).value()).forEach(safeReflection -> {
				processSafeReflection(safeReflection);
			});
		});
		
		//Print error report
		if(!errorReport.isEmpty()) {
			errorReport.stream().forEach(str -> print(str));
			messager.printMessage(Kind.ERROR, "Errors were found... halting compilation");
		}
		return true;
	}
	
	private File root = new File(".");
	
	private void processSafeReflection(SafeReflection safeReflection) {
		if(!safeReflection.field().equals("")) {
			for(String version : safeReflection.versions()) {
				checkValidField(version, safeReflection.field(), getTargetName(safeReflection));
			}
		} else if(!safeReflection.method().equals("")) {
			for(String version : safeReflection.versions()) {
				checkValidMethod(version, safeReflection.method(), getTargetName(safeReflection));
			}
		} else {
			messager.printMessage(Kind.ERROR, "Invalid SafeReflection field/method field");
		}
	}
	
	private String getTargetName(SafeReflection safeReflection) {
		try {
			safeReflection.target();
		} catch(MirroredTypeException e) {
			return e.getTypeMirror().toString();
		}
		return null;
	}
	
	public Class<?> getClass(String target, String version) throws IOException, ClassNotFoundException {
		
		//Load the file
		File spigot = new File(root, "spigotlibs/spigot-" + version + ".jar");
		JarFile jarFile = new JarFile(spigot);
		Enumeration<JarEntry> e = jarFile.entries();

		//Load the classloader for the file
		URL[] urls = { new URL("jar:file:" + spigot + "!/") };
		URLClassLoader cl = URLClassLoader.newInstance(urls);

		String targetClass = target.replace(".", "/") + ".class";		
		boolean found = false;
		
		while (e.hasMoreElements()) {
		    JarEntry entry = e.nextElement();
		    
		    if(entry.getName().equals(targetClass)) {
		    	found = true;
		    	//print(entry.getName());
		    	Arrays.stream(cl.loadClass(target).getDeclaredMethods()).forEach(o -> {
		    		print(o.getName());
		    	});
		    	break;
		    }
		    
//		    if(entry.isDirectory() && index != directories.length - 1) {
//		    	//traverse deeper
//		    	//entry.
//		    }
//		    
//		    if(entry.isDirectory() || !entry.getName().endsWith(".class")){
//		        continue;
//		    }
//		    // -6 because of .class
//		    String className = entry.getName().substring(0,entry.getName().length()-6);
//		    className = className.replace('/', '.');
//		    Class c = cl.loadClass(className);

		}
		
		jarFile.close();
		return null;
	}
	
	//false if it goes wrong
	public boolean checkValidField(String version, String fieldName, String target) {
		try {
			getClass(target, version);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean checkValidMethod(String version, String methodName, String target) {
		try {
			getClass(target, version);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void print(String str) {
		messager.printMessage(Kind.MANDATORY_WARNING, str);
	}

}
