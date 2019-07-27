package io.github.jorelali.commandapi.safereflection;

public class FieldResult {

	private String expectedFieldName;
	private String expectedFieldType;
	private String actualFieldType;
	private String className;
	private Result result;
	
	enum Result {
		WRONG_TYPE, NOT_FOUND, GOOD;
	}
	
	public FieldResult(String expectedFieldName, String expectedFieldType, String actualFieldType, String className) {
		this.expectedFieldName = expectedFieldName;
		this.expectedFieldType = expectedFieldType;
		this.actualFieldType = actualFieldType;
		this.className = className;
		result = Result.WRONG_TYPE;
	}
	
	public FieldResult(String expectedFieldName, String className) {
		this.expectedFieldName = expectedFieldName;
		this.className = className;
		result = Result.NOT_FOUND;
	}
	
	public FieldResult() {
		result = Result.GOOD;
	}

	public String getExpectedFieldName() {
		return expectedFieldName;
	}

	public String getActualFieldType() {
		return actualFieldType;
	}

	public String getClassName() {
		return className;
	}

	public Result getResult() {
		return result;
	}

	public String getExpectedFieldType() {
		return expectedFieldType;
	}
	
}
