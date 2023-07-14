package camp.pvp.interactables.impl.party;

import camp.pvp.interactables.ItemInteract;
import camp.pvp.profiles.GameProfile;
import org.bukkit.entity.Player;

public class PartyLeaveInteract implements ItemInteract {
    @Override
    public void onInteract(Player player, GameProfile gameProfile) {
        gameProfile.getParty().leave(player);
    }
}
