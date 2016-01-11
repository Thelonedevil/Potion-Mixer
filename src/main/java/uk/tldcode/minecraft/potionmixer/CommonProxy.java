package uk.tldcode.minecraft.potionmixer;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void say(Object object) {

    }

    public void init(){
        GameRegistry.registerTileEntity(TilePotionMixer.class, "potion_mixer_cauldron");
    }
}
