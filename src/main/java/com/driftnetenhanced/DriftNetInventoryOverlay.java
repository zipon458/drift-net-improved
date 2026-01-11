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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

class DriftNetInventoryOverlay extends Overlay
{
	private static final int TACKLEBOX_ITEM_ID = 25580;

	private final Client client;
	private final DriftNetConfig config;
	private final DriftNetPlugin plugin;

	@Inject
	private DriftNetInventoryOverlay(Client client, DriftNetConfig config, DriftNetPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.highlightTacklebox() || !plugin.isInDriftNetArea())
		{
			return null;
		}

		if (plugin.isDriftNetsInInventory())
		{
			return null;
		}

		Widget inventoryWidget = client.getWidget(149, 0); // Inventory widget
		if (inventoryWidget == null || inventoryWidget.isHidden())
		{
			return null;
		}

		Widget[] children = inventoryWidget.getDynamicChildren();
		if (children == null)
		{
			return null;
		}

		for (Widget child : children)
		{
			if (child.getItemId() == TACKLEBOX_ITEM_ID)
			{
				Rectangle bounds = child.getBounds();
				OverlayUtil.renderPolygon(graphics, rectangleToPolygon(bounds), config.tackleboxColor());

				net.runelite.api.Point textLocation = new net.runelite.api.Point(
					bounds.x,
					bounds.y - 5
				);
				OverlayUtil.renderTextLocation(graphics, textLocation, "Get more nets", config.tackleboxColor());
			}
		}

		return null;
	}

	private java.awt.Polygon rectangleToPolygon(Rectangle rect)
	{
		int[] xpoints = {rect.x, rect.x + rect.width, rect.x + rect.width, rect.x};
		int[] ypoints = {rect.y, rect.y, rect.y + rect.height, rect.y + rect.height};
		return new java.awt.Polygon(xpoints, ypoints, 4);
	}
}
