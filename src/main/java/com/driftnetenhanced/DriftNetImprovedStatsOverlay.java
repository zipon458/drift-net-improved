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
 * AND ANY EXPRESS OR IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.driftnetenhanced;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class DriftNetImprovedStatsOverlay extends Overlay
{
	private final DriftNetConfig config;
	private final DriftNetImprovedPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	// Cache for fish per hour display (updates every 2 seconds)
	private int cachedFishPerHour = 0;
	private long lastFishPerHourUpdate = 0;
	private static final long FISH_PER_HOUR_UPDATE_INTERVAL = 2000;

	@Inject
	private DriftNetImprovedStatsOverlay(DriftNetConfig config, DriftNetImprovedPlugin plugin)
	{
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showStatsOverlay() || !plugin.isInDriftNetArea())
		{
			return null;
		}

		panelComponent.getChildren().clear();

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Drift Net Stats")
			.build());

		// Total fish caught
		if (config.showTotalFish())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Total:")
				.right(String.valueOf(plugin.getTotalFishCaught()))
				.build());
		}

		// Session fish caught
		if (config.showSessionFish())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Session:")
				.right(String.valueOf(plugin.getSessionFishCaught()))
				.build());
		}

		// Fish per hour
		if (config.showFishPerHour())
		{
			long currentTime = System.currentTimeMillis();

			// Only recalculate fish per hour every 2 seconds
			if (currentTime - lastFishPerHourUpdate >= FISH_PER_HOUR_UPDATE_INTERVAL)
			{
				long sessionTime = currentTime - plugin.getSessionStartTime();
				double hoursElapsed = sessionTime / (1000.0 * 60.0 * 60.0);
				cachedFishPerHour = hoursElapsed > 0 ? (int) (plugin.getSessionFishCaught() / hoursElapsed) : 0;
				lastFishPerHourUpdate = currentTime;
			}

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Per hour:")
				.right(String.valueOf(cachedFishPerHour))
				.build());
		}

		return panelComponent.render(graphics);
	}
}
