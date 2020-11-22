package me.epicgodmc.blockstackerx.database;

import com.zaxxer.hikari.HikariDataSource;
import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.support.holograms.StackerHologram;
import me.epicgodmc.epicframework.database.SQLiteError;
import me.epicgodmc.epicframework.item.XMaterial;
import me.epicgodmc.epicframework.util.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class MySqlStorage implements StackerStorage {

    private final BlockStackerX plugin;
    private HikariDataSource hikari;
    private Connection connection;
    private final String table = "bsx_stackers";

    public String MySqlCreateDataTable = "CREATE TABLE IF NOT EXISTS bsx_stackers (" +
            "`owner` varchar(36) NOT NULL," +
            "`type` varchar(32) NOT NULL," +
            "`location` varchar(62) NOT NULL," +
            "`material` text(20) NOT NULL," +
            "`value` int(10) NOT NULL," +
            "primary key (location)" +
            ");";

    public MySqlStorage(BlockStackerX plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setStacker(StackerBlock stacker) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("INSERT INTO " + table + " (owner,type,location,material,value) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE material = ?, value = ?");

            ps.setString(1, stacker.getOwner().toString());
            ps.setString(2, stacker.getType());
            ps.setString(3, stacker.getLocation().toString());
            ps.setString(4, stacker.getStackMaterial().toString());
            ps.setInt(5, stacker.getValue());
            ps.setString(6, stacker.getStackMaterial().toString());
            ps.setInt(7, stacker.getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeStacker(StackerBlock stackerBlock) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, bukkitTask -> {
            PreparedStatement ps = null;

            try {
                ps = connection.prepareStatement("delete from " + table + " where location = ?");
                ps.setString(1, stackerBlock.getLocation().toString());
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
        String query = "select * from " + table + " where location = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {

            statement = connection.prepareStatement(query);
            statement.setString(1, stacker.getLocation().toString());

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
    public void loadStackers() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task -> {
            PreparedStatement ps = null;
            int count = 0;

            try {
                connection = hikari.getConnection();
                ps = connection.prepareStatement("SELECT * FROM " + table);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    count++;
                    UUID owner = UUID.fromString(rs.getString("owner"));
                    String type = rs.getString("type");
                    SimpleLocation location = new SimpleLocation(rs.getString("location"));
                    XMaterial xMaterial = XMaterial.valueOf(rs.getString("material").toUpperCase());
                    int value = rs.getInt("value");

                    Bukkit.getScheduler().runTask(plugin, syncedtask -> {
                        float[] offset = plugin.getSettings().getDisplayOffset(type);
                        Location hologramLocation = location.toBukkitLoc().add(offset[0], offset[1], offset[2]);
                        StackerHologram hologram = plugin.getDependencyManager().getNewHologram(plugin);
                        hologram.create(type, value, hologramLocation);
                        plugin.getStackerStore().setStack(location.toBukkitLoc(), new StackerBlock(plugin, owner, type, hologram, location, xMaterial.parseMaterial(), value));
                    });
                }
                BlockStackerX.logger.info("BlockStackerX: Loaded a total of " + count + " stackers from mysql database");
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
        try {
            connection = hikari.getConnection();
            connection.setAutoCommit(false);
            stackers.forEach((k, v) -> setStacker(v));
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn();
        }
    }

    public boolean connect() {
        boolean connected = false;
        try {
            FileConfiguration cfg = plugin.getConfig();

            String address = cfg.getString("storage.mysql.address");
            String name = cfg.getString("storage.mysql.name");
            String username = cfg.getString("storage.mysql.username");
            String password = cfg.getString("storage.mysql.password");
            int port = cfg.getInt("storage.mysql.port");

            hikari = new HikariDataSource();
            hikari.setMaximumPoolSize(10);
            hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            hikari.addDataSourceProperty("serverName", address);
            hikari.addDataSourceProperty("port", port);
            hikari.addDataSourceProperty("databaseName", name);
            hikari.addDataSourceProperty("user", username);
            hikari.addDataSourceProperty("password", password);
            connected = true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (connected) initialize();
            } catch (SQLException e) {
                return false;
            }

        }
        return true;
    }

    private boolean initialize() throws SQLException {
        if (hikari == null || hikari.getConnection().isClosed()) return false;

        PreparedStatement statement = null;
        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement(MySqlCreateDataTable);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConn();
        }
        return true;
    }

    private void closeConn() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
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
