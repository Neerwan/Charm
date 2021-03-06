package svenhjol.charm.enchanting.feature;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import svenhjol.charm.enchanting.enchantment.EnchantmentMagnetic;
import svenhjol.meson.Feature;
import svenhjol.meson.helper.EnchantmentHelper;
import svenhjol.meson.helper.PlayerHelper;

import java.util.ArrayList;
import java.util.List;

public class Magnetic extends Feature
{
    public static EnchantmentMagnetic enchantment;
    public static int minEnchantability;
    public static PlayerInteractionManager manager;
    public static List<EntityItem> drops = new ArrayList<>();

    @Override
    public String getDescription()
    {
        return "Tools with the Magnetic enchantment automatically add dropped items to your inventory.";
    }

    @Override
    public void setupConfig()
    {
        minEnchantability = 15;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        enchantment = new EnchantmentMagnetic();
    }

    @SubscribeEvent
    public void onEntityCreated(EntityJoinWorldEvent event)
    {
        if (!event.getWorld().isRemote
            && event.getEntity() instanceof EntityItem
            && !event.isCanceled()
            && manager != null
        ) {
            EntityItem item = (EntityItem)event.getEntity();
            drops.add(item);
            event.setCanceled(true);
        }
    }

    public static void startCollectingDrops(PlayerInteractionManager manager)
    {
        if (EnchantmentHelper.hasEnchantment(enchantment, manager.player.getHeldItemMainhand())) {
            Magnetic.manager = manager;
        }
    }

    public static void stopCollectingDrops()
    {
        if (manager != null && !drops.isEmpty()) {

            ArrayList<EntityItem> copy = new ArrayList<>(drops);
            drops.clear();

            EntityPlayerMP player = manager.player;
            for (EntityItem drop : copy) {
                EntityItem fake = new EntityItem(player.world, player.posX, player.posY, player.posZ);
                fake.setItem(drop.getItem());
                if (!MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(player, fake))) {
                    PlayerHelper.addOrDropStack(player, drop.getItem());
                }
            }

            manager = null;
        }
    }

    @Override
    public boolean hasSubscriptions()
    {
        return true;
    }
}
