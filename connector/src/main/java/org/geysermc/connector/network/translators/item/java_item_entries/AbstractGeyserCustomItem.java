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

package org.geysermc.connector.network.translators.item.java_item_entries;

import org.geysermc.connector.api.item.AbstractCustomItemEntry;
import org.geysermc.connector.api.item.NeedsTranslation;
import org.geysermc.connector.utils.LocaleUtils;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public abstract class AbstractGeyserCustomItem extends AbstractCustomItemEntry implements NeedsTranslation {
    public AbstractGeyserCustomItem(String javaIdentifier, String bedrockIdentifier, int javaId, int bedrockId, int bedrockData, boolean block) {
        super(javaIdentifier, bedrockIdentifier, javaId, bedrockId, bedrockData, block);
    }

    @Nonnull
    @Override
    public BiFunction<String, String, String> getTranslationSystem() {
        return LocaleUtils::getLocaleString;
    }
}
