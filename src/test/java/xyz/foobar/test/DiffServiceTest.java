package xyz.foobar.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.foobar.Diff;
import xyz.foobar.DiffException;
import xyz.foobar.DiffRender;
import xyz.foobar.DiffService;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DiffServiceTest {

    private DiffService diffService;
    private DiffRender diffRender;

    @BeforeEach
    void init() {
        diffService = new DiffService();
        diffRender = new DiffRender();
    }

    /*@Test
    void apply() throws DiffException {
        Diff<Person> diff = new Diff<Person>();
        Person person = new Person();
        diffService.apply(person, diff);
    }*/

    @Test
    void calculateUpdate() throws DiffException {
        assertNotNull(this.diffRender);
        assertNotNull(this.diffService);

        Person person1 = new Person();
        person1.setFirstName("Foo");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("Foo-bar");
        person1.setNickNames(nicknames);
        assertEquals(person1.getFirstName(), "Foo");
        Person person2 = new Person();
        Diff<Person> diff = this.diffService.calculate(person1, person2);
        assertNotNull(diff);
        System.out.println(this.diffRender.render(diff));
    }

    @Test
    void calculateCreate() throws DiffException {
        assertNotNull(this.diffRender);
        assertNotNull(this.diffService);

        Person person1 = null;
        Person person2 = new Person();
        person2.setFirstName("Foo");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("Foo-bar");
        person2.setNickNames(nicknames);
        Diff<Person> diff = this.diffService.calculate(person1, person2);
        System.out.println(this.diffRender.render(diff));
    }

    @Test
    void calculateDelete() throws DiffException {
        assertNotNull(this.diffRender);
        assertNotNull(this.diffService);

        Person person1 = new Person();
        person1.setFirstName("Foo");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("Foo-bar");
        person1.setNickNames(nicknames);
        Person person2 = null;
        Diff<Person> diff = this.diffService.calculate(person1, person2);
        System.out.println(this.diffRender.render(diff));
    }
}
