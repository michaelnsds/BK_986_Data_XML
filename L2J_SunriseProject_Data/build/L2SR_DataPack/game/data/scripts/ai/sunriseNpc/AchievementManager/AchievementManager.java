package ai.sunriseNpc.AchievementManager;

import l2r.features.achievementEngine.Achievements;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class AchievementManager extends AbstractNpcAI
{
	private final static int NPC = CustomNpcsConfigs.ACHIEVEMENT_NPC_ID;
	
	public AchievementManager()
	{
		super(AchievementManager.class.getSimpleName(), "ai/sunriseNpc");
		addFirstTalkId(NPC);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_ACHIEVEMENT_MANAGER)
		{
			player.sendMessage("Achievement manager npc is disabled by admin.");
			return "main.htm";
		}
		
		if (player.getLevel() < CustomNpcsConfigs.ACHIEVEMENT_REQUIRED_LEVEL)
		{
			player.sendMessage("You need to be " + CustomNpcsConfigs.ACHIEVEMENT_REQUIRED_LEVEL + " level or higher to use my services.");
			return "main.htm";
		}
		
		Achievements.getInstance().generatePagr(player);
		return "";
	}
}