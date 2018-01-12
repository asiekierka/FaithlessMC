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

	@SubscribeEvent
	public void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (MODID.equals(event.getModID())) {
			updateConfigFields();
			Minecraft.getMinecraft().scheduleResourcesRefresh();
		}
	}

	private void updateConfigFields() {
		multiplier = config.getInt("textureSizeMultiplier", "general", 0, 0, 256, "The texture size multiplier, where 1 = 16x. 0 means it's calculated automatically. Don't set it to a non-power-of-two!");

		if (config.hasChanged()) {
			config.save();
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
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
}
