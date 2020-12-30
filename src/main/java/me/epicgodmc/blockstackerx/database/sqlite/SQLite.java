package me.epicgodmc.blockstackerx.database.sqlite;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.epicapi.sqlibrary.database.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends SQLiteDatabase
{

    private final String dbName = "database";

    public SQLite(BlockStackerX plugin) {
        super(plugin);
    }
    public String SQLiteCreateDataTable = "CREATE TABLE IF NOT EXISTS bsx_stackers (" +
            "`owner` varchar(36) NOT NULL," +
            "`type` varchar(32) NOT NULL," +
            "`location` varchar(32) NOT NULL," +
            "`material` text(20) NOT NULL," +
            "`value` int(10) NOT NULL" +
            ");";


    @Override
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbName+".db");

        if (!dataFolder.exists())
        {
            try{
                dataFolder.createNewFile();
            }catch (IOException e)
            {
                plugin.getLogger().log(Level.SEVERE, "File Write error: "+dbName+".db");
            }
        }
        try{
            if (conn!=null&&!conn.isClosed())
            {
                return conn;
            }
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return conn;
        }catch (SQLException ex)
        {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        }catch (ClassNotFoundException ex)
        {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    @Override
    public boolean load() {
        conn = getSQLConnection();
        try{
            Statement s = conn.createStatement();
            s.executeUpdate(SQLiteCreateDataTable);
            s.close();
        }catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        initialize();
        return true;
    }

    @Override
    public DatabaseConnection getSqlConnection() {
        return null;
    }

    @Override
    public void disconnect() {
        try{
            if (!this.getSQLConnection().isClosed())
            {
                this.getSQLConnection().close();
            }
        }catch (Exception e)
        {

        }

    }

    @Override
    public String getStorageType() {
        return "sqlite";
    }
}
