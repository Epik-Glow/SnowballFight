package me.epikglow.SnowballFight;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

public class SnowballFightEntityListener extends EntityListener{
    
    public static SnowballFight plugin;
    public Map<Player, Integer> kills = new HashMap<Player, Integer>();
    public Map<Player, Integer> deaths = new HashMap<Player, Integer>();
    public ItemStack snowball = new ItemStack(Material.SNOW_BALL, 32);
    
    public SnowballFightEntityListener(SnowballFight instance) {
        plugin = instance;
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event){
        if (event.isCancelled())
            return;
        if (!(event instanceof EntityDamageByProjectileEvent))
            return;
        
        EntityDamageByProjectileEvent entEvent = null;
        
        if (event instanceof EntityDamageByProjectileEvent) {
            entEvent = (EntityDamageByProjectileEvent) event;
        }
        if (entEvent.getEntity() instanceof Player && entEvent.getProjectile() instanceof Snowball && entEvent.getDamager() instanceof Player){
            Player player = (Player) event.getEntity();
            Player damager = (Player) entEvent.getDamager();
            
            if(plugin.isRegistered(player) && plugin.isRegistered(damager)){
                player.damage(playerDamage(damager));
                
                if ((playerDamage(damager) >= player.getHealth()) && !player.isDead()) {
                    kills.put(damager, (kills.get(damager).intValue() + 1));
                    deaths.put(player, (deaths.get(player).intValue() + 1));
                    
                    damager.getInventory().setItem(damager.getInventory().firstEmpty(), snowball);
                    
                    damager.sendMessage("You just killed " + player.getDisplayName() + "!");
                    player.sendMessage("You have just been killed by " + damager.getDisplayName() + ".");
                }
            }
        }
    }
    
    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player && plugin.isRegistered((Player) event.getEntity())) {
            event.getDrops().clear();
            plugin.recon.remove((Player) event.getEntity());
            plugin.support.remove((Player) event.getEntity());
            plugin.assault.remove((Player) event.getEntity());
            plugin.unregisterPlayer((Player) event.getEntity());
        }
    }
    
    public int playerDamage(Player damager) {
        if (plugin.recon.containsKey(damager)) {
            return plugin.reconDamage;
        }
        else if (plugin.support.containsKey(damager)) {
            return plugin.supportDamage;
        }
        else if (plugin.assault.containsKey(damager)) {
            return plugin.assaultDamage;
        }
        else
            return 0;
    }
}
