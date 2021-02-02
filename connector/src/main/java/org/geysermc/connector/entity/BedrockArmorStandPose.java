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

package org.geysermc.connector.entity;

import com.nukkitx.math.vector.Vector3f;
import lombok.Getter;

/**
 * All poses that Bedrock armor stands can be set to.
 * Credit to this link for finding the pose values: https://www.curseforge.com/minecraft/customization/armor-stand-poses-datapack
 */
@Getter
public enum BedrockArmorStandPose {
    DEFAULT(Vector3f.ZERO, Vector3f.ZERO, Vector3f.from(-10f, 0f, -10f),
            Vector3f.from(-1f, 0f, -1f), Vector3f.from(-15f, 0f, 10f), Vector3f.from(1f, 0f, 1f)),
    NONE(Vector3f.ZERO, Vector3f.ZERO, Vector3f.ZERO,
            Vector3f.ZERO, Vector3f.ZERO, Vector3f.ZERO),
    SOLEMN(Vector3f.from(0f, 0f, 2f), Vector3f.from(15f, 0f, 0f), Vector3f.from(-30f, 15f, 15f),
            Vector3f.from(-1f, 0f, -1f), Vector3f.from(-60f, -20f, -10f), Vector3f.from(1f, 0f, 1f)),
    ATHENA(Vector3f.from(0f, 0f, 2f), Vector3f.from(-5f, 0f, 0f), Vector3f.from(10f, 0f, -5f),
            Vector3f.from(-3f, -3f, -3f), Vector3f.from(-60f, 20f, -10f), Vector3f.from(3f, 3f, 3f)),
    BRANDISH(Vector3f.from(0f, 0f, -2f), Vector3f.from(-15f, 0f, 0f), Vector3f.from(20f, 0f, -10f),
            Vector3f.from(5f, -3f, -3f), Vector3f.from(-110f, 50f, 0f), Vector3f.from(-5f, 3f, 3f)),
    HONOR(Vector3f.ZERO, Vector3f.from(-15f, 0f, 0f), Vector3f.from(-110f, 35f, 0f),
            Vector3f.from(5f, -3f, -3f), Vector3f.from(-110f, -35f, 0f), Vector3f.from(-5f, 3f, 3f)),
    ENTERTAIN(Vector3f.ZERO, Vector3f.from(-15f, 0f, 0f), Vector3f.from(-110f, -35f, 0f),
            Vector3f.from(5f, -3f, -3f), Vector3f.from(-110f, 35f, 0f), Vector3f.from(-5f, 3f, 3f)),
    SALUTE(Vector3f.ZERO, Vector3f.ZERO, Vector3f.from(10f, 0f, -5f),
           Vector3f.from(-1f, 0f, -1f), Vector3f.from(-70f, -40f, 0f), Vector3f.from(1f, 0f, 1f)),
    RIPOSTE(Vector3f.ZERO, Vector3f.from(16f, 20f, 0f), Vector3f.from(4f, 8f, 237f),
            Vector3f.from(-14f, -18f, -16f), Vector3f.from(246f, 0f, 89f), Vector3f.from(8f, 20f, 4f)),
    ZOMBIE(Vector3f.ZERO, Vector3f.from(-10f, 0f, -5f), Vector3f.from(-105f, 0f, 0f),
            Vector3f.from(7f, 0f, 0f), Vector3f.from(-100f, 0f, 0f), Vector3f.from(-46f, 0f, 0f)),
    CANCAN_A(Vector3f.from(0f, 22f, 0f), Vector3f.from(-5f, 18f, 0f), Vector3f.from(8f, 0f, -114f),
            Vector3f.from(-111f, 55f, 0f), Vector3f.from(0f, 84f, -111f), Vector3f.from(0f, 23f, -13f)),
    CANCAN_B(Vector3f.from(0f, -18f, 0f), Vector3f.from(-10f, -20f, 0f), Vector3f.from(0f, 0f, -112f),
            Vector3f.from(0f, 0f, 13f), Vector3f.from(8f, 90f, 111f), Vector3f.from(-119f, -42f, 0f)),
    HERO(Vector3f.from(0f, 8f, 0f), Vector3f.from(-4f, 67f, 0f), Vector3f.from(16f, 32f, -8f),
            Vector3f.from(0f, -75f, -8f), Vector3f.from(-99f, 63f, 0f), Vector3f.from(4f, 63f, 8f));

    private final Vector3f body;
    private final Vector3f head;
    private final Vector3f leftArm;
    private final Vector3f leftLeg;
    private final Vector3f rightArm;
    private final Vector3f rightLeg;

    BedrockArmorStandPose(Vector3f body, Vector3f head, Vector3f leftArm, Vector3f leftLeg, Vector3f rightArm, Vector3f rightLeg) {
        this.body = body;
        this.head = head;
        this.leftArm = leftArm;
        this.leftLeg = leftLeg;
        this.rightArm = rightArm;
        this.rightLeg = rightLeg;
    }
}
