package uk.tldcode.minecraft.potionmixer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;

public class ClientProxy extends CommonProxy {

    public void say(Object object){
        if(PotionMixerMod.debug){
            Minecraft.getMinecraft().thePlayer.sendChatMessage(object.toString());
        }
    }
}
