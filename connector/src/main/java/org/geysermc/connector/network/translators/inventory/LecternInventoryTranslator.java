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

package org.geysermc.connector.network.translators.inventory;

import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtMapBuilder;
import com.nukkitx.nbt.NbtType;
import com.nukkitx.protocol.bedrock.data.inventory.InventoryActionData;
import com.nukkitx.protocol.bedrock.data.inventory.ItemData;
import org.geysermc.connector.inventory.Inventory;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.item.ItemTranslator;
import org.geysermc.connector.utils.BlockEntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The lectern is a fully-fledged inventory in Java. Bedrock just opens it lol
 */
public class LecternInventoryTranslator extends InventoryTranslator {

    private static final NbtMap EMPTY_PAGE = NbtMap.builder()
            .putString("photoname", "")
            .putString("text", "")
            .build();

    public LecternInventoryTranslator() {
        super(0);
    }

    @Override
    public void prepareInventory(GeyserSession session, Inventory inventory) {
        session.getConnector().getLogger().warning("Preparing inventory");
    }

    @Override
    public void openInventory(GeyserSession session, Inventory inventory) {
        session.setLecternBookPage(0);
        session.getConnector().getLogger().warning("Opening inventory");
        updateBook(session, inventory);
    }

    @Override
    public void closeInventory(GeyserSession session, Inventory inventory) {

    }

    @Override
    public void updateProperty(GeyserSession session, Inventory inventory, int key, int value) {
        session.getConnector().getLogger().warning("Property " + key + " " + value);
        updateBook(session, inventory);
    }

    @Override
    public void updateInventory(GeyserSession session, Inventory inventory) {
        session.getConnector().getLogger().warning("Updating inventory");
        updateBook(session, inventory);
    }

    @Override
    public void updateSlot(GeyserSession session, Inventory inventory, int slot) {
        session.getConnector().getLogger().warning("Updating slot " + slot);
        updateBook(session, inventory);
    }

    @Override
    public int bedrockSlotToJava(InventoryActionData action) {
        return action.getSlot();
    }

    @Override
    public int javaSlotToBedrock(int slot) {
        return slot;
    }

    @Override
    public SlotType getSlotType(int javaSlot) {
        return SlotType.NORMAL;
    }

    @Override
    public void translateActions(GeyserSession session, Inventory inventory, List<InventoryActionData> actions) {
        session.getConnector().getLogger().warning("Translating actions");
    }

    private void updateBook(GeyserSession session, Inventory inventory) {
        //System.out.println("Updating book...");
        if (inventory.getItem(0) == null) return;
        Vector3i lastInteractionPosition = session.getLastInteractionPosition();
        ItemData item = ItemTranslator.translateToBedrock(session, inventory.getItem(0));
        ArrayList<NbtMap> pages = new ArrayList<>(item.getTag().getList("pages", NbtType.COMPOUND));
        pages.add(0, EMPTY_PAGE);
        pages.add(0, EMPTY_PAGE);
        item = ItemData.of(item.getId(), item.getDamage(), item.getCount(), item.getTag().toBuilder().putList("pages", NbtType.COMPOUND, pages).build());
        NbtMapBuilder builder = NbtMap.builder()
                .putInt("x", lastInteractionPosition.getX())
                .putInt("y", lastInteractionPosition.getY())
                .putInt("z", lastInteractionPosition.getZ())
                .putString("id", "Lectern")
                .putByte("hasBook", (byte) 1)
                .putInt("page", session.getLecternBookPage() + 1) // Page appears to have to do with both pages (I.E. setting 1 here actually shows pages 3 and 4)
                .putInt("totalPages", pages.size()) // Total pages appears to have to do with the total amount of pages
                .putCompound("book", NbtMap.builder()
                        .putByte("Count", (byte) item.getCount())
                        .putShort("Damage", item.getDamage())
                        .putString("Name", "minecraft:written_book") // It can't be anything else... right?
                        .putCompound("tag", item.getTag()).build());
        System.out.println(builder.build());
        BlockEntityUtils.updateBlockEntity(session, builder.build(), lastInteractionPosition);
        pages.add(0, EMPTY_PAGE);
        pages.add(0, EMPTY_PAGE);
        item = ItemData.of(item.getId(), item.getDamage(), item.getCount(), item.getTag().toBuilder().putList("pages", NbtType.COMPOUND, pages).build());
        builder = NbtMap.builder()
                .putInt("x", lastInteractionPosition.getX())
                .putInt("y", lastInteractionPosition.getY())
                .putInt("z", lastInteractionPosition.getZ())
                .putString("id", "Lectern")
                .putByte("hasBook", (byte) 1)
                .putInt("page", session.getLecternBookPage() + 2) // Page appears to have to do with both pages (I.E. setting 1 here actually shows pages 3 and 4)
                .putInt("totalPages", pages.size()) // Total pages appears to have to do with the total amount of pages
                .putCompound("book", NbtMap.builder()
                        .putByte("Count", (byte) item.getCount())
                        .putShort("Damage", item.getDamage())
                        .putString("Name", "minecraft:written_book") // It can't be anything else... right?
                        .putCompound("tag", item.getTag()).build());
        System.out.println(builder.build());
        BlockEntityUtils.updateBlockEntity(session, builder.build(), lastInteractionPosition);
    }
}
