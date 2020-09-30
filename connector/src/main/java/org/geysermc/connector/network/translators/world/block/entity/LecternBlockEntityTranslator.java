/*
 * Copyright (c) 2019-2020 GeyserMC. http://geysermc.org
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

package org.geysermc.connector.network.translators.world.block.entity;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtMapBuilder;
import com.nukkitx.nbt.NbtType;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.world.block.BlockStateValues;
import org.geysermc.connector.utils.BlockEntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@BlockEntity(name = "Lectern", regex = "lectern")
public class LecternBlockEntityTranslator extends BlockEntityTranslator implements BedrockOnlyBlockEntity, RequiresBlockState {

    @Override
    public void updateBlock(GeyserSession session, int blockState, Vector3i position) {
        if (session.getLecternBookPage() != -1) return; // Don't worry about this; it's being handled for us in LecternInventoryTranslator
        CompoundTag javaTag = getConstantJavaTag("lectern", position.getX(), position.getY(), position.getZ());
        NbtMapBuilder tagBuilder = getConstantBedrockTag(BlockEntityUtils.getBedrockBlockEntityId("lectern"),
                position.getX(), position.getY(), position.getZ()).toBuilder();
        translateTag(javaTag, blockState).forEach(tagBuilder::put);
        BlockEntityUtils.updateBlockEntity(session, tagBuilder.build(), position);
    }

    @Override
    public Map<String, Object> translateTag(CompoundTag tag, int blockState) {
        Map<String, Object> map = new HashMap<>();
        System.out.println(tag);
        boolean hasBook = BlockStateValues.getLecternValues().getOrDefault(blockState, false);
        map.put("hasBook", (byte) (hasBook ? 1 : 0));
        if (hasBook) {
            // Fun fact of the day: remove these values and you can crash the client!
            map.put("book", generateEmptyBook());
            map.put("totalPages", 3);
            map.put("page", 0);
        }
        System.out.println(map);
        return map;
    }

    @Override
    public boolean isBlock(int blockState) {
        return BlockStateValues.getLecternValues().containsKey(blockState);
    }

    private NbtMap generateEmptyBook() {
        NbtMapBuilder builder = NbtMap.builder()
                .putString("Name", "minecraft:written_book")
                .putShort("Damage", (short) 0)
                .putByte("Count", (byte) 1)
                .putCompound("tag", NbtMap.builder()
                        .putList("pages", NbtType.COMPOUND, generateEmptyPages())
                        .putString("title", "")
                        .putString("author", "").build());
        return builder.build();
    }

    private List<NbtMap> generateEmptyPages() {
        List<NbtMap> pages = new ArrayList<>(4);
        for (int i = 0; i < 2; i++) {
            pages.add(NbtMap.builder()
                    .putString("photoname", "")
                    .putString("text", "")
                    .build());
        }
        return pages;
    }
}
