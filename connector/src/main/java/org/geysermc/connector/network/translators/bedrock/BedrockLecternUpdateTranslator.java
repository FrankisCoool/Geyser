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

import com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientClickWindowButtonPacket;
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

        // Page = 0 means in most cases that the client wants to close the window
        if (session.getInventoryCache().getOpenInventory() != null && packet.getPage() == 0) {
            InventoryUtils.closeInventory(session, session.getInventoryCache().getOpenInventory().getId());
            session.setLecternBookPage(-1);
        } else if (session.getInventoryCache().getOpenInventory() != null) {
            ClientClickWindowButtonPacket buttonPacket = new ClientClickWindowButtonPacket(
                    session.getInventoryCache().getOpenInventory().getId(),
                    100 + session.getLecternBookPage());
            session.sendDownstreamPacket(buttonPacket);
            session.setLecternBookPage(packet.getPage());
        }
    }
}
