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

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

class DriftNetImprovedInventoryOverlay extends WidgetItemOverlay
{
	private static final int TACKLEBOX_ITEM_ID = 25580;
	private static final String TEXT_GET_MORE_NETS = "Get more nets";

	private final DriftNetConfig config;
	private final DriftNetImprovedPlugin plugin;
	private final ItemManager itemManager;

	private final TextComponent textComponent = new TextComponent();

	@Inject
	private DriftNetImprovedInventoryOverlay(DriftNetConfig config, DriftNetImprovedPlugin plugin, ItemManager itemManager)
	{
		this.config = config;
		this.plugin = plugin;
		this.itemManager = itemManager;
		showOnInventory();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		if (!config.highlightTacklebox() || !plugin.isInDriftNetArea())
		{
			return;
		}

		if (plugin.isDriftNetsInInventory())
		{
			return;
		}

		if (itemId != TACKLEBOX_ITEM_ID)
		{
			return;
		}

		Rectangle bounds = widgetItem.getCanvasBounds();

		final BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), config.tackleboxColor());
		graphics.drawImage(outline, (int) bounds.getX(), (int) bounds.getY(), null);

		textComponent.setColor(config.tackleboxColor());
		textComponent.setText(TEXT_GET_MORE_NETS);

		FontMetrics fontMetrics = graphics.getFontMetrics();
		int textWidth = fontMetrics.stringWidth(TEXT_GET_MORE_NETS);
		int textHeight = fontMetrics.getHeight();

		textComponent.setPosition(new Point(
			bounds.x + bounds.width / 2 - textWidth / 2,
			bounds.y + bounds.height + textHeight / 2
		));

		textComponent.render(graphics);
	}
}
