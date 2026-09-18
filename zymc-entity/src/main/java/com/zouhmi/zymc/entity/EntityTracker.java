package com.zouhmi.zymc.entity;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EntityTracker {
    private final Map<Integer, Entity> entities = new ConcurrentHashMap<>();
    private final AtomicInteger nextEntityId = new AtomicInteger(0);

    public EntityTracker() {
    }

    public int nextEntityId() {
        return nextEntityId.getAndIncrement();
    }

    public void addEntity(Entity entity) {
        entities.put(entity.getEntityId(), entity);
    }

    public void removeEntity(int entityId) {
        entities.remove(entityId);
    }

    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }

    public Collection<Entity> getAllEntities() {
        return entities.values();
    }
}
