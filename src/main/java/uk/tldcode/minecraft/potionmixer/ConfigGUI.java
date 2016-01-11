package uk.tldcode.minecraft.potionmixer;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ConfigGUI extends GuiConfig {
    public ConfigGUI(GuiScreen parent) {
        super(parent,
                new ConfigElement(PotionMixerMod.configFile.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                PotionMixerMod.MODID, false, false, GuiConfig.getAbridgedConfigPath(PotionMixerMod.configFile.toString()));
    }
}
