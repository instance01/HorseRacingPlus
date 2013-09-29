package com.comze_instancelabs.horseracingplus;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.horseracingplus.HorseModifier.HorseType;
import com.comze_instancelabs.horseracingplus.HorseModifier.HorseVariant;

/**
 * 
 * @author instancelabs
 *
 */

public class Main extends JavaPlugin implements Listener{
	
	
	public static Economy econ = null;
	public boolean economy = false;
	public boolean gambling = false;
	ArenaSystem as = null;
	public int max_cycle = 0;
	
	static HashMap<Player, String> arenap = new HashMap<Player, String>(); // playername -> arenaname
	static HashMap<String, Integer> rounds = new HashMap<String, Integer>();
	static HashMap<String, String> tpthem = new HashMap<String, String>(); // playername -> arenaname
	static HashMap<String, Integer> arenaspawn = new HashMap<String, Integer>(); // arena -> current spawn count
	static HashMap<String, Boolean> gamestarted = new HashMap<String, Boolean>(); // arena -> game started 
	static HashMap<String, Integer> secs_ = new HashMap<String, Integer>(); // arena -> Seconds before game starts
	static HashMap<Player, String> creation = new HashMap<Player, String>(); // playername -> arena CREATION
	HashMap<String, Player> secs_updater = new HashMap<String, Player>(); // the current seconds updater (player) in the arena
	HashMap<Player, Integer> canceltask = new HashMap<Player, Integer>(); // player -> task
	HashMap<Player, Integer> pspawn = new HashMap<Player, Integer>(); // player -> spawn 1 etc.
	ArrayList<String> arenas = new ArrayList<String>(); // all arenas
	HashMap<Player, Integer> cyclep = new HashMap<Player, Integer>(); // player -> current cycle
	HashMap<Player, Player> bet_player = new HashMap<Player, Player>(); // player -> player who got bet on
	HashMap<Player, Integer> bet_amount = new HashMap<Player, Integer>(); // player -> bet amount
	static HashMap<Player, ItemStack[]> pinv = new HashMap<Player, ItemStack[]>(); // player -> Inventory
	static HashMap<Player, String> specp = new HashMap<Player, String>(); // playername -> arenaname [SPECTATING]

	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);	
		getConfig().addDefault("config.use_economy", true);
		getConfig().addDefault("config.use_gambling", true);
		getConfig().addDefault("config.normal_money_reward", 50);
		getConfig().addDefault("config.itemid", 264);
		getConfig().addDefault("config.itemamount", 1);
		getConfig().addDefault("config.min_players", 2);
		getConfig().addDefault("config.entry_money", 10);
		getConfig().addDefault("config.starting_cooldown", 10);
		getConfig().addDefault("config.horsecolor", "BLACK");
		getConfig().addDefault("config.horsename", "Racehorse");
		getConfig().addDefault("config.use_rounds_system", false);
		getConfig().addDefault("config.rounds", 2);
		getConfig().addDefault("config.announce_winner", true);
		getConfig().addDefault("config.arena_cycling", false);
		getConfig().addDefault("config.auto_updating", true);
		getConfig().addDefault("config.enable_betting", false);
		getConfig().addDefault("config.max_bet_amount", 500);
		getConfig().addDefault("shop.jumppotion_price", 50);
		getConfig().addDefault("shop.speedpotion_price", 150);
		getConfig().addDefault("shop.barding_price", 100);
		getConfig().addDefault("config.lastmanstanding", true);
		
		
		getConfig().addDefault("strings.nopermission", "§4You don't have permission!");
		getConfig().addDefault("strings.createrace", "§2Race saved. Now create a few spawnpoints and a lobby. :)");
		getConfig().addDefault("strings.help1", "§2HorseRacing help:");
		getConfig().addDefault("strings.help2", "§2Use §3'/hr createrace <name>' §2to create a new race.");
		getConfig().addDefault("strings.help3", "§2Use §3'/hr setlobby <name>' §2to set the lobby for an course.");
		getConfig().addDefault("strings.help4", "§2Use §3'/hr setspawn <count> <name>' §2to set a new race spawn.");
		getConfig().addDefault("strings.help5", "§2Use §3'/hr finishline <name>' §2to set a finish line for the race.");
		getConfig().addDefault("strings.help6", "§2Use §3'/hr removerace <name>' §2to remove a race.");
		getConfig().addDefault("strings.lobbycreated", "§2Lobby successfully created!");
		getConfig().addDefault("strings.spawn", "§2Spawnpoint registered.");
		getConfig().addDefault("strings.raceremoved", "§4Race removed.");
		getConfig().addDefault("strings.reload", "§2HorseRacing config successfully reloaded.");
		getConfig().addDefault("strings.nothing", "§4This command action was not found.");
		getConfig().addDefault("strings.ingame", "§eYou are not able to use any commands while in a race. You can use /hr leave or /horseracing leave if you want to leave this race.");
		getConfig().addDefault("strings.left", "§eYou left the race!");
		getConfig().addDefault("strings.won", "§2You won the race!");
		getConfig().addDefault("strings.lost", "§4You lost!");
		getConfig().addDefault("strings.creation", "§2Leftclick the first point and rightclick the second point of the finish line.");
		
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		if(getConfig().getBoolean("config.use_economy")){
			economy = true;
			if (!setupEconomy()) {
	            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
	            //getServer().getPluginManager().disablePlugin(this);
	            economy = false;
	        }
		}
		
		if(getConfig().getBoolean("config.use_gambling")){
			gambling = true;
		}

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :(
		}
		
		ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(getConfig().getKeys(false));
        keys.remove("config");
        keys.remove("strings");
        keys.remove("shop");
        keys.remove("stats");
        for(int i = 0; i < keys.size(); i++){
            gamestarted.put(keys.get(i), false);
            if(getConfig().getBoolean("config.use_rounds_system")){
            	rounds.put(keys.get(i), 0);
            }
            if(getConfig().getBoolean("config.arena_cycling")){
            	max_cycle += 1;
            	arenas.add(keys.get(i));
            }
        }
        
        if(getConfig().getBoolean("config.auto_updating")){
        	Updater updater = new Updater(this, "horse-racing-plus", this.getFile(), Updater.UpdateType.DEFAULT, false);
        }
        
        for(Player p : Bukkit.getOnlinePlayers()){
	        if(tpthem.containsKey(p)){ //TODO doesn't work, tpthem is null
				String arena = tpthem.get(p);
				final Player p_ = p;
				final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world")), getConfig().getDouble(arena + ".lobbyspawn.x"), getConfig().getDouble(arena + ".lobbyspawn.y"), getConfig().getDouble(arena + ".lobbyspawn.z"));
				p.teleport(t);
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					//int secs = 11;
					
					@Override
		            public void run() {
						p_.teleport(t);
					}
				}, 20);
			}
        }
        
        as = new ArenaSystem(this);
        
        
        for(String p_ : getConfig().getConfigurationSection("tpthem.").getKeys(false)){
        	if(Bukkit.getOfflinePlayer(p_).isOnline()){
        		Player p = Bukkit.getPlayer(p_);
        		String arena = getConfig().getString("tpthem." + p_);
        		
        		Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
    	    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
    	    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
        		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
    	    	final Location t = new Location(w, x, y, z);
    	    	
    	    	final Player p__ = p;
    	    	
    	    	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
    				@Override
    	            public void run() {
    					p__.teleport(t);
    				}
    			}, 20);
    	    	
        		getConfig().set("tpthem." + p_, null);
        		this.saveConfig();
        	}
        }
	}
	
	public Plugin getWorldGuard(){
		return Bukkit.getPluginManager().getPlugin("WorldGuard");
	}


	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
		    return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
		    return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	
	@Override
	public void onDisable(){
		for(String arena : arenap.values()){
    		for(Player p2 : this.getKeysByValue(arenap, arena)){
        		//remove vehicle, remove snowballs, tp away
    	    	p2.getVehicle().remove();
    	    	
    	    	p2.updateInventory();
    	    	p2.getInventory().setContents(pinv.get(p2));
    	    	p2.updateInventory();
    	    	
    	    	getConfig().set("tpthem." + p2.getName(), arenap.get(p2));
    	    	this.saveConfig();
    	    	
    	    	if(p2.isOnline()){
	    	    	Location t = as.getLocFromArena(arena, "lobbyspawn");
	    	    	
	    	    	p2.teleport(t);
    	    	}
    	    	arenap.remove(p2);
    	    	

    	    	Location b = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
    	    	Sign s = (Sign)Bukkit.getWorld(getConfig().getString(arena + ".sign.world")).getBlockAt(b).getState();
    	    	// update sign: 
                if(s != null && s.getLine(3) != ""){
                	String d = s.getLine(3).split("/")[0];
                	int bef = Integer.parseInt(d);
                	if(bef > 0){
                		s.setLine(3, Integer.toString(bef - 1) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
                		s.setLine(2, "§2Join");
                		s.update();
                	}
                }
    		}
    	}
		
		
		/*for(Player p : arenap.keySet()){
			if(p.isInsideVehicle()){
				p.getVehicle().remove();
			}
			String arena = arenap.get(p);
			final Location t = as.getLocFromArena(arena, "lobbyspawn");
 			p.teleport(t);
 			tpthem.put(p, arena);
 			
 			p.getInventory().setContents(pinv.get(p));
			p.updateInventory();
 			
	    	Sign s_ = as.getSignFromArena(arena);
	    	// update sign:
            if(s_ != null && s_.getLine(3) != ""){
            	s_.setLine(2, "§2Join");
        		s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
        		s_.update();
            }
		}*/
		arenap.clear();
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("hr") || cmd.getName().equalsIgnoreCase("horseracing")){
 		if(args.length < 1){
 			sender.sendMessage(getConfig().getString("strings.help1"));
 			sender.sendMessage(getConfig().getString("strings.help2"));
 			sender.sendMessage(getConfig().getString("strings.help3"));
 			sender.sendMessage(getConfig().getString("strings.help4"));
 			sender.sendMessage(getConfig().getString("strings.help5"));
 			sender.sendMessage(getConfig().getString("strings.help6"));
 		}else{
 			Player p = (Player)sender;
 			if(args.length > 0){
 				String action = args[0];
 				if(action.equalsIgnoreCase("createrace") && args.length > 1){
 					// Create arena
 					if(p.hasPermission("horseracing.create")){
 						this.getConfig().set(args[1] + ".name", args[1]);
    	    			this.getConfig().set(args[1] + ".world", p.getWorld().getName());
    	    			this.saveConfig();
    	    			String arenaname = args[1];
    	    			sender.sendMessage(getConfig().getString("strings.createrace"));
    	    			gamestarted.put(args[1], false);
    	    			arenas.add(args[1]);
 					}
 				}else if(action.equalsIgnoreCase("setlobby") && args.length > 1){
 					// setlobby
 					if(p.hasPermission("horseracing.setlobby")){
 						String arena = args[1];
    		    		Location l = p.getLocation();
    		    		getConfig().set(args[1] + ".lobbyspawn.x", (int)l.getX());
    		    		getConfig().set(args[1] + ".lobbyspawn.y", (int)l.getY());
    		    		getConfig().set(args[1] + ".lobbyspawn.z", (int)l.getZ());
    		    		getConfig().set(args[1] + ".lobbyspawn.world", p.getWorld().getName());
    		    		this.saveConfig();
    		    		sender.sendMessage(getConfig().getString("strings.lobbycreated"));
 					}
 				}else if(action.equalsIgnoreCase("setspawn") && args.length > 2){
 					// setspawn
 					if(p.hasPermission("horseracing.setspawn")){
 						String arena = args[2];
 						String count = args[1];
 			    		Location l = p.getLocation();
 			    		getConfig().set(args[2] + ".spawn" + count + ".x", (int)l.getX());
 			    		getConfig().set(args[2] + ".spawn" + count + ".y", (int)l.getY());
 			    		getConfig().set(args[2] + ".spawn" + count + ".z", (int)l.getZ());
 			    		getConfig().set(args[2] + ".spawn" + count + ".world", p.getWorld().getName());
 			    		this.saveConfig();
 			    		sender.sendMessage(getConfig().getString("strings.spawn"));
 					}
 				}else if(action.equalsIgnoreCase("setspectate") && args.length > 1){
 					// setspectatespawn
 					if(p.hasPermission("horseracing.setspawn")){
 						String arena = args[1];
 			    		Location l = p.getLocation();
 			    		getConfig().set(args[1] + ".spectate.x", (int)l.getX());
 			    		getConfig().set(args[1] + ".spectate.y", (int)l.getY());
 			    		getConfig().set(args[1] + ".spectate.z", (int)l.getZ());
 			    		getConfig().set(args[1] + ".spectate.world", p.getWorld().getName());
 			    		this.saveConfig();
 			    		sender.sendMessage("§2Spectator platform successfully created!");
 					}
 				}else if(action.equalsIgnoreCase("spectate") && args.length > 1){
 					if(!args[1].equalsIgnoreCase("") && gamestarted.get(args[1]) && getConfig().isSet(args[1] + ".spectate")){
 						String arena = args[1];
 						if(getConfig().isSet(args[1] + ".spectate.world")){
 							Location l = new Location(Bukkit.getWorld(getConfig().getString(args[1] + ".spectate.world")), getConfig().getDouble(args[1] + ".spectate.x"), getConfig().getDouble(args[1] + ".spectate.y"), getConfig().getDouble(args[1] + ".spectate.z"));
 							p.teleport(l);
 							specp.put(p, arena);
 						}else{
 							p.sendMessage("§4This arena doesn't support spectating. Ask an operator to set up a platform by using /hr setspectate [arena].");
 						}
							sender.sendMessage("§2You are now spectating in "
									+ args[1]
									+ ". Use §3/hr leavespectate §2to leave the minigame.");
 					}else{
 						p.sendMessage("§4The game hasn't started yet!");
 					}
 				}else if(action.equalsIgnoreCase("leavespectate")){
 					if(specp.containsKey(p)){
 						if(getConfig().isSet(args[1] + ".lobbyspawn.world")){
 							String arena = specp.get(p);
 							Location l = as.getLocFromArena(arena, "lobbyspawn");
 							p.teleport(l);
 							specp.remove(p);
 						}
 					}else{
 						p.sendMessage("§4You aren't in spectating mode.");
 					}
 				}else if(action.equalsIgnoreCase("removerace") && args.length > 1){
 					// removearena
 					if(p.hasPermission("horseracing.remove")){
 						this.getConfig().set(args[1], null);
    	    			this.saveConfig();
    	    			sender.sendMessage(getConfig().getString("strings.raceremoved"));
 					}
 				}else if(action.equalsIgnoreCase("removespawn") && args.length > 2){
 					// removearena
 					if(p.hasPermission("horseracing.remove")){
 						String arena = args[1];
 						String count = args[2];
 						this.getConfig().set(arena + ".spawn" + args[2], null);
    	    			this.saveConfig();
    	    			sender.sendMessage(getConfig().getString("§4Spawnpoint removed."));
 					}
 				}else if(action.equalsIgnoreCase("setfinish") && args.length > 1){
 					if(p.hasPermission("horseracing.setfinish")){
	 					creation.put(p, args[1]);	
	 					p.getInventory().addItem(new ItemStack(Material.WOOD_SPADE, 1));
	 					p.updateInventory();
	 					p.sendMessage(getConfig().getString("strings.creation"));
 					}
 					
 				}else if(action.equalsIgnoreCase("leave")){
 					// leave
 					if(p.hasPermission("horseracing.leave")){
 						if(arenap.containsKey(p)){
 							if(!getConfig().getBoolean("config.arena_cycling")){
	 							/*if(p.isInsideVehicle()){
	 								p.getVehicle().remove();
	 							}
	 							final Player p_ = p;
		 						String arena = arenap.get(p);
		 						arenaspawn.remove(arena);
		 						final Location t = as.getLocFromArena(arena, "lobbyspawn");
		             			p.teleport(t);
		             			arenap.remove(p);
		             			p.sendMessage(getConfig().getString("strings.left"));
		             			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		            				//int secs = 11;
		            				@Override
		            	            public void run() {
		            					p_.teleport(t);
		            				}
		            			}, 20);
		             			
		             			Sign s_ = as.getSignFromArena(arena); 
		    			    	// update sign:
		                        if(s_ != null && s_.getLine(3) != ""){
		                        	String d = s_.getLine(3).split("/")[0];
		                        	int bef = Integer.parseInt(d);
		                        	if(bef > 0){
		                        		s_.setLine(3, Integer.toString(bef - 1) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
		                        		s_.update();
		                        		if(bef == 1){
		                        			if(canceltask.get(p) != null){
			                        			getServer().getScheduler().cancelTask(canceltask.get(p));
		                        			}
			                        		s_.setLine(2, "§2Join");
				                        	s_.update();
				                        	if(s_ != null){
												s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
					                    		s_.update();
											}
			                        	}
		                        	}
		                        }
		                        //p.getInventory().setContents(pinv.get(p));*/
 								as.playerLeaveEvent(p);
 							}else{ // arena_cycling true ->>
 								if(p.isInsideVehicle()){
	 								p.getVehicle().remove();
	 							}
	 							final Player p_ = p;
		 						String arena = arenas.get(cyclep.get(p));
		 						arenaspawn.remove(arena);
		 						final Location t = as.getLocFromArena(arena, "lobbyspawn");
		             			p.teleport(t);
		             			arenap.remove(p);
		             			p.sendMessage(getConfig().getString("strings.left"));
		             			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		            				//int secs = 11;
		            				@Override
		            	            public void run() {
		            					p_.teleport(t);
		            				}
		            			}, 20);
		             			
		             			Sign s_ = as.getSignFromArena("config.cycle"); 
		    			    	// update sign:
		                        if(s_ != null && s_.getLine(3) != ""){
		                        	String d = s_.getLine(3).split("/")[0];
		                        	int bef = Integer.parseInt(d);
		                        	if(bef > 0){
		                        		s_.setLine(3, Integer.toString(bef - 1) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
		                        		s_.update();
		                        		if(bef == 1){
		                        			if(canceltask.get(p) != null){
			                        			getServer().getScheduler().cancelTask(canceltask.get(p));
		                        			}
		                        			for(Player cp : canceltask.keySet()){
		    		             				if(canceltask.get(cp) != null){
		    		             					try{
		    		             						getServer().getScheduler().cancelTask(canceltask.get(p));
		    		             					}catch(Exception e){
		    		             						
		    		             					}
		    		             				}
		    		             			}
			                        		s_.setLine(2, "§2Join");
				                        	s_.update();
				                        	if(s_ != null){
												s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
					                    		s_.update();
											}
			                        	}
		                        	}
		                        	if(bef < 2){
			                        	secs_updater.remove(arena);
				                        secs_.put(arena, getConfig().getInt("config.starting_cooldown"));
			                        }
			                        
		                        }
		                        p.getInventory().setContents(pinv.get(p));
		                        //secs_updater.remove(p);
		                        //secs_updater.clear();
 							}
 						}
 					}
 				}else if(action.equalsIgnoreCase("cancel")){
 					if(args.length > 1){
 						String arena = args[1];
		
				    	Sign s_ = as.getSignFromArena(arena);
				    	
				    	// update sign:
	                    if(s_ != null && s_.getLine(3) != ""){
	                    	s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
	                    	s_.setLine(2, "§2Join");
	                    	s_.update();
	                    }
	                    
				    	final Location t2 = as.getLocFromArena(arena, "lobbyspawn");

						ArrayList<Player> arenaplayers = new ArrayList<Player>(getKeysByValue(arenap, arena));
						for(Player ap : arenaplayers){
							ap.getVehicle().remove();
							// tp away
	    			    	arenap.remove(ap);
					    	ap.teleport(t2);
					    	final Player p_ = ap;
					    	
					    	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	            				//int secs = 11;
	            				
	            				@Override
	            	            public void run() {
	            					p_.teleport(t2);
	            				}
	            			}, 20);
					    	
					    	if(canceltask.get(ap) != null){
                    			getServer().getScheduler().cancelTask(canceltask.get(ap));
                			}
						}
						
						if(s_ != null){
							s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
                    		s_.update();
						}
						arenaspawn.remove(arena);
						gamestarted.put(arena, false);
 					}
 				}else if(action.equalsIgnoreCase("join")){
 					if(!getConfig().getBoolean("config.arena_cycling")){
	 					if(args.length > 1){
	 						final String arena = args[1];
	 						final Player p_ = p;
	 						boolean cont1 = true;
	 						
	 						if(validArena(arena)){
		 						int size = as.getSpawnsFromArena(arena).size();
		 						
		 						Sign s_ = as.getSignFromArena(arena);
						    	// update sign:
			                    as.handleSign(s_, arena);
		 						
		 						final Sign s = s_;
			                    
		 						if(s != null && s.getLine(2).equalsIgnoreCase("§2Starting") || s.getLine(2).equalsIgnoreCase("§2Join")){
			                		// update sign:
				                    if(s.getLine(3) != ""){
				                    	String d = s.getLine(3).split("/")[0];
				                    	int bef = Integer.parseInt(d);
				                    	
				                    	//getLogger().info(Integer.toString(bef) + " " + Integer.toString(keys.size()));
				                    	if(bef < size){
				                    		if(!arenaspawn.containsKey(arena)){
				    	                		arenaspawn.put(arena, 1);
				    	                		pspawn.put(p, 1);
				    	                	}else{
				    	                		arenaspawn.put(arena, arenaspawn.get(arena) + 1);
				    	                		pspawn.put(p, arenaspawn.get(arena));
				    	                	}
				                    		s.setLine(3, Integer.toString(bef + 1) + "/" + Integer.toString(size));
				                    		s.update();
				                    		if(bef > (getConfig().getInt("config.min_players") - 2)){ // there was one player in there, bef > 0
				                    			//start the cooldown for start (10 secs)
				                    			if(!secs_updater.containsKey(arena) && !gamestarted.get(arena)){
				                    			int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				                    				@Override
				    	        		            public void run() {	
					        		                	as.countdownfunc(arena, p_, s);
				                					}
				    	        	            }, 20, 20);
				                    			canceltask.put(p, id);
				                    			}
				                    			
				                    		}
				                    		
				                    	}else{
				                    		cont1 = false;
				                    	}
				                    }
		                		}else{
		                			cont1 = false;
		                		}
		 						
		 						if(cont1){
		 							pinv.put(p, p.getInventory().getContents());
		 							p.getInventory().clear();
		                    		p.updateInventory();
		 	                		// take money
		 	                		if(!as.ManageMoney(p, "entry")){
		 	                			cont1 = false;
		 	                		}
		 	                		
		 	                		// teleport and spawn horse
		 		                	arenap.put(p, arena);
		 		                	
		 		                	p.sendMessage("§2You have entered a Horse Race!");
		 		                	
		 		                	String count = Integer.toString(arenaspawn.get(arena));
		 		                	
		 		                	//final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn" + count + ".world")), getConfig().getDouble(arena + ".spawn" + count + ".x"), getConfig().getDouble(arena + ".spawn" + count + ".y"), getConfig().getDouble(arena + ".spawn" + count + ".z"));
		 		                	final Location t = as.getLocFromArena(arena, "spawn" + count);
		 		                	p.teleport(t);
		 		                	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		 		                		public void run(){
		 		                			as.spawnHorse(t, p_);
		 		                		}
		 		                	}, 20);
		 	                	} 
	 						}else{
	 							p.sendMessage("§4This arena is not set up properly!");
	 						}
	 						
	 						
	 					}
 					}
 					
 				}else if(action.equalsIgnoreCase("bet")){
 					// list
					if(args.length > 2){
						if(getConfig().getBoolean("config.enable_betting")){
							String amount = args[1];
							String player = args[2];
							int amount_ = 0;
							boolean cont1 = true;
							try{
								amount_ = Integer.parseInt(amount);
							}catch(Exception e){
								sender.sendMessage("§4Usage: /hr bet [amount] [name]");
								cont1 = false;
							}
							if(cont1){
								if(amount_ < getConfig().getInt("config.max_bet_amount")){
									//TODO try
									if(econ.getBalance(p.getName()) >= amount_){
	 		                    		EconomyResponse r = econ.withdrawPlayer(p.getName(), amount_);
		 		  	                    if(!r.transactionSuccess()) {
		 		  	                    	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
		 		  	                    }
									}
									bet_player.put(p, Bukkit.getPlayer(player));
									bet_amount.put(p, amount_);
									sender.sendMessage("§2Thank you for your bet! If this players wins, you'll get the doubled amount back.");
								}else{
									sender.sendMessage("§4You can't bet so much! The max amount is " + Integer.toString( getConfig().getInt("config.max_bet_amount")) + ".");
								}	
							}
							
						}
					}
 				}else if(action.equalsIgnoreCase("list")){
 					// list
 					if(p.hasPermission("horseracing.list")){
 						p.sendMessage("§3 -- Arenas --");
 						ArrayList<String> keys = new ArrayList<String>();
    			        keys.addAll(getConfig().getKeys(false));
    			        try{
    			        	keys.remove("config");
    			        	keys.remove("strings");
    			        	keys.remove("shop");
    			        	keys.remove("stats");
    			        }catch(Exception e){
    			        	
    			        }
    			        for(int i = 0; i < keys.size(); i++){
    			        	if(!keys.get(i).equalsIgnoreCase("config") && !keys.get(i).equalsIgnoreCase("strings")){
    			        		sender.sendMessage("§2" + keys.get(i));
    			        	}
    			        }
 					}
 				}else if(action.equalsIgnoreCase("reload")){
 					if(sender.hasPermission("horseracing.reload")){
	    					this.reloadConfig();
	    					if(getConfig().getBoolean("config.use_gambling")){
	    						gambling = true;
	    					}else{
	    						gambling = false;
	    					}
	    					sender.sendMessage(getConfig().getString("strings.reload"));
 					}else{
 						sender.sendMessage(getConfig().getString("strings.nopermission"));
 					}
 				}else if(action.equalsIgnoreCase("reset") && args.length > 0){
 					if(args.length > 1){
    					if (sender.hasPermission("boatgame.cleararena"))
    	                {
	    	    			String arena = args[1];
	    	    			
	    	    			if(getConfig().contains(arena)){
		    	    			// tp players out
		    	    			for(final Player p_ : arenap.keySet()) {
		    	    				if(arenap.get(p_).equalsIgnoreCase(arena)){
		    	    					Double x = getConfig().getDouble(arena + ".lobbyspawn.x");
		        				    	Double y = getConfig().getDouble(arena + ".lobbyspawn.y");
		        				    	Double z = getConfig().getDouble(arena + ".lobbyspawn.z");
		        			    		World w = Bukkit.getWorld(getConfig().getString(arena + ".lobbyspawn.world"));
		        				    	final Location t = new Location(w, x, y, z);
		        			    		
		        				    	if(p_.isInsideVehicle()){
		        				    		p_.getVehicle().remove();	
		        				    	}
		        				    	
		        				    	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		    	            				@Override
		    	            	            public void run() {
		    	            					p_.teleport(t);
		    	            				}
		    	            			}, 20);

		        			    		p.sendMessage("§4The arena just got reset by an operator - leaving game..");
		    	    				}
		    	    			}
		    	    			
		    	    			while (arenap.values().remove(arena));
		    	    			gamestarted.put(arena, false);
	        			    	arenaspawn.remove(arena);
	        			    	
	        			    	Location b = new Location(Bukkit.getWorld(getConfig().getString(arena + ".sign.world")), getConfig().getDouble(arena + ".sign.x"),getConfig().getDouble(arena + ".sign.y"), getConfig().getDouble(arena + ".sign.z"));
	        			    	Sign s = (Sign)Bukkit.getWorld(getConfig().getString(arena + ".sign.world")).getBlockAt(b).getState();
	        			    	// update sign: 
	        		            if(s != null && s.getLine(3) != ""){
	    		            		s.setLine(3, Integer.toString(0) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
	    		            		s.setLine(2, "§2Join");
	    		            		s.update();
	    		            		secs_.remove(arena);
	        		            }
	        		            sender.sendMessage("§2Arena reset.");	
	    	    			}else{
	    	    				sender.sendMessage("§4This arena couldn't be found.");
	    	    			}
    	                }else{
    	                	sender.sendMessage(getConfig().getString("strings.nopermission"));
    	                }	
					}else{
						sender.sendMessage("§4Please provide an arenaname! Usage: /sb reset [name]");
					}
				}else if(action.equalsIgnoreCase("leaderboards") || action.equalsIgnoreCase("lb")){
 					ArrayList<String> keys = new ArrayList<String>();
 					boolean cont1 = true;
 					try{
 						keys.addAll(getConfig().getConfigurationSection("stats").getKeys(false));
 					}catch (Exception e){
 						cont1 = false;
 						sender.sendMessage("§4There are no recorded games yet.");
 					}
 			        
 					if(cont1){
	 					HashMap<String, Integer> pwon = new HashMap<String, Integer>();
	 			    	
	 			        for(String key : keys){
	 			        	pwon.put(key, getConfig().getInt("stats." + key + ".won"));
	 			        }
	 			        HashMap<String, Integer> pwon_ = new HashMap<String, Integer>();
	 			        pwon_.putAll(this.sortByValue(pwon, p));
 					}
 			    	
				}else if(action.equalsIgnoreCase("shop")){
					Player p_ = (Player)sender;
					sender.sendMessage("§3 -- HorseRacing Shop -- ");
					if(!getConfig().contains("shop." + p_.getName() + ".jump")){
						getConfig().set("shop." + p_.getName() + ".jump", 0);
					}
					if(!getConfig().contains("shop." + p_.getName() + ".speed")){
						getConfig().set("shop." + p_.getName() + ".speed", 0);
					}
					if(!getConfig().contains("shop." + p_.getName() + ".barding")){
						getConfig().set("shop." + p_.getName() + ".barding", false);
					}
					sender.sendMessage("§2Jump potions: " + Integer.toString(getConfig().getInt("shop." + p_.getName() + ".jump")));
					sender.sendMessage("§2Speed potions: " + Integer.toString(getConfig().getInt("shop." + p_.getName() + ".speed")));
					sender.sendMessage("§2Barding: " + Boolean.toString(getConfig().getBoolean("shop." + p_.getName() + ".barding")));
 					
					final Plugin plugin = this;
 					final Main main = this;
 					IconMenu iconm = new IconMenu("test", 9, new IconMenu.OptionClickEventHandler() {
 		    			@Override
 		                public void onOptionClick(IconMenu.OptionClickEvent event) {
 		                    String d = event.getName();
 		                    Player p = event.getPlayer();
 		                    if(d.equalsIgnoreCase("Jump Effect")){
 		                    	int price = getConfig().getInt("shop.jumppotion_price");
 		                    	if(main.econ.getBalance(p.getName()) >= price){
 		                    		EconomyResponse r = main.econ.withdrawPlayer(p.getName(), price);
	 		  	                    if(!r.transactionSuccess()) {
	 		  	                    	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
	 		  	                    }
	 		  	                    if(getConfig().contains("shop." + p.getName() + ".jump")){
	 		  	                    	getConfig().set("shop." + p.getName() + ".jump", getConfig().getInt("shop." + p.getName() + ".jump") + 3);
	 		  	                    }else{
	 		  	                    	getConfig().set("shop." + p.getName() + ".jump", 3);
	 		  	                    }
 		                    		p.sendMessage("§2You bought a Jump Effect!");
 		                            plugin.saveConfig();
 		                    	}else{
 		                    		p.sendMessage("§4You don't have enough money!");
 		                    	}
 		                    }else if(d.equalsIgnoreCase("Speed Effect")){
 		                    	int price = getConfig().getInt("shop.speedpotion_price");
 		                    	if(main.econ.getBalance(p.getName()) >= price){
 		                    		EconomyResponse r = main.econ.withdrawPlayer(p.getName(), price);
	 		  	                    if(!r.transactionSuccess()) {
	 		  	                    	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
	 		  	                    }
	 		  	                    if(getConfig().contains("shop." + p.getName() + ".speed")){
	 		  	                    	getConfig().set("shop." + p.getName() + ".speed", getConfig().getInt("shop." + p.getName() + ".speed") + 3);
	 		  	                    }else{
	 		  	                    	getConfig().set("shop." + p.getName() + ".speed", 3);
	 		  	                    }
 		                    		p.sendMessage("§2You bought a Speed Effect!");
 		                            plugin.saveConfig();
 		                    	}else{
 		                    		p.sendMessage("§4You don't have enough money!");
 		                    	}
 		                    }else if(d.equalsIgnoreCase("Diamond Barding")){
 		                    	int price = getConfig().getInt("shop.barding_price");
 		                    	if(main.econ.getBalance(p.getName()) > price){
 		                    		EconomyResponse r = main.econ.withdrawPlayer(p.getName(), price);
 	 		  	                    if(!r.transactionSuccess()) {
 	 		  	                    	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
 	 		  	                    }
 		                    		getConfig().set("shop." + p.getName() + ".barding", true);
 		                    		p.sendMessage("§2You bought a Diamond Barding!");
 		                            plugin.saveConfig();
 		                    	}else{
 		                    		p.sendMessage("§4You don't have enough money!");
 		                    	}
 		                    }
 		                    event.setWillClose(true);
 		                }
 		            }, this)
 		    		.setOption(3, new ItemStack(Material.POTION, 1), "Jump Effect",  "Jump Effect for your horse! (Three-time use) [COST: " + Integer.toString(getConfig().getInt("shop.")))
 		            .setOption(4, new ItemStack(Material.POTION, 1), "Speed Effect", "Speed Effect for your horse! (Three-time use)")
 		            .setOption(5, new ItemStack(Material.DIAMOND_BARDING, 1), "Diamond Barding", "A nice looking diamond barding for your horse! (Forever)");
 		        	
 		        	iconm.open((Player) sender);
				}else if(action.equalsIgnoreCase("stats") && args.length > 1){
 					String pname = args[1];
					sender.sendMessage("§3HorseRacing Stats for " + pname + ":");
					sender.sendMessage("§2Won games: " + Integer.toString(getConfig().getInt("stats." + pname + ".won")));
					sender.sendMessage("§2Lost games: " + Integer.toString(getConfig().getInt("stats." + pname + ".lost")));
				}else{
 					sender.sendMessage(getConfig().getString("strings.nothing"));
 				}
 			}
 		}
 		return true;
 	}
 	return false;
	}
	
	
	static int maxcount = 5;
	public static Map<String, Integer> sortByValue(Map<String, Integer> map, Player p) {
		p.sendMessage("§3 -- Leaderboards --");
		
        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

            public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
                return (m2.getValue()).compareTo(m1.getValue());
            }
        });

        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
        	if(maxcount > 0){
        		p.sendMessage("§2" + entry.getKey() + " | " + entry.getValue() + "");
        	}
        	maxcount -= 1;
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		tpthem.put(event.getPlayer().getName(), arenap.get(event.getPlayer()));
		as.playerLeaveEvent(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event){
		as.playerLeaveEvent(event.getEntity());
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		getLogger().info(tpthem.get(event.getPlayer().getName()) + " joined");
		if(tpthem.containsKey(event.getPlayer().getName())){
			String arena = tpthem.get(event.getPlayer().getName());
			final Player p = event.getPlayer();
			final Location t = as.getLocFromArena(arena, "lobbyspawn");
			p.teleport(t);
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
	            public void run() {
					p.teleport(t);
				}
			}, 20);
		}
	}
	
	
	@EventHandler
	public void onSignUse(PlayerInteractEvent event)
	{	
	    if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
	    {
	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
	        {
	            final Sign s = (Sign) event.getClickedBlock().getState();
                if (s.getLine(0).equalsIgnoreCase("§2[HorseRace]") && !s.getLine(1).equalsIgnoreCase(""))
                {
                	if(s.getLine(1).equalsIgnoreCase("[cycle]") && getConfig().getBoolean("config.arena_cycling")){
                		final String arena = arenas.get(0);
                    	final Player p = event.getPlayer();
                    	cyclep.put(p, 0);
                    	boolean cont1 = true;
                    	
                		if(s.getLine(2).equalsIgnoreCase("§2Starting") || s.getLine(2).equalsIgnoreCase("§2Join")){
	                		// update sign:
		                    if(s.getLine(3) != ""){
		                    	String d = s.getLine(3).split("/")[0];
		                    	int bef = Integer.parseInt(d);
		                    	
		                    	//getLogger().info(Integer.toString(bef) + " " + Integer.toString(keys.size()));
		                    	if(bef < as.getSpawnsFromArena(arena).size()){
		                    		if(!arenaspawn.containsKey(arena)){
		    	                		arenaspawn.put(arena, 1);
		    	                		pspawn.put(p, 1);
		    	                	}else{
		    	                		arenaspawn.put(arena, arenaspawn.get(arena) + 1);
		    	                		pspawn.put(p, arenaspawn.get(arena));
		    	                	}
		                    		s.setLine(3, Integer.toString(bef + 1) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
		                    		s.update();
		                    		if(bef > (getConfig().getInt("config.min_players") - 2)){ // there was one player in there, bef > 0
		                    			//start the cooldown for start (10 secs)
	        		                	as.logMessage("0LOG canceltask " + Integer.toString(canceltask.size()));
	        		                	as.logMessage("0LOG secs_updater " + Integer.toString(secs_updater.size()));
		                    			if(!secs_updater.containsKey(arena)){
		                    				secs_updater.put(arena, p);
			                    			int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			                    				@Override
			    	        		            public void run() {	
			                    					as.countdownfunc(arena, p, s);	
			                					}
			    	        	            }, 20, 20);
			                    			canceltask.put(event.getPlayer(), id);
		                    			}
		                    		}
		                    		
		                    	}else{
		                    		cont1 = false;
		                    	}
		                    }
                		}else{
                			cont1 = false;
                		}
                    	
                    	
                    	if(cont1){
                    		// take money
                    		as.ManageMoney(p, "entry");
                    		
                    		// teleport and spawn horse
    	                	arenap.put(event.getPlayer(), arena);
    	                	
    	                	pinv.put(p, p.getInventory().getContents());
    	                	
    	                	event.getPlayer().sendMessage("§2You have entered a Horse Race!");
    	                	
    	                	String count = Integer.toString(arenaspawn.get(arena));
    	                	
    	                	final Location t = as.getLocFromArena(arena, "spawn" + count);
    	                	p.teleport(t);
    	                	
    	                	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 		                		public void run(){
 		                			as.spawnHorse(t, p);
 		                		}
 		                	}, 20);
                    	}
                    }else if(!getConfig().getBoolean("config.arena_cycling")){
                    	final String arena = s.getLine(1).substring(2);
                    	final Player p = event.getPlayer();
                    	boolean cont1 = true;
                    	
                    	if(!arena.equalsIgnoreCase("") && validArena(arena)){           
                    		if(s.getLine(2).equalsIgnoreCase("§2Starting") || s.getLine(2).equalsIgnoreCase("§2Join")){
    	                		// update sign:
    		                    if(s.getLine(3) != ""){
    		                    	String d = s.getLine(3).split("/")[0];
    		                    	int bef = Integer.parseInt(d);
    		                    	
    		                    	//getLogger().info(Integer.toString(bef) + " " + Integer.toString(keys.size()));
    		                    	if(bef < as.getSpawnsFromArena(arena).size()){
    		                    		if(!arenaspawn.containsKey(arena)){
    		    	                		arenaspawn.put(arena, 1);
    		    	                		pspawn.put(p, 1);
    		    	                	}else{
    		    	                		arenaspawn.put(arena, arenaspawn.get(arena) + 1);
    		    	                		pspawn.put(p, arenaspawn.get(arena));
    		    	                	}
    		                    		s.setLine(3, Integer.toString(bef + 1) + "/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
    		                    		s.update();
    		                    		if(bef > (getConfig().getInt("config.min_players") - 2)){ // there was one player in there, bef > 0
    		                    			//start the cooldown for start (10 secs)
    	        		                	as.logMessage("0LOG canceltask " + Integer.toString(canceltask.size()));
    	        		                	as.logMessage("0LOG secs_updater " + Integer.toString(secs_updater.size()));
    		                    			if(!secs_updater.containsKey(arena)){
    		                    				
    		                    			int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
    		                    				@Override
    		    	        		            public void run() {	
    		                    					as.countdownfunc(arena, p, s);	
    		                					}
    		    	        	            }, 20, 20);
    		                    			canceltask.put(event.getPlayer(), id);
    		                    			}
    		                    		}
    		                    		
    		                    	}else{
    		                    		cont1 = false;
    		                    	}
    		                    }
                    		}else{
                    			cont1 = false;
                    		}
                    		
    	                	
                    	}else{
                    		p.sendMessage("§4There's no such arena or the arena itself is set up wrong!");
                    		cont1 = false;
                    	}
                    	
                    	
                    	if(cont1){
                    		pinv.put(p, p.getInventory().getContents());
                    		p.getInventory().clear();
                    		p.updateInventory();
                    		// take money
                    		as.ManageMoney(p, "entry");
                    		
                    		// teleport and spawn horse
    	                	arenap.put(event.getPlayer(), arena);
    	                	
    	                	event.getPlayer().sendMessage("§2You have entered a Horse Race!");
    	                	
    	                	String count = Integer.toString(arenaspawn.get(arena));
    	                	
    	                	final Location t = as.getLocFromArena(arena, "spawn" + count);
    	                	p.teleport(t);
    	                	
    	                	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 		                		public void run(){
 		                			as.spawnHorse(t, p);
 		                		}
 		                	}, 20);
                    	}
                    	
                    	//Auto fix: If player rightclicks on a screwed boatgame sign, it "repairs" itself.
                    	//getLogger().info("ARENAP COUNT: " + Integer.toString(arenap.values().size()));
                    	// no players in given arena anymore -> update sign
                    	if(!arenap.values().contains(arena)){
    	                	s.setLine(2, "§2Join");
    	                	s.setLine(3, "0/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
    	                	s.update();
                    	}
                    }
                }
	        }
	    }
	}
	
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		if (event.getLine(0).toLowerCase().contains("[horserace]") && p.hasPermission("horseracing.sign")) {
			event.setLine(0, "§2[HorseRace]");
			if(event.getLine(1).equalsIgnoreCase("[cycle]") && getConfig().getBoolean("config.arena_cycling")){
				event.setLine(1, "[Cycle]");
            	
            	event.setLine(2, "§2Join");
            	event.setLine(3, "0/" + Integer.toString(as.getSpawnsFromArena(arenas.get(0)).size()));
            	
            	getConfig().set("config.cycle.sign.world", p.getWorld().getName());
            	getConfig().set("config.cycle.sign.x", event.getBlock().getX());
            	getConfig().set("config.cycle.sign.y", event.getBlock().getY());
            	getConfig().set("config.cycle.sign.z", event.getBlock().getZ());
            	this.saveConfig();
			}else{
				if (!event.getLine(1).equalsIgnoreCase("") && getConfig().contains(event.getLine(1))){
					String arena = event.getLine(1);
					event.setLine(1, "§5" + arena);
	            	
	            	event.setLine(2, "§2Join");
	            	event.setLine(3, "0/" + Integer.toString(as.getSpawnsFromArena(arena).size()));
	            	
	            	getConfig().set(arena + ".sign.world", p.getWorld().getName());
	            	getConfig().set(arena + ".sign.x", event.getBlock().getX());
	            	getConfig().set(arena + ".sign.y", event.getBlock().getY());
	            	getConfig().set(arena + ".sign.z", event.getBlock().getZ());
	            	this.saveConfig();
				}else{
					event.getPlayer().sendMessage("§4Unfortunately, this arena was not found!");
					event.getBlock().breakNaturally();
				}
			}
			
		}
	}
	
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
	
	public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        Set<T> keys = new HashSet<T>();
        for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
   }

	@EventHandler
	public void onmove(PlayerMoveEvent event){
		if(arenap.containsKey(event.getPlayer())){
			final Player p = event.getPlayer();
			String aren = arenap.get(p);
			
			if(!gamestarted.get(aren)){
				//event.setCancelled(true);
				String spawnstr = "spawn" + Integer.toString(pspawn.get(p));
				Location currentspawn = as.getLocFromArena(aren, spawnstr);
	    		
	    		Location t1 = p.getLocation();
	    		final Location t2 = currentspawn;
	    		final Player p_ = p;
	    		
	    		if(t1.getX() - t2.getX() > 2 || t1.getX() - t2.getX() < -2  || t1.getZ() - t2.getZ() > 2 || t1.getZ() - t2.getZ() < -2){
		    		if(p.isInsideVehicle()){
			    		p.getVehicle().remove();
		    			p.teleport(t2);
		    			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		    				public void run(){        	                	
        	                	as.spawnHorse(t2, p_);
		    				}
		    			}, 10);
		    		}
	    		}
			}else if(gamestarted.get(aren)){
				if(!p.isInsideVehicle()){
					as.spawnHorse(p.getLocation(), p);
				}
				
				int x = event.getFrom().getBlockX();
				int y = event.getFrom().getBlockY();
				int z = event.getFrom().getBlockZ();
				Location loc = new Location(event.getFrom().getWorld(), x, y, z);
				
				Location loc1 = new Location(Bukkit.getWorld(getConfig().getString(aren + ".finishline.world")), getConfig().getInt(aren + ".finishline.x1"), getConfig().getInt(aren + ".finishline.y1"), getConfig().getInt(aren + ".finishline.z1"));
				Location loc2 = new Location(Bukkit.getWorld(getConfig().getString(aren + ".finishline.world")), getConfig().getInt(aren + ".finishline.x2"), getConfig().getInt(aren + ".finishline.y2"), getConfig().getInt(aren + ".finishline.z2"));
				
				Cuboid c = new Cuboid(loc1, loc2);
				
				secs_updater.remove(aren);
				
				if(c.containsLoc(loc)){  // CROSSED FINISH LINE
					if(getConfig().getBoolean("config.arena_cycling")){
						int currentcycle = cyclep.get(p);
						as.logMessage("NLOG " + Integer.toString(cyclep.get(p)));
						as.logMessage("NLOG " + Integer.toString(arenas.size()));
						as.logMessage("NLOG " + arenas.get(currentcycle));
						if(currentcycle < max_cycle - 1){
							// get new arena and tp to that arena spawn, then start countdown
							cyclep.put(p, currentcycle + 1);
							final String arena = arenas.get(currentcycle + 1);
							final String befarena = arenas.get(currentcycle);
							if(!arenaspawn.containsKey(arena)){
    	                		arenaspawn.put(arena, 1);
    	                		pspawn.put(p, 1);
    	                	}else{
    	                		arenaspawn.put(arena, arenaspawn.get(arena) + 1);
    	                		pspawn.put(p, arenaspawn.get(arena));
    	                	}
							p.sendMessage("§2You won this race!");
							if(getConfig().getBoolean("config.use_economy")){
								//give money
								as.ManageMoney(p, "win");
							}else{
								//give item
								p.getInventory().addItem(new ItemStack(Material.getMaterial(getConfig().getInt("config.itemid")), getConfig().getInt("config.itemamount")));
							}
							int countspawn = 1;
							ArrayList<Player> arenaplayers = new ArrayList<Player>(getKeysByValue(arenap, befarena));
							for(Player ap : arenaplayers){
								ap.getVehicle().remove();
								// tp away
		    			    	if(ap != p){
		    			    		ap.sendMessage(getConfig().getString("strings.lost"));
		    			    	}
		    			    	pspawn.put(ap, countspawn);
						    	final Player p_ = ap;
						    	final Location t2 = as.getLocFromArena(arena, "spawn" + Integer.toString(countspawn));
						    	countspawn += 1;
						    	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		            				@Override
		            	            public void run() {
		            					p_.teleport(t2);
		            					as.spawnHorse(t2, p_);
		            				}
		            			}, 20);
						    	arenap.put(ap, arena);
							}
							gamestarted.put(arena, false);
							gamestarted.put(arenas.get(currentcycle), false);
							arenap.put(p, arena);
							if(!secs_updater.containsKey(arena)){
								secs_updater.put(arena, p);
                    			int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                    				@Override
    	        		            public void run() {	
                    					as.countdownfunc(arena, p, null);
                					}
    	        	            }, 20, 20);
                    			canceltask.put(event.getPlayer(), id);
                    		}
							//arenaspawn.remove(arena);
						}else{
							final String arena = arenas.get(currentcycle);
							// tp to lobby of last arena
							p.sendMessage("§2You won the race!");
							if(getConfig().getInt("stats." + p.getName() + ".won") > 0){
								getConfig().set("stats." + p.getName() + ".won", getConfig().getInt("stats." + p.getName() + ".won") + 1);
								this.saveConfig();
							}else{
								getConfig().set("stats." + p.getName() + ".won", 1);
								this.saveConfig();
							}
							if(getConfig().getBoolean("config.use_economy")){
								//give money
								as.ManageMoney(p, "win");
							}else{
								//give item
								p.getInventory().addItem(new ItemStack(Material.getMaterial(getConfig().getInt("config.itemid")), getConfig().getInt("config.itemamount")));
							}
							
							gamestarted.put(arena, false);
							cyclep.put(p, 0);
							
							int size = as.getSpawnsFromArena(arena).size();
					    	
					    	Sign s_ = as.getSignFromArena("config.cycle");
					    	// update sign:
		                    as.handleSign(s_, arena);
		                    
					    	final Location t2 = as.getLocFromArena(arena, "lobbyspawn");
					    	p.getVehicle().remove();
		                    arenap.remove(p);
		                    
		                    final Player p_1 = p;
		                    
		                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		        				@Override
		        	            public void run() {
		        					p_1.teleport(t2);
		        				}
		        			}, 20);
		                    
		                    p.getInventory().setContents(pinv.get(p));
							
							ArrayList<Player> arenaplayers = new ArrayList<Player>(getKeysByValue(arenap, arena));
							for(Player ap : arenaplayers){
								ap.getVehicle().remove();
		    			    	ap.sendMessage(getConfig().getString("strings.lost"));
		    			    	
		    			    	if(getConfig().getInt("stats." + ap.getName() + ".lost") > 0){
									getConfig().set("stats." + ap.getName() + ".lost", getConfig().getInt("stats." + ap.getName() + ".lost") + 1);
									this.saveConfig();
		    			    	}else{
									getConfig().set("stats." + ap.getName() + ".lost", 1);
									this.saveConfig();
								}
		    			    	arenap.remove(ap);
		    			    	
						    	ap.teleport(t2);
		    			    	
						    	final Player p_ = ap;
						    	
						    	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		            				@Override
		            	            public void run() {
		            					p_.teleport(t2);
		            				}
		            			}, 20);
						    	
						    	as.handleSign(s_, arena);
						    	ap.getInventory().setContents(pinv.get(ap));
							}
							
							if(s_ != null){
								s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(size));
	                    		s_.update();
							}
							arenaspawn.clear();
							pspawn.clear();
						}
					}else{// end of if arena_cycling
						if(getConfig().getBoolean("config.use_rounds_system") && gamestarted.get(aren)){
							final String arena = aren;
							int round = rounds.get(arena);
							int max_round = getConfig().getInt("config.rounds");
							Player winner = p;
							if(round < max_round){
								winner.sendMessage("§2You won the round!");
								if(getConfig().getBoolean("config.use_economy")){
									//give money
									as.ManageMoney(p, "win");
								}else{
									//give item
									p.getInventory().addItem(new ItemStack(Material.getMaterial(getConfig().getInt("config.itemid")), getConfig().getInt("config.itemamount")));
								}
								int countspawn = 1;
								ArrayList<Player> arenaplayers = new ArrayList<Player>(getKeysByValue(arenap, arena));
								for(Player ap : arenaplayers){
									ap.getVehicle().remove();
									// tp away
			    			    	if(ap != winner){
			    			    		ap.sendMessage(getConfig().getString("strings.lost"));
			    			    	}
							    	final Player p_ = ap;
							    	final Location t2 = as.getLocFromArena(arena, "spawn" + Integer.toString(countspawn));
							    	countspawn += 1;
							    	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			            				@Override
			            	            public void run() {
			            					p_.teleport(t2);
			            					as.spawnHorse(t2, p_);
			            				}
			            			}, 20);
								}
								gamestarted.put(aren, false);
								if(!secs_updater.containsKey(arena)){
	                    			int id = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
	                    				//int secs = 11;
	                    				
	                    				@Override
	    	        		            public void run() {	
	                    					as.countdownfunc(arena, p, null);
	                					}
	    	        	            }, 20, 20);
	                    			canceltask.put(event.getPlayer(), id);
	                    			}
								rounds.put(arena, round + 1);
								getLogger().info("ROUND: " + Integer.toString(round));
							}else{
								rounds.put(arena, 0);
								p.sendMessage("§2You won the race!");
								if(getConfig().getInt("stats." + p.getName() + ".won") > 0){
									getConfig().set("stats." + p.getName() + ".won", getConfig().getInt("stats." + p.getName() + ".won") + 1);
									this.saveConfig();
								}else{
									getConfig().set("stats." + p.getName() + ".won", 1);
									this.saveConfig();
								}
								if(getConfig().getBoolean("config.use_economy")){
									//give money
									as.ManageMoney(p, "win");
								}else{
									//give item
									p.getInventory().addItem(new ItemStack(Material.getMaterial(getConfig().getInt("config.itemid")), getConfig().getInt("config.itemamount")));
								}
								
								gamestarted.put(aren, false);
								
								int size = as.getSpawnsFromArena(arena).size();
						    	
						    	Sign s_ = as.getSignFromArena(arena);
						    	// update sign:
			                    as.handleSign(s_, arena);
			                    
						    	final Location t2 = as.getLocFromArena(arena, "lobbyspawn");
						    	p.getVehicle().remove();
			                    arenap.remove(p);
			                    
			                    final Player p_1 = p;
			                    
			                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			        				@Override
			        	            public void run() {
			        					p_1.teleport(t2);
			        				}
			        			}, 20);
								
			                    p.getInventory().setContents(pinv.get(p));
			                    
								ArrayList<Player> arenaplayers = new ArrayList<Player>(getKeysByValue(arenap, arena));
								for(Player ap : arenaplayers){
									ap.getVehicle().remove();
			    			    	ap.sendMessage(getConfig().getString("strings.lost"));
			    			    	
			    			    	if(getConfig().getInt("stats." + ap.getName() + ".lost") > 0){
										getConfig().set("stats." + ap.getName() + ".lost", getConfig().getInt("stats." + ap.getName() + ".lost") + 1);
										this.saveConfig();
			    			    	}else{
										getConfig().set("stats." + ap.getName() + ".lost", 1);
										this.saveConfig();
									}
			    			    	arenap.remove(ap);
			    			    	
							    	ap.teleport(t2);
			    			    	
							    	final Player p_ = ap;
							    	
							    	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			            				@Override
			            	            public void run() {
			            					p_.teleport(t2);
			            				}
			            			}, 20);
							    	
							    	as.handleSign(s_, arena);
							    	ap.getInventory().setContents(pinv.get(ap));
								}
								
								if(s_ != null){
									s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(size));
		                    		s_.update();
								}
								arenaspawn.remove(arena);
							} // end of if(round < max_round)
						}else{ // ROUNDS OR NORMAL:
							p.sendMessage("§2You won the race!");
							if(getConfig().getBoolean("config.announce_winner")){
								getServer().broadcastMessage("§3" + p.getName() + " won a HorseRace!");
							}
							if(getConfig().getInt("stats." + p.getName() + ".won") > 0){
								getConfig().set("stats." + p.getName() + ".won", getConfig().getInt("stats." + p.getName() + ".won") + 1);
								this.saveConfig();
							}else{
								getConfig().set("stats." + p.getName() + ".won", 1);
								this.saveConfig();
							}
							
							if(getConfig().getBoolean("config.use_economy")){
								//give money
								as.ManageMoney(p, "win");
							}else{
								//give item
								p.getInventory().addItem(new ItemStack(Material.getMaterial(getConfig().getInt("config.itemid")), getConfig().getInt("config.itemamount")));
							}
							
							gamestarted.put(aren, false);
							
							int size = as.getSpawnsFromArena(aren).size();
			
					    	Sign s_ = as.getSignFromArena(aren);
					    	// update sign:
		                    as.handleSign(s_, aren);
		                    
					    	final Location t2 = as.getLocFromArena(aren, "lobbyspawn");
					    	if(p.isInsideVehicle()){
					    		p.getVehicle().remove();
					    	}
					    	for(Entity tt : p.getNearbyEntities(50, 50, 50)){
					    		if(tt instanceof Horse){
					    			Horse t = (Horse)tt;
					    			if(t.getPassenger() == null){
					    				tt.remove();
					    			}
					    		}
					    	}
					    	String arena = arenap.get(p);
		                    arenap.remove(p);
		                    
		                    final Player p_1 = p;
		                    
		                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		        				@Override
		        	            public void run() {
		        					p_1.teleport(t2);
		        				}
		        			}, 20);
							
		                    p.getInventory().setContents(pinv.get(p));
		                    
							ArrayList<Player> arenaplayers = new ArrayList<Player>(getKeysByValue(arenap, arena));
							for(Player ap : arenaplayers){
								ap.getVehicle().remove();
								// tp away
		    			    	arenap.remove(ap);
		    			    	
		    			    	ap.sendMessage(getConfig().getString("strings.lost"));
		
		    			    	if(getConfig().getInt("stats." + p.getName() + ".lost") > 0){
									getConfig().set("stats." + p.getName() + ".lost", getConfig().getInt("stats." + p.getName() + ".lost") + 1);
									this.saveConfig();
		    			    	}else{
									getConfig().set("stats." + p.getName() + ".lost", 1);
									this.saveConfig();
								}
		    			    	
						    	ap.teleport(t2);
		    			    	
						    	final Player p_ = ap;
						    	
						    	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		            				@Override
		            	            public void run() {
		            					p_.teleport(t2);
		            				}
		            			}, 20);
						    	
						    	as.handleSign(s_, aren);
						    	ap.getInventory().setContents(pinv.get(ap));
							}
							if(s_ != null){
								s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(size));
	                    		s_.update();
							}
							arenaspawn.remove(arena); 
						} // END OF if(use_round_system){}else{} 
	
					} // END OF if (arena_cycling){}else{}
				} // END OF if(crossed_line){}
			}
		}
	}
	
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
		if(arenap.containsKey(event.getPlayer())){
			// j leave
			if(event.getMessage().equalsIgnoreCase("/hr leave") || event.getMessage().equalsIgnoreCase("/horseracing leave")){
				// nothing
			}else{
				event.setCancelled(true);
				event.getPlayer().sendMessage(getConfig().getString("strings.ingame"));
			}
		}
	}

	
	
	@EventHandler
    public void onblockbreak(BlockBreakEvent event) {
    	if(creation.containsKey(event.getPlayer()) && event.getPlayer().getItemInHand() != null ){
    		if(event.getPlayer().getItemInHand().getType() == Material.WOOD_SPADE){
                Player p = event.getPlayer();
		        Block bp = event.getBlock();
		        int id = bp.getTypeId();
		
		        Location l = bp.getLocation();
		        int xp = (int)l.getX();
	            int yp = (int)l.getY();
	            int zp = (int)l.getZ();
		        
	            String arenaname = creation.get(event.getPlayer());
	            
	            getConfig().set(arenaname + ".finishline.x1", xp);
	            getConfig().set(arenaname + ".finishline.y1", yp);
	            getConfig().set(arenaname + ".finishline.z1", zp);
	            getConfig().set(arenaname + ".finishline.world", p.getWorld().getName());
	            this.saveConfig();
	            
		        event.getPlayer().sendMessage("§2Finishline point registered.");
		        event.setCancelled(true);
           }
    	}
    }

    @EventHandler
    public void onRightclick(PlayerInteractEvent event){
    	if(creation.containsKey(event.getPlayer()) && event.getPlayer().getItemInHand() != null ){
    		if(event.getPlayer().getItemInHand().getType() == Material.WOOD_SPADE){
	            org.bukkit.event.block.Action click = event.getAction();
		        if(click == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK){
			            Block block = event.getClickedBlock();
			            Location l = block.getLocation();
			            
			            int xo = (int)l.getX();
			            int yo = (int)l.getY();
			            int zo = (int)l.getZ();
			            
			            String arenaname = creation.get(event.getPlayer());
			            
			            getConfig().set(arenaname + ".finishline.x2", xo);
			            getConfig().set(arenaname + ".finishline.y2", yo);
			            getConfig().set(arenaname + ".finishline.z2", zo);
			            getConfig().set(arenaname + ".finishline.world", event.getPlayer().getWorld().getName());
			            
			            this.saveConfig();
			            
			            if(getConfig().contains(arenaname + ".finishline.x1")){
			            	event.getPlayer().sendMessage("§2Finishline successfully registered.");
			            }else{
			            	event.getPlayer().sendMessage("§2First Finishline point registered. Now leftclick the other point.");
			            }
		        }
            }
    	}
        
    }
    
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent event){
    	if(event.getVehicle().getPassenger() instanceof Player){
    		if(arenap.containsKey(event.getVehicle().getPassenger())){
    			//event.getVehicle().getPassenger().eject();
    			event.setCancelled(true);
    		}
    	}
    }
    
    public boolean validArena(String arena){
    	
    	if(getConfig().isSet(arena + ".sign") && getConfig().isSet(arena + ".world") && getConfig().isSet(arena + ".finishline") && getConfig().isSet(arena + ".lobbyspawn") && getConfig().isSet(arena + ".spawn1")){
    		return true;
    	}
    	
    	return false;
    }
	
}
