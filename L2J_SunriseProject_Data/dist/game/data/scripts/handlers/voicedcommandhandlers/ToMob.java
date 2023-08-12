/*
 * Copyright (C) 2020 L2J DataPack
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.SetupGauge;
import l2r.gameserver.util.Broadcast;

import gr.sr.interf.SunriseEvents;

/**
 * @author Gremlin
 */
public class ToMob implements IVoicedCommandHandler
{
	private final static String ToMob = Config.TO_MOB_COMMAND;
	
	private static final String[] _voicedCommands =
	{
		ToMob
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (activeChar == null)
		{
			return false;
		}
		if (!Config.TELEPORT_TO_MOB_COMMAND)
		{
			activeChar.sendMessage("This command is disabled!");
			return false;
		}
		
		if (command.equalsIgnoreCase(ToMob))
		{
			if (target == null)
			{
				activeChar.sendMessage("Usage: ." + ToMob + " <name>");
			}
			else
			{
				String name = null;
				try
				{
					name = target;
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage("You must enter a name. Usage: ." + ToMob + " <name>");
					return false;
				}
				
				if (name == "")
				{
					return false;
				}
				
				List<L2NpcTemplate> mobs = NpcTable.getInstance().getAllNpcOfClassType("L2Monster");
				List<Integer> xyz = new ArrayList<>();
				
				for (L2NpcTemplate mob : mobs)
				{
					if (mob.getName().equalsIgnoreCase(name))
					{
						final String SELECT_SPAWNS = "SELECT locx, locy, locz FROM spawnlist WHERE npc_templateid = " + mob.getId();
						try (Connection con = L2DatabaseFactory.getInstance().getConnection();
							Statement s = con.createStatement();
							ResultSet rs = s.executeQuery(SELECT_SPAWNS))
						{
							while (rs.next())
							{
								xyz.add(rs.getInt("locx"));
								xyz.add(rs.getInt("locy"));
								xyz.add(rs.getInt("locz"));
							}
						}
						catch (Exception e)
						{
							System.out.println(" === Error reading locs === ");
						}
					}
				}
				if (xyz.isEmpty())
				{
					activeChar.sendMessage("Any monster found");
					return false;
				}
				Random rnd = new Random();
				int mobRandomPos = (rnd.nextInt(xyz.size())) + 0;
				int posX;
				int posY;
				int posZ;
				
				if ((mobRandomPos % 3) == 0)
				{
					posX = xyz.get(mobRandomPos);
					posY = xyz.get(mobRandomPos + 1);
					posZ = xyz.get(mobRandomPos + 2);
				}
				else
				{
					posX = xyz.get(0);
					posY = xyz.get(1);
					posZ = xyz.get(2);
				}
				if (activeChar.isFestivalParticipant())
				{
					activeChar.sendMessage("Can not use this acction at event!");
					return false;
				}
				else if (activeChar.isJailed())
				{
					activeChar.sendMessage("Can not use this acction at Jail");
					return false;
				}
				else if (activeChar.isDead())
				{
					activeChar.sendMessage("Can not use this acction Dead.");
					return false;
				}
				else if (activeChar.isInCombat())
				{
					activeChar.sendMessage("You can't teleport while you are in combat.");
					return false;
				}
				else if (activeChar.isInDuel())
				{
					activeChar.sendMessage("You can't teleport while you are doing a duel.");
					return false;
				}
				else if (activeChar.isInOlympiadMode())
				{
					activeChar.sendMessage("You can't teleport while you are in olympiad");
					return false;
				}
				else if (activeChar.inObserverMode())
				{
					activeChar.sendMessage("You can't teleport while you are in observer mode");
					return false;
				}
				else if (activeChar.isCursedWeaponEquipped())
				{
					activeChar.sendMessage("While you are holding a Cursed Weapon you can't go to your mob!");
					return false;
				}
				else if (SunriseEvents.isInEvent(activeChar))
				{
					activeChar.sendMessage("It was not possible to teleport, you are participating in an event!");
					return false;
				}
				else if (!activeChar.isInsideZone(ZoneIdType.PEACE) && Config.ALLOW_IN_PEACE_ZONE)
				{
					activeChar.sendMessage("You are not in a peaceful zone!");
					return false;
				}
				
				final int teleportTimer = Config.TO_MOB_TIME * 1000;
				
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				
				// SoE Animation section
				activeChar.setTarget(activeChar);
				activeChar.disableAllSkills();
				
				final MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, teleportTimer, 0);
				Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 900);
				
				final SetupGauge sg = new SetupGauge(SetupGauge.BLUE, teleportTimer);
				activeChar.sendPacket(sg);
				
				final EscapeFinalizer ef = new EscapeFinalizer(activeChar, posX, posY, posZ);
				// continue execution later
				activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
				activeChar.forceIsCasting(GameTimeController.getInstance().getGameTicks() + (teleportTimer / GameTimeController.MILLIS_IN_TICK));
				
				if (!Config.TO_MOB_FREE)
				{
					String itemName = ItemData.getInstance().getTemplate(Config.ID_ITEM_TO_MOB).getName();
					activeChar.getInventory().destroyItemByItemId("GoToMob", Config.ID_ITEM_TO_MOB, Config.COUNT_ITEM_TO_MOB, activeChar, activeChar.getTarget());
					activeChar.sendMessage(Config.COUNT_ITEM_TO_MOB + " " + itemName + " has dissapeared! Thank you!");
				}
				
				// activeChar.teleToLocation(posX, posY, posZ);
				activeChar.sendMessage("Teleported to " + name + "'s Zone");
			}
			
		}
		return true;
	}
	
	static class EscapeFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final int _mobx;
		private final int _moby;
		private final int _mobz;
		private final boolean _to7sDungeon;
		
		EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z)
		{
			this._to7sDungeon = false;
			_activeChar = activeChar;
			_mobx = x;
			_moby = y;
			_mobz = z;
		}
		
		@Override
		public void run()
		{
			if (_activeChar.isDead())
			{
				return;
			}
			
			_activeChar.setIsIn7sDungeon(_to7sDungeon);
			_activeChar.enableAllSkills();
			_activeChar.setIsCastingNow(false);
			
			try
			{
				_activeChar.teleToLocation(_mobx, _moby, _mobz);
			}
			catch (Exception e)
			{
				_log.error(String.valueOf(e));
			}
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
}