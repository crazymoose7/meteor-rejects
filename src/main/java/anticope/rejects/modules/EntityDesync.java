package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.text.Text;

// Port : https://github.com/CreepyOrb924/creepy-salhack/blob/master/src/main/java/me/ionar/salhack/module/exploit/EntityDesyncModule.java
public class EntityDesync extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> toggleOnLog = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-on-log")
            .description("Disables EntityDesync when you disconnect from a server.")
            .defaultValue(true)
            .build()
    );

    private Entity riding = null;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (riding == null) {
            return;
        }

        if (mc.player.hasVehicle()) {
            return;
        }

        riding.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        mc.getNetworkHandler().sendPacket(new VehicleMoveC2SPacket(riding));
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof EntityPassengersSetS2CPacket) {
            if (riding == null) {
                return;
            }

            EntityPassengersSetS2CPacket packet = (EntityPassengersSetS2CPacket) event.packet;
            Entity entity = mc.world.getEntityById(packet.getId());

            if (entity == riding) {
                for (int i : packet.getPassengerIds()) {
                    Entity entityPlayer = mc.world.getEntityById(i);

                    if (entityPlayer == mc.player) {
                        return;
                    }
                }

                ChatUtils.sendMsg(Text.of("You dismounted. RIP"));
                toggle();
            }
        } else if (event.packet instanceof EntitiesDestroyS2CPacket) {
            EntitiesDestroyS2CPacket packet = (EntitiesDestroyS2CPacket) event.packet;

            for (int entityId : packet.getEntityIds()) {
                if (entityId == riding.getId()) {
                    ChatUtils.sendMsg(Text.of("Entity is now null"));
                    return;
                }
            }
        }
    }

    public EntityDesync() {
        super(MeteorRejectsAddon.CATEGORY,"entity-desync", "Clientside dismount from entity.");
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!toggleOnLog.get()) return;
        toggle();
    }

    @Override
    public void onActivate() {

        if (mc.player == null) {
            riding = null;
            toggle();
            return;
        }

        if (mc.player.isOnGround()) {
            ChatUtils.sendMsg(Text.of("You are not riding an entity."));
            mc.player.openRidingInventory();
            riding = null;
            toggle();
            return;
        }

        riding = mc.player.getVehicle();
        mc.player.dismountVehicle();
        mc.world.removeEntity(riding.getId(), Entity.RemovalReason.KILLED);
    }

    @Override
    public void onDeactivate() {
        if (riding != null) {
            // Riding.isDead = false;
            if (mc.player.isOnGround()) {
                mc.world.spawnEntity(riding);
                mc.player.startRiding(riding, true);
            }
            riding = null;
            ChatUtils.sendMsg(Text.of("Forced a remount."));
        }
    }
}
