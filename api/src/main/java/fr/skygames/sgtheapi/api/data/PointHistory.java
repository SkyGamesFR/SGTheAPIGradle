package fr.skygames.sgtheapi.api.data;

import java.sql.Date;

public class PointHistory {

	private int point;
	private Date date;
	
	public PointHistory(int point, Date date) {
		super();
		this.point = point;
		this.date = date;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
