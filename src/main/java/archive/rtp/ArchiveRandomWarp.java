package archive.rtp;

import com.mojang.brigadier.Command;
import de.codingair.warpsystem.api.TeleportService;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ArchiveRandomWarp extends JavaPlugin {

    @Override
    public void onEnable() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                Commands.literal("rwarp")
                    .requires(ctx -> ctx.getSender().hasPermission("warpsystem.use.simplewarps"))
                    .executes(ctx -> {
                        if (!(ctx.getSource().getExecutor() instanceof Player)) {
                            ctx.getSource().getSender().sendMessage("Only players can use this command");
                            return Command.SINGLE_SUCCESS;
                        }
                        teleport((Player) ctx.getSource().getExecutor());
                        return Command.SINGLE_SUCCESS;
                    })
                    .build(),
                "Teleport to a random warp"
            );
        });
    }

    public static void teleport(Player player) {
        var teleportService = TeleportService.get();
        var simpleWarps = List.copyOf(teleportService.simpleWarps());
        if (simpleWarps.isEmpty()) return;
        var randomWarp = simpleWarps.get((int) (Math.random() * simpleWarps.size()));
        var options = teleportService.options()
            .setDestination(teleportService.destinationBuilder().simpleWarpDestination(randomWarp))
            .setMessage("Teleporting to " + randomWarp)
            .setPermission("%NO_PERMISSION%")
            .setSkip(true);
        teleportService.teleport(player, options);
    }

    @Override
    public void onDisable() {
    }
}
