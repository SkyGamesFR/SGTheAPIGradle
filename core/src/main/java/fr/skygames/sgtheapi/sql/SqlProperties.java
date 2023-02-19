package fr.skygames.sgtheapi.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.skygames.sgtheapi.utils.DefaultProperties;
import fr.skygames.sgtheapi.utils.MissingPropertyException;

public class SqlProperties extends DefaultProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6343827783660630239L;

	@Override
	public String filename() {
		return "sql.properties";
	}

	private static final String JDBC_DRIVER = "jdbc:mariadb:";

	private String getFirstMissingProperty(List<String> list) {
		for (String property : list) {
			if(!this.containsKey(property)) {
				return property;
			}
		}
		return null;
	}
	
	private static List<String> URL_SQL_NEEDED_PROPERTIES(){
		ArrayList<String> l = new ArrayList<String>();
		
		l.add("host");
		l.add("port");
		l.add("database");
		
		return l;
	}
	
	public String getFirstMissingUrlProperty() {
		return this.getFirstMissingProperty(URL_SQL_NEEDED_PROPERTIES());
	}
	
	public String buildUrl() throws MissingPropertyException {
		
		String missing = this.getFirstMissingUrlProperty();
		if(missing != null) {
			throw new MissingPropertyException(missing);
		}

		return JDBC_DRIVER + "//" + this.getProperty("host") + ":" + this.getProperty("port") + "/" + this.getProperty("database");
		//return JDBC_DRIVER + "//" + this.getProperty("host") + ":" + this.getProperty("port") + "/" + this.getProperty("database");
		
	}
	
	private static List<String> CONNECTION_SQL_NEEDED_PROPERTIES(){
		ArrayList<String> l = new ArrayList<String>();
		
		l.add("user");
		l.add("password");
		
		return l;
	}
	
	public String getFirstMissingConnectionProperty() {
		return this.getFirstMissingProperty(CONNECTION_SQL_NEEDED_PROPERTIES());
	}
	
	public Properties buildConnectionProperties() throws MissingPropertyException {
		
		Properties p = new Properties();
		p.putAll(this);

		for (String property : URL_SQL_NEEDED_PROPERTIES()) {
			p.remove(property);
		}
		
		String missing = this.getFirstMissingConnectionProperty();
		if(missing != null) {
			throw new MissingPropertyException(missing);
		}
		
		return p;
		
	}
}
