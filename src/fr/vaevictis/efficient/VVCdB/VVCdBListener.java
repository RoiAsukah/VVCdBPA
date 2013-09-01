package fr.vaevictis.efficient.VVCdB;

import java.util.HashMap;

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
	
	public void pointsManager(Faction winner)
	{
		boolean plusNombreux = false, moinsNombreux = false, egaux = false;
		HashMap<Faction, Integer> resultats = new HashMap<Faction, Integer>();
		for(Faction MapKey : VVCdB.Lobby.keySet())
		{
			resultats.put(MapKey, 1);
			if(MapKey == winner)
			{
				for(Faction key : VVCdB.Lobby.keySet())
				{
					if(VVCdB.Lobby.get(winner).size() < VVCdB.Lobby.get(key).size())
					{
						moinsNombreux = true;
					}
					else if(VVCdB.Lobby.get(winner).size() > VVCdB.Lobby.get(key).size())
					{
						plusNombreux = true;
					}
					else if(VVCdB.Lobby.get(winner).size() == VVCdB.Lobby.get(key).size())
					{
						egaux = true;
					}
				}
				if(moinsNombreux)
				{
					resultats.remove(winner);
					resultats.put(winner, 5);
					plusNombreux = false;
					egaux = false;
				}
				if(egaux)
				{
					resultats.remove(winner);
					resultats.put(winner, 3);
					plusNombreux = false;
				}
				if(plusNombreux)
				{
					resultats.remove(winner);
					resultats.put(winner, 2);
				}
			}
			
		}
		for(Faction MapKey : resultats.keySet())
		{
			VVCdB.ajouterPoints(MapKey, resultats.get(MapKey).intValue());
		}
	}
	
	private boolean WinHasBeenCalled = false;
	public static HashMap<String, String> joueursLeger = new HashMap<String, String>();
	public static HashMap<String, String> joueursLourd = new HashMap<String, String>();
	
	@EventHandler(priority = EventPriority.LOWEST)
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
					if(VVCdB.peuPayer(p, 50))
					{
						if(joueursLourd.containsKey(p.getName()))
						{
							joueursLourd.remove(p.getName());
						}
							joueursLeger.put(p.getName(), FPlayers.i.get(p).getFaction().getTag());
					}
					else
					{
						p.sendMessage(ChatColor.RED + "Vous n'avez pas l'argent pour disposer de cette classe (50As)");
						e.setCancelled(true);
					}
				}
				if(sign.getLine(0).endsWith("Lourd"))
				{
					if(VVCdB.peuPayer(p, 100))
					{
						if(joueursLeger.containsKey(p.getName()))
						{
							joueursLeger.remove(p.getName());
						}
							joueursLourd.put(p.getName(), FPlayers.i.get(p).getFaction().getTag());
					}
					else
					{
						p.sendMessage(ChatColor.RED + "Vous n'avez pas l'argent pour disposer de cette classe (100As)");
						e.setCancelled(true);
					}
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
				int prixLeger = 50;
				int prixLourd = 100;
				for(ArenaPlayer p : e.getArena().getFighters())
				{
					if(joueursLeger.containsKey(p.getName()))
					{
						if(joueursLeger.get(p.getName()) == f.getTag())
						{
							try
							{
								VVCdB.Payer(p, prixLeger);
							}
							catch(pasAssezDAsException z)
							{
								//Bukkit.getPlayerExact(p.getName()).performCommand("pa CdB class load St");
								//e.getArena().selectClass(p, "StuffBasique");
								Bukkit.getPlayerExact(p.getName()).sendMessage(ChatColor.RED + "Vous n'aviez pas l'argent pour disposer de la classe légère, contactez le staff si vous obtenez ce message.");
							}
						}
					}
					else if(joueursLourd.containsKey(p.getName()))
					{
						if(joueursLourd.get(p.getName()) == f.getTag())
						{
							try
							{
								VVCdB.Payer(p, prixLourd);
							}
							catch(pasAssezDAsException z)
							{
//								Bukkit.getPlayerExact(p.getName()).performCommand("pa CdB class load CombattantAlpha");
								//e.getArena().selectClass(p, "StuffBasique");
								Bukkit.getPlayerExact(p.getName()).sendMessage(ChatColor.RED + "Vous n'aviez pas l'argent pour disposer de la classe lourde, contactez le staff si vous obtenez ce message.");
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
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void PAWinEvent(net.slipcor.pvparena.events.PAWinEvent e)
	{
		Player p = e.getPlayer();
		FPlayer fp = FPlayers.i.get(p);
		if(e.getArena().getName().equalsIgnoreCase("cdb"))
		{
			if(!WinHasBeenCalled)
			{
				this.pointsManager(fp.getFaction());
				WinHasBeenCalled = true;
				VVCdB.peuEntrerLobby = true;
				VVCdB.peuQuitterLobby = true;
				VVCdB.Lobby.clear();
			}
		}
	}
	
}
