package fr.vaevictis.efficient.VVCdB;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.managers.ArenaManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.massivecraft.factions.Faction;

public class TaskLauching extends BukkitRunnable{

	@SuppressWarnings("unused")
	private final JavaPlugin plugin;
	 
    public TaskLauching(JavaPlugin plugin) {
        this.plugin = plugin;
    }
 
    public void run() {
    	Arena a = ArenaManager.getArenaByName("CdB");
    	for(Faction MapKey : VVCdB.Lobby.keySet())
    	{
    		for(Player p : VVCdB.Lobby.get(MapKey))
    		{
    			a.tryJoin(p, a.getTeam(MapKey.getTag()));
    		}
    	}
    	VVCdB.peuEntrerLobby = false;
    	VVCdB.peuQuitterLobby = false;
    	VVCdB.LauchingTaskRunning = false;
    }
}
