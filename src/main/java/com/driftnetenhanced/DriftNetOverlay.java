/*
 * Copyright (c) 2020, dekvall <https://github.com/dekvall>
 * Copyright (c) 2026, Zipon <https://github.com/zipon458>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.driftnetenhanced;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

class DriftNetOverlay extends Overlay
{
	private final DriftNetConfig config;
	private final DriftNetPlugin plugin;

	@Inject
	private DriftNetOverlay(DriftNetConfig config, DriftNetPlugin plugin)
	{
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInDriftNetArea())
		{
			return null;
		}

		if (config.highlightUntaggedFish() || config.highlightTaggedFish() || config.timerMode() != TimerMode.OFF)
		{
			renderFish(graphics);
		}
		if (config.showNetStatus())
		{
			renderNets(graphics);
		}
		if (config.tagAnnetteWhenNoNets())
		{
			renderAnnette(graphics);
		}

		return null;
	}

	private void renderFish(Graphics2D graphics)
	{
		for (NPC fish : plugin.getFish())
		{
			boolean isTagged = plugin.getTaggedFish().containsKey(fish);

			if (!isTagged && config.highlightUntaggedFish())
			{
				// Render untagged fish
				Polygon tilePoly = fish.getCanvasTilePoly();
				if (tilePoly != null)
				{
					graphics.setColor(config.untaggedFishFillColor());
					graphics.fill(tilePoly);
					graphics.setColor(config.untaggedFishColor());
					graphics.draw(tilePoly);
				}
			}
			else if (isTagged)
			{
				// Render tagged fish highlight if enabled
				if (config.highlightTaggedFish())
				{
					Polygon tilePoly = fish.getCanvasTilePoly();
					if (tilePoly != null)
					{
						graphics.setColor(config.taggedFishFillColor());
						graphics.fill(tilePoly);
						graphics.setColor(config.taggedFishColor());
						graphics.draw(tilePoly);
					}
				}

				// Render timer on tagged fish if enabled
				if (config.timerMode() != TimerMode.OFF)
				{
					renderFishTimer(graphics, fish);
				}
			}
		}
	}

	private void renderFishTimer(Graphics2D graphics, NPC fish)
	{
		Integer taggedTick = plugin.getTaggedFish().get(fish);
		if (taggedTick == null)
		{
			return;
		}

		int currentTick = plugin.getClient().getTickCount();
		int ticksRemaining = (taggedTick + config.timeoutDelay()) - currentTick;

		if (ticksRemaining <= 0)
		{
			return;
		}

		Point location = fish.getCanvasTextLocation(graphics, "", 0);
		if (location == null)
		{
			return;
		}

		TimerMode mode = config.timerMode();
		switch (mode)
		{
			case TICKS:
				String tickText = String.valueOf(ticksRemaining);
				OverlayUtil.renderTextLocation(graphics, location, tickText, Color.WHITE);
				break;
			case SECONDS:
				int secondsRemaining = (int) Math.ceil(ticksRemaining * 0.6);
				String secondText = secondsRemaining + "s";
				OverlayUtil.renderTextLocation(graphics, location, secondText, Color.WHITE);
				break;
			case PIE:
				double progress = (double) ticksRemaining / config.timeoutDelay();
				ProgressPieComponent pie = new ProgressPieComponent();
				pie.setPosition(location);
				pie.setProgress(progress);
				pie.setDiameter(20);
				pie.setBorderColor(Color.WHITE);
				pie.setFill(config.untaggedFishColor());
				pie.render(graphics);
				break;
		}
	}

	private void renderNets(Graphics2D graphics)
	{
		for (DriftNet net : plugin.getNETS())
		{
			if (net.getNet() == null)
			{
				continue;
			}

			final Shape polygon;
			if (config.useNetClickbox())
			{
				polygon = net.getNet().getClickbox();
			}
			else
			{
				polygon = net.getNet().getConvexHull();
			}

			if (polygon != null)
			{
				Color statusColor = net.getStatus().getColor();
				if (config.netFillOpacity() > 0)
				{
					Color fillColor = new Color(statusColor.getRed(), statusColor.getGreen(), statusColor.getBlue(), config.netFillOpacity());
					graphics.setColor(fillColor);
					graphics.fill(polygon);
				}
				graphics.setColor(statusColor);
				graphics.draw(polygon);
			}

			String text = net.getFormattedCountText();
			Point textLocation = net.getNet().getCanvasTextLocation(graphics, text, 0);
			if (textLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, textLocation, text, config.countColor());
			}
		}
	}

	private void renderAnnette(Graphics2D graphics)
	{
		GameObject annette = plugin.getAnnette();
		if (annette != null && !plugin.isDriftNetsInInventory())
		{
			Shape polygon = annette.getConvexHull();
			if (polygon != null)
			{
				Color annetteColor = config.annetteTagColor();
				if (config.annetteFillOpacity() > 0)
				{
					Color fillColor = new Color(annetteColor.getRed(), annetteColor.getGreen(), annetteColor.getBlue(), config.annetteFillOpacity());
					graphics.setColor(fillColor);
					graphics.fill(polygon);
				}
				graphics.setColor(annetteColor);
				graphics.draw(polygon);
			}
		}
	}
}
