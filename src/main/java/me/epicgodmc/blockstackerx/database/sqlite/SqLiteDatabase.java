package me.epicgodmc.blockstackerx.database.sqlite;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.database.StackerStorage;
import me.epicgodmc.blockstackerx.support.holograms.StackerHologram;
import me.epicgodmc.epicframework.database.SQLiteError;
import me.epicgodmc.epicframework.database.SQLiteErrors;
import me.epicgodmc.epicframework.item.XMaterial;
import me.epicgodmc.epicframework.util.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public abstract class SqLiteDatabase implements StackerStorage {

    BlockStackerX plugin;
    Connection connection;

    public String table = "bsx_stackers";


    public SqLiteDatabase(BlockStackerX plugin) {
        this.plugin = plugin;
    }

    public abstract Connection getSQLConnection();

    public abstract boolean load();

    public void initialize() {
        connection = getSQLConnection();

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table + " WHERE owner = ?");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);
        } catch (SQLException e) {
            BlockStackerX.logger.severe(SQLiteErrors.noSQLConnection());
        }
    }

    public boolean bulkContains(Connection conn, StackerBlock stacker) {
        String query = "select * from "+table+" where owner = ? and location = ?";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            conn = getSQLConnection();
            statement = conn.prepareStatement(query);
            statement.setString(1, stacker.getOwner().toString());
            statement.setString(2, stacker.getLocation().toString());

            resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
            else return false;
        }catch (SQLException e)
        {
            e.printStackTrace();
        }finally {
            try{
                if (statement != null) statement.close();
                if (resultSet != null) resultSet.close();
            }catch (SQLException e)
            {
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    public boolean contains(StackerBlock stacker) {
        String query = "select * from "+table+" where owner = ? and location = ?";

        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try{
            conn = getSQLConnection();
            statement = conn.prepareStatement(query);
            statement.setString(1, stacker.getOwner().toString());
            statement.setString(2, stacker.getLocation().toString());

            resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
            else return false;
        }catch (SQLException e)
        {
            e.printStackTrace();
        }finally {
            try{
                if (statement != null) statement.close();
                if (resultSet != null) resultSet.close();
                if (conn != null) conn.close();
            }catch (SQLException e)
            {
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    public StackerBlock getStacker(UUID owner) {
        return null;
    }

    @Override
    public void removeStacker(StackerBlock stackerBlock)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            Connection conn = null;
            PreparedStatement ps = null;

            try{
                conn = getSQLConnection();
                ps = connection.prepareStatement("delete from "+table+" where owner = ? and location = ?");
                ps.setString(1, stackerBlock.getOwner().toString());
                ps.setString(2, stackerBlock.getLocation().toString());
                ps.executeUpdate();
            }catch (SQLException e)
            {
                e.printStackTrace();
            }finally {
                try{
                    if (conn != null) conn.close();
                    if (ps != null) ps.close();
                }catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public void setStacker(StackerBlock stacker) {
        PreparedStatement ps = null;



        try {
            if (!bulkContains(connection, stacker)) {

                ps = connection.prepareStatement("INSERT INTO " + table + " (owner,type,location,material,value) VALUES (?,?,?,?,?)");
                ps.setString(1, stacker.getOwner().toString());
                ps.setString(2, stacker.getType());
                ps.setString(3, stacker.getLocation().toString());
                ps.setString(4, stacker.getStackMaterial().toString());
                ps.setInt(5, stacker.getValue());

                ps.executeUpdate();
            }else{
                ps = connection.prepareStatement("UPDATE "+table+" SET material = ?, value = ? WHERE owner = ? and location = ?");
                ps.setString(1, stacker.getStackMaterial().toString());
                ps.setInt(2, stacker.getValue());
                ps.setString(3, stacker.getOwner().toString());
                ps.setString(4, stacker.getLocation().toString());

                ps.executeUpdate();
            }
        } catch (SQLException e) {
            BlockStackerX.logger.log(Level.SEVERE, SQLiteErrors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                BlockStackerX.logger.log(Level.SEVERE, SQLiteErrors.sqlConnectionExecute(), e);
            }
        }
    }

    @Override
    public void loadStackers() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
            Connection conn = null;
            PreparedStatement ps = null;
            int count = 0;

            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement("SELECT * FROM " + table);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    count++;
                    UUID owner = UUID.fromString(rs.getString("owner"));
                    String type = rs.getString("type");
                    SimpleLocation location = new SimpleLocation(rs.getString("location"));
                    XMaterial xMaterial = XMaterial.valueOf(rs.getString("material").toUpperCase());
                    int value = rs.getInt("value");

                    Bukkit.getScheduler().runTask(plugin, synctask -> {
                        float[] offset = plugin.getSettings().getDisplayOffset(type);
                        Location hologramLocation = location.toBukkitLoc().add(offset[0], offset[1], offset[2]);
                        StackerHologram hologram = plugin.getDependencyManager().getNewHologram(plugin);
                        hologram.create(type, value, hologramLocation);
                        plugin.getStackerStore().setStack(location.toBukkitLoc(), new StackerBlock(plugin, owner, type, hologram, location, xMaterial.parseMaterial(), value));
                    });
                }
                BlockStackerX.logger.info("BlockStackerX: Loaded a total of "+count+" stackers from sqlite database");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ps != null) ps.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public void saveStackers(Map<Location, StackerBlock> stackers) {
        connection = getSQLConnection();

        try {
            connection.setAutoCommit(false);
            stackers.forEach((k, v) -> {
                setStacker(v);
            });
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }


    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            SQLiteError.close(ex);
        }
    }
}
