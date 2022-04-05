package com.mrjoshuat.banishmentbeacon;

import com.mrjoshuat.banishmentbeacon.config.BanishmentConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModInit implements ModInitializer {
	public static final String ModID = "banishment-beacon";
	public static final Logger LOGGER = LogManager.getLogger(ModID);

	@Override
	public void onInitialize() {
		BanishmentConfig.INSTANCE.load();
	}
}
