/*
 * Copyright (c) 2020, dekvall <https://github.com/dekvall>
 * Copyright (c) 2026, Zipon <https://github.com/zipon458>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.driftnetenhanced;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.Skill;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.Notifier;
import net.runelite.client.config.Notification;

@PluginDescriptor(
	name = "Drift Net Improved",
	description = "Drift net plugin with additional features",
	tags = {"hunter", "fishing", "drift", "net"}
)
public class DriftNetImprovedPlugin extends Plugin
{
	static final String CONFIG_GROUP = "driftnetenhanced";
	private static final int UNDERWATER_REGION = 15008;
	private static final String CHAT_PRODDING_FISH = "You prod at the shoal of fish to scare it.";

	@Inject
	@Getter
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private DriftNetConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DriftNetImprovedOverlay overlay;

	@Inject
	private DriftNetImprovedStatsOverlay statsOverlay;

	@Inject
	private DriftNetImprovedInventoryOverlay inventoryOverlay;

	@Inject
	private Notifier notifier;

	@Inject
	private ConfigManager configManager;

	@Getter
	private final Set<NPC> fish = new HashSet<>();
	@Getter
	private final Map<NPC, Integer> taggedFish = new HashMap<>();
	@Getter
	private final List<DriftNet> NETS = ImmutableList.of(
		new DriftNet(ObjectID.FOSSIL_DRIFT_NET1_MULTI, VarbitID.FOSSIL_DRIFT_NET1, VarbitID.FOSSIL_DRIFT_NET1_CATCH, ImmutableSet.of(
			new WorldPoint(3746, 10297, 1),
			new WorldPoint(3747, 10297, 1),
			new WorldPoint(3748, 10297, 1),
			new WorldPoint(3749, 10297, 1)
		)),
		new DriftNet(ObjectID.FOSSIL_DRIFT_NET2_MULTI, VarbitID.FOSSIL_DRIFT_NET2, VarbitID.FOSSIL_DRIFT_NET2_CATCH, ImmutableSet.of(
			new WorldPoint(3742, 10288, 1),
			new WorldPoint(3742, 10289, 1),
			new WorldPoint(3742, 10290, 1),
			new WorldPoint(3742, 10291, 1),
			new WorldPoint(3742, 10292, 1)
		)));

	@Getter
	private boolean inDriftNetArea;
	private boolean armInteraction;

	@Getter
	private boolean driftNetsInInventory;

	@Getter
	private GameObject annette;

	// Statistics tracking
	@Getter
	private int totalFishCaught;
	@Getter
	private int sessionFishCaught;
	@Getter
	private long sessionStartTime;
	private int lastHunterXp;

	private static final double MIN_XP = 72.0;
	private static final double MAX_XP = 101.5;
	private static final int MIN_LEVEL = 50;
	private static final int MAX_LEVEL = 70;
	private static final double XP_PER_LEVEL = (MAX_XP - MIN_XP) / (MAX_LEVEL - MIN_LEVEL);

	@Provides
	DriftNetConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DriftNetConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(statsOverlay);
		overlayManager.add(inventoryOverlay);
		sessionStartTime = System.currentTimeMillis();

		Integer savedTotal = configManager.getConfiguration(CONFIG_GROUP, "totalFishCaught", Integer.class);
		totalFishCaught = (savedTotal != null) ? savedTotal : 0;

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				inDriftNetArea = checkArea();
				updateDriftNetVarbits();
				lastHunterXp = client.getSkillExperience(Skill.HUNTER);
			});
		}
	}

	@Override
	protected void shutDown()
	{
		// Save total fish count to config
		configManager.setConfiguration(CONFIG_GROUP, "totalFishCaught", totalFishCaught);

		overlayManager.remove(overlay);
		overlayManager.remove(statsOverlay);
		overlayManager.remove(inventoryOverlay);
		reset();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
			case LOADING:
				annette = null;
				reset();
				break;
			case LOGGED_IN:
				inDriftNetArea = checkArea();
				updateDriftNetVarbits();
				break;
		}
	}

	private void reset()
	{
		fish.clear();
		taggedFish.clear();
		armInteraction = false;
		inDriftNetArea = false;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		updateDriftNetVarbits();
	}

	private void updateDriftNetVarbits()
	{
		if (!inDriftNetArea)
		{
			return;
		}

		for (DriftNet net : NETS)
		{
			DriftNetStatus oldStatus = net.getStatus();
			DriftNetStatus status = DriftNetStatus.of(client.getVarbitValue(net.getStatusVarbit()));
			int count = client.getVarbitValue(net.getCountVarbit());

			net.setStatus(status);
			net.setCount(count);

			if (config.notifyNetFull() != Notification.OFF && oldStatus != DriftNetStatus.FULL && status == DriftNetStatus.FULL)
			{
				notifier.notify(config.notifyNetFull(), "A drift net is full!");
			}
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (armInteraction
			&& event.getSource() == client.getLocalPlayer()
			&& event.getTarget() instanceof NPC
			&& ((NPC) event.getTarget()).getId() == NpcID.FOSSIL_FISH_SHOAL)
		{
			tagFish(event.getTarget());
			armInteraction = false;
		}
	}

	private boolean isFishNextToNet(NPC fish, Collection<DriftNet> nets)
	{
		if (nets.isEmpty())
		{
			return false;
		}
		final WorldPoint fishTile = WorldPoint.fromLocalInstance(client, fish.getLocalLocation());
		for (DriftNet net : nets)
		{
			if (net.getAdjacentTiles().contains(fishTile))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isTagExpired(Integer tick)
	{
		return tick + config.timeoutDelay() <= client.getTickCount();
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (!inDriftNetArea)
		{
			return;
		}

		if (!taggedFish.isEmpty())
		{
			List<DriftNet> closedNets = null;
			for (DriftNet net : NETS)
			{
				if (net.isNotAcceptingFish())
				{
					if (closedNets == null)
					{
						closedNets = new java.util.ArrayList<>(2);
					}
					closedNets.add(net);
				}
			}

			if (closedNets != null)
			{
				final List<DriftNet> finalClosedNets = closedNets;
				taggedFish.entrySet().removeIf(entry ->
					isTagExpired(entry.getValue()) ||
					isFishNextToNet(entry.getKey(), finalClosedNets)
				);
			}
			else
			{
				taggedFish.entrySet().removeIf(entry -> isTagExpired(entry.getValue()));
			}
		}

		for (DriftNet net : NETS)
		{
			net.setPrevTickStatus(net.getStatus());
		}

		armInteraction = false;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!inDriftNetArea)
		{
			return;
		}

		if (event.getType() == ChatMessageType.SPAM && event.getMessage().equals(CHAT_PRODDING_FISH))
		{
			Actor target = client.getLocalPlayer().getInteracting();

			if (target instanceof NPC && ((NPC) target).getId() == NpcID.FOSSIL_FISH_SHOAL)
			{
				tagFish(target);
			}
			else
			{
				// If the fish is on an adjacent tile, the interaction change happens after
				// the chat message is sent, so we arm it
				armInteraction = true;
			}
		}
	}

	private void tagFish(Actor fish)
	{
		NPC fishTarget = (NPC) fish;
		taggedFish.put(fishTarget, client.getTickCount());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		if (npc.getId() == NpcID.FOSSIL_FISH_SHOAL)
		{
			fish.add(npc);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		final NPC npc = event.getNpc();
		fish.remove(npc);
		taggedFish.remove(npc);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject object = event.getGameObject();
		int objectId = object.getId();

		if (objectId == ObjectID.FOSSIL_MERMAID_DRIFTNETS)
		{
			annette = object;
			return;
		}

		if (objectId != ObjectID.FOSSIL_DRIFT_NET1_MULTI && objectId != ObjectID.FOSSIL_DRIFT_NET2_MULTI)
		{
			return;
		}

		for (DriftNet net : NETS)
		{
			if (net.getObjectId() == objectId)
			{
				net.setNet(object);
				break;
			}
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject object = event.getGameObject();
		if (object == annette)
		{
			annette = null;
			return;
		}

		int objectId = object.getId();

		if (objectId != ObjectID.FOSSIL_DRIFT_NET1_MULTI && objectId != ObjectID.FOSSIL_DRIFT_NET2_MULTI)
		{
			return;
		}

		for (DriftNet net : NETS)
		{
			if (net.getObjectId() == objectId)
			{
				net.setNet(null);
				break;
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event)
	{
		final ItemContainer itemContainer = event.getItemContainer();
		if (itemContainer != client.getItemContainer(InventoryID.INV))
		{
			return;
		}

		boolean hadNets = driftNetsInInventory;
		driftNetsInInventory = itemContainer.contains(ItemID.FOSSIL_DRIFT_NET);

		if (config.notifyOutOfNets() != Notification.OFF && inDriftNetArea && hadNets && !driftNetsInInventory)
		{
			notifier.notify(config.notifyOutOfNets(), "You are out of drift nets!");
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!inDriftNetArea)
		{
			return;
		}

		// Hide "Take down" option from drift nets
		if (config.hideTakeDown() && event.getOption().equals("Take down") && event.getTarget().contains("Drift net"))
		{
			MenuEntry[] menuEntries = client.getMenuEntries();
			menuEntries = Arrays.copyOf(menuEntries, menuEntries.length - 1);
			client.setMenuEntries(menuEntries);
			return;
		}

		// Swap fish menu entries to prioritize untagged fish
		if (config.swapUntaggedFish() && event.getOption().equals("Chase") && event.getTarget().contains("Fish shoal"))
		{
			swapFishMenuEntries();
		}
	}

	private void swapFishMenuEntries()
	{
		MenuEntry[] menuEntries = client.getMenuEntries();
		if (menuEntries.length < 2)
		{
			return;
		}

		if (taggedFish.isEmpty())
		{
			return;
		}

		// Find "Chase" options for fish shoals - separate tagged from untagged
		int untaggedIndex = -1;
		int taggedIndex = -1;

		for (int i = menuEntries.length - 1; i >= 0; i--)
		{
			MenuEntry entry = menuEntries[i];
			if (entry.getOption().equals("Chase") && entry.getTarget().contains("Fish shoal"))
			{
				// Get the NPC from the menu entry
				NPC npc = entry.getNpc();
				if (npc != null)
				{
					// Check if this fish is in our tagged fish map
					boolean isTagged = taggedFish.containsKey(npc);

					if (!isTagged && untaggedIndex == -1)
					{
						// Found the first untagged fish
						untaggedIndex = i;
					}
					else if (isTagged && taggedIndex == -1)
					{
						// Found the first tagged fish
						taggedIndex = i;
					}

					// Early exit: if we found both, no need to continue
					if (untaggedIndex != -1 && taggedIndex != -1)
					{
						break;
					}
				}
			}
		}

		// If we found both tagged and untagged fish, and untagged is NOT already on top
		// Swap them so untagged becomes the left-click option
		if (untaggedIndex != -1 && taggedIndex != -1 && untaggedIndex < taggedIndex)
		{
			MenuEntry temp = menuEntries[untaggedIndex];
			menuEntries[untaggedIndex] = menuEntries[taggedIndex];
			menuEntries[taggedIndex] = temp;
			client.setMenuEntries(menuEntries);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.HUNTER)
		{
			return;
		}

		int currentXp = event.getXp();
		if (currentXp > lastHunterXp)
		{
			int xpGained = currentXp - lastHunterXp;

			// Calculate XP per fish based on Hunter level
			// Drift net XP scales from 72 XP at level 50 to 101.5 XP at level 70+
			int hunterLevel = event.getLevel();
			double xpPerFish = calculateDriftNetXp(hunterLevel);

			// Calculate fish caught (rounded to handle decimal XP values)
			int fishCaught = (int) Math.round(xpGained / xpPerFish);

			if (fishCaught > 0 && inDriftNetArea)
			{
				totalFishCaught += fishCaught;
				sessionFishCaught += fishCaught;

				// Save total fish count to config
				configManager.setConfiguration(CONFIG_GROUP, "totalFishCaught", totalFishCaught);
			}
		}
		lastHunterXp = currentXp;
	}

	private double calculateDriftNetXp(int hunterLevel)
	{
		// Drift net XP scales linearly from level 50 to 70
		// Level 50: 72 XP per fish
		// Level 70+: 101.5 XP per fish
		if (hunterLevel < MIN_LEVEL)
		{
			return MIN_XP;
		}
		else if (hunterLevel >= MAX_LEVEL)
		{
			return MAX_XP;
		}
		else
		{
			// Linear scaling between 50 and 70 using precomputed constant
			return MIN_XP + ((hunterLevel - MIN_LEVEL) * XP_PER_LEVEL);
		}
	}

	private boolean checkArea()
	{
		final Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || !client.isInInstancedRegion())
		{
			return false;
		}

		final WorldPoint point = WorldPoint.fromLocalInstance(client, localPlayer.getLocalLocation());
		if (point == null)
		{
			return false;
		}

		return point.getRegionID() == UNDERWATER_REGION;
	}
}
