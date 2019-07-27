package io.github.jorelali.commandapi.safereflection;

public class MethodResult {

	private String expectedMethodName;
	
	private String expectedMethodReturnType;
	private String actualMethodReturnType;
	
	private String[] expectedMethodArgs;
	private String[] actualMethodArgs;
	
	private String className;
	private Result result;
	
	enum Result {
		WRONG_ARGS, WRONG_RETURN_TYPE, NOT_FOUND, GOOD;
	}
	
	public MethodResult(String expectedMethodName, String expectedMethodReturnType, String actualMethodReturnType, String[] expectedMethodArgs, String[] actualMethodArgs, String className) {
		this.expectedMethodName = expectedMethodName;
		
		this.expectedMethodReturnType = expectedMethodReturnType;
		this.actualMethodReturnType = actualMethodReturnType;
		
		this.expectedMethodArgs = expectedMethodArgs;
		this.actualMethodArgs = actualMethodArgs;
		
		this.className = className;
		result = Result.WRONG_RETURN_TYPE;
	}

	public MethodResult(String expectedMethodName, String expectedMethodReturnType, String actualMethodReturnType, String className) {
		this.expectedMethodName = expectedMethodName;
		
		this.expectedMethodReturnType = expectedMethodReturnType;
		this.actualMethodReturnType = actualMethodReturnType;
		
		this.className = className;
		result = Result.WRONG_RETURN_TYPE;
	}
	
	public MethodResult(String expectedMethodName, String className) {
		this.expectedMethodName = expectedMethodName;
		this.className = className;
		result = Result.NOT_FOUND;
	}
	

	
	public String getExpectedMethodName() {
		return expectedMethodName;
	}

	public String getExpectedMethodReturnType() {
		return expectedMethodReturnType;
	}

	public String getActualMethodReturnType() {
		return actualMethodReturnType;
	}

	public String[] getExpectedMethodArgs() {
		return expectedMethodArgs;
	}

	public String[] getActualMethodArgs() {
		return actualMethodArgs;
	}

	public String getClassName() {
		return className;
	}

	public Result getResult() {
		return result;
	}
	
	public MethodResult() {
		result = Result.GOOD;
	}
	
}
