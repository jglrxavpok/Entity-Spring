package org.jglrxavpok.spring;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jglrxavpok.spring.common.EventSink;
import org.jglrxavpok.spring.common.SpringEntity;
import org.jglrxavpok.spring.common.SpringItem;
import org.jglrxavpok.spring.common.SpringCutterItem;

import java.io.*;

@Mod(value = EntitySpringMod.MODID)
public class EntitySpringMod {

    public static final String MODID = "entityspring";

    public static EntityType<SpringEntity> SpringType;
    public static SpringItem SPRING = new SpringItem();
    public static SpringCutterItem CUTTER = new SpringCutterItem();

    public static final Logger LOGGER = LogManager.getLogger();

    public EntitySpringMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().register(EventSink.Registration.class);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(EventSink.class);
    }

    private void setup(final FMLCommonSetupEvent event) {
        try {
            loadEntityBlacklist(new File("./config/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadEntityBlacklist(File configFolder) throws IOException {
        File blacklist = new File(configFolder, "entity_spring_blacklist.txt");
        if(!blacklist.getParentFile().exists()) {
            blacklist.getParentFile().mkdirs();
        }
        if(!blacklist.exists()) {
            LOGGER.info("Blacklist file does not exist, creating one at {}", blacklist.getCanonicalPath());
            if(blacklist.createNewFile()) {
                try(FileWriter writer = new FileWriter(blacklist)) {
                    writer.write("#Remove entities at your own risk!\n");
                    for(ResourceLocation entity : EntitySpringAPI.defaultBlacklistedEntities) {
                        writer.write(entity.toString());
                        writer.write("\n");
                    }
                    writer.flush();
                }
            } else {
                LOGGER.error("Failed to create blacklist file. OS returned an error");
            }
        }

        LOGGER.info("Loading blacklist...");
        try(BufferedReader reader = new BufferedReader(new FileReader(blacklist))) {
            reader.lines().forEach(line -> {
                if(line.startsWith("#"))
                    return;
                ResourceLocation correspondingEntityName = new ResourceLocation(line);
                EntitySpringAPI.blacklist(correspondingEntityName);
            });
        }
    }

}
