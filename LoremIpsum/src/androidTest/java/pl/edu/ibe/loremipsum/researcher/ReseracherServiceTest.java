package pl.edu.ibe.loremipsum.researcher;

import pl.edu.ibe.loremipsum.BaseInstrumentationTestCase;
import pl.edu.ibe.loremipsum.db.schema.Researcher;

/**
 * @author Mariusz Pluciński
 */
public class ReseracherServiceTest extends BaseInstrumentationTestCase {
    public void testRemoveResearcher() {
        Researcher researcher = new Researcher();
        researcher.setTextId("alice");
        researcher.setFirstName("Alice");
        researcher.setSurName("Anonymous");
        researcher.setPassword("password-alice");
        execute(getServiceProvider().researcher().insertResearcher(researcher));

        assertTrue(execute(getServiceProvider().researcher().researcherExist("alice")));
        assertEquals("Alice", execute(getServiceProvider().researcher().getResearcherByTextId("alice")).getFirstName());

        execute(getServiceProvider().researcher().removeResearcher(researcher));
        assertFalse(execute(getServiceProvider().researcher().researcherExist("alice")));
        assertThrowsWrapped(IndexOutOfBoundsException.class, () -> execute(getServiceProvider().researcher().getResearcherByTextId("alice")).getFirstName());
    }
}
