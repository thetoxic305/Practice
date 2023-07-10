package camp.pvp.commands;

import camp.pvp.Practice;
import camp.pvp.kits.DuelKit;
import camp.pvp.profiles.DuelRequest;
import camp.pvp.profiles.GameProfile;
import camp.pvp.profiles.GameProfileManager;
import camp.pvp.utils.buttons.GuiButton;
import camp.pvp.utils.guis.Gui;
import camp.pvp.utils.guis.GuiAction;
import camp.pvp.utils.guis.StandardGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DuelCommand implements CommandExecutor {

    private Practice plugin;
    public DuelCommand(Practice plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginCommand("duel").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player) {
            if(args.length > 0) {
                Player player = (Player) sender;
                GameProfileManager gpm = plugin.getGameProfileManager();
                GameProfile profile = gpm.getLoadedProfiles().get(player.getUniqueId());

                if(profile.getGame() == null) {

                    Player target = Bukkit.getPlayer(args[0]);

                    if(target != null && target != player) {

                        GameProfile targetProfile = gpm.getLoadedProfiles().get(target.getUniqueId());

                        if(targetProfile.getGame() == null) {
                            StandardGui gui = new StandardGui("Duel " + target.getName(), 9);

                            int x = 0;
                            for(DuelKit duelKit : DuelKit.values()) {
                                if(duelKit.isQueueable()) {
                                    ItemStack item = duelKit.getIcon();
                                    GuiButton button = new GuiButton(item, duelKit.getColor() + duelKit.getDisplayName());

                                    button.setLore(
                                            "&7Click to duel " + targetProfile.getName() + "!"
                                    );

                                    button.setAction((pl, igui) -> {
                                        GameProfile gp = gpm.getLoadedProfiles().get(target.getUniqueId());
                                        if(gp != null) {
                                            DuelRequest duelRequest = new DuelRequest(pl.getUniqueId(), target.getUniqueId(), duelKit, null, 30);
                                            duelRequest.send();
                                            gp.getDuelRequests().put(pl.getUniqueId(), duelRequest);
                                        } else {
                                            pl.sendMessage(ChatColor.RED + "The player you specified is not on this server.");
                                        }

                                        pl.closeInventory();
                                    });

                                    button.setSlot(x);
                                    gui.addButton(button, false);
                                    x++;
                                }
                            }

                            gui.open(player);
                        } else {
                            player.sendMessage(ChatColor.RED + "The player you specified is in a game.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "The player you specified is not on this server.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You cannot duel someone when you are in a game.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /duel <player>");
            }
        }

        return true;
    }
}
