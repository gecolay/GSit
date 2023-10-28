package dev.geco.gsit.objects;

import java.util.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.*;

import dev.geco.gsit.GSitMain;

public class GEmote {

    private final GSitMain GPM = GSitMain.getInstance();

    protected final String id;

    protected final List<GEmotePart> parts;

    protected final long loop;

    protected final boolean head;

    protected final HashMap<Long, List<GEmotePart>> setParts = new HashMap<>();

    protected HashMap<Player, UUID> tasks = new HashMap<>();

    protected double range = GPM.getCManager().E_MAX_DISTANCE;

    private final long spawnTime = System.nanoTime();

    public GEmote(String Id, List<GEmotePart> Parts, long Loop, boolean Head) {

        id = Id;
        parts = Parts;
        loop = Loop;
        head = Head;

        long partCounter = 0;

        for(GEmotePart part : parts) {
            partCounter += part.getDelay();
            List<GEmotePart> sParts = setParts.containsKey(partCounter) ? setParts.get(partCounter) : new ArrayList<>();
            sParts.add(part);
            setParts.put(partCounter, sParts);
        }
    }

    public void start(Player Player) {

        if(parts.isEmpty()) return;

        UUID uuid = GPM.isBasicPaperBased() ? startPaper(Player) : startSpigot(Player);

        tasks.put(Player, uuid);
    }

    private UUID startSpigot(Player Player) {

        final long[] tick = {0};
        final long[] loopTick = {0};
        final long maxTick = Collections.max(setParts.keySet());
        final double finalRange = range * range;

        return GPM.getTManager().runAtFixedRate(() -> {

            Location location = isFromHead() ? Player.getEyeLocation() : Player.getLocation();

            List<GEmotePart> emoteParts = setParts.get(tick[0]);

            if(emoteParts != null) for(GEmotePart part : emoteParts) {

                org.bukkit.util.Vector vector = getCords(location, part.getXOffset(), part.getYOffset(), part.getZOffset());
                for(Player player : location.getWorld().getPlayers()) {
                    if(location.distanceSquared(player.getLocation()) > finalRange) continue;
                    part.spawn(player, vector);
                }
            }

            tick[0]++;

            if(tick[0] >= maxTick) {

                if(getLoop() > 0 && getLoop() <= loopTick[0]) GPM.getEmoteManager().stopEmote(Player);
                else {

                    tick[0] = 0;
                    loopTick[0]++;
                }
            }
        }, false, Player, 0, 1);
    }

    private UUID startPaper(Player Player) {

        final long[] tick = {0};
        final long[] loopTick = {0};
        final long maxTick = Collections.max(setParts.keySet());

        HashMap<GEmotePart, ParticleBuilder> particleBuilders = new HashMap<>();

        for(List<GEmotePart> partEntry : setParts.values()) for(GEmotePart part : partEntry) {

            particleBuilders.put(part, new ParticleBuilder(part.getParticle()).location(Player.getLocation()).source(Player).count(part.getAmount()).extra(part.getExtra()).data(part.getData()));
        }

        return GPM.getTManager().runAtFixedRate(() -> {

            Location location = isFromHead() ? Player.getEyeLocation() : Player.getLocation();

            List<GEmotePart> emoteParts = setParts.get(tick[0]);

            if(emoteParts != null) for(GEmotePart part : emoteParts) {

                org.bukkit.util.Vector vector = getCords(location, part.getXOffset(), part.getYOffset(), part.getZOffset());
                particleBuilders.get(part).location(location.getWorld(), vector.getX(), vector.getY(), vector.getZ()).receivers((int) range).spawn();
            }

            tick[0]++;

            if(tick[0] >= maxTick) {

                if(getLoop() > 0 && getLoop() <= loopTick[0]) GPM.getEmoteManager().stopEmote(Player);
                else {

                    tick[0] = 0;
                    loopTick[0]++;
                }
            }
        }, false, Player, 0, 1);
    }

    private Vector getCords(Location PlayerLocation, double XOffset, double YOffset, double ZOffset) {

        float yaw = PlayerLocation.getYaw(), yawF = yaw + 180f;
        Vector xVector = new Vector(Math.cos(Math.toRadians(yawF)) * XOffset, 0, Math.sin(Math.toRadians(yawF)) * XOffset);
        Vector zVector = new Vector(-Math.sin(Math.toRadians(yaw)) * ZOffset, 0, Math.cos(Math.toRadians(yaw)) * ZOffset);
        Location location = PlayerLocation.clone().add(xVector).add(zVector);
        return new Vector(location.getX(), location.getY() + YOffset, location.getZ());
    }

    public void stop(Player Player) {

        if(!tasks.containsKey(Player)) return;
        GPM.getTManager().cancel(tasks.get(Player));
        tasks.remove(Player);
    }

    public String getId() { return id; }

    public List<GEmotePart> getParts() { return parts; }

    public long getLoop() { return loop; }

    public boolean isFromHead() { return head; }

    public long getSeconds() { return (System.nanoTime() - spawnTime) / 1_000_000_000; }

    public String toString() { return getId(); }

}
