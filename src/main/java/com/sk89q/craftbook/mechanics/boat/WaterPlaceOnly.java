package com.sk89q.craftbook.mechanics.boat;

import com.google.common.collect.ImmutableSet;
import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class WaterPlaceOnly extends AbstractCraftBookMechanic {

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getItem() != null
                    && (event.getItem().getType() == Material.BOAT
                    || event.getItem().getType() == Material.BOAT_ACACIA
                    || event.getItem().getType() == Material.BOAT_BIRCH
                    || event.getItem().getType() == Material.BOAT_DARK_OAK
                    || event.getItem().getType() == Material.BOAT_JUNGLE
                    || event.getItem().getType() == Material.BOAT_SPRUCE)) {
                Block above = event.getClickedBlock().getRelative(0,1,0);
                if ((!isWater(above) || event.getClickedBlock().getY() == event.getClickedBlock().getWorld().getMaxHeight() - 1) && !isWater(event.getClickedBlock())) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Result.DENY);
                    event.getPlayer().sendMessage(ChatColor.RED + "You can't place that boat on land!");
                }
            }
        }
    }

    private static boolean isWater(Block b) {
        return b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER;
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}