package me.epicgodmc.blockstackerx.database.model;

import lombok.ToString;
import me.epicgodmc.epicapi.sqlibrary.database.utils.Result;
import me.epicgodmc.epicapi.sqlibrary.model.Model;
import me.epicgodmc.epicapi.sqlibrary.model.ModelFactory;
import me.epicgodmc.epicapi.sqlibrary.model.annotation.Fillable;
import me.epicgodmc.epicapi.sqlibrary.model.annotation.PrimaryKey;
import me.epicgodmc.epicapi.sqlibrary.model.annotation.Schema;
import me.epicgodmc.epicapi.sqlibrary.model.annotation.UniqueKey;
import me.epicgodmc.epicapi.sqlibrary.model.common.UUIDFormat;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

@ToString
public class StackerModel extends Model {

    @PrimaryKey(name = "pk_stacker")
    @Fillable(column = "stacker_id", sortingIndex = 1)
    @Schema(attributes = "INT NOT NULL AUTO_INCREMENT")
    public Integer stackerID;

    @UniqueKey(name = "uk_location")
    @Fillable(column = "location", sortingIndex = 2, formatField = LocationFormat.class)
    @Schema(attributes = "VARCHAR(62) NOT NULL")
    public Location location;

    @Fillable(column = "owner", sortingIndex = 3, formatField = UUIDFormat.class)
    @Schema(attributes = "VARCHAR(50) NOT NULL")
    public UUID owner;

    @Fillable(column = "type", sortingIndex = 4)
    @Schema(attributes = "TEXT(20) NOT NULL")
    public String type;

    @Fillable(column = "material", sortingIndex = 5, formatField = MaterialFormat.class)
    @Schema(attributes = "TEXT(20) NOT NULL")
    public Material material;

    @Fillable(column = "value", sortingIndex = 6)
    @Schema(attributes = "INT(10) NOT NULL")
    public int value;


    public StackerModel(ModelFactory<Model> model, Result result)
            throws
            IllegalArgumentException,
            InstantiationException,
            IllegalAccessException
    {
        super(model, result);
    }


}
