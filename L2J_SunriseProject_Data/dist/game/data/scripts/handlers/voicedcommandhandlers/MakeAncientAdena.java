/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author llean
 */
public class MakeAncientAdena implements IVoicedCommandHandler
{
	
	private static final String[] _voicedCommands =
	{
		"aa",
		"makeaa"
	};
	private static final int ANCIENT_ADENA = 5575;
	private static final int BLUE_SEAL_STONE = 6360;
	private static final int GREEN_SEAL_STONE = 6361;
	private static final int RED_SEAL_STONE = 6362;
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("aa") || command.equalsIgnoreCase("makeaa"))
		{
			final L2ItemInstance redStones = activeChar.getInventory().getItemByItemId(RED_SEAL_STONE);
			final L2ItemInstance greenStones = activeChar.getInventory().getItemByItemId(GREEN_SEAL_STONE);
			final L2ItemInstance blueStones = activeChar.getInventory().getItemByItemId(BLUE_SEAL_STONE);
			
			int redStonesCount = 0;
			int blueStonesCount = 0;
			int greenStonesCount = 0;
			
			int count = 0;
			int aa = 0;
			
			if (redStones != null)
			{
				redStonesCount += redStones.getCount();
				if (activeChar.destroyItem("MakeAA", redStones, null, true))
				{
					count += redStonesCount;
					aa += redStonesCount * 10;
				}
			}
			if (greenStones != null)
			{
				greenStonesCount += greenStones.getCount();
				if (activeChar.destroyItem("MakeAA", greenStones, null, true))
				{
					count += greenStonesCount;
					aa += greenStonesCount * 5;
				}
			}
			if (blueStones != null)
			{
				blueStonesCount += blueStones.getCount();
				if (activeChar.destroyItem("MakeAA", blueStones, null, true))
				{
					count += blueStonesCount;
					aa += blueStonesCount * 3;
				}
			}
			if (count == 0)
			{
				activeChar.sendMessage("You do not have any seal stones to exchange.");
				return false;
			}
			activeChar.addItem("MakeAA", ANCIENT_ADENA, aa, activeChar, true);
			activeChar.sendMessage("You have successfully exchanged " + count + " seal stones!");
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}