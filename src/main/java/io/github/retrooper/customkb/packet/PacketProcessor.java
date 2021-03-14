package io.github.retrooper.customkb.packet;

import io.github.retrooper.customkb.Main;
import io.github.retrooper.customkb.data.PlayerData;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.PacketListenerDynamic;
import io.github.retrooper.packetevents.event.impl.PacketPlaySendEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.play.out.entityvelocity.WrappedPacketOutEntityVelocity;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import org.bukkit.entity.Player;

public class PacketProcessor extends PacketListenerDynamic {
    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        byte packetID = event.getPacketId();
        if (packetID == PacketType.Play.Server.ENTITY_VELOCITY) {
            Player player = event.getPlayer();
            WrappedPacketOutEntityVelocity velocity = new WrappedPacketOutEntityVelocity(event.getNMSPacket());
            Vector3d knockbackFactor = Main.INSTANCE.getKnockbackFactor();
            if (!knockbackFactor.equals(Main.INSTANCE.defaultKnockbackFactor)) {
                double velX = velocity.getVelocityX();
                double velY = velocity.getVelocityY();
                double velZ = velocity.getVelocityZ();
                velocity.setVelocityX(velX * knockbackFactor.x);
                velocity.setVelocityY(velY * knockbackFactor.y);
                velocity.setVelocityZ(velZ * knockbackFactor.z);
            }
            int entityID = velocity.getEntityId();
            if (player.getEntityId() == entityID) {
                PlayerData data = Main.INSTANCE.getPlayerData(player.getUniqueId());
                if (Main.INSTANCE.isAsyncKB()) {
                    if (!data.isProcessingCustomKB) {
                        event.setCancelled(true);
                        Runnable sendCustomEntityVelocityTask = new Runnable() {
                            @Override
                            public void run() {
                                data.isProcessingCustomKB = true;
                                PacketEvents.get().getPlayerUtils().sendPacket(player, velocity);
                            }
                        };
                        Main.INSTANCE.executorService.execute(sendCustomEntityVelocityTask);
                    }
                    else {
                        data.isProcessingCustomKB = false;
                    }
                }
            }
        }
    }
}
