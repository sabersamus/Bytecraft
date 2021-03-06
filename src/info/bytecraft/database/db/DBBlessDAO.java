package info.bytecraft.database.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.google.common.collect.Maps;

import info.bytecraft.api.BytecraftPlayer;
import info.bytecraft.database.DAOException;
import info.bytecraft.database.IBlessDAO;

public class DBBlessDAO implements IBlessDAO
{
    private Connection conn;

    public DBBlessDAO(Connection conn)
    {
        this.conn = conn;
    }
    
    public Map<Location, Integer> getBlessedBlocks()
    throws DAOException
    {
        Map<Location, Integer> blessedBlocks = Maps.newHashMap();
        
        String sql = "SELECT * FROM bless";
        try(PreparedStatement stm = conn.prepareStatement(sql)){
            stm.execute();
            
            try(ResultSet rs = stm.getResultSet()){
                while(rs.next()){
                    String world = rs.getString("world");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    int id = rs.getInt("player_id");
                    
                    blessedBlocks.put(new Location(Bukkit.getWorld(world), x, y, z), id);
                }
            }
            
        }catch(SQLException e){
            throw new DAOException(sql, e);
        }
        
        return blessedBlocks;
    }

    public boolean isBlessed(Block block)
    throws DAOException
    {
        String sql = "SELECT * FROM bless WHERE x = ? AND y = ? AND z = ? AND world = ?";
        try(PreparedStatement stm = conn.prepareStatement(sql)){
            stm.setInt(1, block.getX());
            stm.setInt(2, block.getY());
            stm.setInt(3, block.getZ());
            stm.setString(4, block.getWorld().getName());
            stm.execute();
            
            try(ResultSet rs = stm.getResultSet()){
                return rs.next();
            }
        }catch(SQLException e){
            throw new DAOException(sql, e);
        }
    }

    public boolean bless(Block block, BytecraftPlayer owner)
    throws DAOException
    {
        String sql = "INSERT INTO bless (player_id, x, y, z, world) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stm = conn.prepareStatement(sql)){
            stm.setInt(1, owner.getId());
            stm.setInt(2, block.getX());
            stm.setInt(3, block.getY());
            stm.setInt(4, block.getZ());
            stm.setString(5, block.getWorld().getName());
            stm.execute();
            return true;
        } catch (SQLException e) {
            throw new DAOException(sql, e);
        }
    }

    public int getOwner(Block block)
    throws DAOException
    {
        if (!isBlessed(block))
            return -1;
        
        String sql = "SELECT * FROM bless WHERE x = ? AND y = ? AND z = ? AND world = ?";
        try (PreparedStatement stm = conn.prepareStatement(sql)){
            stm.setInt(1, block.getX());
            stm.setInt(2, block.getY());
            stm.setInt(3, block.getZ());
            stm.setString(4, block.getWorld().getName());
            stm.execute();
            
            try(ResultSet rs = stm.getResultSet()){
                if (rs.next()) {
                    return rs.getInt("player_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    @Override
    public int getBlessId(Block block) throws DAOException
    {
        if(!isBlessed(block))return 0;
        
        String sql = "SELECT * FROM bless WHERE x = ? AND y = ? AND z = ? AND world = ?";
        try (PreparedStatement stm = conn.prepareStatement(sql)){
            stm.setInt(1, block.getX());
            stm.setInt(2, block.getY());
            stm.setInt(3, block.getZ());
            stm.setString(4, block.getWorld().getName());
            stm.execute();
            
            try(ResultSet rs = stm.getResultSet()){
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        return 0;
    }

    
}
