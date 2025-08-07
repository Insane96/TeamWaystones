package insane96mcp.teamwaystones;

import com.mojang.logging.LogUtils;
import net.blay09.mods.waystones.api.IWaystone;
import net.blay09.mods.waystones.api.WaystoneActivatedEvent;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.annotation.Nullable;

@Mod(TeamWaystones.MOD_ID)
public class TeamWaystones
{
    public static final String MOD_ID = "teamwaystones";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TeamWaystones(FMLJavaModLoadingContext context)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onActivatedWaystone(WaystoneActivatedEvent event)
    {
        LOGGER.debug("Activated Waystone: {}", event.getWaystone().getName());
        ServerPlayer player = validateServerPlayer(event.getPlayer());
        if (player == null)
            return;

        activateWaystoneForAllies(player, event.getWaystone());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer playerLoggedIn = validateServerPlayer(event.getEntity());
        if (playerLoggedIn == null)
            return;

        PlayerWaystoneManager.getWaystones(playerLoggedIn).forEach(waystone -> activateWaystoneForAllies(playerLoggedIn, waystone));
        //noinspection DataFlowIssue - Checked in validateServerPlayer()
        playerLoggedIn.getServer().getPlayerList().getPlayers().forEach(playerAlreadyIn -> PlayerWaystoneManager.getWaystones(playerAlreadyIn).forEach(waystone -> activateWaystoneForAlly(playerAlreadyIn, waystone, playerLoggedIn)));
    }

    @Nullable
    private ServerPlayer validateServerPlayer(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || player.getTeam() == null
                || player.getServer() == null)
            return null;
        return serverPlayer;
    }

    private void activateWaystoneForAlly(ServerPlayer player, IWaystone waystone, ServerPlayer otherPlayer) {
        if (otherPlayer != player && isAllied(otherPlayer, player)) {
            LOGGER.debug("Activated Waystone {} for {}", waystone.getName(), otherPlayer.getDisplayName());
            PlayerWaystoneManager.activateWaystone(otherPlayer, waystone);
        }
    }

    private void activateWaystoneForAllies(ServerPlayer player, IWaystone waystone) {
        //noinspection DataFlowIssue - Checked in validateServerPlayer()
        for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            activateWaystoneForAlly(player, waystone, otherPlayer);
        }
    }

    public static boolean isAllied(ServerPlayer player, ServerPlayer otherPlayer) {
        return player.getTeam() != null && player.getTeam().isAlliedTo(otherPlayer.getTeam());
    }
}
