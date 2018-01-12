/*
 * Copyright (c) 2018 Adrian Siekierka
 *
 * This file is part of Faithless.
 *
 * Faithless is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Faithless is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Faithless.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.faithless;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProxyClient extends ProxyCommon {
	private static final Set<TextureAtlasSprite> spriteSet = new HashSet<>();
	private static int multiplier;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onModelBake(ModelBakeEvent event) {
		// Clear caches
		spriteSet.clear();
	}

	public static int getExistingMultiplier(TextureAtlasSprite sprite) {
		ResourceLocation loc = new ResourceLocation(sprite.getIconName());
		loc = new ResourceLocation(loc.getResourceDomain(), "textures/" + loc.getResourcePath() + ".png");
		try {
			List<IResource> resourceList = Minecraft.getMinecraft().getResourceManager().getAllResources(loc);
			if (resourceList.size() > 1) {
				PngSizeInfo first = PngSizeInfo.makeFromResource(resourceList.get(0));
				PngSizeInfo second = PngSizeInfo.makeFromResource(resourceList.get(resourceList.size() - 1));
				int multiplierTmp = second.pngWidth / first.pngWidth;
				if (second.pngWidth % first.pngWidth != 0 || MathHelper.smallestEncompassingPowerOfTwo(multiplierTmp) != multiplierTmp) {
					return 1;
				} else {
					return multiplierTmp;
				}
			} else {
				return 1;
			}
		} catch (IOException e) {
			return 1;
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTextureStitchPre(TextureStitchEvent.Pre event) {
		try {
			int multiplierTmp = Faithless.multiplier;
			if (multiplierTmp <= 0) {
				IResource cobblestone = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft:textures/blocks/cobblestone.png"));
				PngSizeInfo cobblestoneInfo = PngSizeInfo.makeFromResource(cobblestone);
				multiplierTmp = cobblestoneInfo.pngWidth / 16;
				if (cobblestoneInfo.pngWidth % 16 != 0) {
					Faithless.logger.error("Cobblestone width multiplier not a power of two (" + cobblestoneInfo.pngWidth + " / 16), cannot proceed further! Setting multiplier to 1.");
					multiplierTmp = 1;
				}
			}

			if (multiplierTmp <= 0 || MathHelper.smallestEncompassingPowerOfTwo(multiplierTmp) != multiplierTmp) {
				Faithless.logger.error("Multiplier set to non-power-of-two or <= 0 (" + multiplierTmp + "), cannot proceed further! Setting multiplier to 1.");
				multiplier = 1;
			} else {
				multiplier = multiplierTmp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void processTexture(TextureAtlasSprite sprite) {
		if (multiplier == 1 || spriteSet.contains(sprite)) {
			return;
		}

		spriteSet.add(sprite);

		if (sprite.getClass() == TextureAtlasSprite.class || sprite.getClass().getName().startsWith("pl.asie.foamfix") /* hue hue hue */) {
			boolean wrap = false;
			ResourceLocation loc = new ResourceLocation(sprite.getIconName());
			if (loc.getResourcePath().startsWith("blocks/")) {
				wrap = true;
			}

			int ow = sprite.getIconWidth();
			int oh = sprite.getIconHeight();
			int m = multiplier / getExistingMultiplier(sprite);
			if (m > 1) {
				sprite.setIconWidth(ow * m);
				sprite.setIconHeight(oh * m);
				for (int i = 0; i < sprite.getFrameCount(); i++) {
					int[][] data = sprite.getFrameTextureData(i);
					if (data.length > 0 && data[0] != null) {
						int mm = m;
						while (mm > 1) {
							data[0] = Scale2x.scale(data[0], ow * m / mm, oh * m / mm, wrap);
							mm >>= 1;
						}
					}
				}
			}
		}
	}
}
