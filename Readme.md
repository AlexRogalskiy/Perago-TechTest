<h1 align="center">Perago Technical Test</h1>

`practical-test.pdf` is the original document which describes the full use case.

The objective is to write a generic library to calculate the difference
between two objects - **original** and **modified** - and return a `diff`
object. The library also has to take into consideration
nested objects, cyclic dependencies and collections such as shown below.

```java
class Bar<T extends Serializable> {
    private String name;
    private String lastName;
}

class Foo<T extends Serializable> {
    // nested object
    private Bar bar;
    // cyclic dependency on itself
    private Foo friend;
    // collections
    private Set<String> nicknames;
}
```

The library should also be able to restore the **modified** object from its 
saved `diff` state.

## Assumptions

- Assumed Java 8 can be used
- Minimal dependencies should be used
- Maven should be used to run the project build
- JUint5 can be used to run the tests

### Run the Tests

```mvn test```

### Some notable resources

- https://stackoverflow.com/questions/48321997/how-to-compare-two-objects-and-find-the-fields-properties-changed
- https://www.javatpoint.com/java-field-setaccessible-method
- https://stackoverflow.com/a/7596565
- https://stackoverflow.com/a/14034555
