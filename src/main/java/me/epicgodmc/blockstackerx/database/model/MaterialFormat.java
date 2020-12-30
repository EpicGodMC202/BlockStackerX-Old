package me.epicgodmc.blockstackerx.database.model;

import me.epicgodmc.epicapi.item.XMaterial;
import me.epicgodmc.epicapi.sqlibrary.model.utils.FieldFormat;
import org.bukkit.Material;

import java.util.Optional;

public class MaterialFormat extends FieldFormat<String, Material>
{

    public MaterialFormat()
    {
        super(String.class, Material.class);
    }


    @Override
    public Material toModel(String s) {
        if (s != null)
        {
            Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(s);
            if (xMaterial.isPresent())
            {
                return xMaterial.get().parseMaterial();
            }
        }
        return null;
    }

    @Override
    public String fromModel(Material material) {
        return material == null ? null : material.toString();
    }
}
