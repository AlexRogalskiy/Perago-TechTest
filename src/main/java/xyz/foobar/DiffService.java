package xyz.foobar;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class DiffService implements DiffEngine {

    public <T extends Serializable> T apply(T original, Diff<?> diff) throws DiffException {
        return (T) diff.getModified(original.getClass());
    }

    public <T extends Serializable> Diff<T> calculate(T original, T modified) throws DiffException {
        // List<String> lastChanges = new ArrayList<String>();
        Diff<T> diff = new Diff<T>();
        diff.updateModified(modified);
        this.calculate("1", 1, diff, original, modified, null);
        return diff;
    }

    /**
     * Calculate helper method
     *
     * @param diff
     * @param val1
     * @param val2
     * @param parent
     * @param <T>
     * @throws DiffException
     */
    private <T extends Serializable> void calculate(String id, int recurseId, Diff<T> diff, Object val1, Object val2, String parent) throws DiffException {
        /*
         * Related to https://stackoverflow.com/questions/48321997/how-to-compare-two-objects-and-find-the-fields-properties-changed
         */
        DiffFieldReflection diffFieldReflectionOverride = null;
        DiffFieldReflection diffFieldReflection = null;
        Field[] fields = null;

        diffFieldReflectionOverride = this.getFieldReflection(val1, val2);

        Class c;

        switch (diffFieldReflectionOverride) {
            case CREATE:
                c = val2.getClass();
                diff.addLastChange(id, diffFieldReflectionOverride, val2.getClass().getSimpleName());
                break;
            case DELETE:
                diff.addLastChange(id, diffFieldReflectionOverride, val1.getClass().getSimpleName());
                return;
            case UPDATE:
                c = val1.getClass();
                diff.addLastChange(id, diffFieldReflectionOverride, val1.getClass().getSimpleName());
                break;
            case BOTH_NULL:
                return;
            default:
                // in both SAME and UPDATE val1 is non-null;
                c = val1.getClass();
        }

        fields = c.getDeclaredFields();

        // add the current object name as the parent
        if (parent == null) {
            parent = c.getSimpleName();
        }

        try {
            int fieldId = 0;
            for (int i = 0; i < fields.length; i++) {

                String currentFieldName = fields[i].getName();
                // https://www.javatpoint.com/java-field-setaccessible-method
                fields[i].setAccessible(true);
                Object innerVal1 = null;
                if (val1 != null) {
                    innerVal1 = fields[i].get(val1);
                }

                Object innerVal2 = null;

                if (val2 != null) {
                    innerVal2 = fields[i].get(val2);
                }

                Class currentClass;

                if (innerVal1 != null) {
                    currentClass = innerVal1.getClass();
                } else if (innerVal2 != null) {
                    currentClass = innerVal2.getClass();
                } else {
                    continue;
                }

                // Test if the field is a Primitive or an Object
                if (!Utils.isWrapperType(currentClass)) {
                    // Test if the Object is a Collection
                    if (!Utils.isClassCollections(currentClass)) {
                        // It's an object, so run recursive operation on this inner object.
                        recurseId += 1;
                        fieldId += 1;
                        this.calculate(String.format("%s.%d", id, recurseId), recurseId, diff, innerVal1, innerVal2, String.format("%s.%s", parent, currentFieldName));
                    } else {
                        // It's a Collection, so we get what type of Collection it is
                        // and calculate the difference based on that.
                        diffFieldReflection = this.getFieldReflection(innerVal1, innerVal2);
                        if (diffFieldReflection == DiffFieldReflection.DELETE || diffFieldReflection == DiffFieldReflection.CREATE || diffFieldReflection == DiffFieldReflection.UPDATE) {
                            if (Utils.isClassCollection(currentClass)) {
                                fieldId += 1;
                                this.calculateCollection(String.format("%s.%d", id, fieldId), diff, diffFieldReflection, currentFieldName, innerVal1, innerVal2);
                            }
                            if (Utils.isClassMap(currentClass)) {
                                fieldId += 1;
                                this.calculateMap(String.format("%s.%d", id, fieldId), diff, diffFieldReflection, currentFieldName, innerVal1, innerVal2);
                            }
                        }

                    }
                } else {
                    if (diffFieldReflectionOverride == DiffFieldReflection.CREATE) {
                        diffFieldReflection = diffFieldReflectionOverride;
                    } else {
                        diffFieldReflection = this.getFieldReflection(innerVal1, innerVal2);
                    }

                    if (diffFieldReflection == DiffFieldReflection.CREATE || diffFieldReflection == DiffFieldReflection.UPDATE || diffFieldReflection == DiffFieldReflection.DELETE) {
                        fieldId += 1;
                        diff.addLastChange(String.format("%s.%d", id, fieldId), diffFieldReflection, currentFieldName, String.format("%s", innerVal1), String.format("%s", innerVal2));
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new DiffException(e);
        }
    }

    /**
     * Calculate map helper method
     *
     * @param val1      original
     * @param val2      modified
     * @param operation the operation type (change between original and modified)
     * @return the change string containing the operation and the data that has changed
     * @throws DiffException
     */
    private <T extends Serializable> void calculateMap(String id, Diff<T> diff, DiffFieldReflection operation, String currentFieldName, Object val1, Object val2) throws DiffException {
        StringBuilder out = new StringBuilder();

        try {
            Object tmp = this.deepClone(val1);
            Object tmp2 = this.deepClone(val2);

            switch (operation) {
                case DELETE:
                    diff.addLastChange(id, operation, currentFieldName, Utils.mapToString((Map) tmp), "null");
                    break;
                case CREATE:
                    diff.addLastChange(id, operation, currentFieldName, "null", Utils.mapToString((Map) tmp2));
                    break;
                case UPDATE:
                    diff.addLastChange(id, operation, currentFieldName, Utils.mapToString((Map) tmp), Utils.mapToString((Map) tmp2));
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new DiffException(e);
        }
    }


    /**
     * Calculate collection helper method
     *
     * @param val1      original
     * @param val2      modified
     * @param operation the operation type (change between original and modified)
     * @throws DiffException
     */
    private <T extends Serializable> void calculateCollection(String id, Diff<T> diff, DiffFieldReflection operation, String currentFieldName, Object val1, Object val2) throws DiffException {
        try {
            Object tmp = this.deepClone(val1);
            Object tmp2 = this.deepClone(val2);

            switch (operation) {
                case DELETE:
                    diff.addLastChange(id, operation, currentFieldName, Utils.collectionToString((Collection) tmp), "null");
                    break;
                case CREATE:
                    diff.addLastChange(id, operation, currentFieldName, "null", Utils.collectionToString((Collection) tmp2));
                    break;
                case UPDATE:
                    diff.addLastChange(id, operation, currentFieldName, Utils.collectionToString((Collection) tmp), Utils.collectionToString((Collection) tmp2));
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new DiffException(e);
        }
    }

    /**
     * A helper method to get the type of operation we are dealing with
     *
     * @param currentVal original
     * @param anotherVal modified
     * @return DiffFieldReflection enum
     */
    private DiffFieldReflection getFieldReflection(Object currentVal, Object anotherVal) {
        if (currentVal == null && anotherVal != null) {
            // when currentVal is null and anotherVal is not, we create
            return DiffFieldReflection.CREATE;
        } else if (currentVal != null && anotherVal == null) {
            // when currentVal is not null and anotherVal is null, we delete from the current
            return DiffFieldReflection.DELETE;
        } else if (currentVal != null) {
            // when currentVal and anotherVal is not null, and not the same, we update.
            if (!currentVal.equals(anotherVal)) {
                return DiffFieldReflection.UPDATE;
            } else {
                return DiffFieldReflection.SAME;
            }
        }
        // last check is unnecessary
        // assuming last check is both being null
        return DiffFieldReflection.BOTH_NULL;
    }

    /**
     * Deep clone an object using Serialization
     * https://stackoverflow.com/a/7596565
     *
     * @param obj the object to clone
     * @return a new cloned object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object deepClone(Object obj) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        bos.close();
        byte[] byteData = bos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
        Object cloned = new ObjectInputStream(bais).readObject();
        return cloned;
    }


}
