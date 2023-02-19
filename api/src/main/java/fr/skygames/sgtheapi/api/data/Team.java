package fr.skygames.sgtheapi.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Team {

	private String id;
	private String display_name;
	private String color_code;
	private Integer point;
	private String owner;
	
	public Team(String id, String display_name, String color_code, Integer point) {
		this.id = id;
		this.display_name = display_name;
		this.color_code = color_code;
		this.point = point;
		this.owner = "owner";
	}

	public Team(String id, String display_name, String color_code) {
		this(id, display_name, color_code, null);
	}

	public Team() {}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getColor_code() {
		return color_code;
	}

	public void setColor_code(String color_code) {
		this.color_code = color_code;
	}

	public Integer getPoint() {
		return point;
	}

	public void setPoint(Integer point) {
		this.point = point;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}
