package uk.tldcode.minecraft.potionmixer;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.IBrewItem;

import java.util.List;
import java.util.Random;

public class BlockPotionMixer extends BlockCauldron implements ITileEntityProvider {

    public static final PropertyInteger POTION = PropertyInteger.create("potion", 0, 1);

    public BlockPotionMixer() {
        super();
        this.setDefaultState(this.blockState.getBaseState().withProperty(POTION, 0));
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, LEVEL, POTION);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TilePotionMixer();
    }

    /**
     * Get the Item that this Block should drop when harvested.
     *
     * @param fortune the level of the Fortune enchantment on the player's tool
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos) {
        return Item.getItemFromBlock(this);
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta) {

        switch (meta) {
            default:
                return this.getDefaultState().withProperty(LEVEL, 0).withProperty(POTION, 0);
            case 1:
                return this.getDefaultState().withProperty(LEVEL, 1).withProperty(POTION, 0);
            case 2:
                return this.getDefaultState().withProperty(LEVEL, 2).withProperty(POTION, 0);
            case 3:
                return this.getDefaultState().withProperty(LEVEL, 3).withProperty(POTION, 0);
            case 4:
                return this.getDefaultState().withProperty(LEVEL, 0).withProperty(POTION, 1);
            case 5:
                return this.getDefaultState().withProperty(LEVEL, 1).withProperty(POTION, 1);
            case 6:
                return this.getDefaultState().withProperty(LEVEL, 2).withProperty(POTION, 1);
            case 7:
                return this.getDefaultState().withProperty(LEVEL, 3).withProperty(POTION, 1);

        }
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state) {
        if (state.getValue(POTION) == 1) {
            switch (state.getValue(LEVEL)) {
                default:
                    return 4;
                case 1:
                    return 5;
                case 2:
                    return 6;
                case 3:
                    return 7;
            }
        } else {
            return state.getValue(LEVEL);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
        final TilePotionMixer tilePotionMixer = ((TilePotionMixer) worldIn.getTileEntity(pos));
        if (entityIn instanceof EntityItem) {
            EntityItem entityItem = ((EntityItem) entityIn);
            ItemStack stack = entityItem.getEntityItem();
            if (stack.getItem() == Items.potionitem && stack.getMetadata() != 0) {
                if (stack.getMetadata() == 2) {
                    setPotionFull(worldIn, pos, state, 1);
                    state = worldIn.getBlockState(pos);
                    setWaterLevel(worldIn, pos, state, state.getValue(LEVEL) + 1);
                    entityIn.setDead();
                    if (!worldIn.isRemote)
                        worldIn.spawnEntityInWorld(new EntityItem(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.5D, (double) pos.getZ() + 0.5D, new ItemStack(Items.glass_bottle)));
                } else if (state.getValue(LEVEL) == 3 && state.getValue(POTION) == 1) {
                    tilePotionMixer.addPotion(stack);
                    setPotionFull(worldIn, pos, state, 1);
                    entityIn.setDead();
                    if (!worldIn.isRemote)
                        worldIn.spawnEntityInWorld(new EntityItem(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.5D, (double) pos.getZ() + 0.5D, new ItemStack(Items.glass_bottle)));
                }
            } else if (state.getValue(LEVEL) == 3 && state.getValue(POTION) == 1) {
                boolean bool = false;
                EntityPlayer player = worldIn.getClosestPlayerToEntity(entityIn, 3);
                if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("CustomPotionEffects")) {
                    tilePotionMixer.addPotion(stack);
                    setPotionFull(worldIn, pos, state, 1);
                    entityIn.setDead();
                    bool= true;
                    if (!worldIn.isRemote)
                        worldIn.spawnEntityInWorld(new EntityItem(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.5D, (double) pos.getZ() + 0.5D, new ItemStack(Items.glass_bottle)));
                } else if (stack.getItem() == Items.golden_apple) {
                    if (stack.getMetadata() == 0) {
                        if (player != null)
                            player.addStat(Achievements.getAchievement("mix.gold.apple"), 1);
                        tilePotionMixer.addEffect(new PotionEffect(Potion.absorption.id, 2400, 0));
                        tilePotionMixer.addEffect(new PotionEffect(Potion.regeneration.id, 5, 1));
                        bool = true;
                    } else if (stack.getMetadata() == 1) {
                        if (player != null)
                            player.addStat(Achievements.getAchievement("mix.gold.apple.notch"), 1);
                        tilePotionMixer.addEffect(new PotionEffect(Potion.regeneration.id, 600, 4));
                        tilePotionMixer.addEffect(new PotionEffect(Potion.resistance.id, 6000, 0));
                        tilePotionMixer.addEffect(new PotionEffect(Potion.fireResistance.id, 6000, 0));
                        tilePotionMixer.addEffect(new PotionEffect(Potion.absorption.id, 2400, 0));
                        bool = true;
                    }
                } else if (stack.getItem() == Items.fish && stack.getMetadata() == 3) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.puffer.fish"), 1);
                    tilePotionMixer.addEffect(new PotionEffect(Potion.poison.id, 1200, 3));
                    tilePotionMixer.addEffect(new PotionEffect(Potion.hunger.id, 300, 2));
                    tilePotionMixer.addEffect(new PotionEffect(Potion.confusion.id, 300, 1));
                    bool = true;
                } else if (stack.getItem() == Items.rotten_flesh) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.rotten.flesh"), 1);
                    tilePotionMixer.addEffect(new PotionEffect(Potion.hunger.id, 600, 0));
                    bool = true;
                } else if (stack.getItem() == Items.spider_eye) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.spider.eye"), 1);
                    tilePotionMixer.addEffect(new PotionEffect(Potion.poison.id, 600, 0));
                    bool = true;
                } else if (stack.getItem() instanceof ItemFood) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.food"), 1);
                    ItemFood food = (ItemFood) stack.getItem();
                    int heal = food.getHealAmount(stack);
                    PotionMixerMod.proxy.say((heal));
                    tilePotionMixer.addEffect(new PotionEffect(Potion.saturation.id, heal * 20, 0));
                    bool = true;
                } else if (stack.getItem() == Items.nether_star) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.nether.star"), 1);
                    tilePotionMixer.doubleEffect();
                    bool = true;
                } else if (stack.getItem() == Items.prismarine_crystals) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.prismarine.crystal"), 1);
                    tilePotionMixer.addEffect(new PotionEffect(Potion.digSpeed.id, 600, 0));
                    bool = true;
                } else if (stack.getItem() == Items.prismarine_shard) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.prismarine.shard"), 1);
                    tilePotionMixer.addEffect(new PotionEffect(Potion.digSlowdown.id, 600, 0));
                    bool = true;
                } else if (stack.getItem() == Items.skull) {
                    if (stack.getMetadata() == 1) {
                        if (player != null)
                            player.addStat(Achievements.getAchievement("mix.skull.wither"), 1);
                        tilePotionMixer.addEffect(new PotionEffect(Potion.wither.id, 600, 0));
                        bool = true;
                    } else if (stack.getMetadata() == 2) {
                        if (player != null)
                            player.addStat(Achievements.getAchievement("mix.skull.zombie"), 1);
                        tilePotionMixer.addEffect(new PotionEffect(Potion.healthBoost.id, 600, 0));
                        bool = true;
                    }
                } else if (stack.getItem() == Items.fermented_spider_eye) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.fermented.spider.eye"), 1);
                    tilePotionMixer.addEffect(new PotionEffect(Potion.blindness.id, 600, 0));
                    bool = true;
                } else if (stack.getItem() == Item.getItemFromBlock(Blocks.redstone_block)) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.redstone"), 1);
                    tilePotionMixer.increaseDuration();
                    bool = true;
                } else if (stack.getItem() == Item.getItemFromBlock(Blocks.dragon_egg)) {
                    if (player != null)
                        player.addStat(Achievements.getAchievement("mix.stabilizer"), 1);
                    tilePotionMixer.stabilize();
                    bool = true;
                } else if (PotionMixerMod.botania) {
                    if (stack.getItem() instanceof IBrewItem) {
                        Brew brew = ((IBrewItem) stack.getItem()).getBrew(stack);
                        List<PotionEffect> brewEffects = brew.getPotionEffects(stack);
                        for (PotionEffect effect : brewEffects) {
                            tilePotionMixer.addEffect(effect);
                        }
                        //brewEffects.forEach(tilePotionMixer::addEffect); Java 8 version of the above for loop
                        bool = true;
                    }
                }
                if (!bool && (stack.getRarity() != EnumRarity.UNCOMMON || stack.getRarity() != EnumRarity.RARE || stack.getRarity() != EnumRarity.EPIC)) {
                    int amount = stack.stackSize;
                    while (amount-- > 0)
                        tilePotionMixer.addRandomEffect();
                    bool = true;
                }
                if (bool) {
                    Random rand = new Random();
                    int i = 45;
                    worldIn.playSound(pos.getX(), pos.getY(), pos.getZ(), "game.neutral.swim.splash", 1.0f, 1.0f, false);
                    while (i-- > 0) {
                        worldIn.spawnParticle(EnumParticleTypes.WATER_SPLASH, pos.getX() + rand.nextDouble(), pos.getY() + 1, pos.getZ() + rand.nextDouble(), 0, 0, 0);
                    }
                    entityIn.setDead();
                }
            }
        }
    }

    public void setPotionFull(World worldIn, BlockPos pos, IBlockState state, int level) {
        worldIn.setBlockState(pos, state.withProperty(POTION, MathHelper.clamp_int(level, 0, 1)));
        worldIn.updateComparatorOutputLevel(pos, this);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            ItemStack itemstack = playerIn.inventory.getCurrentItem();

            if (itemstack == null) {
                return true;
            } else {
                int level = state.getValue(LEVEL);
                Item item = itemstack.getItem();

                if (item == Items.water_bucket) {
                    if (level < 3) {
                        if (!playerIn.capabilities.isCreativeMode) {
                            playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, new ItemStack(Items.bucket));
                        }

                        this.setWaterLevel(worldIn, pos, state, 3);
                    }

                    return true;
                } else if (item == Items.bucket) {
                    if (level >= 3) {
                        if (!playerIn.capabilities.isCreativeMode) {
                            playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, new ItemStack(Items.water_bucket));
                        }
                        this.setWaterLevel(worldIn, pos, state, 0);
                        state = worldIn.getBlockState(pos);
                        this.setPotionFull(worldIn, pos, state, 0);
                    }

                    return true;
                } else {
                    ItemStack itemstack1;
                    if (item == Items.glass_bottle) {
                        if (level >= 3 && state.getValue(POTION) == 1) {
                            playerIn.addStat(Achievements.getAchievement("mix.potion"), 1);
                            itemstack1 = new ItemStack(Items.potionitem, 1, 8192);
                            NBTTagList nbtTagList = new NBTTagList();
                            for (PotionEffect effect : ((TilePotionMixer) worldIn.getTileEntity(pos)).getEffects()) {
                                NBTTagCompound effectCompound = new NBTTagCompound();
                                effectCompound = effect.writeCustomPotionEffectToNBT(effectCompound);
                                nbtTagList.appendTag(effectCompound);
                            }
                            NBTTagCompound nbtTagCompound = new NBTTagCompound();
                            nbtTagCompound.setTag("CustomPotionEffects", nbtTagList);
                            itemstack1.setTagCompound(nbtTagCompound);
                            if (!playerIn.inventory.addItemStackToInventory(itemstack1)) {
                                worldIn.spawnEntityInWorld(new EntityItem(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.5D, (double) pos.getZ() + 0.5D, itemstack1));
                            } else if (playerIn instanceof EntityPlayerMP) {
                                ((EntityPlayerMP) playerIn).sendContainerToPlayer(playerIn.inventoryContainer);
                            }
                            if (!playerIn.capabilities.isCreativeMode) {
                                --itemstack.stackSize;
                                if (itemstack.stackSize <= 0) {
                                    playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, null);
                                }
                                ((TilePotionMixer) worldIn.getTileEntity(pos)).clearEffects();
                                setWaterLevel(worldIn, pos, state, 0);
                                setPotionFull(worldIn, pos, state, 0);

                            }
                        } else if (level > 0) {
                            if (!playerIn.capabilities.isCreativeMode) {
                                itemstack1 = new ItemStack(Items.potionitem, 1, 0);

                                if (!playerIn.inventory.addItemStackToInventory(itemstack1)) {
                                    worldIn.spawnEntityInWorld(new EntityItem(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.5D, (double) pos.getZ() + 0.5D, itemstack1));
                                } else if (playerIn instanceof EntityPlayerMP) {
                                    ((EntityPlayerMP) playerIn).sendContainerToPlayer(playerIn.inventoryContainer);
                                }

                                --itemstack.stackSize;

                                if (itemstack.stackSize <= 0) {
                                    playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, (ItemStack) null);
                                }
                            }

                            this.setWaterLevel(worldIn, pos, state, level - 1);
                        }

                        return true;
                    } else {
                        if (level > 0 && item instanceof ItemArmor) {
                            ItemArmor itemarmor = (ItemArmor) item;

                            if (itemarmor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && itemarmor.hasColor(itemstack)) {
                                itemarmor.removeColor(itemstack);
                                this.setWaterLevel(worldIn, pos, state, level - 1);
                                return true;
                            }
                        }

                        if (level > 0 && item instanceof ItemBanner && TileEntityBanner.getPatterns(itemstack) > 0) {
                            itemstack1 = itemstack.copy();
                            itemstack1.stackSize = 1;
                            TileEntityBanner.removeBannerData(itemstack1);

                            if (itemstack.stackSize <= 1 && !playerIn.capabilities.isCreativeMode) {
                                playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, itemstack1);
                            } else {
                                if (!playerIn.inventory.addItemStackToInventory(itemstack1)) {
                                    worldIn.spawnEntityInWorld(new EntityItem(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 1.5D, (double) pos.getZ() + 0.5D, itemstack1));
                                } else if (playerIn instanceof EntityPlayerMP) {
                                    ((EntityPlayerMP) playerIn).sendContainerToPlayer(playerIn.inventoryContainer);
                                }

                                if (!playerIn.capabilities.isCreativeMode) {
                                    --itemstack.stackSize;
                                }
                            }

                            if (!playerIn.capabilities.isCreativeMode) {
                                this.setWaterLevel(worldIn, pos, state, level - 1);
                            }

                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
    }
}
