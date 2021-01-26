package xyz.foobar;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    // https://stackoverflow.com/a/711226
    // Check if the current type of object is primitive or not.
    public static final Set<Class> WRAPPER_TYPES = new HashSet(Arrays.asList(
            String.class, Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Void.class));

    public static boolean isWrapperType(Class clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    // https://stackoverflow.com/a/2651654
    // Check if a class is a Collection or Map
    public static boolean isClassCollections(Class c) {
        return Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c);
    }

    public static boolean isClassMap(Class c) {
        return Map.class.isAssignableFrom(c);
    }

    public static boolean isClassCollection(Class c) {
        return Collection.class.isAssignableFrom(c);
    }

    public static String collectionToString(Collection<?> val) {
        return val.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "{", "}"));
    }

    public static String mapToString(Map<?, ?> val) {
        return val.keySet().stream()
                .map(key -> key + "=" + val.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    public static <T extends Serializable> byte[] serializeObject(T obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return baos.toByteArray();
    }

    /**
     * Deserialize back into the object
     * https://stackoverflow.com/a/14034555
     * @param data
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T extends Serializable> T deserializeObject(byte[] data, Class<T> type) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        T obj = type.cast(ois.readObject());
        ois.close();
        return obj;
    }
}
