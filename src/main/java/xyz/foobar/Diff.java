package xyz.foobar;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The object representing a diff.
 * Implement this class as you see fit.
 */
public class Diff<T extends Serializable> {

    private byte[] instanceData;
    private List<String> lastChanges;

    public Diff() {
        this.instanceData = null;
        this.lastChanges = new ArrayList<String>();
    }

    /**
     * Update the modified object
     * This is ued to keep track of the modified object without storing
     * a reference or clone of modified.
     * @param obj
     * @throws DiffException
     */
    public void updateModified(T obj) throws DiffException {
        try {
            this.instanceData = Utils.serializeObject(obj);
        } catch (IOException e) {
            throw new DiffException(e);
        }
    }

    /**
     * Get the modified object
     * @param type the type to cast the object back to
     * @param <T>
     * @return the modified object
     * @throws DiffException
     */
    public <T extends Serializable> T getModified(Class<T> type) throws DiffException {
        try {
            return Utils.deserializeObject(this.instanceData, type);
        } catch (IOException | ClassNotFoundException e) {
            throw new DiffException(e);
        }
    }

    /**
     * Add an entry into the operation and the difference between the
     * original and modified object
     * @param lastChange
     */
    public void addLastChange(String lastChange) {
        this.lastChanges.add(lastChange);
    }

    /**
     * Get all the changes
     * @return All the changes in a List format
     */
    public List<String> getLastChanges() {
        return lastChanges;
    }

    /**
     * Reset this diff object
     */
    public void reset() {
        this.lastChanges = new ArrayList<String>();
        this.instanceData = null;
    }
}
