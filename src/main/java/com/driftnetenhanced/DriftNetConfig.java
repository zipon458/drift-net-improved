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

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Notification;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(DriftNetPlugin.CONFIG_GROUP)
public interface DriftNetConfig extends Config
{
	@ConfigItem(
		position = 1,
		keyName = "showNetStatus",
		name = "Show net status",
		description = "Show net status and fish count."
	)
	default boolean showNetStatus()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "netHighlightStyle",
			name = "Use clickbox for nets",
			description = "Use clickbox instead of hull to highlight drift nets."
	)
	default boolean useNetClickbox()
	{
		return false;
	}

	@Range(
			min = 0,
			max = 255
	)
	@ConfigItem(
			position = 3,
			keyName = "netFillOpacity",
			name = "Net fill opacity",
			description = "Opacity of drift net fill color (0 = transparent, 255 = solid)."
	)
	default int netFillOpacity()
	{
		return 0;
	}

	@ConfigItem(
		position = 4,
		keyName = "countColor",
		name = "Fish count color",
		description = "Color of the fish count text."
	)
	default Color countColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		position = 5,
		keyName = "highlightUntaggedFish",
		name = "Highlight untagged fish",
		description = "Highlight the untagged fish."
	)
	default boolean highlightUntaggedFish()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = "highlightTaggedFish",
		name = "Highlight tagged fish",
		description = "Highlight the tagged fish."
	)
	default boolean highlightTaggedFish()
	{
		return false;
	}

	@ConfigItem(
		position = 7,
		keyName = "timeoutDelay",
		name = "Tagged timeout",
		description = "Time required for a tag to expire."
	)
	@Range(
		min = 1,
		max = 100
	)
	@Units(Units.TICKS)
	default int timeoutDelay()
	{
		return 40;
	}

	@Alpha
	@ConfigItem(
		keyName = "untaggedFishColor",
		name = "Untagged fish color",
		description = "Color of untagged fish.",
		position = 8
	)
	default Color untaggedFishColor()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
			keyName = "untaggedFishFillColor",
			name = "Untagged fish fill color",
			description = "Fill color of untagged fish tile.",
			position = 9
	)
	default Color untaggedFishFillColor()
	{
		return new Color(0, 255, 255, 50);
	}

	@Alpha
	@ConfigItem(
		keyName = "taggedFishColor",
		name = "Tagged fish color",
		description = "Color of tagged fish.",
		position = 10
	)
	default Color taggedFishColor()
	{
		return Color.YELLOW;
	}

	@Alpha
	@ConfigItem(
		keyName = "taggedFishFillColor",
		name = "Tagged fish fill color",
		description = "Fill color of tagged fish tile.",
		position = 11
	)
	default Color taggedFishFillColor()
	{
		return new Color(255, 255, 0, 50);
	}

	@ConfigItem(
		keyName = "tagAnnette",
		name = "Tag Annette",
		description = "Tag Annette when no nets in inventory.",
		position = 12
	)
	default boolean tagAnnetteWhenNoNets()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightTacklebox",
		name = "Highlight tacklebox",
		description = "Highlight the tacklebox in your inventory when you have no drift nets.",
		position = 13
	)
	default boolean highlightTacklebox()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "tackleboxColor",
		name = "Tacklebox highlight color",
		description = "Color for tacklebox inventory highlight.",
		position = 14
	)
	default Color tackleboxColor()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
		keyName = "annetteTagColor",
		name = "Annette tag color",
		description = "Color of Annette tag.",
		position = 15
	)
	default Color annetteTagColor()
	{
		return Color.RED;
	}

	@Range(
			min = 0,
			max = 255
	)
	@ConfigItem(
			position = 16,
			keyName = "annetteFillOpacity",
			name = "Annette fill opacity",
			description = "Opacity of Annette fill color (0 = transparent, 255 = solid)."
	)
	default int annetteFillOpacity()
	{
		return 0;
	}

	@ConfigSection(
		name = "Timer",
		description = "Timer settings for tagged fish",
		position = 17,
		closedByDefault = true
	)
	String timerSection = "timer";

	@ConfigItem(
		keyName = "timerMode",
		name = "Timer display mode",
		description = "How to display the timer on tagged fish.",
		position = 18,
		section = timerSection
	)
	default TimerMode timerMode()
	{
		return TimerMode.OFF;
	}

	@ConfigSection(
		name = "Menu Options",
		description = "Menu entry hiding options",
		position = 19,
		closedByDefault = true
	)
	String menuSection = "menu";

	@ConfigItem(
		keyName = "hideTakeDown",
		name = "Hide 'Take down'",
		description = "Hide the 'Take down' option from empty drift nets.",
		position = 20,
		section = menuSection
	)
	default boolean hideTakeDown()
	{
		return false;
	}

	@ConfigSection(
		name = "Statistics Overlay",
		description = "Fish caught statistics overlay",
		position = 21,
		closedByDefault = true
	)
	String statsSection = "stats";

	@ConfigItem(
		keyName = "showStatsOverlay",
		name = "Show stats overlay",
		description = "Show the statistics overlay in the drift net area.",
		position = 22,
		section = statsSection
	)
	default boolean showStatsOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showTotalFish",
		name = "Show total fish",
		description = "Show total fish caught since plugin installation.",
		position = 23,
		section = statsSection
	)
	default boolean showTotalFish()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSessionFish",
		name = "Show session fish",
		description = "Show fish caught this session.",
		position = 24,
		section = statsSection
	)
	default boolean showSessionFish()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showFishPerHour",
		name = "Show fish/hour",
		description = "Show fish caught per hour this session.",
		position = 25,
		section = statsSection
	)
	default boolean showFishPerHour()
	{
		return true;
	}

	@ConfigSection(
		name = "Notifications",
		description = "Notification settings",
		position = 26,
		closedByDefault = false
	)
	String notificationSection = "notifications";

	@ConfigItem(
		keyName = "notifyOutOfNets",
		name = "Out of nets",
		description = "Send a notification when you run out of drift nets.",
		position = 27,
		section = notificationSection
	)
	default Notification notifyOutOfNets()
	{
		return Notification.OFF;
	}

	@ConfigItem(
		keyName = "notifyNetFull",
		name = "Net is full",
		description = "Send a notification when a drift net becomes full.",
		position = 28,
		section = notificationSection
	)
	default Notification notifyNetFull()
	{
		return Notification.OFF;
	}
}
