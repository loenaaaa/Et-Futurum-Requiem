package ganymedes01.etfuturum.compat;

import cpw.mods.ironchest.IronChestType;
import cpw.mods.ironchest.ItemChestChanger;
import ganymedes01.etfuturum.ModItems;
import ganymedes01.etfuturum.items.ItemBarrelUpgrade;
import ganymedes01.etfuturum.items.ItemShulkerBoxUpgrade;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.mrnavastar.r.R;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import roadhog360.hogutils.api.utils.RecipeHelper;

import java.util.Map;

public class CompatIronChests {
	public static String[] blacklist = {};
	private static final Map<String, ItemChestChanger> upgrades = new Object2ObjectOpenHashMap<>();
	private static double renderDistance;

	public static boolean isUpgradeEnabled(String from, String to) {
		to = to.toUpperCase();
		from = from.toUpperCase();
		return isTierEnabled(from) && isTierEnabled(to) && !ArrayUtils.contains(blacklist, from.toUpperCase() + ":" + to.toUpperCase());
	}

	public static boolean isTierEnabled(String type) {
		return switch (type.toLowerCase()) {
			case "silver", "copper", "netherite" ->
					OreDictionary.getOres("ingot" + type.substring(0, 1).toUpperCase() + type.substring(1)).isEmpty();
			case "darksteel" -> ModsList.ENDER_IO.isLoaded();
			default -> true;
		};
	}

	public static void init() {
		try {
			CompatIronChests.blacklist = R.of(Class.forName("cpw.mods.ironchest.IronChest")).get("blocklistUpgrades", String[].class);
		} catch (Exception ignored) {
		}
		try {
			renderDistance = R.of(Class.forName("cpw.mods.ironchest.IronChest")).get("TRANSPARENT_RENDER_INSIDE", Boolean.class) ?
					R.of(Class.forName("cpw.mods.ironchest.IronChest")).get("TRANSPARENT_RENDER_DISTANCE", Double.class) : 0F;
		} catch (Exception ignored) {
			renderDistance = 128F;
		}
	}

	public static void registerRecipes() {
		Map<String, ItemChestChanger> changerMap = upgrades;
		if(ModItems.BARREL_UPGRADE.isEnabled()) {
			ItemBarrelUpgrade upgrade = ((ItemBarrelUpgrade) ModItems.BARREL_UPGRADE.get());
			for (int i = 0; i < upgrade.types.length; i++) {
				Item icUpgrade;
				if(isUpgradeEnabled(upgrade.getSource(i), upgrade.getTarget(i)) && (icUpgrade = changerMap.get(upgrade.getSource(i) + upgrade.getTarget(i))) != null) {
					RecipeHelper.addShapedRecipe(RecipeHelper.Priority.NORMAL, ModItems.BARREL_UPGRADE.newItemStack(1, i), "X", 'X', new ItemStack(icUpgrade));
					RecipeHelper.addShapedRecipe(RecipeHelper.Priority.NORMAL, new ItemStack(icUpgrade), "X", 'X', ModItems.BARREL_UPGRADE.newItemStack(1, i));
				}
			}
		}
		if(ModItems.SHULKER_BOX_UPGRADE.isEnabled()) {
			ItemShulkerBoxUpgrade upgrade = ((ItemShulkerBoxUpgrade) ModItems.SHULKER_BOX_UPGRADE.get());
			for (int i = 0; i < upgrade.types.length; i++) {
				Item icUpgrade;
				if(isUpgradeEnabled(upgrade.getSource(i), upgrade.getTarget(i)) && (icUpgrade = changerMap.get(upgrade.getSource(i) + upgrade.getTarget(i))) != null) {
					RecipeHelper.addShapelessRecipe(RecipeHelper.Priority.NORMAL, ModItems.SHULKER_BOX_UPGRADE.newItemStack(1, i), ModItems.SHULKER_SHELL.newItemStack(), new ItemStack(icUpgrade));
					RecipeHelper.addShapedRecipe(RecipeHelper.Priority.NORMAL, new ItemStack(icUpgrade), "X", 'X', ModItems.SHULKER_BOX_UPGRADE.newItemStack(1, i));
				}
			}
		}
	}

	public static void registerUpgradeToMap(Item item) {
		if(item instanceof ItemChestChanger changer) {
			upgrades.put(changer.getType().name(), changer);
		}
	}

	@Nullable
	public static String getUpgradeName(String from, Item item) {
		if(item instanceof ItemChestChanger changer) {
			R type = R.of(changer.getType());
			if(type.get("source", IronChestType.class).name().equals(from.toUpperCase().replace("VANILLA", "WOOD"))) {
				return type.get("target", IronChestType.class).name();
			}
		}
		return null;
	}

	public static String getNextBarrelUpgrade(String current, ItemStack stack) {
		if(ModItems.BARREL_UPGRADE.isEnabled()) {
			if(stack.getItem() instanceof ItemBarrelUpgrade upgrade && upgrade.getSource(stack.getItemDamage()).equals(current)) {
				return upgrade.getTarget(stack.getItemDamage());
			}
			return null;
		}
		return CompatIronChests.getUpgradeName(current, stack.getItem());
	}

	public static String getNextShulkerUpgrade(String current, ItemStack stack) {
		if(ModItems.SHULKER_BOX_UPGRADE.isEnabled()) {
			if(stack.getItem() instanceof ItemShulkerBoxUpgrade upgrade && upgrade.getSource(stack.getItemDamage()).equals(current)) {
				return upgrade.getTarget(stack.getItemDamage());
			}
			return null;
		}
		return CompatIronChests.getUpgradeName(current, stack.getItem());
	}

	public static boolean enableCrystalRendering() {
		return renderDistance > 0;
	}

	public static double crystalRenderDistance() {
		return renderDistance;
	}
}
