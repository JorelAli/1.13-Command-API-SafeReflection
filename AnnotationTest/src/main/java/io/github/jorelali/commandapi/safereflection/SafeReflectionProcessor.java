package io.github.jorelali.commandapi.safereflection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
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
		for(String version : safeReflection.versions()) {
			switch(safeReflection.type()) {
				case FIELD: {
					FieldResult result = checkValidField(safeReflection, version);
					switch(result.getResult()) {
						case GOOD:
							break;
						case NOT_FOUND:
							error("Field " + result.getClassName() + "." + result.getExpectedFieldName() + " was not found for version " + version);
							break;
						case WRONG_TYPE:
							error("Field " + result.getExpectedFieldType() + " " + result.getClassName() + "." + result.getExpectedFieldName() + 
									" was not found for version " + version + ". Instead, found field returning " + result.getActualFieldType());
							break;
					}
				}
				case METHOD: {
					MethodResult result = checkValidMethod(safeReflection, version);
					switch(result.getResult()) {
						case GOOD:
							break;
						case NOT_FOUND:
							error("Method " + result.getClassName() + "." + result.getExpectedMethodName() + "() was not found for version " + version);
							break;
						case WRONG_RETURN_TYPE:
							error("Method " + result.getExpectedMethodReturnType() + " " + result.getClassName() + "." + result.getExpectedMethodName() + 
									"() was not found for version " + version + ". Instead, found method returning " + result.getActualMethodReturnType());
							break;
						case WRONG_ARGS:
							error("Method " + result.getExpectedMethodReturnType() + " " + result.getClassName() + "." + result.getExpectedMethodName() + 
									"(" + arrToStr(result.getExpectedMethodArgs()) +  ") was not found for version " + version + 
									". Instead, found method with arguments as " + arrToStr(result.getActualMethodArgs()));
							break;
						
					}
				}
			}
		}
	}
	
	private String arrToStr(String[] arr) {
		String arrStr = Arrays.toString(arr);
		return arrStr.substring(1, arrStr.length() - 1);
	}
	
	//false if it goes wrong
	private FieldResult checkValidField(SafeReflection safeReflection, String version) {
		String target = getTargetName(safeReflection);
		try {
			//Check field existance
			Field field = searchSpigotClass(target, version).getDeclaredField(safeReflection.name());
			
			//Check field type
			String providedReturnType = getReturnType(safeReflection);
			String actualReturnType = field.getType().getCanonicalName();
			if(!actualReturnType.equals(providedReturnType)) {
				return new FieldResult(safeReflection.name(), providedReturnType, actualReturnType, target);
			}
		} catch (NoSuchFieldException e) {
			return new FieldResult(safeReflection.name(), target);
		}
		return new FieldResult();
	}
	
	private MethodResult checkValidMethod(SafeReflection safeReflection, String version) {
		String target = getTargetName(safeReflection);
		
		try {
			//Check existance
			Method method = searchSpigotClass(target, version).getDeclaredMethod(safeReflection.name());
			
			//Check return type
			String providedReturnType = getReturnType(safeReflection);
			String actualReturnType = method.getReturnType().getCanonicalName();
			if(!actualReturnType.equals(providedReturnType)) {
				new MethodResult(safeReflection.name(), providedReturnType, actualReturnType, target);
			}
			
			//Check method args
			String[] providedMethodArgs = getMethodArgs(safeReflection);
			String[] actualMethodArgs = Arrays.stream(method.getParameterTypes()).map(Class::getCanonicalName).toArray(String[]::new);
			if(!Arrays.equals(providedMethodArgs, actualMethodArgs)) {
				return new MethodResult(safeReflection.name(), providedReturnType, actualReturnType, providedMethodArgs, actualMethodArgs, target);
			}
			
		} catch (NoSuchMethodException e) {
			return new MethodResult(safeReflection.name(), target);
		}
		return new MethodResult();
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
	
	private String[] getMethodArgs(SafeReflection safeReflection) {
		try {
			safeReflection.methodArgs();
		} catch(MirroredTypesException e) {
			return e.getTypeMirrors().stream().map(TypeMirror::toString).toArray(String[]::new);
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
