package com.driftnetenhanced;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DriftNetPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DriftNetPlugin.class);
		RuneLite.main(args);
	}
}
