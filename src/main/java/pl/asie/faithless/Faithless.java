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
import net.minecraft.client.resources.IResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(
		modid = Faithless.MODID,
		name = "Faithless",
		version = Faithless.VERSION,
		clientSideOnly = true,
		acceptableRemoteVersions = "*",
		guiFactory = "pl.asie.faithless.FaithlessConfigGuiFactory"
)
public class Faithless {
	public static final String MODID = "faithless";
	public static final String VERSION = "@VERSION@";

	@SidedProxy(serverSide = "pl.asie.faithless.ProxyCommon", clientSide = "pl.asie.faithless.ProxyClient")
	public static ProxyCommon proxy;
	public static Logger logger;
	public static Configuration config;
	public static int multiplier;
	public static float scale2xEqualityBound;
	public static IScalingAlgorithm algorithm;

	private static Map<String, IScalingAlgorithm> algorithmMap = new HashMap<>();

	@SubscribeEvent
	public void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (MODID.equals(event.getModID())) {
			updateConfigFields();
			Minecraft.getMinecraft().scheduleResourcesRefresh();
		}
	}

	private String[] getValidAlgorithmNames() {
		return algorithmMap.keySet().toArray(new String[algorithmMap.size()]);
	}

	private void updateConfigFields() {
		multiplier = config.getInt("textureSizeMultiplier", "general", 0, 0, 256, "The texture size multiplier, where 1 = 16x. 0 means it's calculated automatically. Don't set it to a non-power-of-two!");
		scale2xEqualityBound = config.getFloat("pixelEqualityThreshold", "general", 0.02f, 0.0f, 1.0f, "The threshold of equality between two pixels for the Scale2x algorithm.");
		String algorithmName = config.getString("scalingAlgorithm", "general", "scale2x", "The scaling algorithm to use.", getValidAlgorithmNames());

		scale2xEqualityBound = scale2xEqualityBound * scale2xEqualityBound;
		algorithm = algorithmMap.get(algorithmName);

		if (config.hasChanged()) {
			config.save();
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		algorithmMap.put("scale2x", Scale2x::scale);
		algorithmMap.put("scale2xSFX", Scale2xSFX::scale);

		logger = LogManager.getLogger(MODID);

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(proxy);

		config = new Configuration(event.getSuggestedConfigurationFile());
		updateConfigFields();

		for (Property property : Faithless.config.getCategory("general").values()) {
			property.setRequiresMcRestart(false);
			property.setRequiresWorldRestart(false);
		}
	}

	private static int[] asArray(int v) {
		return new int[] {
				((v >> 16) & 0xFF),
				((v >> 8) & 0xFF),
				(v & 0xFF)
		};
	}

	public static boolean compareEqual(int one, int two) {
		if ((one & 0xFF000000) != (two & 0xFF000000)) {
			return false;
		}

		if (scale2xEqualityBound < 1e-6f) {
			return (one & 0xFFFFFF) == (two & 0xFFFFFF);
		}

		int[] oneA = asArray(one);
		int[] twoA = asArray(two);
		int rMean = (oneA[0] + twoA[0]) >> 1;
		int rDiff = oneA[0] - twoA[0];
		int gDiff = oneA[1] - twoA[1];
		int bDiff = oneA[2] - twoA[2];
		float diffSq = (((512 + rMean) * rDiff * rDiff) >> 8) + 4 * gDiff * gDiff + (((767 - rMean) * bDiff * bDiff) >> 8) / (765.0f * 765.0f);
		return diffSq < Faithless.scale2xEqualityBound;
	}

	public static int get(int[] a, int x, int y, int w, int h, boolean wrap) {
		if (wrap) {
			if (x < 0) x += w;
			else if (x >= w) x -= w;
			if (y < 0) y += h;
			else if (y >= h) y -= h;
		} else {
			if (x < 0 || x >= w || y < 0 || y >= h) {
				return 0;
			}
		}
		return a[y*w+x];
	}

	public static void set(int[] a, int x, int y, int w, int h, int value) {
		if (x >= 0 && x < w && y >= 0 || y < h) {
			a[y*w+x] = value;
		}
	}
}
