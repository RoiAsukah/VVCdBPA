package fr.vaevictis.efficient.VVCdB;

import java.util.HashMap;
import java.util.Iterator;

import net.slipcor.pvparena.arena.ArenaPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import fr.vaevictis.efficient.VVCdB.VVCdB;

public class VVCdBListener implements Listener
{
	@SuppressWarnings("unused")
	private VVCdB plugin;

	public VVCdBListener(VVCdB plugin)
	{
		this.plugin = plugin;
	}
	
	public int returnNbrJoueurs(String faction, boolean lourd)
	{
		int i = 0;
		if(lourd == false)
		{
			for(String MapKey : joueursLeger.keySet())
			{
				if(joueursLeger.get(MapKey).equalsIgnoreCase(faction))
				{
					i++;
				}
			}
		}
		else if(lourd == true)
		{
			for(String MapKey : joueursLourd.keySet())
			{
				if(joueursLourd.get(MapKey).equalsIgnoreCase(faction))
				{
					i++;
				}
			}
		}
		return i;
	}
	
	public static HashMap<String, String> joueursLeger = new HashMap<String, String>();
	public static HashMap<String, String> joueursLourd = new HashMap<String, String>();
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBuysStuff (PlayerInteractEvent e)
	{
		Player p = e.getPlayer();
		if(e.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			Block clickedBlock = e.getClickedBlock();
			if((clickedBlock.getState() instanceof Sign))
			{
				Sign sign = (Sign)clickedBlock.getState();
				if(sign.getLine(0).endsWith("Leger"))
				{
					if(joueursLourd.containsKey(p.getName()))
					{
						joueursLourd.remove(p.getName());
					}
						joueursLeger.put(p.getName(), FPlayers.i.get(p).getFaction().getTag());
				}
				if(sign.getLine(0).endsWith("Lourd"))
				{
					if(joueursLeger.containsKey(p.getName()))
					{
						joueursLeger.remove(p.getName());
					}
						joueursLourd.put(p.getName(), FPlayers.i.get(p).getFaction().getTag());
				}
				if(sign.getLine(0).endsWith("StuffBasique"))
				{
					if(joueursLeger.containsKey(p.getName()))
					{
						joueursLeger.remove(p.getName());
					}
					else if(joueursLourd.containsKey(p.getName()))
					{
						joueursLourd.remove(p.getName());
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void PAStartEvent(net.slipcor.pvparena.events.PAStartEvent e)
	{
		if(e.getArena().getName().equalsIgnoreCase("cdb"))
		{
			for(Faction f : Factions.i.get())
			{
				int prixLeger = 25 + (25 * this.returnNbrJoueurs(f.getTag(), false));
				int prixLourd = 50 + (50 * this.returnNbrJoueurs(f.getTag(), true));
				for(ArenaPlayer p : e.getArena().getFighters())
				{
					if(joueursLeger.containsKey(p.getName()))
					{
						if(joueursLeger.get(p) == f.getTag())
						{
							try
							{
								VVCdB.Payer(p, prixLeger);
							}
							catch(pasAssezDAsException z)
							{
								e.getArena().selectClass(p, "StuffBasique");
								Bukkit.getPlayerExact(p.getName()).sendMessage(ChatColor.RED + "Vous n'aviez pas l'argent pour disposer de la classe légère, vous avez maintenant la classe basique.");
							}
						}
					}
					else if(joueursLourd.containsKey(p.getName()))
					{
						if(joueursLourd.get(p) == f.getTag())
						{
							try
							{
								VVCdB.Payer(p, prixLourd);
							}
							catch(pasAssezDAsException z)
							{
								e.getArena().selectClass(p, "StuffBasique");
								Bukkit.getPlayerExact(p.getName()).sendMessage(ChatColor.RED + "Vous n'aviez pas l'argent pour disposer de la classe légère, vous avez maintenant la classe basique.");
							}
						}
					}
				}
			}
		}	
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void PAEndEvent(net.slipcor.pvparena.events.PAEndEvent e)
	{
		if(e.getArena().getName().equalsIgnoreCase("cdb"))
		{
			joueursLeger.clear();
			joueursLourd.clear();
			//TODO gestion des points
			VVCdB.peuEntrerLobby = true;
			VVCdB.peuQuitterLobby = true;
			VVCdB.Lobby.clear();
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void playerLeaveEvent(PlayerQuitEvent e)
	{
		Player p = e.getPlayer();
		FPlayer fp = FPlayers.i.get(p);
		if(VVCdB.Lobby.containsKey(fp.getFaction()))
		{
			if(VVCdB.Lobby.get(fp.getFaction()).contains(p))
			{
				p.performCommand("cdbleave");
			}
		}
	}
	
}
