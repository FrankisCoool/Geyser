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

package org.geysermc.connector.api.item;

import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtMapBuilder;
import com.nukkitx.protocol.bedrock.data.inventory.ComponentItemData;

import javax.annotation.Nonnull;

public abstract class AbstractCustomItemEntry extends ItemEntry {
    public AbstractCustomItemEntry(String javaIdentifier, String bedrockIdentifier, int javaId, int bedrockId, int bedrockData, boolean block) {
        super(javaIdentifier, bedrockIdentifier, javaId, bedrockId, bedrockData, block);
    }

    /**
     * @return the maximum stack size for this item.
     */
    public int getMaxStackSize() {
        return 64;
    }

    /**
     * By default, items in Bedrock cannot be put in the offhand. As this deviates from Java Edition behavior,
     * all custom items in Geyser default to allowing the offhand.
     *
     * @return whether this item is allowed in the offhand.
     */
    public boolean canPutInOffhand() {
        return true;
    }

    /**
     * @return whether the item should be held like a tool, if true.
     */
    public boolean isHandEquipped() {
        return false;
    }

    /**
     * @return if this item can be used in a bow
     */
    public boolean isArrow() {
        return false;
    }

    /**
     * @return the texture string needed to represent this item. Appears to just be the file name.
     */
    @Nonnull
    public abstract String getTextureIdentifier();

    /**
     * Builds the NBT required for Java. Not needed for external use.
     */
    public ComponentItemData build() {
        NbtMapBuilder builder = NbtMap.builder();
        builder.putString("name", getBedrockIdentifier());
        builder.putInt("id", getBedrockId());

        NbtMapBuilder componentBuilder = NbtMap.builder();

        componentBuilder.putCompound("minecraft:icon", NbtMap.builder().putString("texture", getTextureIdentifier()).build());

        NbtMapBuilder itemProperties = NbtMap.builder();
        itemProperties.putBoolean("allow_off_hand", canPutInOffhand());
        itemProperties.putBoolean("hand_equipped", isHandEquipped());
        itemProperties.putInt("max_stack_size", getMaxStackSize());

        if (isArrow()) {
            componentBuilder.putCompound("minecraft:projectile", NbtMap.builder()
                    .putString("projectile_entity", "minecraft:arrow")
                    .putFloat("minimum_critical_power", 0.5f)
            .build());
        }

        componentBuilder.putCompound("item_properties", itemProperties.build());
        builder.putCompound("components", componentBuilder.build());

        return new ComponentItemData(getBedrockIdentifier(), builder.build());
    }
}
