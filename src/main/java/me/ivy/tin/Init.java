package me.ivy.tin;

import net.fabricmc.api.ModInitializer;

public class Init implements ModInitializer {
	@Override
	public void onInitialize() {
		Tin.getInstance().init();
    }
}