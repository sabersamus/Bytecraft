package info.bytecraft.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import info.bytecraft.Bytecraft;
import info.bytecraft.api.BytecraftPlayer;
import info.bytecraft.database.DAOException;
import info.bytecraft.database.IContext;
import info.bytecraft.database.IWarpDAO;

public class WarpCommand extends AbstractCommand
{

    public WarpCommand(Bytecraft instance)
    {
        super(instance, "warp");
    }

    public class WarpTask implements Runnable
    {
        private BytecraftPlayer player;
        private Location loc;
        private String name;

        public WarpTask(BytecraftPlayer player, Location loc, String name)
        {
            this.loc = loc;
            this.player = player;
            this.name = name;
        }

        public void run()
        {
            player.teleportWithVehicle(loc.add(0, 0.5, 0));
            player.sendMessage(ChatColor.AQUA + "Successfully teleported to " + name);
        }

    }

    public boolean handlePlayer(BytecraftPlayer player, String[] args)
    {
        if (args.length != 1)return true;
        String warp = args[0];
        try (IContext ctx = plugin.createContext()){
            IWarpDAO dao = ctx.getWarpDAO();
            Location loc = dao.getWarp(warp);
            if (loc != null) {
                loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new WarpTask(player, loc, warp), player.getWarpTimeout());
                player.sendMessage(ChatColor.AQUA + "Teleporting to " + ChatColor.GOLD + warp + ChatColor.AQUA + " please wait...");
            }else{
                player.sendMessage(ChatColor.RED + "Could not find warp " + args[0]);
            }
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

}
