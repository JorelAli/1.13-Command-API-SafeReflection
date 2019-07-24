package io.github.jorelali.annotationtest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.google.auto.service.AutoService;

@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {

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
				checkValidField(version, safeReflection.field());
			}
		}
		for(String version : safeReflection.versions()) {
			File lib = new File(root, version + ".txt");
			if (!lib.exists()) {
				messager.printMessage(Kind.ERROR, "Cannot find library123123!");
			}
		}
		
	}
	
	//false if it goes wrong
	public boolean checkValidField(String version, String fieldName) {
		return false;
	}
	
	public boolean checkValidMethod(String version, String methodName) {
		return false;
	}
	
	public void print(String str) {
		messager.printMessage(Kind.MANDATORY_WARNING, str);
	}

}
