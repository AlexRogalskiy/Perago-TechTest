package xyz.foobar;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The object representing a diff.
 * Implement this class as you see fit. 
 *
 */
public class Diff<T extends Serializable> {

    private T current;
    private List<String> lastChanges;

    public Diff() {
        this.current = null;
        this.lastChanges = new ArrayList<String>();
    }

    public T getCurrent() {
        return this.current;
    }

    public void addLastChange(String lastChange) {
        this.lastChanges.add(lastChange);
    }

    public List<String> getLastChanges() {
        return lastChanges;
    }
}
