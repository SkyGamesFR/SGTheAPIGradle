package fr.skygames.sgtheapi.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.skygames.sgtheapi.api.data.Player;
import fr.skygames.sgtheapi.sql.SqlConnector;
import fr.skygames.sgtheapi.utils.MissingPropertyException;


public class PlayerService {

	private SqlConnector connector;
	
    public PlayerService(SqlConnector connector) {
		this.connector = connector;
	}

    public List<Player> getAll() throws Exception {
		List<Player> players = new ArrayList<Player>();

		Statement stmt = this.connector.getConnection().createStatement();
		ResultSet rs = stmt.executeQuery("{CALL getAllPlayers()}");
		while(rs.next()){

			Player player = PlayerService.getPlayerFromResultSet(rs);
			players.add(player);
			
		}

		return players;
	}

    private Player get(final ResultSet rs) throws SQLException {
    	if(rs.first()) {
			return PlayerService.getPlayerFromResultSet(rs);
		}else {
			return null;
		}
    }
    
	public Player get(final String uuid) throws SQLException, ClassNotFoundException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getPlayer(?)}");
		stmt.setString(1, uuid);
		return get(stmt.executeQuery());
	}
	
	public final static Player getPlayerFromResultSet(ResultSet rs) throws SQLException {
		return new Player(rs.getString("uuid"),rs.getString("name"),rs.getDate("first_login"),rs.getDate("last_login"),rs.getString("team"));
	}
	
	public int delete(final String uuid) throws ClassNotFoundException, SQLException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL deletePlayer(?)}");
		stmt.setString(1, uuid);
		return stmt.executeUpdate();
	}
	
	public Player add(final String uuid, final String name) throws ClassNotFoundException, SQLException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL addPlayer(?,?)}");
		stmt.setString(1, uuid);
		stmt.setString(2, name);
		return get(stmt.executeQuery());
	}
	
	public Player update(final String uuid, final String name, final Date date) throws ClassNotFoundException, SQLException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL updatePlayer(?,?,?)}");
		stmt.setString(1, uuid);
		stmt.setString(2, name);
		stmt.setDate(3, date);
		return get(stmt.executeQuery());
	}

	private String getTeam(ResultSet rs) throws SQLException {
		if(rs.first()) {
			return rs.getString("team");
		}else {
			return null;
		}
	}
	
	public String getTeam(final String uuid) throws ClassNotFoundException, SQLException, MissingPropertyException{
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getPlayerTeam(?)}");
		stmt.setString(1, uuid);
		return this.getTeam(stmt.executeQuery());
	}
	
	public String updateTeam(final String uuid, final String team) throws SQLException, ClassNotFoundException, MissingPropertyException {
		PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL updatePlayerTeam(?,?)}");
		stmt.setString(1, uuid);
		stmt.setString(2, team);
		return this.getTeam(stmt.executeQuery());
	}
    
}