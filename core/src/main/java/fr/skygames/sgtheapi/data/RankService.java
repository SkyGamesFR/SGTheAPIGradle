package fr.skygames.sgtheapi.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.skygames.sgtheapi.api.data.Player;
import fr.skygames.sgtheapi.api.data.Rank;
import fr.skygames.sgtheapi.sql.SqlConnector;
import fr.skygames.sgtheapi.utils.MissingPropertyException;


public class RankService {

    private SqlConnector connector;

    public RankService(SqlConnector connector) {
        this.connector = connector;
    }

    public List<Rank> getAll() throws Exception {
        List<Rank> ranks = new ArrayList<Rank>();

        Statement stmt = this.connector.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("{CALL getAllRanks()}");
        while(rs.next()){

            Rank rank = RankService.getRankFromResultSet(rs);
            ranks.add(rank);

        }

        return ranks;
    }

    private Rank get(final ResultSet rs) throws SQLException {
        if(rs.first()) {
            return RankService.getRankFromResultSet(rs);
        }else {
            return null;
        }
    }

    public Rank get(final String name) throws SQLException, ClassNotFoundException, MissingPropertyException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL getRank(?)}");
        stmt.setString(1, name);
        return get(stmt.executeQuery());
    }

    public static Rank getRankFromResultSet(ResultSet rs) throws SQLException {
        return new Rank(rs.getString("name"), rs.getString("prefix"), rs.getString("suffix"), rs.getString("color"), rs.getInt("priority"));
    }

    public int delete(final String name) throws ClassNotFoundException, SQLException, MissingPropertyException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL deleteRank(?)}");
        stmt.setString(1, name);
        return stmt.executeUpdate();
    }

    public Rank add(final String name, final String prefix, String suffix, String color, int priority) throws ClassNotFoundException, SQLException, MissingPropertyException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL addRank(?,?,?,?,?)}");
        stmt.setString(1, name);
        stmt.setString(2, prefix);
        stmt.setString(3, suffix);
        stmt.setString(4, color);
        stmt.setInt(5, priority);
        return get(stmt.executeQuery());
    }

    public Rank update(final String name, final String prefix, String suffix, String color, int priority) throws ClassNotFoundException, SQLException, MissingPropertyException {
        PreparedStatement stmt = this.connector.getConnection().prepareStatement("{CALL updateRank(?,?,?,?,?)}");
        stmt.setString(1, name);
        stmt.setString(2, prefix);
        stmt.setString(3, suffix);
        stmt.setString(4, color);
        stmt.setInt(5, priority);
        return get(stmt.executeQuery());
    }
}