package camp.pvp.games.impl;

import camp.pvp.Practice;
import camp.pvp.arenas.Arena;
import camp.pvp.arenas.ArenaPosition;
import camp.pvp.cooldowns.PlayerCooldown;
import camp.pvp.games.Game;
import camp.pvp.games.GameParticipant;
import camp.pvp.games.GameSpectator;
import camp.pvp.games.PostGameInventory;
import camp.pvp.kits.DuelKit;
import camp.pvp.parties.Party;
import camp.pvp.profiles.GameProfile;
import camp.pvp.profiles.GameProfileManager;
import camp.pvp.queue.GameQueue;
import camp.pvp.utils.Colors;
import camp.pvp.utils.PlayerUtils;
import camp.pvp.utils.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FreeForAll extends Game {

    private @Getter @Setter Party party;

    public FreeForAll(Practice plugin, UUID uuid, Party party) {
        super(plugin, uuid);
        this.party = party;

        if(party != null) {
            party.setGame(this);
        }
    }

    @Override
    public void eliminate(Player player, boolean leftGame) {
        super.eliminate(player, leftGame);
        if(this.getAlive().size() < 2) {
            this.end();
        }
    }

    @Override
    public List<String> getScoreboard(GameProfile profile) {
        List<String> lines = new ArrayList<>();
        GameParticipant self = getAlive().get(profile.getUuid());

        switch(getState()) {
            case STARTING:
                lines.add("&6Kit: &f" + kit.getDisplayName());
                lines.add("&6Arena: &f" + arena.getDisplayName());
                lines.add("&6Players: &f" + this.getAlive().size());
                break;
            case ACTIVE:
                int ping = PlayerUtils.getPing(self.getPlayer());
                lines.add("&6Duration: &f" + TimeUtil.get(new Date(), getStarted()));
                lines.add(" ");

                lines.add("&6Your Ping: &f" + ping + " ms");
                lines.add("&6Players Left: &f" + this.getAlive().size() + "/" + this.getParticipants().size());
                break;
            case ENDED:
                lines.add("&6&lYou win!");
                lines.add("&6Duration: &f&n" + TimeUtil.get(getEnded(), getStarted()));
                break;
        }
        return lines;
    }

    @Override
    public List<String> getSpectatorScoreboard(GameProfile profile) {
        List<String> lines = new ArrayList<>();

        lines.add("&6Players:");

        if(getAlive().size() < 7) {
            for (GameParticipant participant : getParticipants().values()) {
                if (participant.isAlive()) {
                    lines.add(" &f" + participant.getName() + " &c" + Math.round(participant.getPlayer().getHealth()) + " ❤");
                }
            }
        } else {
            lines.add("&6Players Left: &f" + this.getAlive().size() + "/" + this.getParticipants().size());
        }

        lines.add(" ");

        switch(getState()) {
            case STARTING:
                lines.add("&6Kit: &f" + kit.getDisplayName());
                lines.add("&6Arena: &f" + arena.getDisplayName());
                break;
            case ACTIVE:
                lines.add("&6Duration: &f" + TimeUtil.get(new Date(), getStarted()));
                break;
            case ENDED:
                lines.add("&6Duration: &f&n" + TimeUtil.get(getEnded(), getStarted()));
                break;
        }

        return lines;
    }

    @Override
    public void start() {
        if(getArena() == null) {
            List<Arena> list = new ArrayList<>();
            for(Arena a : getPlugin().getArenaManager().getArenas()) {
                if(a.isEnabled()) {
                    if(a.getType().equals(Arena.Type.FFA)) {
                        list.add(a);
                    }
                }
            }

            if(!list.isEmpty()) {
                Collections.shuffle(list);
                this.setArena(list.get(0));
            }
        }

        if(arena == null) {
            for(Player p : getAlivePlayers()) {
                GameProfile profile = getPlugin().getGameProfileManager().getLoadedProfiles().get(p.getUniqueId());
                p.sendMessage(ChatColor.RED + "There are no arenas currently available for the ladder selected. Please notify a staff member.");
                profile.setGame(null);
                profile.playerUpdate();
            }
            return;
        }

        Map<String, ArenaPosition> positions = arena.getPositions();
        ArenaPosition spawn = positions.get("spawn");

        if(spawn != null) {

            this.setState(State.STARTING);

            StringBuilder stringBuilder = new StringBuilder();
            List<Player> players = new ArrayList<>(this.getAlivePlayers());
            int s = 0;
            while(s != this.getAlive().size()) {
                Player p = players.get(0);
                stringBuilder.append(ChatColor.WHITE + p.getName());

                players.remove(p);
                s++;
                if(s == this.getAlivePlayers().size()) {
                    stringBuilder.append(ChatColor.GRAY + ".");
                } else {
                    stringBuilder.append(ChatColor.GRAY + ", ");
                }
            }

            for(Player p : this.getAllPlayers()) {
                p.sendMessage(" ");
                p.sendMessage(Colors.get("&6&lMatch starting in 10 seconds."));
                p.sendMessage(Colors.get(" &7● &6Mode: &fFree for All"));
                p.sendMessage(Colors.get(" &7● &6Kit: &f" + kit.getColor() + kit.getDisplayName()));
                p.sendMessage(Colors.get(" &7● &6Map: &f" + Colors.get(getArena().getDisplayName())));
                p.sendMessage(Colors.get(" &7● &6Participants: &f" + stringBuilder));
                p.sendMessage(" ");
            }

            for (Map.Entry<UUID, GameParticipant> entry : this.getParticipants().entrySet()) {
                GameParticipant participant = entry.getValue();
                Player p = Bukkit.getPlayer(entry.getKey());
                Location location = null;

                if (p != null) {
                    p.teleport(spawn.getLocation());

                    getPlugin().getGameProfileManager().getLoadedProfiles().get(entry.getKey()).givePlayerItems();
                }
            }

            getPlugin().getGameProfileManager().updateGlobalPlayerVisibility();

            this.startingTimer = new BukkitRunnable() {
                int i = 10;
                public void run() {
                    if (i == 0) {
                        for(Player p : FreeForAll.this.getAlivePlayers()) {
                            if(p != null) {
                                p.playSound(p.getLocation(), Sound.NOTE_PIANO, 1, 12);
                                p.sendMessage(ChatColor.GREEN + "The game has started, good luck!");
                            }
                        }

                        FreeForAll ffa = FreeForAll.this;
                        ffa.setStarted(new Date());
                        ffa.setState(State.ACTIVE);

                        this.cancel();
                    } else {
                        if (i > 0) {
                            for (Player p : FreeForAll.this.getAllPlayers()) {
                                p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
                                p.sendMessage(ChatColor.GREEN.toString() + i + "...");
                            }
                        }

                        i -= 1;
                    }
                }
            }.runTaskTimer(this.getPlugin(), 20, 20);
        } else {
            for(Player p : getAlivePlayers()) {
                GameProfile profile = getPlugin().getGameProfileManager().getLoadedProfiles().get(p.getUniqueId());
                p.sendMessage(ChatColor.RED + "The arena " + arena.getName() + " does not have valid spawn points, please notify a staff member.");
                profile.setGame(null);
                profile.playerUpdate();
            }
        }
    }

    @Override
    public void end() {
        GameProfileManager gpm = getPlugin().getGameProfileManager();
        this.setEnded(new Date());
        this.setState(State.ENDED);

        if(getStarted() == null) {
            setStarted(new Date());
            getStartingTimer().cancel();
        }

        if(getParty() != null) {
            getParty().setGame(null);
        }

        for(GameParticipant participant : getAlive().values()) {
            Player player = participant.getPlayer();
            PostGameInventory pgi = new PostGameInventory(UUID.randomUUID(), participant, player.getInventory().getContents(), player.getInventory().getArmorContents());
            participant.setPostGameInventory(pgi);
            getPlugin().getGameManager().getPostGameInventories().put(pgi.getUuid(), pgi);
        }

        GameParticipant winnerParticipant = null;

        for(Map.Entry<UUID, GameParticipant> entry : this.getParticipants().entrySet()) {
            GameParticipant participant = entry.getValue();
            Player player = Bukkit.getPlayer(entry.getKey());

            boolean alive = participant.isAlive();
            if(alive) {
                winnerParticipant = participant;
                break;
            }
        }

        List<TextComponent> components = new ArrayList<>();
        for(GameParticipant p : this.getParticipants().values()) {
            TextComponent text = new TextComponent(ChatColor.WHITE + p.getName());
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/postgameinventory " + p.getPostGameInventory().getUuid().toString()));
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to view " + ChatColor.WHITE + p.getName() + "'s " + ChatColor.GREEN + "inventory.").create()));
            components.add(text);
        }

        // Match Summary Message
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" ");
        stringBuilder.append(Colors.get("&6&lMatch ended."));
        stringBuilder.append(Colors.get("\n &7● &6Winner: &f" + winnerParticipant.getName()));

        TextComponent text = new TextComponent(ChatColor.GRAY + " ● " + ChatColor.GOLD + "Inventories: ");
        final int size = components.size();
        int x = 0;
        while(x != size) {
            TextComponent t = components.get(0);
            text.addExtra(t);
            components.remove(t);
            x++;
            if (x == size) {
                text.addExtra(new TextComponent(ChatColor.GRAY + "."));
            } else {
                text.addExtra(new TextComponent(ChatColor.GRAY + ", "));
            }
        }

        for(Player player : this.getAllPlayers()) {
            player.sendMessage(" ");
            player.sendMessage(stringBuilder.toString());
            player.spigot().sendMessage(text);
            player.sendMessage(" ");
        }

        this.endingTimer = Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            for(Map.Entry<UUID, GameParticipant> entry : this.getParticipants().entrySet()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                GameParticipant participant = entry.getValue();
                GameProfile profile = getPlugin().getGameProfileManager().getLoadedProfiles().get(entry.getKey());

                if(player != null) {
                    if(entry.getValue().isAlive()) {

                        for(PlayerCooldown cooldown : participant.getCooldowns().values()) {
                            cooldown.remove();
                        }

                        profile.setGame(null);
                        profile.playerUpdate();
                    }
                }
            }

            for(GameSpectator spectator : new ArrayList<>(this.getSpectators().values())) {
                Player player = Bukkit.getPlayer(spectator.getUuid());
                this.spectateEnd(player);
            }

            FreeForAll.this.clearEntities();
        }, 100);
    }
}
