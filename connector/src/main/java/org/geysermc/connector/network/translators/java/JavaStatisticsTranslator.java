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

package org.geysermc.connector.network.translators.java;

import com.github.steveice10.mc.protocol.data.game.statistic.GenericStatistic;
import com.github.steveice10.mc.protocol.data.game.statistic.Statistic;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerStatisticsPacket;
import com.nukkitx.protocol.bedrock.packet.ModalFormRequestPacket;
import org.geysermc.common.window.CustomFormBuilder;
import org.geysermc.common.window.component.LabelComponent;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.PacketTranslator;
import org.geysermc.connector.network.translators.Translator;
import org.geysermc.connector.utils.LocaleUtils;

import java.util.Map;

@Translator(packet = ServerStatisticsPacket.class)
public class JavaStatisticsTranslator extends PacketTranslator<ServerStatisticsPacket> {

    @Override
    public void translate(ServerStatisticsPacket packet, GeyserSession session) {
        System.out.println(packet.toString());
        String languageCode = session.getClientData().getLanguageCode();
        CustomFormBuilder builder = new CustomFormBuilder(LocaleUtils.getLocaleString("gui.stats", languageCode));
        for (Map.Entry<Statistic, Integer> entry : packet.getStatistics().entrySet()) {
            Statistic statistic = entry.getKey();

            if (statistic instanceof GenericStatistic) {
                GenericStatistic genericStatistic = (GenericStatistic) statistic;
                String fullKey = "stat.minecraft." + genericStatistic.toString().toLowerCase();
                builder.addComponent(new LabelComponent(LocaleUtils.getLocaleString(fullKey, languageCode) + ": " + entry.getValue().toString()));
            }
        }
        ModalFormRequestPacket formRequestPacket = new ModalFormRequestPacket();
        formRequestPacket.setFormId(6969);
        formRequestPacket.setFormData(builder.build().getJSONData());

        session.sendUpstreamPacket(formRequestPacket);
    }
}
