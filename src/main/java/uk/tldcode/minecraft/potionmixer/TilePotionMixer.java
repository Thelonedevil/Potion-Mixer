package uk.tldcode.minecraft.potionmixer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;

import java.util.*;

public class TilePotionMixer extends TileEntity implements ITickable {

    //PotionID -> PotionEffect
    HashMap<Integer, PotionEffect> potions = new HashMap<Integer, PotionEffect>();
    boolean stable = false;

    public TilePotionMixer() {
        super();
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("CustomPotionEffects", 9)) {
            NBTTagList nbtTagList = compound.getTagList("CustomPotionEffects", 10);
            for (int i = 0; i < nbtTagList.tagCount(); i++) {
                NBTTagCompound effectCompound = nbtTagList.getCompoundTagAt(i);
                PotionEffect effect = PotionEffect.readCustomPotionEffectFromNBT(effectCompound);
                if (effect != null)
                    addEffect(effect);
            }
        }

    }


    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList nbtTagList = new NBTTagList();
        for (PotionEffect effect : getEffects()) {
            NBTTagCompound effectCompound = new NBTTagCompound();
            effectCompound = effect.writeCustomPotionEffectToNBT(effectCompound);
            nbtTagList.appendTag(effectCompound);
        }
        compound.setTag("CustomPotionEffects", nbtTagList);

    }

    @Override
    public void update() {
        if (!potions.keySet().isEmpty()) {
            Random rand = new Random();
            switch (rand.nextInt(12)) {
                case 1:
                    switch (potions.keySet().size()) {
                        default:
                            corrupt(rand);
                        case 10:
                            if (rand.nextInt(1000) < 99) {
                                corrupt(rand);
                            }
                        case 9:
                            if (rand.nextInt(1000) < 83) {
                                corrupt(rand);
                            }
                        case 8:
                            if (rand.nextInt(1000) < 54) {
                                corrupt(rand);
                            }
                        case 7:
                        case 6:
                            if (rand.nextInt(1000) < 21) {
                                corrupt(rand);
                            }

                        case 5:
                        case 4:
                        case 3:
                            if (rand.nextInt(1000) < 1) {
                                corrupt(rand);
                            }
                        case 2:
                        case 1:
                            break;
                    }
                    break;
                case 2:
                    int i = 0;
                    for (int id : new HashSet<Integer>(potions.keySet())) {
                        if (potions.get(id).getAmplifier() > 4 && potions.get(id).getAmplifier() < 128 && rand.nextInt(1000) < 1) {
                            i++;
                        }
                    }
                    while (i-- > 0)
                        corrupt(rand);
                    break;
                case 3:
                    i = 0;
                    for (int id : new HashSet<Integer>(potions.keySet())) {
                        if (potions.get(id).getDuration() > 600 && rand.nextInt(1000) < 1) {
                            i++;
                        }
                    }
                    while (i-- > 0)
                        corrupt(rand);
                    break;
                default:
                    break;

            }
        }
    }

    private void corrupt(Random rand) {
        if (potions.keySet().isEmpty()|| stable) {
            return;
        }
        int id = new ArrayList<Integer>(potions.keySet()).get(rand.nextInt(potions.keySet().size()));
        PotionEffect effect = potions.remove(id);
/*        int duration = effect.getDuration() > 0 ? effect.getDuration() : rand.nextInt(200) + 45;
        PotionEffect newEffect = new PotionEffect(rand.nextInt(24), duration, 0);
        addEffect(newEffect);*/
        addRandomEffect(true);
        EntityPlayer player = worldObj.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 3);
        int i = 12;
        worldObj.playSound(pos.getX(), pos.getY(), pos.getZ(), "ambient.weather.thunder", 40.0f, 1.0f, false);
        while (i-- > 0) {
            worldObj.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + rand.nextDouble(), pos.getY() + 1 + rand.nextDouble(), pos.getZ() + rand.nextDouble(), 0, 0, 0);
            worldObj.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + rand.nextDouble(), pos.getY() + 1 + rand.nextDouble(), pos.getZ() + rand.nextDouble(), 0, 0, 0);
            worldObj.spawnParticle(EnumParticleTypes.REDSTONE, pos.getX() + rand.nextDouble(), pos.getY() + 1 + rand.nextDouble(), pos.getZ() + rand.nextDouble(), 0, 0, 0);
        }


        if (!worldObj.isRemote && player != null) {
            player.addStat(Achievements.getAchievement("mix.potion.corrupt"), 1);
        }
    }

    public void addPotion(ItemStack item) {
        @SuppressWarnings("unchecked")
        List<PotionEffect> effects = ((ItemPotion) item.getItem()).getEffects(item);
        for (PotionEffect effect : effects)
            addEffect(effect);
    }

    public void addEffect(PotionEffect effect) {
        if (potions.keySet().isEmpty()) {
            potions.put(effect.getPotionID(), effect);
        } else {
            potions.put(effect.getPotionID(), potions.keySet().contains(effect.getPotionID()) ? combine(potions.get(effect.getPotionID()), effect) : effect);
        }
    }

    public  void addRandomEffect(){
        addRandomEffect(false);
    }
    public void addRandomEffect(boolean force) {
        Random rand = new Random();
        int max = Potion.potionTypes.length - 1;
        Potion potion = Potion.potionTypes[rand.nextInt(max)];
        if (force) {
            while (potion == null) {
                potion = Potion.potionTypes[rand.nextInt(max)];
            }
        }
        if (potion != null) {
            PotionEffect potionEffect = new PotionEffect(potion.getId(), 600, rand.nextInt(2));
            if (!potions.keySet().contains(potionEffect.getPotionID())) {
                potions.put(potionEffect.getPotionID(), potionEffect);
            }
        }
    }

    public HashSet<PotionEffect> getEffects() {
        return new HashSet<PotionEffect>(potions.values());
    }

    public void clearEffects() {
        potions.clear();
    }

    public PotionEffect combine(PotionEffect one, PotionEffect two) {
        if (one.getPotionID() == two.getPotionID()) {
            int id = one.getPotionID();
            int amplifier = one.getAmplifier() == two.getAmplifier() ? one.getAmplifier() + 1 : one.getAmplifier() > two.getAmplifier() ? one.getAmplifier() : two.getAmplifier();
            int duration = one.getDuration() + two.getDuration();
            return new PotionEffect(id, duration, amplifier);
        } else {
            return (one);
        }
    }

    public void doubleEffect() {
        for (PotionEffect effect : potions.values()) {
            potions.put(effect.getPotionID(), new PotionEffect(effect.getPotionID(), effect.getDuration(), effect.getAmplifier() + 1));
        }
    }

    public void increaseDuration() {
        for (PotionEffect effect : potions.values()) {
            potions.put(effect.getPotionID(), new PotionEffect(effect.getPotionID(), effect.getDuration() + 200, effect.getAmplifier()));
        }
    }

    public void stabilize(){
        stable = true;
    }
}
