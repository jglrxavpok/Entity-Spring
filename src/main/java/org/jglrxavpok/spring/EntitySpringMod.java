package org.jglrxavpok.spring;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jglrxavpok.spring.common.ESProxyCommon;
import org.jglrxavpok.spring.common.SpringItem;

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
    }

}
