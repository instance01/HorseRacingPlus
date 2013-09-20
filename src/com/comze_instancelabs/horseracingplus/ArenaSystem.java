package com.comze_instancelabs.horseracingplus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.comze_instancelabs.horseracingplus.HorseModifier.HorseType;
import com.comze_instancelabs.horseracingplus.HorseModifier.HorseVariant;

public class ArenaSystem {

	public String newline = System.getProperty("line.separator");
	Main main;
	
	public ArenaSystem(Main main){
		this.main = main;
	}
	
	/***
	 * Gets all Spawns from the arena
	 * @param arena The arenaname
	 * @return Array of spawn strings
	 */
	public ArrayList<String> getSpawnsFromArena(String arena){
		ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(main.getConfig().getConfigurationSection(arena).getKeys(false));
    	keys.remove("lobbyspawn");
    	keys.remove("finishline");
    	keys.remove("name");
    	keys.remove("world");
    	keys.remove("sign");
    	return keys;
	}
	
	/***
	 * Gets the sign from the configuration of the arena
	 * @param arena The arenaname
	 * @return Sign
	 */
	public Sign getSignFromArena(String arena){
		Location b_ = new Location(Bukkit.getWorld(main.getConfig().getString(arena + ".sign.world")), main.getConfig().getDouble(arena + ".sign.x"), main.getConfig().getDouble(arena + ".sign.y"), main.getConfig().getDouble(arena + ".sign.z"));
    	BlockState bs = b_.getBlock().getState();
    	Sign s_ = null;
    	if(bs instanceof Sign){
    		s_ = (Sign)bs;
    	}else{
    		main.getLogger().info("Could not find sign: " + bs.getBlock().toString());
    		logMessage("Could not find sign: " + bs.getBlock().toString());
    	}
		return s_;
	}
	
	/***
	 * Returns the Location for a specification of an arena
	 * @param arena The arenaname
	 * @param spec can be lobbyspawn or spawn1, spawn_n ..
	 * @return Location
	 */
	public Location getLocFromArena(String arena, String spec){
		
		Double x = main.getConfig().getDouble(arena + "." + spec + ".x");
    	Double y = main.getConfig().getDouble(arena + "." + spec + ".y");
    	Double z = main.getConfig().getDouble(arena + "." + spec + ".z");
    	World w = Bukkit.getWorld(main.getConfig().getString(arena + "." + spec + ".world"));
    	Location l = new Location(w, x, y, z);
    	
		return l;
	}
	
	/***
	 * Logs a message to the plugin_log.txt
	 * @param msg The message to write
	 */
	public void logMessage(String msg){
		File yml = new File("plugins\\HorseRacingPlus\\plugin_log.txt");
        if (!yml.exists()){
        	try {
				yml.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        try {
			java.io.PrintWriter pw = new PrintWriter(new FileWriter(yml, true));
			pw.write(msg + newline);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void countdownfunc(String arena, Player p_, Sign s){
		int secs = main.getConfig().getInt("config.starting_cooldown");
		if(main.secs_.containsKey(arena)){
			secs = main.secs_.get(arena);
		}else{
			if(s != null){
				s.setLine(2, "§2Starting");
	        	s.update();
			}
			
        	main.secs_.put(arena, secs - 1);
        	main.secs_updater.put(arena, p_);
		}
		if(main.secs_updater.containsValue(p_)){
			main.secs_.put(arena, secs - 1);
			for(Player p : main.arenap.keySet()){
        		p.sendMessage("§2Starting in " + Integer.toString(secs));
        	}
		}
		
		logMessage("1LOG secs_updater[0] " + main.secs_updater.get(arena).getName()); // TODO <- Error when cycling twice
		logMessage("1LOG canceltask " + Integer.toString(main.canceltask.size()));
		logMessage("1LOG secs_updater " + Integer.toString(main.secs_updater.size()));
        
        if(secs < 1){
        	main.gamestarted.put(arena, true);
        	
        	logMessage("2LOG secs_updater[0] " + main.secs_updater.get(arena).getName());
        	logMessage("2LOG canceltask " + Integer.toString(main.canceltask.size()));
        	logMessage("2LOG secs_updater " + Integer.toString(main.secs_updater.size()));
        	
        	main.secs_.remove(arena);        	
        	main.getServer().getScheduler().cancelTask(main.canceltask.get(p_));
        	main.canceltask.remove(p_);
        	if(s != null){
	        	s.setLine(2, "§4Ingame");
	        	s.update();
        	}
        }
	}
	
	
	/***
	 * Manages the money of a player
	 * @param p Player
	 * @param action The action to do: entry, win
	 * @return True if action could be finished successfully, false if not
	 */
	public boolean ManageMoney(Player p, String action){
		if(action.equalsIgnoreCase("entry")){
			if(main.economy){
	 			if(main.gambling){
	     			if(main.econ.getBalance(p.getName()) < 10){
	 					p.sendMessage(main.getConfig().getString("strings.notenoughmoney"));
	 					return false;
	 				}else{
	     				EconomyResponse r = main.econ.withdrawPlayer(p.getName(), main.getConfig().getDouble("config.entry_money"));
	                     if(!r.transactionSuccess()) {
	                     	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
	                         //sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
	                     }
	 				}
	 			}
			}
	 		return true;
		}else if(action.equalsIgnoreCase("win")){
			if(main.economy){
				if(main.gambling){
					EconomyResponse r = main.econ.depositPlayer(p.getName(), main.getConfig().getDouble("config.entry_money") * main.arenap.size());
        			if(!r.transactionSuccess()) {
                    	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
                       return false;
                    }
				}else{
					EconomyResponse r = main.econ.depositPlayer(p.getName(), main.getConfig().getDouble("config.normal_money_reward"));
        			if(!r.transactionSuccess()) {
                    	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
                        //sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
                    }
				}
				
				for(Player p_ : main.bet_player.keySet()){
	 				if(p == main.bet_player.get(p_)){
	 					p_.sendMessage("§2Nice bet!");
	 					EconomyResponse r = main.econ.depositPlayer(p_.getName(), main.bet_amount.get(p_) * 2);
	        			if(!r.transactionSuccess()) {
	                    	p.sendMessage(String.format("An error occured: %s", r.errorMessage));
	                    }
	        			main.bet_player.remove(p_);
						main.bet_amount.remove(p_);
	 				}
	 			}
	    	}
			return true;
		}
 		
		return false;
	}
	
	
	/***
	 * Spawns a horse at the given Location and sets the Player as passenger
	 * @param t Location
	 * @param p Player
	 */
	public void spawnHorse(Location t, Player p){
		HorseModifier hm = HorseModifier.spawn(t);
     	hm.setType(HorseType.NORMAL);
     	hm.setVariant(HorseVariant.fromName(main.getConfig().getString("config.horsecolor")));
     	hm.getHorse().setCustomName(main.getConfig().getString("config.horsename"));
     	hm.getHorse().setCustomNameVisible(true);
     	int jump = main.getConfig().getInt("shop." + p.getName() + ".jump");
     	int speed = main.getConfig().getInt("shop." + p.getName() + ".speed");
     	boolean barding = main.getConfig().getBoolean("shop." + p.getName() + ".barding");
     	if(jump > 0){
     		hm.getHorse().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 1));
     		main.getConfig().set("shop." + p.getName() + ".jump", jump - 1);
     	}
     	if(speed > 0){
     		hm.getHorse().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
     		main.getConfig().set("shop." + p.getName() + ".speed", speed - 1);
     	}
     	if(barding){
     		Horse h = (Horse)hm.getHorse();
     		h.setCarryingChest(true);
     		h.getInventory().setArmor(new ItemStack(Material.DIAMOND_BARDING, 1));
     	}
     	hm.setTamed(true);
     	hm.setSaddled(true);
     	hm.getHorse().setPassenger(p);
     	
     	
     	//Horse h = p.getWorld().spawn(t, Horse.class);
     	
	}
	
	
	
	public void playerLeaveEvent(Player p){
		if(main.arenap.containsKey(p)){
			main.tpthem.put(p, main.arenap.get(p));
			String arena = main.arenap.get(p);
			main.arenap.remove(p);
			main.arenaspawn.remove(arena);
			p.getVehicle().remove();
			
			p.getInventory().setContents(main.pinv.get(p));
 			
 			Sign s_ = getSignFromArena(arena);
	    	// update sign:
            if(s_ != null && s_.getLine(3) != ""){
            	String d = s_.getLine(3).split("/")[0];
            	int bef = Integer.parseInt(d);
            	if(bef > 0){
            		s_.setLine(3, Integer.toString(bef - 1) + "/" + Integer.toString(getSpawnsFromArena(arena).size()));
            		s_.update();
            		if(bef == 1){
            			if(main.canceltask.get(p) != null){
            				main.getServer().getScheduler().cancelTask(main.canceltask.get(p));
            			}
                		s_.setLine(2, "§2Join");
                    	s_.update();
                    	if(s_ != null){
							s_.setLine(3, Integer.toString(0) + "/" + Integer.toString(getSpawnsFromArena(arena).size()));
                    		s_.update();
						}
                	}
            	}
            }
		}
	}
	
	
	public void handleSign(Sign s_, String arena){
		if(s_ != null && s_.getLine(3) != ""){
        	String d = s_.getLine(3).split("/")[0];
        	//getLogger().info(d);
        	int bef = Integer.parseInt(d);
        	if(bef > 0){
        		s_.setLine(3, Integer.toString(bef - 1) + "/" + Integer.toString(getSpawnsFromArena(arena).size()));
        		s_.update();
        	}
        	s_.setLine(2, "§2Join");
        	s_.update();
        }
	}
}
