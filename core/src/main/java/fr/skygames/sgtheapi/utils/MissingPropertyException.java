package fr.skygames.sgtheapi.utils;

public class MissingPropertyException extends Exception {

	private String value;
	
	public MissingPropertyException(String missing) {
		
		super("Missing Sql Property \"" + missing + "\"");
		this.value = missing;
		
	}
	
	public String getValue() {
		return value;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8377209460055659623L;

}
