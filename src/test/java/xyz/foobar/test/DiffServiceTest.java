package xyz.foobar.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.foobar.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DiffServiceTest {

    private DiffService diffService;
    private DiffRender diffRender;

    @BeforeEach
    void init() {
        diffService = new DiffService();
        diffRender = new DiffRender();
    }

    @Test
    void test_apply_properties() throws DiffException {
        // Original Person
        Person person1 = new Person();
        // Add data to original (person1)
        person1.setFirstName("Foo");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("Foo-bar");
        person1.setNickNames(nicknames);
        // assert the data set is correct.
        assertEquals(person1.getFirstName(), "Foo");
        assertEquals(person1.getNickNames(), nicknames);

        // Modified Person
        Person person2 = new Person();
        // Add data to modified (person2)
        Pet pet = new Pet();
        pet.setName("FooPet");
        person2.setPet(pet);

        // Run through use case

        // first calculate the diff between person1 and person2
        Diff<Person> diff = this.diffService.calculate(person1, person2);
        // check that diff is not null
        assertNotNull(diff);

        // Get the difference in human readable format
        System.out.println(String.format("DiffServiceTest.test_apply_properties\n%s", this.diffRender.render(diff)));

        // Restore the mutated object and test if it is the same
        Person mutated = diffService.apply(person1, diff);
        assertEquals(person2.getPet().getName(), mutated.getPet().getName());
        assertEquals(person2, mutated);
    }

    @Test
    void test_calculate_apply_cyclic() throws DiffException {
        // Original Person
        Person person1 = new Person();
        // Add data to original (person1)

        // Add a friend
        Person friend = new Person();
        friend.setFirstName("Friend1");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("0101");
        friend.setNickNames(nicknames);

        // Add a pet to friend
        Pet friendPet = new Pet();
        friendPet.setName("Friend1Pet");
        friend.setPet(friendPet);

        // Add friend to original
        person1.setFriend(friend);

        // assert the data set is correct.
        assertEquals(person1.getFriend().getFirstName(), "Friend1");
        assertEquals(person1.getFriend().getNickNames(), nicknames);
        assertEquals(person1.getFriend().getPet(), friendPet);

        // Modified Person
        Person person2 = new Person();
        // Add data to modified (person2)
        person2.setFirstName("Foo");
        Pet pet = new Pet();
        pet.setName("FooPet");
        person2.setPet(pet);

        // Run through use case

        // first calculate the diff between person1 and person2
        Diff<Person> diff = this.diffService.calculate(person1, person2);
        // check that diff is not null
        assertNotNull(diff);

        // Get the difference in human readable format
        System.out.println(String.format("DiffServiceTest.test_calculate_apply_cyclic\n%s", this.diffRender.render(diff)));

        // Restore the mutated object and test if it is the same
        Person mutated = diffService.apply(person1, diff);
        assertEquals(person2.getPet().getName(), mutated.getPet().getName());
        assertEquals(person2.getFirstName(), mutated.getFirstName());
        assertEquals(person2, mutated);
    }

    @Test
    void test_apply_object() throws DiffException {
        // Original Person
        Person person1 = new Person();
        // Add data to original (person1)
        person1.setFirstName("Foo");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("Foo-bar");
        person1.setNickNames(nicknames);
        // assert the data set is correct.
        assertEquals(person1.getFirstName(), "Foo");
        assertEquals(person1.getNickNames(), nicknames);

        // Modified Person
        Person person2 = null;

        // Run through use case

        // first calculate the diff between person1 and person2
        Diff<Person> diff = this.diffService.calculate(person1, person2);
        // check that diff is not null
        assertNotNull(diff);

        // Get the difference in human readable format
        System.out.println(String.format("DiffServiceTest.test_apply_object\n%s", this.diffRender.render(diff)));

        // Restore the mutated object and test if it is the same
        Person mutated = diffService.apply(person1, diff);
        assertNull(person2);
        assertNull(mutated);
        assertEquals(person2, mutated);
    }


    @Test
    void test_calculate_update() throws DiffException {
        assertNotNull(this.diffRender);
        assertNotNull(this.diffService);

        // Get the original
        Person person1 = new Person();
        // Set its data
        person1.setFirstName("Foo");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("Foo-bar");
        person1.setNickNames(nicknames);

        // ensure data is set
        assertEquals(person1.getFirstName(), "Foo");
        assertEquals(person1.getNickNames(), nicknames);

        // Get the modified
        Person person2 = new Person();
        // Get the difference
        Diff<Person> diff = this.diffService.calculate(person1, person2);
        assertNotNull(diff);

        // Get the difference in human readable format
        System.out.println(String.format("DiffServiceTest.test_calculate_update\n%s", this.diffRender.render(diff)));
    }

    @Test
    void test_calculate_create() throws DiffException {
        assertNotNull(this.diffRender);
        assertNotNull(this.diffService);

        // Get the original
        Person person1 = null;

        // Get the modified
        Person person2 = new Person();
        person2.setFirstName("Foo");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("Foo-bar");
        person2.setNickNames(nicknames);

        // Get the difference
        Diff<Person> diff = this.diffService.calculate(person1, person2);

        // Get the difference in human readable format
        System.out.println(String.format("DiffServiceTest.test_calculate_create\n%s", this.diffRender.render(diff)));
    }

    @Test
    void test_calculate_delete() throws DiffException {
        assertNotNull(this.diffRender);
        assertNotNull(this.diffService);

        // Get the original
        Person person1 = new Person();
        // Set the data
        person1.setFirstName("Foo");
        Set<String> nicknames = new HashSet<String>();
        nicknames.add("Foo-bar");
        person1.setNickNames(nicknames);

        // Get the modified
        Person person2 = null;

        // Get the difference
        Diff<Person> diff = this.diffService.calculate(person1, person2);

        // Get the difference in human readable format
        System.out.println(String.format("DiffServiceTest.test_calculate_delete\n%s", this.diffRender.render(diff)));
    }
}
