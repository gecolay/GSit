package dev.geco.gsit.objects;

import java.util.*;
import java.util.stream.*;

import org.bukkit.entity.*;

import dev.geco.gsit.GSitMain;

public class GEmote {

    protected final String id;

    protected final List<GEmotePart> parts;

    protected final long loop;

    protected final boolean head;

    protected final HashMap<Long, List<GEmotePart>> setParts = new HashMap<>();

    protected HashMap<Entity, UUID> tasks = new HashMap<>();

    protected double range = GSitMain.getInstance().getCManager().E_MAX_DISTANCE;

    public GEmote(String Id, List<GEmotePart> Parts, long Loop, boolean Head) {

        id = Id;
        parts = new ArrayList<>(Parts);
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

    public void start(LivingEntity Entity) {

        if(parts.isEmpty()) return;

        boolean isPlayer = Entity instanceof Player;

        final long[] tick = {0};
        final long[] loopTick = {0};
        final long maxTick = Collections.max(setParts.keySet());

        UUID uuid = GSitMain.getInstance().getTManager().runAtFixedRate(() -> {

            if(setParts.containsKey(tick[0])) {

                for(GEmotePart part : setParts.get(tick[0])) {

                    if(isPlayer) {

                        Player p = (Player) Entity;

                        for(Player t : Entity.getWorld().getPlayers().stream().filter(o -> Entity.getLocation().distance(o.getLocation()) <= range && o.canSee(p)).collect(Collectors.toSet())) {
                            part.start(t, Entity, isFromHead());
                        }
                    } else {

                        for(Player t : Entity.getWorld().getPlayers().stream().filter(o -> Entity.getLocation().distance(o.getLocation()) <= range).collect(Collectors.toSet())) {
                            part.start(t, Entity, isFromHead());
                        }
                    }
                }
            }

            tick[0]++;

            if(tick[0] >= maxTick) {

                if(getLoop() > 0 && getLoop() <= loopTick[0]) GSitMain.getInstance().getEmoteManager().stopEmote(Entity);
                else {

                    tick[0] = 0;
                    loopTick[0]++;
                }
            }
        }, Entity, 0, 1);

        tasks.put(Entity, uuid);
    }

    public void stop(Entity Entity) {

        if(!tasks.containsKey(Entity)) return;

        GSitMain.getInstance().getTManager().cancel(tasks.get(Entity));

        tasks.remove(Entity);
    }

    public String getId() { return id; }

    public List<GEmotePart> getParts() { return parts; }

    public long getLoop() { return loop; }

    public boolean isFromHead() { return head; }

    public String toString() { return getId(); }

}
