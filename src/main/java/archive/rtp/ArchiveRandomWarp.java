package archive.rtp;

import com.mojang.brigadier.Command;
import de.codingair.warpsystem.api.TeleportService;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class ArchiveRandomWarp extends JavaPlugin {

    private static final double SM_LODGE_REROLL = 0.5;  // 0-1 chance to reroll

    @Override
    public void onEnable() {
        var translationRegistry = TranslationStore.messageFormat(Key.key("archive.rtp"));
        ResourceBundle bundle = ResourceBundle.getBundle("archive.rtp.Bundle", Locale.US, UTF8ResourceBundleControl.utf8ResourceBundleControl());
        translationRegistry.registerAll(Locale.US, bundle, true);
        GlobalTranslator.translator().addSource(translationRegistry);

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                Commands.literal("randomwarp")
                    .requires(ctx -> ctx.getSender().hasPermission("warpsystem.use.simplewarps"))
                    .executes(ctx -> {
                        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
                            ctx.getSource().getSender().sendMessage(
                                Component.translatable("archive.rtp.players_only")
                            );
                            return Command.SINGLE_SUCCESS;
                        }
                        this.teleport(player);
                        return Command.SINGLE_SUCCESS;
                    })
                    .build(),
                "Teleport to a random warp",
                List.of("rw")
            );
        });
    }

    /**
     * Teleports a player to a random warp using WarpSystem-API with history tracking
     *
     * @param player the player to teleport
     */
    private void teleport(Player player) {
        var teleportService = TeleportService.get();

        if (teleportService == null) {
            player.sendMessage(
                Component.text("WarpSystem plugin not found!", NamedTextColor.RED)
            );
            getLogger().warning("WarpSystem not loaded - cannot teleport " + player.getName());
            return;
        }

        var simpleWarps = List.copyOf(teleportService.simpleWarps());
        if (simpleWarps.isEmpty()) {
            player.sendMessage(
                Component.text("No warps available!", NamedTextColor.RED)
            );
            return;
        }

        // Select random warp
        var randomWarp = simpleWarps.get((int) (Math.random() * simpleWarps.size()));

        // Reroll logic: 50% chance to reroll if spawnmason_lodge is selected
        if (randomWarp.toLowerCase().contains("spawnmason_lodge") && Math.random() < SM_LODGE_REROLL) {
            randomWarp = simpleWarps.get((int) (Math.random() * simpleWarps.size()));
        }

        // Build destination and options
        var destination = teleportService.destinationBuilder()
            .simpleWarpDestination(randomWarp);
        var options = teleportService.options()
            .setDestination(destination)
            .setDisplayName(randomWarp);  // For history tracking

        // Execute teleport
        teleportService.teleport(player, options);
    }

    @Override
    public void onDisable() {
        getLogger().info("ArchiveRandomWarp disabled successfully");
    }
}
