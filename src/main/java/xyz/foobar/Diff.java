package xyz.foobar;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The object representing a diff.
 * Implement this class as you see fit.
 */
public class Diff<T extends Serializable> {

    private byte[] instanceData;
    private List<String> lastChanges;
    private HashMap<String, HashMap<Type, Object>> changes;

    public Diff() throws DiffException {
        /*try {
            // https://stackoverflow.com/a/75345
            // using reflection to create a new instance of type T
            this.instance = this.getClass().getConstructor().newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new DiffException(e);
        }*/
        this.instanceData = null;
        this.lastChanges = new ArrayList<String>();
        this.changes = new HashMap<>();
    }

    /*public void addChange(String parent, String fieldName, Object value) {
        if (!this.changes.containsKey(parent)) {
            this.changes.put(parent, new HashMap<Class, Object>(Map.class, new HashMap<String, Object>()));
        }
        ((HashMap<String, Object>) this.changes.get(parent)).put(fieldName, value);
    }*/

    public void updateModified(T obj) throws DiffException {
        try {
            this.instanceData = Utils.serializeObject(obj);
        } catch (IOException e) {
            throw new DiffException(e);
        }
    }

    public <T extends Serializable> T getModified(Class<T> type) throws DiffException {
        try {
            return Utils.deserializeObject(this.instanceData, type);
        } catch (IOException | ClassNotFoundException e) {
            throw new DiffException(e);
        }
    }

    public void addLastChange(String lastChange) {
        this.lastChanges.add(lastChange);
    }

    public List<String> getLastChanges() {
        return lastChanges;
    }
}
