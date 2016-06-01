package com.sk89q.craftbook.mechanics.area.simple;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.pipe.PipeFinishEvent;
import com.sk89q.craftbook.mechanics.pipe.PipePutEvent;
import com.sk89q.craftbook.mechanics.pipe.PipeSuckEvent;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.exceptions.InvalidMechanismException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that can be a mechanic that toggles a cuboid. This is basically either Door or Bridge.
 */
public abstract class CuboidToggleMechanic extends AbstractCraftBookMechanic {

    public abstract Block getFarSign(Block trigger);

    public abstract Block getBlockBase(Block trigger) throws InvalidMechanismException;

    public abstract CuboidRegion getCuboidArea(Block trigger, Block proximalBaseCenter, Block distalBaseCenter) throws InvalidMechanismException;

    public abstract boolean isApplicableSign(String line);

    public static boolean open(Block sign, Block farSide, Block base, CuboidRegion toggle) {

        ChangedSign s = BukkitUtil.toChangedSign(sign);
        ChangedSign other = BukkitUtil.toChangedSign(farSide);
        for (Vector bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            if (BlockUtil.areBlocksIdentical(b, base) || BlockUtil.isBlockReplacable(b.getType())) {
                if (CraftBookPlugin.inst().getConfiguration().safeDestruction && BlockUtil.areBlocksIdentical(b, base))
                    addBlocks(s, other, 1);
                b.setType(Material.AIR);
            }
        }

        return true;
    }

    public static boolean close(Block sign, Block farSide, Block base, CuboidRegion toggle, LocalPlayer player) {

        ChangedSign s = BukkitUtil.toChangedSign(sign);
        ChangedSign other = BukkitUtil.toChangedSign(farSide);
        for (Vector bv : toggle) {
            Block b = sign.getWorld().getBlockAt(bv.getBlockX(), bv.getBlockY(), bv.getBlockZ());
            if (BlockUtil.isBlockReplacable(b.getType())) {
                if (CraftBookPlugin.inst().getConfiguration().safeDestruction) {
                    if (hasEnoughBlocks(s, other)) {
                        b.setType(base.getType());
                        b.setData(base.getData());
                        removeBlocks(s, other, 1);
                    } else {
                        if (player != null) {
                            player.printError("mech.not-enough-blocks");
                        }
                        return false;
                    }
                } else {
                    b.setType(base.getType());
                    b.setData(base.getData());
                }
            }
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeFinish(PipeFinishEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getOrigin())) return;
        ChangedSign sign = BukkitUtil.toChangedSign(event.getOrigin());

        if(!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> leftovers = new ArrayList<ItemStack>();
        try {
            Block base = getBlockBase(event.getOrigin());
            for(ItemStack stack : event.getItems()) {
                if(stack.getType() != base.getType() || stack.getData().getData() != base.getData()) {
                    leftovers.add(stack);
                    continue;
                }

                addBlocks(sign, null, stack.getAmount());
            }

            event.setItems(leftovers);
        } catch (InvalidMechanismException e) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipePut(PipePutEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getPuttingBlock())) return;
        ChangedSign sign = BukkitUtil.toChangedSign(event.getPuttingBlock());

        if(!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> leftovers = new ArrayList<ItemStack>();
        try {
            Block base = getBlockBase(event.getPuttingBlock());
            for(ItemStack stack : event.getItems()) {
                if(stack.getType() != base.getType() || stack.getData().getData() != base.getData()) {
                    leftovers.add(stack);
                    continue;
                }

                addBlocks(sign, null, stack.getAmount());
            }

            event.setItems(leftovers);
        } catch (InvalidMechanismException e) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPipeSuck(PipeSuckEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(!SignUtil.isSign(event.getSuckedBlock())) return;
        ChangedSign sign = BukkitUtil.toChangedSign(event.getSuckedBlock());

        if(!isApplicableSign(sign.getLine(1))) return;

        List<ItemStack> items = event.getItems();
        try {
            Block base = getBlockBase(event.getSuckedBlock());
            int blocks = getBlocks(sign, null);
            if(blocks > 0) {
                items.add(new ItemStack(base.getType(), blocks, base.getData()));
                setBlocks(sign, 0);
            }
            event.setItems(items);
        } catch (InvalidMechanismException e) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!SignUtil.isSign(event.getBlock())) return;
        if (!isApplicableSign(BukkitUtil.toChangedSign(event.getBlock()).getLine(1))) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        ChangedSign sign = null, other;

        if (SignUtil.isSign(event.getBlock()))
            sign = BukkitUtil.toChangedSign(event.getBlock());

        if (sign == null) return;

        other = BukkitUtil.toChangedSign(getFarSign(event.getBlock()));

        if (hasEnoughBlocks(sign, other)) {
            Block base;
            try {
                base = getBlockBase(event.getBlock());
                int amount = getBlocks(sign, other);
                while(amount > 0) {
                    ItemStack toDrop = new ItemStack(base.getType(), Math.min(amount, 64), base.getData());
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), toDrop);
                    amount -= 64;
                }
            } catch (InvalidMechanismException e) {
                if(e.getMessage() != null)
                    player.printError(e.getMessage());
            }
        }
    }

    public static boolean removeBlocks(ChangedSign s, ChangedSign other, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s, other) - amount;
        s.setLine(0, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public static boolean addBlocks(ChangedSign s, ChangedSign other, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return true;
        int curBlocks = getBlocks(s, other) + amount;
        s.setLine(0, String.valueOf(curBlocks));
        s.update(false);
        return curBlocks >= 0;
    }

    public static void setBlocks(ChangedSign s, int amount) {

        if (s.getLine(0).equalsIgnoreCase("infinite")) return;
        s.setLine(0, String.valueOf(amount));
        s.update(false);
    }

    public static int getBlocks(ChangedSign s, ChangedSign other) {

        if (s.getLine(0).equalsIgnoreCase("infinite") || other != null && other.getLine(0).equalsIgnoreCase("infinite"))
            return 0;
        int curBlocks = 0;
        try {
            curBlocks = Integer.parseInt(s.getLine(0));
            if(other != null) {
                try {
                    curBlocks += Integer.parseInt(other.getLine(0));
                    setBlocks(s, curBlocks);
                    setBlocks(other, 0);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            curBlocks = 0;
        }
        return curBlocks;
    }

    public static boolean hasEnoughBlocks(ChangedSign s, ChangedSign other) {

        return s.getLine(0).equalsIgnoreCase("infinite") || getBlocks(s, other) > 0;
    }
}