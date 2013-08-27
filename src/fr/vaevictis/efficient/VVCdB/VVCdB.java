package fr.vaevictis.efficient.VVCdB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import fr.vaevictis.efficient.VVCdB.VVCdBListener;

import net.slipcor.pvparena.arena.ArenaPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

public class VVCdB extends JavaPlugin{

	private Logger logger = Logger.getLogger("Minecraft");
	public static HashMap<Faction, ArrayList<Player>> Lobby = new HashMap<Faction, ArrayList<Player>>();
	public static ArrayList<Player> ssListeLobby;
	public static boolean peuEntrerLobby;
	public static boolean peuQuitterLobby;
	public static boolean LauchingTaskRunning;
	private int taskid = 0;
	
	@Override
	public void onEnable()
	{
		PluginManager pm = getServer().getPluginManager();		
		pm.registerEvents(new VVCdBListener(this), this);
		peuEntrerLobby = true;
		peuQuitterLobby = true;
		LauchingTaskRunning = false;
		logger.info("VVCdB est charg�");
	}
	
	@Override
	public void onDisable()
	{
		logger.info("VVCdB est d�charg�");
	}
	
	public static boolean Payer(ArenaPlayer p, int prix)
	{
		if(Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("argent." + p.getName()) == null)
		{
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p.getName(), 0);
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
			return false;
		}
		else if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p.getName()) >= prix)
		{
			int argentJoueurActuel = Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p.getName());
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p.getName(), argentJoueurActuel - prix);
			int argentBanqueActuel = Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argentBanque");
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argentBanque", argentBanqueActuel + prix);
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getNbreFactionsLobby()
	{
		int i = 0;
		for(Faction f : Factions.i.get())
		{
			if(Lobby.containsKey(f))
			{
				i++;
			}
		}
		return i;
	}
	
	public String getNomFactionsLobby()
	{
		String s = new String("");
		for(Faction f : Factions.i.get())
		{
			if(Lobby.containsKey(f))
			{
				s = s + f.getTag() + ", ";
			}
		}
		s.substring(0, s.lastIndexOf(","));
		return s;
	}
	
	public String chaineJoueursDansLobby()
	{
		String s = new String("");
		for(Faction f : Lobby.keySet())
		{
			s = s + f.getTag() + ": ";
			for(Player p : Lobby.get(f))
			{
				s = s + p.getName() + ", ";
			}
		}
		s.substring(0, s.lastIndexOf(","));
		return s;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label,String[] args)
	{
		Player p = (Player) sender;
		FPlayer fp = FPlayers.i.get(p);
		
		if(label.equalsIgnoreCase("cdbjoin"))
		{
			if(peuEntrerLobby)
			{
				if(Lobby.containsKey(fp.getFaction()))
				{
					if(!Lobby.get(fp.getFaction()).contains(p))
					{
						p.sendMessage(ChatColor.AQUA + "Les joueurs actuellement dans le lobby sont : " + chaineJoueursDansLobby());
						Lobby.get(fp.getFaction()).add(p);
						if(getNbreFactionsLobby() >= 3)
						{
							if(!LauchingTaskRunning)
							{
								LauchingTaskRunning = true;
								Bukkit.getServer().broadcastMessage("Un Champ de bataille est sur le point de ce lancer. Les factions " + getNomFactionsLobby() + " y participent, les derniers ont une minute pour le rejoindre !");
								BukkitTask task = new TaskLauching(this).runTaskLater(this, 1200);
								taskid = task.getTaskId();
							}
						}
					}
					else
					{
						p.sendMessage(ChatColor.DARK_RED + "Vous �tes d�j� dans le lobby");
					}
				}
				else
				{
					ssListeLobby = new ArrayList<Player>();
					ssListeLobby.add(p);
					Lobby.put(fp.getFaction(), ssListeLobby);
					if(getNbreFactionsLobby() >= 3)
					{
						if(!LauchingTaskRunning)
						{
							LauchingTaskRunning = true;
							Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "Un Champ de bataille est sur le point de ce lancer. Les factions " + getNomFactionsLobby() + " y participent, les derniers ont une minute pour le rejoindre !");
							BukkitTask task = new TaskLauching(this).runTaskLater(this, 1200);
							taskid = task.getTaskId();
						}
					}
				}
			}
			else
			{
				p.sendMessage(ChatColor.DARK_RED + "Une partie est d�j� en cours, vous ne pouvez pas rejoindre le lobby");
			}
			return true;
		}
		else if(label.equalsIgnoreCase("cdbleave"))
		{
			if(peuQuitterLobby)
			{
				if(Lobby.containsKey(fp.getFaction()))
				{
					if(Lobby.get(fp.getFaction()).contains(p))
					{
						Lobby.get(fp.getFaction()).remove(p);
						if(getNbreFactionsLobby() < 3)
						{
							if(LauchingTaskRunning)
							{
								LauchingTaskRunning = false;
								Bukkit.getServer().getScheduler().cancelTask(taskid);
								Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "Le lancement du champ de bataille est annul� car " + p.getName() + "a quitt� le lobby");
							}
						}
					}
					else
					{
						p.sendMessage(ChatColor.DARK_RED + "Vous n'�tes pas dans le lobby");
					}
				}
				else
				{
					p.sendMessage(ChatColor.DARK_RED + "Vous n'�tes pas dans le lobby");
				}
			}
			else
			{
				p.sendMessage(ChatColor.DARK_RED + "Vous ne pouvez pas quitter le Lobby, la partie est lanc�e");
			}
			return true;
		}
		else if(label.equalsIgnoreCase("whoisinlobby"))
		{
			p.sendMessage(ChatColor.AQUA + "Les joueurs actuellement dans le lobby sont : " + chaineJoueursDansLobby());
			return true;
		}
		return false;
	}
	
}