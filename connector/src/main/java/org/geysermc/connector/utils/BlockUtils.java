/*
 * Copyright (c) 2019-2021 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.connector.utils;

import com.github.steveice10.mc.protocol.data.game.entity.Effect;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.nukkitx.math.vector.Vector3i;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.item.ItemEntry;
import org.geysermc.connector.network.translators.item.ToolItemEntry;
import org.geysermc.connector.network.translators.world.block.BlockTranslator;

public class BlockUtils {
    /**
     * A static constant of {@link Position} with all values being zero.
     */
    public static final Position POSITION_ZERO = new Position(0, 0, 0);

    private static boolean correctTool(String blockToolType, String itemToolType) {
        return (blockToolType.equals("sword") && itemToolType.equals("sword")) ||
                (blockToolType.equals("shovel") && itemToolType.equals("shovel")) ||
                (blockToolType.equals("pickaxe") && itemToolType.equals("pickaxe")) ||
                (blockToolType.equals("axe") && itemToolType.equals("axe")) ||
                (blockToolType.equals("shears") && itemToolType.equals("shears"));
    }

    private static double toolBreakTimeBonus(String toolType, String toolTier, boolean isWoolBlock) {
        if (toolType.equals("shears")) return isWoolBlock ? 5.0 : 15.0;
        if (toolType.isEmpty()) return 1.0;
        switch (toolTier) {
            // https://minecraft.gamepedia.com/Breaking#Speed
            case "wooden":
                return 2.0;
            case "stone":
                return 4.0;
            case "iron":
                return 6.0;
            case "diamond":
                return 8.0;
            case "netherite":
                return 9.0;
            case "golden":
                return 12.0;
            default:
                return 1.0;
        }
    }

    /**
     * @return true if this tool tier (wood, stone...) can mine this block
     */
    private static boolean canToolTierBreakBlock(int javaBlockState, String toolTier) {
        int toolTierRequired = BlockTranslator.getJavaBlockToolTier(javaBlockState);
        if (toolTierRequired == 0) {
            // Anything can break this block, as long as the correct tool is used
            return true;
        }

        switch (toolTier) {
            // See if our tool would be able to mine this block
            case "stone":
                // If tool tier isn't 1 (we already checked if it was 0), then it has to be 1 or we can't mine it
                return toolTierRequired == 1;
            case "iron":
                return toolTierRequired <= 2;
            case "diamond":
            case "netherite":
                // You can break the block at this stage
                return true;
            default:
                // wood or gold on a >0 tool tier required
                return false;
        }
    }

    // http://minecraft.gamepedia.com/Breaking
    private static double calculateBreakTime(double blockHardness, String toolTier, boolean canHarvestWithHand, boolean correctTool, boolean toolCanBreak,
                                             String toolType, boolean isWoolBlock, boolean isCobweb, int toolEfficiencyLevel, int hasteLevel, int miningFatigueLevel,
                                             boolean insideOfWaterWithoutAquaAffinity, boolean notOnGround) {
        // Apply all modifiers first
        double speed;
        if (correctTool) {
            speed = toolBreakTimeBonus(toolType, toolTier, isWoolBlock);
            speed += toolEfficiencyLevel == 0 ? 0 : toolEfficiencyLevel * toolEfficiencyLevel + 1;
        } else if (toolType.equals("sword")) {
            speed = (isCobweb ? 15.0 : 1.5);
        } else {
            speed = 1.0d;
        }
        speed *= 1.0 + (0.2 * hasteLevel);

        switch (miningFatigueLevel) {
            case 0:
                break;
            case 1:
                speed -= (speed * 0.7);
                break;
            case 2:
                speed -= (speed * 0.91);
                break;
            case 3:
                speed -= (speed * 0.9973);
                break;
            default:
                speed -= (speed * 0.99919);
                break;
        }

        if (insideOfWaterWithoutAquaAffinity) speed *= 0.2;
        if (notOnGround) speed *= 0.2;

        // "The base time in seconds is the block's hardness multiplied by 1.5 if the player can harvest the block with the current tool, or 5 if the player cannot." - Minecraft Wiki
        double baseTime = ((((correctTool && toolCanBreak) || canHarvestWithHand) ? 1.5 : 5.0) * blockHardness) * 20;

//        System.out.println("(1.0 / (" + speed + " / (" + (((correctTool && toolCanBreak) || canHarvestWithHand) ? 1.5 : 5.0) + " * " +
//                blockHardness + "))) * 20 = " + (1.0 / (speed / baseTime)) * 20);
        return 1.0 / (1.0 / baseTime) / speed;
    }

    public static double getBreakTime(double blockHardness, int blockId, ItemEntry item, CompoundTag nbtData, GeyserSession session) {
        boolean isWoolBlock = BlockTranslator.JAVA_RUNTIME_WOOL_IDS.contains(blockId);
        boolean isCobweb = blockId == BlockTranslator.JAVA_RUNTIME_COBWEB_ID;
        String blockToolType = BlockTranslator.JAVA_RUNTIME_ID_TO_TOOL_TYPE.getOrDefault(blockId, "");
        boolean canHarvestWithHand = BlockTranslator.JAVA_RUNTIME_ID_TO_CAN_HARVEST_WITH_HAND.get(blockId);
        String toolType = "";
        String toolTier = "";
        boolean correctTool = false;
        boolean toolCanBreak = false;
        if (item instanceof ToolItemEntry) {
            ToolItemEntry toolItem = (ToolItemEntry) item;
            toolType = toolItem.getToolType();
            toolTier = toolItem.getToolTier();
            correctTool = correctTool(blockToolType, toolType);

            if (correctTool) {
                toolCanBreak = canToolTierBreakBlock(blockId, toolTier);
            }
        }
        int toolEfficiencyLevel = ItemUtils.getEnchantmentLevel(nbtData, "minecraft:efficiency");
        int hasteLevel = 0;
        int miningFatigueLevel = 0;

        if (session == null) {
            return calculateBreakTime(blockHardness, toolTier, canHarvestWithHand, correctTool, toolCanBreak, toolType, isWoolBlock, isCobweb, toolEfficiencyLevel, hasteLevel, miningFatigueLevel, false, false);
        }

        System.out.println(session.getEffectCache().getEffectLevel(Effect.FASTER_DIG));
        hasteLevel = Math.max(session.getEffectCache().getEffectLevel(Effect.FASTER_DIG), session.getEffectCache().getEffectLevel(Effect.CONDUIT_POWER));
        miningFatigueLevel = session.getEffectCache().getEffectLevel(Effect.SLOWER_DIG);

        boolean isInWater = session.getConnector().getConfig().isCacheChunks()
                && session.getBlockTranslator().getBedrockBlockId(session.getConnector().getWorldManager().getBlockAt(session, session.getPlayerEntity().getPosition().toInt())) == session.getBlockTranslator().getBedrockWaterId();

        boolean insideOfWaterWithoutAquaAffinity = isInWater &&
                ItemUtils.getEnchantmentLevel(session.getPlayerInventory().getItem(5).getNbt(), "minecraft:aqua_affinity") < 1;

        boolean notOnGround = (!isInWater) && (!session.getPlayerEntity().isOnGround());
        return calculateBreakTime(blockHardness, toolTier, canHarvestWithHand, correctTool, toolCanBreak, toolType, isWoolBlock, isCobweb, toolEfficiencyLevel, hasteLevel, miningFatigueLevel, insideOfWaterWithoutAquaAffinity, notOnGround);
    }

    /**
     * Given a position, return the position if a block were located on the specified block face.
     * @param blockPos the block position
     * @param face the face of the block - see {@link com.github.steveice10.mc.protocol.data.game.world.block.BlockFace}
     * @return the block position with the block face accounted for
     */
    public static Vector3i getBlockPosition(Vector3i blockPos, int face) {
        switch (face) {
            case 0:
                return blockPos.sub(0, 1, 0);
            case 1:
                return blockPos.add(0, 1, 0);
            case 2:
                return blockPos.sub(0, 0, 1);
            case 3:
                return blockPos.add(0, 0, 1);
            case 4:
                return blockPos.sub(1, 0, 0);
            case 5:
                return blockPos.add(1, 0, 0);
        }
        return blockPos;
    }

}
