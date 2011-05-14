package me.epikglow.SnowballFight;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SnowballFight extends JavaPlugin{

    static final Logger log = Logger.getLogger("Minecraft");
    private final SnowballFightEntityListener entityListener = new SnowballFightEntityListener(this);   
    
    Player player = null;
    boolean enabled = false;
    public int reconDamage = 10;
    public int supportDamage = 2;
    public int assaultDamage = 4;
    PlayerStartPoint pstartpoint = new PlayerStartPoint();
    public Map<Player, Boolean> registered = new HashMap<Player, Boolean>();
    public Map<Player, Boolean> recon = new HashMap<Player, Boolean>();
    public Map<Player, Boolean> support = new HashMap<Player, Boolean>();
    public Map<Player, Boolean> assault = new HashMap<Player, Boolean>();
    // Use Hashmaps for holding players' names for teams. Each team gets its own hashmap
    public ItemStack reconSnowball = new ItemStack(Material.SNOW_BALL, 1);
    public ItemStack supportSnowball = new ItemStack(Material.SNOW_BALL, 64);
    public ItemStack assaultSnowball = new ItemStack(Material.SNOW_BALL, 32);

    @Override
    public void onEnable() {
        logMessage("SnowballFight", "SnowballFight has been enabled!");
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Priority.Highest, this);
    }
    
    @Override
    public void onDisable() {
        logMessage("SnowballFight", "SnowballFight has been disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        player = (Player) sender;

        if (commandLabel.equalsIgnoreCase("sf")) {
            if (args.length == 1){
                if (args[0].equalsIgnoreCase("start")) {
                    startGame(player);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("end")) {
                    endGame(player);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("join")) {
                    if (registered.containsKey(player) == false) {
                        registerPlayer(player);
                        sendMessage("SnowballFight", "You have joined the snowball fight!", player);
                        player.teleport(pstartpoint.getPStartPoint());
                        player.setHealth(20);
                        player.getInventory().clear();
                        entityListener.kills.put(player, 0);
                        entityListener.deaths.put(player, 0);
                        giveSnowballs(player);
                        return true;
                    }
                    else if (registered.containsKey(player) == true) {
                        sendMessage("SnowballFight", "You have already joined the snowball fight!", player);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("leave")) {
                    if (registered.containsKey(player) == true){
                        unregisterPlayer(player);
                        sendMessage("SnowballFight", "You have left the snowball fight!", player);
                        return true;
                    }
                    else if (registered.containsKey(player) == false) {
                        sendMessage("SnowballFight", "You have already left the snowball fight!", player);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("kills")) {
                    if(entityListener.kills.containsKey(player) == true) {
                        sendMessage("SnowballFight", "You have " + entityListener.kills.get(player) + " kills.", player);
                        return true;
                        }
                    else {
                        sendMessage("SnowballFight", "You haven't killed anybody yet.", player);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("deaths")) {
                    if(entityListener.kills.containsKey(player)) {
                        sendMessage("SnowballFight", "You have " + entityListener.deaths.get(player) + " deaths.", player);
                        return true;
                        }
                    else {
                        sendMessage("SnowballFight", "You haven't died......yet.", player);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase("recon")) {
                    support.remove(player);
                    assault.remove(player);
                    recon.put(player, true);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("support")) {
                    recon.remove(player);
                    assault.remove(player);
                    support.put(player, true);
                    return true;
                }
                else if (args[0].equalsIgnoreCase("assault")) {
                    recon.remove(player);
                    support.remove(player);
                    assault.put(player, true);
                    return true;
                }
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("spawn")) {
                    if (args[1].equalsIgnoreCase("set")) {
                        pstartpoint.setPStartPoint(player.getLocation());
                        sendMessage("SnowballFight", "Player spawn set to your location!", player);
                        return true;
                    }
                    else if (args[1].equalsIgnoreCase("get")){
                        if (pstartpoint == null) {
                        sendMessage("SnowballFight", "You haven't set the player spawn yet!", player);
                        return true;
                        }
                    }
                    else {
                        sendMessage("SnowballFight", "The player spawn is set to " + pstartpoint.pstartpoint_xyz + ".", player);
                        return true;
                    }
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("setdamage") && args[1].equalsIgnoreCase("recon")){
                    reconDamage = Integer.parseInt(args[2]);
                    return true;
                }
                if (args[0].equalsIgnoreCase("setdamage") && args[1].equalsIgnoreCase("support")){
                    supportDamage = Integer.parseInt(args[2]);
                    return true;
                }
                if (args[0].equalsIgnoreCase("setdamage") && args[1].equalsIgnoreCase("assault")){
                    assaultDamage = Integer.parseInt(args[2]);
                    return true;
                }
            }
        }
        return false;
    }
    public void startGame(Player player) {
        if (enabled == false) {
            broadcastMessage("SnowballFight", "Game started!");
            enabled = true;
            return;
        }
        if (enabled == true)
            sendMessage("SnowballFight", "A game has already been started!", player);
            return;
    }

    public void endGame (Player player) {
        if (enabled == true) {
            broadcastMessage("SnowballFight", "Game ended!");
            enabled = false;
            return;
        }
        if (enabled == false) {
            sendMessage("SnowballFight", "There is no game to end!", player);
            return;
        }
    }
    
    public void logMessage(String prefix, String string) {
        String Message = ("[" + prefix + "] " + string);
        log.info(Message);
    }
    
    public void sendMessage(String prefix, String string, Player player) {
        String Message = ("[" + prefix + "] " + string);
        player.sendMessage(Message);
    }
    
    public void broadcastMessage(String prefix, String string) {
        String Message = ("[" + prefix + "] " + string);
        this.getServer().broadcastMessage(Message);
    }
    
    public void registerPlayer(Player player) {
        registered.put(player, true);
    }
    
    public void unregisterPlayer(Player player) {
        registered.remove(player);
    }
    
    public boolean isRegistered(Player player) {
        if (registered.containsKey(player))
            return true;
        else if (!(registered.containsKey(player)))
            return false;
        return false;
    }
    
    public boolean isStarted() {
        return enabled;
    }
    
    public void giveSnowballs(Player player) {
        if (recon.containsKey(player))
            reconSnowballs();
        else if (support.containsKey(player))
            supportSnowballs();
        else
            assaultSnowballs();
    }
    public void reconSnowballs() {
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), reconSnowball);
    }
    public void supportSnowballs() {
        player.getInventory().setItem(player.getInventory().firstEmpty(), supportSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), supportSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), supportSnowball);
    }
    
    public void assaultSnowballs() {
        player.getInventory().setItem(player.getInventory().firstEmpty(), assaultSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), assaultSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), assaultSnowball);
        player.getInventory().setItem(player.getInventory().firstEmpty(), assaultSnowball);
    }

    
    public class PlayerStartPoint{
        private Location pstartpoint;
        private boolean pstartpoint_set;
        String pstartpoint_xyz;

        public void setPStartPoint(Location location) {
            pstartpoint = location;
            pstartpoint_xyz = (pstartpoint.getX() + ", " + pstartpoint.getY() + ", " + pstartpoint.getZ());
            pstartpoint_set = true;
        }
        
        public Location getPStartPoint() {
            if (pstartpoint_set == false) {
                return null;
            } else
                return pstartpoint;
        }
        
        public boolean isSet() {
            return pstartpoint_set;
        }
        
        public double getX() {
            return pstartpoint.getX();
        }

        public double getY() {
            return pstartpoint.getY();
        }

        public double getZ() {
            return pstartpoint.getZ();
        }
    }
}
