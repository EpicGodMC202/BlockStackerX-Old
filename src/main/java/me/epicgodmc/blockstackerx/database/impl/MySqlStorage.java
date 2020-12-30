package me.epicgodmc.blockstackerx.database.impl;

import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.blockstackerx.StackerBlock;
import me.epicgodmc.blockstackerx.database.StackerStorage;
import me.epicgodmc.blockstackerx.database.model.StackerModel;
import me.epicgodmc.blockstackerx.database.model.StackerModelFactory;
import me.epicgodmc.blockstackerx.support.holograms.StackerHologram;
import me.epicgodmc.epicapi.sqlibrary.database.DatabaseConnection;
import me.epicgodmc.epicapi.sqlibrary.database.MySQLConnection;
import me.epicgodmc.epicapi.sqlibrary.model.utils.Condition;
import me.epicgodmc.epicapi.util.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MySqlStorage implements StackerStorage {

    //TODO fix error when stacker was replaced after autosave due to duplicate with different ID

    private DatabaseConnection connection;
    private BlockStackerX plugin;
    private String storageType;

    private static String address, name, username, password;
    private static int port;


    static
    {
        Object[] credentials = BlockStackerX.inst().getStackerSettings().getMysqlCredentials();

        address = (String) credentials[0];
        name = (String) credentials[1];
        username = (String) credentials[2];
        password = (String) credentials[3];
        port = (int) credentials[4];
    }

    public boolean connectSqLite(BlockStackerX plugin, String storageType)
    {
        File dbFile = new File(plugin.getDataFolder(), "database.db");


        //connection = new SQLiteConnection();
        return false;
    }

    public boolean connectMySQL(BlockStackerX plugin, String storageType) {
        connection = new MySQLConnection(address, port, name, username, password);
        try {
            connection.openConnection();
        } catch (SQLException | ClassNotFoundException e) {
            BlockStackerX.logger.log(Level.SEVERE, "Failed to connect to mysql database");
            return false;
        }
        new StackerModelFactory(connection);

        StackerModelFactory.getINSTANCE().buildSchema();
        this.plugin = plugin;
        this.storageType = storageType;

        return true;
    }

    @Override
    public DatabaseConnection getSqlConnection() {
        return connection;
    }

    @Override
    public void setStacker(StackerBlock stacker) {
        StackerModel model = createModel(stacker);
        if (!contains(stacker)) {
            if (!StackerModelFactory.getINSTANCE().insert(model)) {
                BlockStackerX.logger.log(Level.SEVERE, "Failed to insert model:\n" + model.toString());
                return;
            }
        }else{
            if (!StackerModelFactory.getINSTANCE().update(model))
            {
                BlockStackerX.logger.log(Level.SEVERE, "Failed to update model:\n" + model.toString());
                return;
            }
        }
    }

    @Override
    public void removeStacker(StackerBlock stacker) {
        StackerModel model = createModel(stacker);
        if (!StackerModelFactory.getINSTANCE().delete(model)) {
            BlockStackerX.logger.log(Level.SEVERE, "Failed to delete model:\n" + model.toString());
            return;
        }
    }

    @Override
    public boolean contains(StackerBlock stacker) {
        Condition condition = Condition.fieldEquals("stacker_id", stacker.getSqlID(),
                StackerModelFactory.getINSTANCE().getProfile());

        List<StackerModel> models = StackerModelFactory.getINSTANCE().select(condition);
        if (models == null) {
            BlockStackerX.logger.log(Level.SEVERE, "Failed to select model at:\n" + stacker.getLocation().toString());
            return false;
        }
        return models.size() > 0;
    }

    @Override
    public List<StackerModel> getStackers() {
        Condition condition = Condition.fieldEqualOrGreatorThan("stacker_id", 0,
                StackerModelFactory.getINSTANCE().getProfile());

        List<StackerModel> models = StackerModelFactory.getINSTANCE().select(condition);

        if (models == null) {
            BlockStackerX.logger.log(Level.SEVERE, "Failed to select all models");
            return new ArrayList<>();
        }

        return models;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void clearData() {
        Condition condition = Condition.fieldEqualOrGreatorThan("stacker_id", 0,
                StackerModelFactory.getINSTANCE().getProfile());

        if (!StackerModelFactory.getINSTANCE().delete(condition)) {
            BlockStackerX.logger.log(Level.SEVERE, "Failed to purge data");
            return;
        }
        BlockStackerX.logger.log(Level.INFO, "Successfully purged data");
    }

    @Override
    public void loadStackers() {
        HashMap<Location, StackerBlock> output = new HashMap<>();
        List<StackerModel> models = getStackers();

        Bukkit.getScheduler().runTaskLater(plugin, task ->
        {
            models.forEach(e ->
            {
                StackerHologram hologram = plugin.getDependencyManager().getNewHologram(plugin);
                float[] offset = plugin.getStackerSettings().getDisplayOffset(e.type);
                Location holoLocation = e.location.clone().add(offset[0], offset[1], offset[2]);
                hologram.create(e.type, e.value, holoLocation);

                output.put(e.location, new StackerBlock(BlockStackerX.inst(), e.stackerID, e.owner, e.type, hologram, new SimpleLocation(e.location), e.material, e.value));
            });
        },  1L);

        plugin.getStackerStore().setStackerStorage(output);
        BlockStackerX.logger.log(Level.INFO, String.format("Loaded %s stackers from %s database", models.size(), this.getStorageType()));
    }

    @Override
    public void saveStackers(Map<Location, StackerBlock> stackers, boolean onDisable) {
        try {
            this.connection.getDatabaseConnection().setAutoCommit(false);
            stackers.values().forEach(this::setStacker);
            this.connection.getDatabaseConnection().commit();
        } catch (SQLException e) {
            BlockStackerX.logger.log(Level.SEVERE, "Failed to save stackers to " + this.getStorageType() + "database");
        }
        BlockStackerX.logger.log(Level.INFO, String.format("Saved %s stackers to %s database", stackers.keySet().size(), this.getStorageType()));
    }

    @Override
    public String getStorageType() {
        return storageType;
    }

    private StackerModel createModel(StackerBlock block) {
        StackerModel model = StackerModelFactory.getINSTANCE().prepareBlankModel();
        model.owner = block.getOwner();
        model.stackerID = block.getSqlID();
        model.location = block.getLocation().toBukkitLoc();
        model.type = block.getType();
        model.material = block.getStackMaterial();
        model.value = block.getValue();

        return model;
    }
}
