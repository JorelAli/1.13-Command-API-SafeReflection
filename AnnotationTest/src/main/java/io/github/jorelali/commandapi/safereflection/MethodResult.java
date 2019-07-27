package io.github.jorelali.commandapi.safereflection;

public class MethodResult {

	private String expectedMethodName;
	
	private String expectedMethodReturnType;
	private String actualMethodReturnType;
	
	
	
	private String className;
	private Result result;
	
	enum Result {
		WRONG_TYPE, NOT_FOUND, GOOD;
	}
	
	public MethodResult(String expectedMethodName, String expectedMethodReturnType, String actualMethodReturnType, String className) {
		this.expectedMethodName = expectedMethodName;
		this.expectedMethodReturnType = expectedMethodReturnType;
		this.actualMethodReturnType = actualMethodReturnType;
		this.className = className;
		result = Result.WRONG_TYPE;
	}
	
	public MethodResult(String expectedMethodName, String className) {
		this.expectedMethodName = expectedMethodName;
		this.className = className;
		result = Result.NOT_FOUND;
	}
	
	public MethodResult() {
		result = Result.GOOD;
	}
	
}
