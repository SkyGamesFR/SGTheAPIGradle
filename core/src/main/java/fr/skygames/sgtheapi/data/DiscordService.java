package fr.skygames.sgtheapi.data;

import fr.skygames.sgtheapi.SkyGamesTheApp;
import fr.skygames.sgtheapi.api.data.Discord;
import fr.skygames.sgtheapi.sql.SqlConnector;
import fr.skygames.sgtheapi.utils.MissingPropertyException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DiscordService {

    private final SqlConnector connector;

    public DiscordService(SqlConnector connector) {
        this.connector = connector;
    }

    public static Discord getDiscordFromResultSet(ResultSet rs) throws SQLException {
        return new Discord(rs.getString("uuid"), rs.getString("token"), rs.getString("discord_id"));
    }

    public Discord get(final ResultSet rs) throws SQLException {
        if(rs.first()) {
            return getDiscordFromResultSet(rs);
        }else {
            return null;
        }
    }

    public Discord getTokenFromUUID(final String uuid) throws SQLException, ClassNotFoundException, MissingPropertyException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getTokenFromUUID(?)}");
        stmt.setString(1, uuid);
        return get(stmt.executeQuery());
    }

    public Discord getToken(final String token) throws SQLException, ClassNotFoundException, MissingPropertyException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getToken(?)}");
        stmt.setString(1, token);
        return get(stmt.executeQuery());
    }

    public void create(final Discord discord) throws SQLException, MissingPropertyException, ClassNotFoundException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL addToken(?,?)}");
        stmt.setString(1, discord.getUuid());
        stmt.setString(2, discord.getToken());
        stmt.executeUpdate();
    }

    public void delete(final String uuid) throws SQLException, MissingPropertyException, ClassNotFoundException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL deleteToken(?)}");
        stmt.setString(1, uuid);
        stmt.executeUpdate();
    }

    public Discord setIDFromToken(final String token, final String id) throws SQLException, MissingPropertyException, ClassNotFoundException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL setIDFromToken(?,?)}");
        stmt.setString(1, token);
        stmt.setString(2, id);
        return get(stmt.executeQuery());
    }

    public Discord getIDFromUUID(String uuid) throws SQLException, ClassNotFoundException, MissingPropertyException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getIDFromUUID(?)}");
        stmt.setString(1, uuid);
        return get(stmt.executeQuery());
    }
}
