package fr.vaevictis.efficient.VVCdB;

import java.util.ArrayList;
import java.util.Collections;
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
		logger.info("VVCdB est chargé");
	}
	
	@Override
	public void onDisable()
	{
		logger.info("VVCdB est déchargé");
	}
	
	public static boolean peuPayer(Player p, int prix)
	{
		if(Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("argent." + p.getName()) == null)
		{
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p.getName(), 0);
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
		}
		
		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p.getName()) >= prix)
		{
			return true;
		}
		else if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p.getName()) < prix)
		{
			return false;
		}
		return false;
	}
	
	public static void Payer(ArenaPlayer p, int prix) throws pasAssezDAsException
	{
		if(Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().get("argent." + p.getName()) == null)
		{
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p.getName(), 0);
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
		}
		
		if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p.getName()) >= prix)
		{
			int argentJoueurActuel = Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p.getName());
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argent." + p.getName(), argentJoueurActuel - prix);
			int argentBanqueActuel = Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argentBanque");
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().set("argentBanque", argentBanqueActuel + prix);
			Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").saveConfig();
		}
		else if (Bukkit.getServer().getPluginManager().getPlugin("VVEconomy").getConfig().getInt("argent." + p.getName()) < prix)
		{
			throw new pasAssezDAsException();
		}
	}
	
	public static void ajouterPoints(Faction f, int nbrePoints)
	{
		if(Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig().get("points." + f.getTag()) == null)
		{
			Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig().set("points." + f.getTag(), 0);
			Bukkit.getServer().getPluginManager().getPlugin("VVCdB").saveConfig();
		}
		int pointsAMettre = Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig().getInt("points." + f.getTag()) + nbrePoints;
		Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig().set("points." + f.getTag(), pointsAMettre);
		Bukkit.getServer().getPluginManager().getPlugin("VVCdB").saveConfig();
	}
	
	public void resetPoints()
	{
		for(Faction f : Factions.i.get())
		{
			Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig().set("points." + f.getTag(), 0);
			Bukkit.getServer().getPluginManager().getPlugin("VVCdB").saveConfig();
		}
	}
	
	public static void scoreboard(Player p)
	{
		p.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "Tableau des scores");
		List<Integer> points = new ArrayList<Integer>();
		HashMap<Integer, Faction> board = new HashMap<Integer, Faction>();
		for(Faction f : Factions.i.get())
		{
			points.add(Integer.valueOf(Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig().getInt("points." + f.getTag())));
			board.put(Integer.valueOf(Bukkit.getServer().getPluginManager().getPlugin("VVCdB").getConfig().getInt("points." + f.getTag())), f);
		}
		Collections.sort(points);
		for(int i = 0; i < points.size(); i++)
		{
			p.sendMessage(ChatColor.AQUA + String.valueOf(i + 1) + " - " + board.get(points.get(i)).getTag() + " avec " + String.valueOf(points.get(i)) + " point(s).");
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
		if(s.length() >= 2)
		{
			s.substring(0, s.length() - 2);
		}
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
		if(s.length() >= 2)
		{
			s.substring(0, s.length() - 2);
		}
		return s;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label,String[] args)
	{
		Player p = (Player) sender;
		FPlayer fp = FPlayers.i.get(p);
		
		if(label.equalsIgnoreCase("cdbjoin") || label.equalsIgnoreCase("lobbyjoin") && p.hasPermission("vvcdb.basic"))
		{
			if(!fp.getFaction().getTag().equalsIgnoreCase("wilderness"))
			{
				if(peuEntrerLobby)
				{
					if(Lobby.containsKey(fp.getFaction()))
					{
						if(!Lobby.get(fp.getFaction()).contains(p))
						{
							p.sendMessage(ChatColor.AQUA + "Vous êtes entrés dans le Lobby.");
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
							p.sendMessage(ChatColor.DARK_RED + "Vous êtes déjà dans le lobby");
						}
					}
					else
					{
						p.sendMessage(ChatColor.AQUA + "Vous êtes entrés dans le Lobby.");
						p.sendMessage(ChatColor.AQUA + "Les joueurs actuellement dans le lobby sont : " + chaineJoueursDansLobby());
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
					p.sendMessage(ChatColor.DARK_RED + "Une partie est déjà en cours, vous ne pouvez pas rejoindre le lobby");
				}
			}
			else
			{
				p.sendMessage(ChatColor.DARK_RED + "Vous n'avez pas de faction");
			}
			return true;
		}
		else if(label.equalsIgnoreCase("cdbleave") || label.equalsIgnoreCase("lobbyleave") && p.hasPermission("vvcdb.basic"))
		{
			if(peuQuitterLobby)
			{
				if(Lobby.containsKey(fp.getFaction()))
				{
					if(Lobby.get(fp.getFaction()).contains(p))
					{
						p.sendMessage(ChatColor.AQUA + "Vous avez quitté le Lobby");
						Lobby.get(fp.getFaction()).remove(p);
						if(Lobby.get(fp.getFaction()).isEmpty())
						{
							Lobby.remove(fp.getFaction());
							if(getNbreFactionsLobby() < 3)
							{
								if(LauchingTaskRunning)
								{
									LauchingTaskRunning = false;
									Bukkit.getServer().getScheduler().cancelTask(taskid);
									Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + "Le lancement du champ de bataille est annulé car " + p.getName() + " a quitté le lobby");
								}
							}
						}
					}
					else
					{
						p.sendMessage(ChatColor.DARK_RED + "Vous n'êtes pas dans le lobby");
					}
				}
				else
				{
					p.sendMessage(ChatColor.DARK_RED + "Vous n'êtes pas dans le lobby");
				}
			}
			else
			{
				p.sendMessage(ChatColor.DARK_RED + "Vous ne pouvez pas quitter le Lobby, la partie est lancée");
			}
			return true;
		}
		else if(label.equalsIgnoreCase("whoisinlobby") || label.equalsIgnoreCase("wiilobby") || label.equalsIgnoreCase("quiestdanslelobby") && p.hasPermission("vvcdb.basic"))
		{
			p.sendMessage(ChatColor.AQUA + "Les joueurs actuellement dans le lobby sont : " + chaineJoueursDansLobby());
			return true;
		}
		else if(label.equalsIgnoreCase("scoreboard") && p.hasPermission("vvcdb.basic"))
		{
			VVCdB.scoreboard(p);
			return true;
		}
		else if(label.equalsIgnoreCase("resetsaison") && p.hasPermission("vvcdb.admin"))
		{
			this.resetPoints();
		}
		return false;
	}
	
}

class pasAssezDAsException extends Exception{
	public pasAssezDAsException(){
	}
}
