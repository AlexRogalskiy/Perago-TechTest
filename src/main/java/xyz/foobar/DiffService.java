package xyz.foobar;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class DiffService implements DiffEngine {

    public <T extends Serializable> T apply(T original, Diff<?> diff) throws DiffException {
        if (original == null) {
            return null;
        } else {
            T modified = (T) diff.getModified(original.getClass());
            return modified;
        }
    }

    public <T extends Serializable> Diff<T> calculate(T original, T modified) throws DiffException {
        // List<String> lastChanges = new ArrayList<String>();
        Diff<T> diff = new Diff<T>();
        diff.updateModified(modified);
        this.calculate(diff, original, modified, null);
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
    private <T extends Serializable> void calculate(Diff<T> diff, Object val1, Object val2, String parent) throws DiffException {
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
                diff.addLastChange(String.format("%s: %s", diffFieldReflectionOverride.label, val2.getClass().getName()));
                break;
            case DELETE:
                diff.addLastChange(String.format("%s: %s", diffFieldReflectionOverride.label, val1.getClass().getName()));
                return;
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

                if (!Utils.isWrapperType(currentClass)) {
                    if (!Utils.isClassCollections(currentClass)) {
                        this.calculate(diff, innerVal1, innerVal2, String.format("%s.%s", parent, currentFieldName));
                    } else {
                        diffFieldReflection = this.getFieldReflection(innerVal1, innerVal2);
                        if (diffFieldReflection == DiffFieldReflection.DELETE || diffFieldReflection == DiffFieldReflection.CREATE || diffFieldReflection == DiffFieldReflection.UPDATE) {
                            if (Utils.isClassCollection(currentClass)) {
                                String changes = this.calculateCollection(innerVal1, innerVal2, diffFieldReflection);
                                diff.addLastChange(String.format("%s: %s from %s", diffFieldReflection.label, currentFieldName, changes));
                                // diff.addChange(parent, currentFieldName, innerVal2);
                            }
                            if (Utils.isClassMap(currentClass)) {
                                String changes = this.calculateMap(innerVal1, innerVal2, diffFieldReflection);
                                diff.addLastChange(String.format("%s: %s from %s", diffFieldReflection.label, currentFieldName, changes));
                                // diff.addChange(parent, currentFieldName, innerVal2);
                                //diff.updateModified((T) innerVal2);
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
                        diff.addLastChange(String.format("%s: %s as %s", diffFieldReflection.label, currentFieldName, innerVal2));
                        // diff.addChange(parent, currentFieldName, innerVal2);
                        //diff.updateModified((T) innerVal2);
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
     * @param val1
     * @param val2
     * @param operation
     * @return
     * @throws DiffException
     */
    private String calculateMap(Object val1, Object val2, DiffFieldReflection operation) throws DiffException {
        StringBuilder out = new StringBuilder();

        try {
            Object tmp = this.deepClone(val1);
            Object tmp2 = this.deepClone(val2);

            switch (operation) {
                case DELETE:
                    out.append(Utils.mapToString((Map) tmp));
                    out.append(" to null");
                    break;
                case CREATE:
                    out.append("null to ");
                    out.append(Utils.mapToString((Map) tmp2));
                    break;
                case UPDATE:
                    out.append(Utils.mapToString((Map) tmp));
                    out.append(" to ");
                    out.append(Utils.mapToString((Map) tmp2));
                    break;
            }
            return out.toString();
        } catch (IOException | ClassNotFoundException e) {
            throw new DiffException(e);
        }
    }


    /**
     * Calculate collection helper method
     *
     * @param val1
     * @param val2
     * @param operation
     * @return
     * @throws DiffException
     */
    private String calculateCollection(Object val1, Object val2, DiffFieldReflection operation) throws DiffException {
        StringBuilder out = new StringBuilder();

        try {
            Object tmp = this.deepClone(val1);
            Object tmp2 = this.deepClone(val2);

            switch (operation) {
                case DELETE:
                    out.append(Utils.collectionToString((Collection) tmp));
                    out.append(" to null");
                    break;
                case CREATE:
                    out.append("null to ");
                    out.append(Utils.collectionToString((Collection) tmp2));
                    break;
                case UPDATE:
                    out.append(Utils.collectionToString((Collection) tmp));
                    out.append(" to ");
                    out.append(Utils.collectionToString((Collection) tmp2));
                    break;
            }
            return out.toString();
        } catch (IOException | ClassNotFoundException e) {
            throw new DiffException(e);
        }
    }

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
     * @param obj
     * @return
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
