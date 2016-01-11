package uk.tldcode.minecraft.potionmixer;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;

public class Achievements {

    static HashMap<String, Achievement> achievementHashMap = new HashMap<String, Achievement>();

    public static void addAchievement(String name, Achievement achievement) {
        achievementHashMap.put(name, achievement);
        achievement.registerStat();
    }

    public static Achievement[] getAchievements() {
        return achievementHashMap.values().toArray(new Achievement[achievementHashMap.size()]);
    }

    public static Achievement getAchievement(String name) {
        return achievementHashMap.get(name);
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting != null && event.crafting.getItem() == (Item.getItemFromBlock(PotionMixerMod.mixer))) {
            Achievement achievement = getAchievement("craft.potion.mixer");
            if (achievement != null)
                event.player.addStat(achievement, 1);
        }
    }


    @SubscribeEvent
    public void onPlayerActivateBlock(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.world.getTileEntity(event.pos) instanceof TileEntityBrewingStand) {
            TileEntityBrewingStand tileEntityBrewingStand = ((TileEntityBrewingStand) event.world.getTileEntity(event.pos));
            Achievement achievement = null;
            for (int i = 0; i < 3; i++) {
                if (tileEntityBrewingStand.getStackInSlot(i) != null && tileEntityBrewingStand.getStackInSlot(i).getItem() == Items.potionitem) {
                    if (tileEntityBrewingStand.getStackInSlot(i).getMetadata() == 2 && tileEntityBrewingStand.getStackInSlot(i).getTagCompound().hasKey("CustomPotionEffects",9)) {
                        achievement = getAchievement("brew.uninteresting.potion");
                    } else if (tileEntityBrewingStand.getStackInSlot(i).getMetadata() == 16384 && tileEntityBrewingStand.getStackInSlot(i).getTagCompound().hasKey("CustomPotionEffects", 9)) {
                        achievement = getAchievement("brew.splash.mixed.potion");
                    }
                }
                if (achievement != null)
                    event.entityPlayer.addStat(achievement, 1);
            }
        }
    }
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if(eventArgs.modID.equals(PotionMixerMod.MODID))
            PotionMixerMod.syncConfig();
    }
}
