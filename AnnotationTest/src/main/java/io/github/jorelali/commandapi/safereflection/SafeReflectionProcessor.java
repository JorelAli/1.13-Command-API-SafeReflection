package io.github.jorelali.commandapi.safereflection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
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

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		messager = processingEnv.getMessager();
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
		return true;
	}
	
	private File root = new File(".");
	
	private void processSafeReflection(SafeReflection safeReflection) {
		String targetName = getTargetName(safeReflection);
		
		switch(safeReflection.type()) {
			case FIELD:
				for(String version : safeReflection.versions()) {
					FieldResult result = checkValidField(safeReflection, version);
					switch(result.getResult()) {
						case GOOD:
							break;
						case NOT_FOUND:
							error(result.getClassName() + "." + result.getExpectedFieldName() + " was not found for version " + version);
							break;
						case WRONG_TYPE:
							error(result.getExpectedFieldType() + " " + result.getClassName() + "." + result.getExpectedFieldName() + 
									" was not found for version " + version + ". Instead, found field called " + result.getActualFieldType());
							break;
					}
				}
				return;
			case METHOD:
//				for(String version : safeReflection.versions()) {
//					if(!checkValidMethod(version, safeReflection.method(), targetName)) {
//						error("Could not find method '" + safeReflection.method() + "' in class " + targetName + " for version " + version);
//					}
//				}
				return;
		}
	}
	
	//false if it goes wrong
	private FieldResult checkValidField(SafeReflection safeReflection, String version) {
		String target = getTargetName(safeReflection);
		try {
			//Check field existance
			Field field = searchSpigotClass(target, version).getDeclaredField(safeReflection.name());
			
			//Check field type
			String returnType = getReturnType(safeReflection);
			String expectedType = field.getType().getCanonicalName();
			if(!expectedType.equals(returnType)) {
				return new FieldResult(safeReflection.name(), returnType, expectedType, target);
			}
		} catch (NoSuchFieldException e) {
			return new FieldResult(safeReflection.name(), target);
		}
		return new FieldResult();
	}
	
	private boolean checkValidMethod(String version, String methodName, String target) {
		Class<?> targetClass = searchSpigotClass(target, version);
		try {
			targetClass.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException e) {
			return false;
		}
		return true;
	}
	
	private String getTargetName(SafeReflection safeReflection) {
		try {
			safeReflection.target();
		} catch(MirroredTypeException e) {
			return e.getTypeMirror().toString();
		}
		return null;
	}
	
	private String getReturnType(SafeReflection safeReflection) {
		try {
			safeReflection.returnType();
		} catch(MirroredTypeException e) {
			return e.getTypeMirror().toString();
		}
		return null;
	}
	
	private Class<?> searchSpigotClass(String target, String version) {
		//Load the file
		File spigot = new File(root, "spigotlibs/spigot-" + version + ".jar");
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(spigot);
		} catch (IOException e) {
			e.printStackTrace();
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
	
	private void error(String str) {
		messager.printMessage(Kind.ERROR, str);
	}

}
