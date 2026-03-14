package drinkshop.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public abstract class AbstractRepository<K, E>
        implements Repository<K, E> {

    protected Map<K, E> entities = new HashMap<>();

    @Override
    public E findOne(K id) {
        return entities.get(id);
    }

    @Override
    public List<E> findAll() {
        return StreamSupport.stream(entities.values().spliterator(), false).toList();
    }

    @Override
    public E save(E entity) {
        entities.put(getId(entity), entity);
        return entity;
    }

    @Override
    public E delete(K id) {
        return entities.remove(id);
    }

    @Override
    public E update(E entity) {
        return save(entity);
    }

    protected abstract K getId(E entity);
}
