package me.epicgodmc.blockstackerx.database.model;

import lombok.Getter;
import me.epicgodmc.blockstackerx.BlockStackerX;
import me.epicgodmc.epicapi.sqlibrary.database.DatabaseConnection;
import me.epicgodmc.epicapi.sqlibrary.model.ModelFactory;

import java.util.logging.Level;

public class StackerModelFactory extends ModelFactory<StackerModel>
{

    @Getter
    private static StackerModelFactory INSTANCE;

    private final DatabaseConnection connection;

    public StackerModelFactory(DatabaseConnection connection)
    {
        super(StackerModel.class);
        this.connection = connection;
        StackerModelFactory.INSTANCE = this;
    }

    @Override
    public String getTable() {
        return "bsx_stackers";
    }

    @Override
    public DatabaseConnection getDatabase() {
        return connection;
    }

    @Override
    public void log(Level level, String s, Throwable throwable) {
        BlockStackerX.logger.log(level, s, throwable);
    }
}
