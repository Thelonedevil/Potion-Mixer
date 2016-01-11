package uk.tldcode.minecraft.potionmixer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = PotionMixerMod.MODID, name = PotionMixerMod.NAME, version = PotionMixerMod.VERSION,guiFactory = "uk.tldcode.minecraft.potionmixer.GuiFactory",dependencies = "after:"+PotionMixerMod.BOTANIA)
public class PotionMixerMod {
    public static final String MODID = "potionmixer";
    public static final String VERSION = "1.0";
    public static final String NAME = "Potion Mixer";

    public static AchievementPage potionPage;
    public static int pageIndex;

    public static final String BOTANIA = "Botania";

    public static boolean botania = false;

    public static Configuration configFile;
    public static boolean debug = false;

    @SidedProxy(clientSide = "uk.tldcode.minecraft.potionmixer.ClientProxy", serverSide = "uk.tldcode.minecraft.potionmixer.CommonProxy")
    public static CommonProxy proxy;

    static Block mixer = new BlockPotionMixer().setHardness(10f).setUnlocalizedName("potionmixer").setCreativeTab(CreativeTabs.tabBrewing);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        configFile = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();
    }
    public static void syncConfig() {
        debug = configFile.getBoolean("Debug", Configuration.CATEGORY_GENERAL, debug, "Turn on debugging mode");
        if(configFile.hasChanged())
            configFile.save();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        mixer.setHarvestLevel("pickaxe", 2);
        GameRegistry.registerBlock(mixer, "potionmixer");
        GameRegistry.addRecipe(new ShapedOreRecipe(mixer, "a a", "aba", "aaa", 'a', "ingotIron", 'b', Items.brewing_stand));
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList nbtTagList = new NBTTagList();
        compound.setTag("CustomPotionEffects", nbtTagList);
        ItemStack emptyPotion = new ItemStack(Items.potionitem, 1, 2);
        emptyPotion.setTagCompound(compound);
        ItemStack fullPotion = new ItemStack(Items.potionitem, 1, 8192);
        fullPotion.setTagCompound(compound);
        ItemStack fullSplashPotion = new ItemStack(Items.potionitem, 1, 16384);
        fullSplashPotion.setTagCompound(compound);
        BrewingRecipeRegistry.addRecipe(new ItemStack(Items.potionitem, 1, 0), new ItemStack(Items.potionitem, 1, 0), emptyPotion);
        if (event.getSide() == Side.CLIENT) {
            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
            renderItem.getItemModelMesher().register(Item.getItemFromBlock(mixer), 0, new ModelResourceLocation(MODID + ":" + "potionmixer", "inventory"));
        }
        Achievements.addAchievement("craft.potion.mixer", new Achievement("achievement.craft.potion.mixer", "craft.potion.mixer", 0, 0, mixer, AchievementList.potion));
        Achievements.addAchievement("brew.uninteresting.potion", new Achievement("achievement.brew.uninteresting.potion", "brew.uninteresting.potion", 1, 1, emptyPotion, Achievements.getAchievement("craft.potion.mixer")));
        Achievements.addAchievement("mix.potion", new Achievement("achievement.mix.potion", "mix.potion", 3, 1, fullPotion, Achievements.getAchievement("brew.uninteresting.potion")));
        Achievements.addAchievement("brew.splash.mixed.potion", new Achievement("achievement.brew.splash.mixed.potion", "brew.splash.mixed.potion", 5, 1, fullSplashPotion, Achievements.getAchievement("mix.potion")));
        Achievements.addAchievement("mix.gold.apple", new Achievement("achievement.mix.gold.apple", "mix.gold.apple", 1, 15, Items.golden_apple, Achievements.getAchievement("brew.uninteresting.potion")));
        Achievements.addAchievement("mix.gold.apple.notch", new Achievement("achievement.mix.gold.apple.notch", "mix.gold.apple.notch", 1, 17, Items.golden_apple, Achievements.getAchievement("mix.gold.apple")).setSpecial());
        Achievements.addAchievement("mix.potion.corrupt", new Achievement("achievement.mix.potion.corrupt", "mix.potion.corrupt", 3, -1, Items.fermented_spider_eye, Achievements.getAchievement("mix.potion")));
        Achievements.addAchievement("mix.puffer.fish", new Achievement("achievement.mix.puffer.fish", "mix.puffer.fish", 0, 5, new ItemStack(Items.fish, 1, 3), Achievements.getAchievement("brew.uninteresting.potion")));
        Achievements.addAchievement("mix.rotten.flesh", new Achievement("achievement.mix.rotten.flesh", "mix.rotten.flesh", 2, 5, Items.rotten_flesh, Achievements.getAchievement("brew.uninteresting.potion")));
        Achievements.addAchievement("mix.food", new Achievement("achievement.mix.food", "mix.food", 2, 3, Items.cooked_beef, Achievements.getAchievement("brew.uninteresting.potion")));
        Achievements.addAchievement("mix.nether.star", new Achievement("achievement.mix.nether.star", "mix.nether.star", 0, 3, Items.nether_star, Achievements.getAchievement("brew.uninteresting.potion")).setSpecial());
        Achievements.addAchievement("mix.prismarine.crystal", new Achievement("achievement.mix.prismarine.crystal", "mix.prismarine.crystal", 0, 7, Items.prismarine_crystals, Achievements.getAchievement("brew.uninteresting.potion")).setSpecial());
        Achievements.addAchievement("mix.prismarine.shard", new Achievement("achievement.mix.prismarine.shard", "mix.prismarine.shard", 2, 7, Items.prismarine_shard, Achievements.getAchievement("brew.uninteresting.potion")));
        Achievements.addAchievement("mix.skull.wither", new Achievement("achievement.mix.skull.wither", "mix.skull.wither", 0, 9, new ItemStack(Items.skull,1,1), Achievements.getAchievement("brew.uninteresting.potion")).setSpecial());
        Achievements.addAchievement("mix.skull.zombie", new Achievement("achievement.mix.skull.zombie", "mix.skull.zombie", 2, 9, new ItemStack(Items.skull,1,2), Achievements.getAchievement("brew.uninteresting.potion")).setSpecial());
        Achievements.addAchievement("mix.fermented.spider.eye", new Achievement("achievement.mix.fermented.spider.eye", "mix.fermented.spider.eye", 0, 11, Items.fermented_spider_eye, Achievements.getAchievement("brew.uninteresting.potion")));
        Achievements.addAchievement("mix.redstone", new Achievement("achievement.mix.redstone", "mix.redstone", 2, 11, Items.redstone, Achievements.getAchievement("brew.uninteresting.potion")));
        Achievements.addAchievement("mix.stabilizer",new Achievement("achievement.mix.stabilizer","mix.stabilizer",3,-3, Item.getItemFromBlock(Blocks.dragon_egg),Achievements.getAchievement("mix.potion.corrupt")).setSpecial());

        pageIndex = AchievementPage.getAchievementPages().size();
        potionPage = new AchievementPage(NAME, Achievements.getAchievements());
        AchievementPage.registerAchievementPage(potionPage);


        MinecraftForge.EVENT_BUS.register(new Achievements());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        botania = Loader.isModLoaded(BOTANIA);
    }
}
