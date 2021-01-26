package xyz.foobar;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The object representing a diff.
 * Implement this class as you see fit. 
 *
 */
public class Diff<T extends Serializable> {

    private T diffType;
    private List<String> lastChanges;
    private HashMap<String, Object> changes;

    public Diff() {
        this.diffType = null;
        this.lastChanges = new ArrayList<String>();
        this.changes = new HashMap<>();
    }

    public void addChange(String parent, String fieldName, Object value) {
        if (!this.changes.containsKey(parent)) {
            this.changes.put(parent, new HashMap<String, Object>());
        }
        ((HashMap<String, Object>)this.changes.get(parent)).put(fieldName, value);
    }

    public T getModified() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this.changes);
        oos.flush();
        oos.close();
        bos.close();
        byte[] byteData = bos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
        T cloned = (T) new ObjectInputStream(bais).readObject();
        return cloned;
    }

    public void addLastChange(String lastChange) {
        this.lastChanges.add(lastChange);
    }

    public List<String> getLastChanges() {
        return lastChanges;
    }
}
