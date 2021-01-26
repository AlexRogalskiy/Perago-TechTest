package xyz.foobar;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The object representing a diff.
 * Implement this class as you see fit.
 */
public class Diff<T extends Serializable> {

    public class Change {
        private String id;
        private DiffFieldReflection operation;
        private String fieldName;
        private String original;
        private String modified;

        Change(String id, DiffFieldReflection operation, String fieldName, String original, String modified) {
            this.id = id;
            this.operation = operation;
            this.fieldName = fieldName;
            this.original = original;
            this.modified = modified;
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            out.append(id);
            out.append(" ");
            out.append(operation.label);
            out.append(": ");
            out.append(fieldName);

            switch (operation) {
                case CREATE:
                    if (!modified.equalsIgnoreCase("")) {
                        out.append(" as ");
                        out.append(modified);
                    }
                    break;
                case UPDATE:
                    if (!(original.equalsIgnoreCase("") && modified.equalsIgnoreCase(""))) {
                        out.append(" from ");
                        out.append(original);
                        out.append(" to ");
                        out.append(modified);
                    }
                    break;
            }

            return out.toString();
        }
    }

    private byte[] instanceData;
    private List<Change> lastChanges;
    private float changeId;

    public Diff() {
        this.instanceData = null;
        this.lastChanges = new ArrayList<Change>();
        this.changeId = 0f;
    }

    /**
     * Update the modified object
     * This is ued to keep track of the modified object without storing
     * a reference or clone of modified.
     *
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
     *
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
     *
     * @param operation the operation
     * @param fieldName the object field name
     * @param original  the original values in string
     * @param modified  the modified values in string
     */
    public void addLastChange(String id, DiffFieldReflection operation, String fieldName, String original, String modified) {
        this.lastChanges.add(new Change(id, operation, fieldName, original, modified));
    }

    /**
     * Add last change without any data changes
     * @param id
     * @param operation
     * @param fieldName
     */
    public void addLastChange(String id, DiffFieldReflection operation, String fieldName) {
        this.lastChanges.add(new Change(id, operation, fieldName, "", ""));
    }

    /**
     * Get all the changes
     *
     * @return All the changes in a List of string format
     */
    public List<String> getLastChangesAsListString() {
        return this.lastChanges.stream().map(Change::toString).collect(Collectors.toList());
    }

    /**
     * Get all the changes
     *
     * @return list of change objects
     */
    public List<Change> getLastChanges() {
        return this.lastChanges;
    }

    /**
     * Reset this diff object
     */
    public void reset() {
        this.lastChanges = new ArrayList<Change>();
        this.instanceData = null;
    }
}
