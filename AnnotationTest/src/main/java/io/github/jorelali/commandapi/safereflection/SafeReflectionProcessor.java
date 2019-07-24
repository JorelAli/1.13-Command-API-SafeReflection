package io.github.jorelali.commandapi.safereflection;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
		String targetName = getTargetName(safeReflection);
		
		//Handle fields
		if(!safeReflection.field().equals("")) {
			for(String version : safeReflection.versions()) {
				if(!checkValidField(version, safeReflection.field(), targetName)) {
					errorReport.add("Could not find field '" + safeReflection.field() + "' in class " + targetName + " for version " + version);
				}
			}
			return;
		} 

		//Handle methods
		if(!safeReflection.method().equals("")) {
			for(String version : safeReflection.versions()) {
				if(!checkValidMethod(version, safeReflection.method(), targetName)) {
					errorReport.add("Could not find method '" + safeReflection.method() + "' in class " + targetName + " for version " + version);
				}
			}
			return;
		}

		messager.printMessage(Kind.ERROR, "Invalid SafeReflection field/method field");
	}
	
	private String getTargetName(SafeReflection safeReflection) {
		try {
			safeReflection.target();
		} catch(MirroredTypeException e) {
			return e.getTypeMirror().toString();
		}
		return null;
	}
	
	private Class<?> getClass(String target, String version) {
		//Load the file
		File spigot = new File(root, "spigotlibs/spigot-" + version + ".jar");
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(spigot);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			Enumeration<JarEntry> entries = jarFile.entries();

			//Load the classloader for the file
			URL[] urls = { new URL("jar:file:" + spigot + "!/") };
			URLClassLoader cl = URLClassLoader.newInstance(urls);

			//Find the target we're looking for
			String targetClass = target.replace(".", "/") + ".class";		
			
			//Find the class we're looking for
			while (entries.hasMoreElements()) {
				if(entries.nextElement().getName().equals(targetClass)) {
			    	return cl.loadClass(target);
			    }
			}
		} catch (MalformedURLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				jarFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	//false if it goes wrong
	public boolean checkValidField(String version, String fieldName, String target) {
		Class<?> targetClass = getClass(target, version);
		try {
			targetClass.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			return false;
		}
		return true;
	}
	
	public boolean checkValidMethod(String version, String methodName, String target) {
		Class<?> targetClass = getClass(target, version);
		try {
			targetClass.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException e) {
			return false;
		}
		return true;
	}
	
	public void print(String str) {
		messager.printMessage(Kind.MANDATORY_WARNING, str);
	}

}
