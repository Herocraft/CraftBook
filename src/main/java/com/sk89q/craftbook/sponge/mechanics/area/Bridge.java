package com.sk89q.craftbook.sponge.mechanics.area;

import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.sponge.SpongeConfiguration;
import com.sk89q.craftbook.sponge.util.SignUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;

@Module(moduleName = "Bridge", onEnable="onInitialize", onDisable="onDisable")
public class Bridge extends SimpleArea {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    @Override
    public void onInitialize() throws CraftBookException {
        maximumLength = SpongeConfiguration.<Integer>getValue(config.getNode("maximum-length"), 16, "The maximum length the bridge can be.");
        maximumWidth = SpongeConfiguration.<Integer>getValue(config.getNode("maximum-width"), 5, "The maximum width each side of the bridge can be. The overall max width is this*2 + 1.");
    }

    private int maximumLength;
    private int maximumWidth;

    @Override
    public boolean triggerMechanic(Location block, Sign sign, Human human, Boolean forceState) {

        if (!SignUtil.getTextRaw(sign, 1).equals("[Bridge End]")) {

            Direction back = SignUtil.getBack(block);

            Location baseBlock = block.getRelative(Direction.DOWN);

            Location otherSide = getOtherEnd(block, SignUtil.getBack(block), maximumLength);
            if (otherSide == null) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Missing other end!").build());
                return true;
            }
            Location otherBase = otherSide.getRelative(Direction.DOWN);

            if(!baseBlock.getBlock().equals(otherBase.getBlock())) {
                if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Both ends must be the same material!").build());
                return true;
            }

            int leftBlocks = 0, rightBlocks = 0; //Default to 0. Single width bridge is the default.

            Location left = baseBlock.getRelative(SignUtil.getLeft(block));
            Location right = baseBlock.getRelative(SignUtil.getRight(block));

            //Calculate left distance
            Location otherLeft = otherBase.getRelative(SignUtil.getLeft(block));

            while(true) {
                if(leftBlocks >= maximumWidth) break;
                if(left.getBlock().equals(baseBlock.getBlock()) && otherLeft.getBlock().equals(baseBlock.getBlock())) {
                    leftBlocks ++;
                    left = left.getRelative(SignUtil.getLeft(block));
                    otherLeft = otherLeft.getRelative(SignUtil.getLeft(block));
                } else {
                    break;
                }
            }

            //Calculate right distance
            Location otherRight = otherBase.getRelative(SignUtil.getRight(block));

            while(true) {
                if(rightBlocks >= maximumWidth) break;
                if(right.getBlock().equals(baseBlock.getBlock()) && otherRight.getBlock().equals(baseBlock.getBlock())) {
                    rightBlocks ++;
                    right = right.getRelative(SignUtil.getRight(block));
                    otherRight = otherRight.getRelative(SignUtil.getRight(block));
                } else {
                    break;
                }
            }

            baseBlock = baseBlock.getRelative(back);

            BlockState type = block.getRelative(Direction.DOWN).getBlock();
            if (baseBlock.getBlock().equals(type) && (forceState == null || !forceState)) type = BlockTypes.AIR.getDefaultState();

            while (baseBlock.getBlockX() != otherSide.getBlockX() || baseBlock.getBlockZ() != otherSide.getBlockZ()) {

                baseBlock.setBlock(type);

                left = baseBlock.getRelative(SignUtil.getLeft(block));

                for(int i = 0; i < leftBlocks; i++) {
                    left.setBlock(type);
                    left = left.getRelative(SignUtil.getLeft(block));
                }

                right = baseBlock.getRelative(SignUtil.getRight(block));

                for(int i = 0; i < rightBlocks; i++) {
                    right.setBlock(type);
                    right = right.getRelative(SignUtil.getRight(block));
                }

                baseBlock = baseBlock.getRelative(back);
            }
        } else {
            if (human instanceof CommandSource) ((CommandSource) human).sendMessage(Texts.builder("Bridge not activatable from here!").build());
            return false;
        }

        return true;
    }

    @Override
    public boolean isMechanicSign(Sign sign) {
        return SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Bridge]") || SignUtil.getTextRaw(sign, 1).equalsIgnoreCase("[Bridge End]");
    }

    @Override
    public String[] getValidSigns() {
        return new String[]{"[Bridge]", "[Bridge End]"};
    }
}
