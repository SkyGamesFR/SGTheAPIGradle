package fr.skygames.sgtheapi.api.data;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Player {

	private String uuid;
	private String name;
	
	private Date first_login;
	private Date last_login;
	
	private String team;
	
	public Player(String uuid, String name, Date first_login, Date last_login, String team) {
		this.uuid = uuid;
		this.name = name;
		this.first_login = first_login;
		this.last_login = last_login;
		this.team = team;
	}
	
	public Player(String uuid, String name) {
		this(uuid, name, null, null, null);
	}

	public Player() {}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getFirst_login() {
		return first_login;
	}

	public void setFirst_login(Date first_login) {
		this.first_login = first_login;
	}

	public Date getLast_login() {
		return last_login;
	}

	public void setLast_login(Date last_login) {
		this.last_login = last_login;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

}
