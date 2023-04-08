/*
 * Copyright (C) 2004-2016 L2J DataPack
 *
 * This file is part of L2J DataPack.
 *
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Voiced command to show the player which raids have been killed<br>
 * during Legendary Tales quest (7rb).<br>
 * <br>
 * Created for Share Competition 2018.
 * @author Benman from MaxCheaters.com
 */
public class SevenRB implements IVoicedCommandHandler
{
	private static final String QUEST_NAME = "Q00254_LegendaryTales";
	private static final String SERVER_NAME = "(Server Name)";
	private static final String[] commands =
	{
		"7rb",
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equalsIgnoreCase("7rb"))
		{
			QuestState st = activeChar.getQuestState(QUEST_NAME);
			NpcHtmlMessage m = new NpcHtmlMessage();
			m.setHtml(buildHtml(st));
			activeChar.sendPacket(m);
		}
		return true;
	}
	
	private static final String buildHtml(QuestState st)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head>");
		sb.append("<title>Lineage II " + SERVER_NAME + "</title>");
		sb.append("</head>");
		sb.append("<body><br>");
		sb.append("<br>7 Rb Quest (Legendary Tales) status:<br>");
		if (st == null)
		{
			sb.append("Quest is not started yet. Please visit Glimore in dragon valley in order to start it.");
			sb.append("<br>");
		}
		else
		{
			if (st.isCond(1))
			{
				for (Bosses boss : Bosses.class.getEnumConstants())
				{
					sb.append(boss.getName() + ": ");
					sb.append(checkMask(st, boss) ? "<font color=\"00FF00\">Killed.</font>" : "<font color=\"FF0000\">Not killed.</font>");
					sb.append("<br>");
				}
			}
			else
			{
				sb.append("Legendary Tales quest is completed.");
				sb.append("<br>");
			}
		}
		sb.append("</body></html>");
		return sb.toString();
	}
	
	private static boolean checkMask(QuestState qs, Bosses boss)
	{
		int pos = boss.getMask();
		return ((qs.getInt("raids") & pos) == pos);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return commands;
	}
	
	public static enum Bosses
	{
		EMERALD_HORN("Emerald Horn"),
		DUST_RIDER("Dust Rider"),
		BLEEDING_FLY("Bleeding Fly"),
		BLACK_DAGGER("Blackdagger Wing"),
		SHADOW_SUMMONER("Shadow Summoner"),
		SPIKE_SLASHER("Spike Slasher"),
		MUSCLE_BOMBER("Muscle Bomber");
		
		private final String name;
		private final int _mask;
		
		private Bosses(String name)
		{
			this.name = name;
			_mask = 1 << ordinal();
		}
		
		public int getMask()
		{
			return _mask;
		}
		
		public String getName()
		{
			return name;
		}
	}
}