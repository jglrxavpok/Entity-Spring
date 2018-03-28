package org.jglrxavpok.spring;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jglrxavpok.spring.common.ESProxyCommon;
import org.jglrxavpok.spring.common.SpringItem;

import java.io.*;

@Mod(modid = EntitySpringMod.MODID, name = EntitySpringMod.NAME, version = EntitySpringMod.VERSION)
public class EntitySpringMod
{
    public static final String MODID = "entityspring";
    public static final String NAME = "Entity Spring";
    public static final String VERSION = "1.0.1";
    @Mod.Instance(MODID)
    public static EntitySpringMod instance;
    public SpringItem springItemInstance = new SpringItem();

    @SidedProxy(clientSide = "org.jglrxavpok.spring.client.ESProxyClient", serverSide = "org.jglrxavpok.spring.common.ESProxyCommon")
    public static ESProxyCommon proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        try {
            loadEntityBlacklist(event.getSuggestedConfigurationFile().getParentFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEntityBlacklist(File configFolder) throws IOException {
        File whitelist = new File(configFolder, "entity_spring_blacklist.txt");
        if(!whitelist.getParentFile().exists()) {
            whitelist.getParentFile().mkdirs();
        }
        if(!whitelist.exists()) {
            whitelist.createNewFile();
            try(FileWriter writer = new FileWriter(whitelist)) {
                for(ResourceLocation entity : EntitySpringAPI.defaultBlacklistedEntities) {
                    writer.write(entity.toString());
                    writer.write("\n");
                }
                writer.flush();
            }
        }

        try(BufferedReader reader = new BufferedReader(new FileReader(whitelist))) {
            reader.lines().forEach(line -> {
                ResourceLocation correspondingEntityName = new ResourceLocation(line);
                EntitySpringAPI.blacklist(correspondingEntityName);
            });
        }
    }

}
