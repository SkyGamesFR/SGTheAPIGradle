package fr.skygames.sgtheapi.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.skygames.sgtheapi.SkyGamesTheApp;
import fr.skygames.sgtheapi.api.data.Player;
import fr.skygames.sgtheapi.api.data.PointHistory;
import fr.skygames.sgtheapi.api.data.Team;
import fr.skygames.sgtheapi.sql.SqlConnector;
import fr.skygames.sgtheapi.utils.MissingPropertyException;


public class TeamService {

	private SqlConnector connector;

	public TeamService(SqlConnector connector) {
		this.connector = connector;
	}

	public List<Team> getAll() throws ClassNotFoundException, SQLException, MissingPropertyException {
		List<Team> teams = new ArrayList<Team>();

		Statement stmt = this.connector.getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("{CALL getAllTeams()}");
		
		while(rs.next()){

			Team team = TeamService.get(rs);
			teams.add(team);
			
		}

		return teams;
	}

	public static Team get(final ResultSet rs) throws SQLException {
		if(rs.first()) {
			return TeamService.getTeamFromResultSet(rs);
		}else {
			return null;
		}
	}
	
	public Team get(final String id) throws SQLException, ClassNotFoundException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getTeam(?)}");
		stmt.setString(1, id);
		return get(stmt.executeQuery());
	}
	
	public final static Team getTeamFromResultSet(ResultSet rs) throws SQLException {
		Team team; 
		try {
			team = new Team(rs.getString("id"), rs.getString("display_name"),rs.getString("color_code"),rs.getInt("point"));
		} catch (Exception e) {
			team = new Team(rs.getString("id"), rs.getString("display_name"),rs.getString("color_code"), null);
		}
		return team;
	}
	
	public List<Player> getPlayers(final String id) throws ClassNotFoundException, SQLException, MissingPropertyException{
		List<Player> players = new ArrayList<Player>();

		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getTeamPlayers(?)}");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();

		while(rs.next()){

			Player player = PlayerService.getPlayerFromResultSet(rs);
			players.add(player);

		}

		return players;
	}

	public List<Player> getOwner(final String id) throws ClassNotFoundException, SQLException, MissingPropertyException{
		return null;
	}
	
	public int delete(final String id) throws ClassNotFoundException, SQLException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL deleteTeam(?)}");
		stmt.setString(1, id);
		return stmt.executeUpdate();
	}
	
	public Team add(final String id, final String name, final String color) throws ClassNotFoundException, SQLException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL addTeam(?,?,?)}");
		stmt.setString(1, id);
		stmt.setString(2, name);
		stmt.setString(3, color);
		return get(stmt.executeQuery());
	}
	
	public Team update(final String id, final String name, final String color) throws ClassNotFoundException, SQLException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL updateTeam(?,?,?)}");
		stmt.setString(1, id);
		stmt.setString(2, name);
		stmt.setString(3, color);
		return get(stmt.executeQuery());
	}
	
	private Integer getPoint(ResultSet rs) throws SQLException {
		if(rs.first()) {
			return rs.getInt("point");
		}else {
			return null;
		}
	}
	
	public Integer getPoint(final String id) throws ClassNotFoundException, SQLException, MissingPropertyException{
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getTeamPoint(?)}");
		stmt.setString(1, id);
		return this.getPoint(stmt.executeQuery());
	}
	
	public Integer updatePoint(final String id, final int point) throws ClassNotFoundException, SQLException, MissingPropertyException{
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL updateTeamPoint(?,?)}");
		stmt.setString(1, id);
		stmt.setInt(2, point);
		return this.getPoint(stmt.executeQuery());
	}
	
	public List<PointHistory> getPointHistory(final String id) throws ClassNotFoundException, SQLException, MissingPropertyException{
		List<PointHistory> history = new ArrayList<PointHistory>();
		
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getTeamPointHistory(?)}");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		
		while(rs.next()){

			PointHistory pointHistory = new PointHistory(rs.getInt("value"), rs.getDate("date"));
			history.add(pointHistory);
			
		}
		
		return history;
	}
	
}