package gdn.hypercube.ctoggle;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import gdn.hypercube.ctoggle.compat.area_lib.AreaLibCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CombatToggle implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Combat Toggle");
	public static final List<UUID> TOGGLED_PLAYERS = new ArrayList<>();
	public static final List<UUID> BLOCKED_PLAYERS = new ArrayList<>();

	@Override
	public void onInitialize() {
        AreaLibCompat.register();

		try {
			for (String line : Files.readAllLines(Path.of("ct_toggled.dat"))) {
				TOGGLED_PLAYERS.add(UUID.fromString(line));
			}

			for (String line : Files.readAllLines(Path.of("ct_blocked.dat"))) {
				BLOCKED_PLAYERS.add(UUID.fromString(line));
			}
		} catch (FileNotFoundException ignored) {} catch (IOException exception) {
            LOGGER.error("Failed to read saved player list!", exception);
        }

		ServerLifecycleEvents.SERVER_STOPPING.register(_ -> {
			try {
				try (BufferedWriter writer = Files.newBufferedWriter(Path.of("ct_toggled.dat"), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					for (UUID uuid : TOGGLED_PLAYERS) {
						writer.write(uuid.toString());
						writer.newLine();
					}
				}

				try (BufferedWriter writer = Files.newBufferedWriter(Path.of("ct_blocked.dat"), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					for (UUID uuid : BLOCKED_PLAYERS) {
						writer.write(uuid.toString());
						writer.newLine();
					}
				}
			} catch (IOException exception) {
				LOGGER.error("Failed to write saved player list!", exception);
			}
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
			dispatcher.register(CommandManager.literal("ctoggle").executes(context -> {
				boolean added;

				if (context.getSource().getEntity() instanceof PlayerEntity player) {
					UUID uuid = player.getUuid();

					if (BLOCKED_PLAYERS.contains(uuid)) {
						context.getSource().sendFeedback(() -> Text.literal("You are not allowed to do this!").formatted(Formatting.RED), false);
						return -1;
					}

					if (TOGGLED_PLAYERS.contains(uuid)) { added = false; TOGGLED_PLAYERS.remove(uuid); }
					else {
                        added = true;
                        TOGGLED_PLAYERS.add(uuid);
                    }
					context.getSource().sendFeedback(() -> Text.literal("You can " + (added ? "no longer" : "now") + " be harmed by other players."), true);
					return 1;
				}
                context.getSource().sendFeedback(() -> Text.literal("Only players can run this!").formatted(Formatting.RED), false);
				return -1;
			}).then(CommandManager.argument("target", EntityArgumentType.player()).requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK)).executes(context -> {
				boolean added;
				PlayerEntity target = EntityArgumentType.getPlayer(context, "target");
				UUID uuid = target.getUuid();
				if (TOGGLED_PLAYERS.contains(uuid)) { added = false; TOGGLED_PLAYERS.remove(uuid); }
				else {
					added = true;
					TOGGLED_PLAYERS.add(uuid);
				}
				context.getSource().sendFeedback(() -> Text.literal(target.getName().getString() + " can " + (added ? "no longer" : "now") + " be harmed by other players."), true);
				return 0;
			})));

			dispatcher.register(CommandManager.literal("ctblock").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK)).then(CommandManager.argument("target", EntityArgumentType.player()).executes(context -> {
				boolean added;
				PlayerEntity target = EntityArgumentType.getPlayer(context, "target");
				UUID uuid = target.getUuid();
				if (BLOCKED_PLAYERS.contains(uuid)) { added = false; BLOCKED_PLAYERS.remove(uuid); }
				else {
					added = true;
					BLOCKED_PLAYERS.add(uuid);
				}
				context.getSource().sendFeedback(() -> Text.literal(target.getName().getString() + " can " + (added ? "no longer" : "now") + " toggle their combat-allowed status."), true);
				return 0;
			})));

			dispatcher.register(CommandManager.literal("ctsave").requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)).executes(context -> {
				try {
					try (BufferedWriter writer = Files.newBufferedWriter(Path.of("ct_toggled.dat"), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING)) {
						for (UUID uuid : TOGGLED_PLAYERS) {
							writer.write(uuid.toString());
							writer.newLine();
						}
					}

					try (BufferedWriter writer = Files.newBufferedWriter(Path.of("ct_blocked.dat"), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING)) {
						for (UUID uuid : BLOCKED_PLAYERS) {
							writer.write(uuid.toString());
							writer.newLine();
						}
					}
					return 0;
				} catch (IOException exception) {
					LOGGER.error("Failed to write saved player list!", exception);
					context.getSource().sendFeedback(() -> Text.literal("Error flushing to disk, check server logs.").formatted(Formatting.RED), true);
					return -1;
				}
			}));

			dispatcher.register(CommandManager.literal("ctload").requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK)).executes(context -> {
				List<UUID> toggled = new ArrayList<>(TOGGLED_PLAYERS);
				List<UUID> blocked = new ArrayList<>(BLOCKED_PLAYERS);
				try {
					TOGGLED_PLAYERS.clear();
					BLOCKED_PLAYERS.clear();

					for (String line : Files.readAllLines(Path.of("ct_toggled.dat"))) {
						TOGGLED_PLAYERS.add(UUID.fromString(line));
					}

					for (String line : Files.readAllLines(Path.of("ct_blocked.dat"))) {
						BLOCKED_PLAYERS.add(UUID.fromString(line));
					}

					toggled.clear();
					blocked.clear();
				} catch (FileNotFoundException ignored) {} catch (IOException exception) {
					LOGGER.error("Failed to read saved player list!", exception);
					context.getSource().sendFeedback(() -> Text.literal("Error reading from disk, check server logs.").formatted(Formatting.RED), true);
					TOGGLED_PLAYERS.addAll(toggled);
					BLOCKED_PLAYERS.addAll(blocked);
					return -1;
				}

				return 0;
			}));
		});
	}

	public static Identifier id(String path) {
		return Identifier.of("ctoggle", path);
	}
}