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

package org.geysermc.connector.network.translators.bedrock;

import com.nukkitx.protocol.bedrock.packet.LecternUpdatePacket;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.PacketTranslator;
import org.geysermc.connector.network.translators.Translator;
import org.geysermc.connector.utils.InventoryUtils;

@Translator(packet = LecternUpdatePacket.class)
public class BedrockLecternUpdateTranslator extends PacketTranslator<LecternUpdatePacket> {

    @Override
    public void translate(LecternUpdatePacket packet, GeyserSession session) {
        session.getConnector().getLogger().warning(packet.toString());
        LecternUpdatePacket lecternPacket = new LecternUpdatePacket();
        lecternPacket.setPage(packet.getPage());
        lecternPacket.setTotalPages(packet.getTotalPages());
        lecternPacket.setDroppingBook(packet.isDroppingBook());
        lecternPacket.setBlockPosition(packet.getBlockPosition());
        session.sendUpstreamPacket(packet);
//        if (packet.getPage() == 0) {
//            InventoryUtils.closeInventory(session, session.getInventoryCache().getOpenInventory().getId());
//        }
        if (packet.isDroppingBook()) {
//            int windowId = session.getInventoryCache().getOpenInventory().getId();
//            ClientClickWindowButtonPacket buttonPacket = new ClientClickWindowButtonPacket(windowId, 3);
//            session.sendDownstreamPacket(buttonPacket);
        }
//        Vector3i position = session.getPlayerEntity().getPosition().toInt().add(Vector3i.UP);
//        UpdateBlockPacket updateBlockPacket = new UpdateBlockPacket();
//        updateBlockPacket.setDataLayer(0);
//        updateBlockPacket.setRuntimeId(BlockTranslator.getBedrockBlockId(BlockTranslator.getJavaIdBlockMap().get("minecraft:lectern[facing=north,has_book=true,powered=false]")));
//        updateBlockPacket.getFlags().addAll(UpdateBlockPacket.FLAG_ALL_PRIORITY);
//        updateBlockPacket.setBlockPosition(position);
//        session.sendUpstreamPacket(updateBlockPacket);
//        NbtMapBuilder builder = NbtMap.builder()
//                .putInt("x", position.getX())
//                .putInt("y", position.getY())
//                .putInt("z", position.getZ())
//                .putString("id", "Lectern")
//                .putByte("hasBook", (byte) 1)
//                .putInt("page", 0) // Page appears to have to do with both pages
//                .putInt("totalPages", 0) // Total pages appears to have to do with the total amount of pages per book half
//                .putCompound("book", generateEmptyBook());
//        BlockEntityUtils.updateBlockEntity(session, builder.build(), position);
//        LecternUpdatePacket lecternPacket = new LecternUpdatePacket();
//        lecternPacket.setPage(0);
//        lecternPacket.setTotalPages(0);
//        lecternPacket.setDroppingBook(false);
//        lecternPacket.setBlockPosition(position);
//        session.sendUpstreamPacket(lecternPacket);
        if (session.getInventoryCache().getOpenInventory() != null) {
            InventoryUtils.closeInventory(session, session.getInventoryCache().getOpenInventory().getId());
        }
    }
}
