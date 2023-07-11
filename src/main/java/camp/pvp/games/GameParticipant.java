package camp.pvp.games;

import camp.pvp.cooldowns.PlayerCooldown;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;

import java.util.*;

@Getter @Setter
public class GameParticipant {

    private final UUID uuid;
    private final String name;
    private GameTeam team;
    private boolean alive, kitApplied;

    private Map<PlayerCooldown.Type, PlayerCooldown> cooldowns;

    private UUID attacker;
    private EntityDamageEvent.DamageCause lastDamageCause;

    public long health, maxHealth, hunger,
            hits, currentCombo, longestCombo,
            thrownPotions, missedPotions;

    private List<PotionEffect> potionEffects;
    private PostGameInventory postGameInventory;

    public GameParticipant(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.alive = true;
        this.cooldowns = new HashMap<>();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void clearCooldowns() {
        for(PlayerCooldown cooldown : cooldowns.values()) {
            cooldown.remove();
        }
    }
}
