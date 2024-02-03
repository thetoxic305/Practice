package camp.pvp.practice.games.tasks;

import camp.pvp.practice.Practice;
import camp.pvp.practice.games.GameParticipant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnTask extends BukkitRunnable {

    private final GameParticipant participant;
    private final Player player;
    private int time;
    public RespawnTask(GameParticipant participant, Player player) {
        this.participant = participant;
        this.player = player;
        time = 3;
    }

    @Override
    public void run() {
        if(time == 0) {
            if(participant.getAppliedCustomKit() != null) {
                participant.getAppliedCustomKit().apply(participant);
            } else {
                participant.getDuelKit().apply(participant);
            }

            player.teleport(participant.getSpawnLocation());
            participant.setAlive(true);

            participant.setLastDamageCause(null);
            participant.setAttacker(null);

            Practice.getInstance().getGameProfileManager().updateGlobalPlayerVisibility();

            Bukkit.getScheduler().runTaskLater(Practice.getInstance(), () -> {
                participant.setInvincible(false);
            }, 20L);

            participant.getRespawnTask().cancel();
        } else {
            player.sendMessage(ChatColor.GREEN + "Respawning in " + time + " second(s).");
            time--;
        }
    }
}
