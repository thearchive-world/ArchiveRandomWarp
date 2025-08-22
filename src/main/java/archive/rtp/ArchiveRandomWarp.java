package archive.rtp;

import com.mojang.brigadier.Command;
import de.codingair.warpsystem.api.TeleportService;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        if (randomWarp.toLowerCase().contains("spawnmason_lodge") && Math.random() < SM_LODGE_REROLL) {
            randomWarp = simpleWarps.get((int) (Math.random() * simpleWarps.size()));
        }

        var component = Component
            .translatable("archive.rtp.warping", Component.text(randomWarp).color(NamedTextColor.AQUA)).color(NamedTextColor.GRAY);
        var msg = LegacyComponentSerializer.legacySection().serialize(component);
        var options = teleportService.options()
            .setDestination(teleportService.destinationBuilder().simpleWarpDestination(randomWarp))
            .setMessage(msg)
            .setPermission("%NO_PERMISSION%")
            .setSkip(true);
        teleportService.teleport(player, options);
    }

    @Override
    public void onDisable() {
    }
}
