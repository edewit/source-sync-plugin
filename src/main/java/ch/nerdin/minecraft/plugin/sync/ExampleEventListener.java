package ch.nerdin.minecraft.plugin.sync;

import java.util.Optional;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.action.CollideEvent;
import org.spongepowered.api.text.Text;

public class ExampleEventListener implements EventListener<CollideEvent> {

    @Override
    public void handle(CollideEvent event) throws Exception {
        Optional<Player> playerOptional = event.getCause().<Player>first(Player.class);
        playerOptional.ifPresent(player -> {
            player.sendMessage(Text.builder("boing").build());
        });
    }

}
