package me.epicgodmc.blockstackerx.database.sqlite;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.database.StackerStorage;
import me.epicgodmc.blockstackerx.database.model.StackerModel;
import me.epicgodmc.blockstackerx.support.holograms.StackerHologram;
import me.epicgodmc.epicapi.errors.SQLiteError;
import me.epicgodmc.epicapi.errors.SQLiteErrors;
import me.epicgodmc.epicapi.item.XMaterial;
import me.epicgodmc.epicapi.util.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public abstract class SQLiteDatabase implements StackerStorage {

    public final BlockStackerX plugin;
    public static Connection conn;
    private static final String table = "bsx_stackers";

    public SQLiteDatabase(BlockStackerX plugin) {
        this.plugin = plugin;
    }


    public abstract Connection getSQLConnection();

    public abstract boolean load();


    public void initialize() {
        establishConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE owner = ?");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);
        } catch (SQLException e) {
            BlockStackerX.logger.severe(SQLiteErrors.noSQLConnection());
        } finally {
            closeConn();
        }
    }

    @Override
    public void setStacker(StackerBlock stacker) {
        PreparedStatement ps = null;
        establishConnection();
        try {
            if (!contains(stacker)) {

                ps = conn.prepareStatement("INSERT INTO " + table + " (owner,type,location,material,value) VALUES (?,?,?,?,?)");
                ps.setString(1, stacker.getOwner().toString());
                ps.setString(2, stacker.getType());
                ps.setString(3, stacker.getLocation().toString());
                ps.setString(4, stacker.getStackMaterial().toString());
                ps.setInt(5, stacker.getValue());

                ps.executeUpdate();
            } else {
                ps = conn.prepareStatement("UPDATE " + table + " SET material = ?, value = ? WHERE owner = ? and location = ?");
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
    public void removeStacker(StackerBlock stackerBlock) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            establishConnection();
            PreparedStatement ps = null;

            try {
                ps = conn.prepareStatement("delete from " + table + " where owner = ? and location = ?");
                ps.setString(1, stackerBlock.getOwner().toString());
                ps.setString(2, stackerBlock.getLocation().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ps != null) ps.close();
                    closeConn();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean contains(StackerBlock stacker) {
        establishConnection();
        String query = "select * from " + table + " where owner = ? and location = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {

            statement = conn.prepareStatement(query);
            statement.setString(1, stacker.getOwner().toString());
            statement.setString(2, stacker.getLocation().toString());

            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement, resultSet);
        }
        return false;
    }

    @Override
    public List<StackerModel> getStackers() {
        Map<Location, StackerBlock> map = new HashMap<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
            establishConnection();
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


                    Bukkit.getScheduler().runTask(plugin, syncedtask -> {
                        float[] offset = plugin.getStackerSettings().getDisplayOffset(type);
                        Location hologramLocation = location.toBukkitLoc().add(offset[0], offset[1], offset[2]);
                        StackerHologram hologram = plugin.getDependencyManager().getNewHologram(plugin);
                        hologram.create(type, value, hologramLocation);
                        map.put(location.toBukkitLoc(), new StackerBlock(plugin, 0, owner, type, hologram, location, xMaterial.parseMaterial(), value));
                    });
                }
                BlockStackerX.logger.info("Found a total of " + count + " stackers in sqlite database");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ps != null) ps.close();
                    closeConn();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        return new ArrayList<>();
    }

    @Override
    public void clearData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
            establishConnection();
            PreparedStatement ps = null;

            try {
                ps = conn.prepareStatement("TRUNCATE TABLE " + table);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void loadStackers() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
            establishConnection();
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

                    Bukkit.getScheduler().runTask(plugin, syncedtask -> {
                        float[] offset = plugin.getStackerSettings().getDisplayOffset(type);
                        Location hologramLocation = location.toBukkitLoc().add(offset[0], offset[1], offset[2]);
                        StackerHologram hologram = plugin.getDependencyManager().getNewHologram(plugin);
                        hologram.create(type, value, hologramLocation);
                        plugin.getStackerStore().setStack(location.toBukkitLoc(), new StackerBlock(plugin, 0, owner, type, hologram, location, xMaterial.parseMaterial(), value));
                    });
                }
                BlockStackerX.logger.info("BlockStackerX: Loaded a total of " + count + " stackers from sqlite database");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ps != null) ps.close();
                    closeConn();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void saveStackers(Map<Location, StackerBlock> stackers, boolean onDisable) {
        if (!onDisable) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> saveStackers(stackers));
        } else saveStackers(stackers);
    }

    private void saveStackers(Map<Location, StackerBlock> stackers) {
        establishConnection();
        try {
            conn.setAutoCommit(false);
            stackers.forEach((k, v) -> setStacker(v));
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn();
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

    public void establishConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = getSQLConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void closeConn() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
