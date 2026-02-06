package com.driftnetenhanced;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DriftNetImprovedPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DriftNetImprovedPlugin.class);
		RuneLite.main(args);
	}
}
