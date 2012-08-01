package com.sk89q.craftbook.mech.cauldron;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.craftbook.BaseConfiguration;

/**
 * @author Silthus
 */
public class ImprovedCauldronCookbook extends BaseConfiguration {

    public static ImprovedCauldronCookbook INSTANCE;
    private final Collection<Recipe> recipes;
    private final File config;

    public ImprovedCauldronCookbook(FileConfiguration cfg, File dataFolder) {
	super(cfg, dataFolder);
	recipes = new ArrayList<Recipe>();
	config = new File(dataFolder, "recipes.yml");
	load(cfg.getConfigurationSection("recipes"));
	INSTANCE = this;
    }

    public void reload() {
	recipes.clear();
	load(YamlConfiguration.loadConfiguration(config).getConfigurationSection("recipes"));
    }

    private void load(ConfigurationSection cfg) {
	// lets load all recipes
	for (String key : cfg.getKeys(false)) {
	    recipes.add(new Recipe(key, cfg));
	}
    }

    public Recipe getRecipe(Collection<CauldronItemStack> items) throws UnknownRecipeException {
	for (Recipe recipe : recipes) {
	    if (recipe.checkIngredients(items)) {
		return recipe;
	    }
	}
	throw new UnknownRecipeException("Are you sure you have the right ingredients?");
    }

    public static final class Recipe {

	private final String id;
	private final ConfigurationSection config;

	private String name;
	private String description;
	private Collection<CauldronItemStack> ingredients;
	private Collection<CauldronItemStack> results;
	private double chance;

	private Recipe(String id, ConfigurationSection cfg) {
	    this.id = id;
	    config = cfg.getConfigurationSection(id);
	    ingredients = new ArrayList<CauldronItemStack>();
	    results = new ArrayList<CauldronItemStack>();
	    chance = 60;
	    load();
	}

	private void load() {
	    name = config.getString("name");
	    description = config.getString("description");
	    ingredients = getItems(config.getConfigurationSection("ingredients"));
	    results = getItems(config.getConfigurationSection("results"));
	    chance = config.getDouble("chance", 60);
	}

	private Collection<CauldronItemStack> getItems(ConfigurationSection section) {
	    Collection<CauldronItemStack> items = new ArrayList<CauldronItemStack>();
	    for (String item : section.getKeys(false)) {
		String[] split = item.split(":");
		Material material;
		try {
		    material = Material.getMaterial(Integer.parseInt(split[0]));
		} catch (NumberFormatException e) {
		    // use the name
		    material = Material.getMaterial(split[0].toUpperCase());
		}
		if (material != null) {
		    CauldronItemStack itemStack = new CauldronItemStack(material);
		    if (split.length > 1) {
			itemStack.setData(Short.parseShort(split[1]));
		    } else {
			itemStack.setData((short) -1);
		    }
		    itemStack.setAmount(section.getInt(item, 1));
		    items.add(itemStack);
		}
	    }
	    return items;
	}

	public String getId() {
	    return id;
	}

	public String getName() {
	    return name;
	}

	public String getDescription() {
	    return description;
	}

	public double getChance() {
	    return chance;
	}

	/**
	 * Checks if the recipe
	 *
	 * @param items
	 * @return
	 */
	public boolean checkIngredients(Collection<CauldronItemStack> items) {
	    if (items.size() <= 0) return false;
	    for (CauldronItemStack item : items) {
		if (!ingredients.contains(item)) {
		    return false;
		}
	    }
	    return true;
	}

	public Collection<CauldronItemStack> getResults() {
	    return results;
	}
    }
}
