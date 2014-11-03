package pl.edu.ibe.loremipsum.examinee;

import pl.edu.ibe.loremipsum.DbDependedTest;
import pl.edu.ibe.loremipsum.db.schema.Department;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.tablet.department.DepartmentService;
import pl.edu.ibe.loremipsum.tablet.examinee.ExamineeService;
import pl.edu.ibe.loremipsum.tablet.institution.InstitutionService;
import pl.edu.ibe.loremipsum.util.MockitoSetUpUtils;

import java.util.Date;

/**
 * Created by adam on 23.04.14.
 */
public class ExamineeManagerTest extends DbDependedTest {


    private ExamineeService examineeService;
    private InstitutionService institutionService;
    private DepartmentService departmentService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockitoSetUpUtils.setUp(getInstrumentation().getTargetContext());
        examineeService = new ExamineeService(getServiceProvider());
    }


    public void testAddExaminee() {
        Date date = new Date();
        Examinee examinee = new Examinee();
        examinee.setTextId("textId");
        examinee.setFirstName("firstName");
        examinee.setLastName("lastName");
        examinee.setBirthday(date);

        Long examineeId = examineeService.insertExaminee(examinee).toBlockingObservable().single();
        assertNotNull(examineeId);
        assertTrue(examineeId > 0);

        Examinee recievedExaminee = examineeService.getExaminee(examineeId).toBlockingObservable().single();

        assertEquals(examinee.getTextId(), recievedExaminee.getTextId());
        assertEquals(examinee.getFirstName(), recievedExaminee.getFirstName());
        assertEquals(examinee.getLastName(), recievedExaminee.getLastName());
        assertEquals(examinee.getBirthday().getTime(), recievedExaminee.getBirthday().getTime());
        assertEquals(0l, recievedExaminee.getDepartment_fk());
        assertEquals(0l, recievedExaminee.getInstituion_fk());
    }

    public void testAddExamineeAndInstitution() {
        Date date = new Date();
        Examinee examinee = new Examinee();
        examinee.setTextId("textId");
        examinee.setFirstName("firstName");
        examinee.setLastName("lastName");
        examinee.setBirthday(date);

        Institution institution = new Institution();
        institution.setTextId("intstitutionTextId");
        institution.setCity("city");
        institution.setStreet("street");
        institution.setPostalCode("postalCode");
        institution.setLegacyAddress("legacyAdress");
        institution.setProvince("province");

        Researcher researcher = new Researcher();
        researcher.setTextId("researcherTextId");
        researcher.setFirstName("firstName");
        researcher.setSurName("surName");
        researcher.setPassword("password");
        researcher.setHidden(true);


        Long examineeId = examineeService.insertExaminee(examinee, institution, researcher).toBlockingObservable().single();
        assertNotNull(examineeId);
        assertTrue(examineeId > 0);

        Examinee recievedExaminee = examineeService.getExaminee(examineeId).toBlockingObservable().single();
        assertEquals(examinee.getTextId(), recievedExaminee.getTextId());
        assertEquals(examinee.getFirstName(), recievedExaminee.getFirstName());
        assertEquals(examinee.getLastName(), recievedExaminee.getLastName());
        assertEquals(examinee.getBirthday().getTime(), recievedExaminee.getBirthday().getTime());
        assertTrue(recievedExaminee.getDepartment_fk() == 0);
        assertNotNull(recievedExaminee.getInstituion_fk());
        assertNull(recievedExaminee.getDepartment());
        assertNotNull(recievedExaminee.getInstitution());

        Institution recievedInstitution = recievedExaminee.getInstitution();

        assertEquals(institution.getTextId(), recievedInstitution.getTextId());
        assertEquals(institution.getStreet(), recievedInstitution.getStreet());
        assertEquals(institution.getPostalCode(), recievedInstitution.getPostalCode());
        assertEquals(institution.getCity(), recievedInstitution.getCity());
        assertEquals(institution.getProvince(), recievedInstitution.getProvince());
        assertEquals(institution.getLegacyAddress(), recievedInstitution.getLegacyAddress());

        assertEquals(1, recievedExaminee.getResearcherJoinExamineeList().size());

        Researcher recievedResearcher = recievedExaminee.getResearcherJoinExamineeList().get(0).getResearcher();
        assertTrue(recievedResearcher.getId() != 0);
        assertEquals(researcher.getTextId(), recievedResearcher.getTextId());
        assertEquals(researcher.getFirstName(), recievedResearcher.getFirstName());
        assertEquals(researcher.getSurName(), recievedResearcher.getSurName());
        assertEquals(researcher.getHidden(), recievedResearcher.getHidden());
        assertEquals(researcher.getPassword(), recievedResearcher.getPassword());
    }


    public void testAddExamineeAndInstitutionAndDepartment() {
        Date date = new Date();
        Examinee examinee = new Examinee();
        examinee.setTextId("textId");
        examinee.setFirstName("firstName");
        examinee.setLastName("lastName");
        examinee.setBirthday(date);

        Institution institution = new Institution();
        institution.setTextId("intstitutionTextId");
        institution.setCity("city");
        institution.setStreet("street");
        institution.setPostalCode("postalCode");
        institution.setLegacyAddress("legacyAdress");
        institution.setProvince("province");

        Researcher researcher = new Researcher();
        researcher.setTextId("researcherTextId");
        researcher.setFirstName("firstName");
        researcher.setSurName("surName");
        researcher.setPassword("password");
        researcher.setHidden(true);

        Department department = new Department();
        department.setName("departmentName");

        Long examineeId = examineeService.insertExaminee(examinee, institution, department, researcher).toBlockingObservable().single();
        assertNotNull(examineeId);
        assertTrue(examineeId > 0);

        Examinee recievedExaminee = examineeService.getExaminee(examineeId).toBlockingObservable().single();
        assertEquals(examinee.getTextId(), recievedExaminee.getTextId());
        assertEquals(examinee.getFirstName(), recievedExaminee.getFirstName());
        assertEquals(examinee.getLastName(), recievedExaminee.getLastName());
        assertEquals(examinee.getBirthday().getTime(), recievedExaminee.getBirthday().getTime());
        assertEquals(1, recievedExaminee.getDepartment_fk());
        assertEquals(1, recievedExaminee.getInstituion_fk());
        assertNotNull(recievedExaminee.getDepartment());
        assertNotNull(recievedExaminee.getInstitution());

        Institution recievedInstitution = recievedExaminee.getInstitution();

        assertEquals(institution.getTextId(), recievedInstitution.getTextId());
        assertEquals(institution.getStreet(), recievedInstitution.getStreet());
        assertEquals(institution.getPostalCode(), recievedInstitution.getPostalCode());
        assertEquals(institution.getCity(), recievedInstitution.getCity());
        assertEquals(institution.getProvince(), recievedInstitution.getProvince());
        assertEquals(institution.getLegacyAddress(), recievedInstitution.getLegacyAddress());


        assertEquals(1, recievedExaminee.getResearcherJoinExamineeList().size());

        Researcher recievedResearcher = recievedExaminee.getResearcherJoinExamineeList().get(0).getResearcher();
        assertTrue(recievedResearcher.getId() != 0);
        assertEquals(researcher.getTextId(), recievedResearcher.getTextId());
        assertEquals(researcher.getFirstName(), recievedResearcher.getFirstName());
        assertEquals(researcher.getSurName(), recievedResearcher.getSurName());
        assertEquals(researcher.getHidden(), recievedResearcher.getHidden());
        assertEquals(researcher.getPassword(), recievedResearcher.getPassword());

        Department recievedDepartment = recievedExaminee.getDepartment();
        assertTrue(recievedDepartment.getId() != 0);
        assertEquals(department.getName(), recievedDepartment.getName());
        assertEquals(1, department.getExamineeList().size());
        assertEquals(recievedExaminee.getId(), department.getExamineeList().get(0).getId());


        Date date2 = new Date();
        recievedExaminee.setTextId("NEWtextId");
        recievedExaminee.setFirstName("NEWfirstName");
        recievedExaminee.setLastName("NEWlastName");
        recievedExaminee.setBirthday(date2);

        recievedInstitution.setTextId("NEWintstitutionTextId");
        recievedInstitution.setCity("NEWcity");
        recievedInstitution.setStreet("NEWstreet");
        recievedInstitution.setPostalCode("NEWpostalCode");
        recievedInstitution.setLegacyAddress("NEWlegacyAdress");
        recievedInstitution.setProvince("NEWprovince");

        recievedDepartment.setName("NEWdepartmentName");

        Long updatedExamineeId = examineeService.insertExaminee(recievedExaminee, recievedInstitution, recievedDepartment, recievedResearcher).toBlockingObservable().single();


        assertNotNull(updatedExamineeId);
        assertTrue(updatedExamineeId > 0);

        Examinee updatedRecievedExaminee = examineeService.getExaminee(examineeId).toBlockingObservable().single();
        assertEquals(recievedExaminee.getTextId(), updatedRecievedExaminee.getTextId());
        assertEquals(recievedExaminee.getFirstName(), updatedRecievedExaminee.getFirstName());
        assertEquals(recievedExaminee.getLastName(), updatedRecievedExaminee.getLastName());
        assertEquals(recievedExaminee.getBirthday().getTime(), updatedRecievedExaminee.getBirthday().getTime());
        assertEquals(1, updatedRecievedExaminee.getDepartment_fk());
        assertNotNull(updatedRecievedExaminee.getInstituion_fk());
        assertNotNull(updatedRecievedExaminee.getDepartment());
        assertNotNull(updatedRecievedExaminee.getInstitution());


        Institution updatedRecievedExamineeInstitutionecievedInstitution = updatedRecievedExaminee.getInstitution();

        assertEquals(recievedInstitution.getTextId(), updatedRecievedExamineeInstitutionecievedInstitution.getTextId());
        assertEquals(recievedInstitution.getStreet(), updatedRecievedExamineeInstitutionecievedInstitution.getStreet());
        assertEquals(recievedInstitution.getPostalCode(), updatedRecievedExamineeInstitutionecievedInstitution.getPostalCode());
        assertEquals(recievedInstitution.getCity(), updatedRecievedExamineeInstitutionecievedInstitution.getCity());
        assertEquals(recievedInstitution.getProvince(), updatedRecievedExamineeInstitutionecievedInstitution.getProvince());
        assertEquals(recievedInstitution.getLegacyAddress(), updatedRecievedExamineeInstitutionecievedInstitution.getLegacyAddress());


        Department updatedRecievedExamineeDepartment = updatedRecievedExaminee.getDepartment();
        assertTrue(updatedRecievedExamineeDepartment.getId() != 0);
        assertEquals(recievedDepartment.getName(), updatedRecievedExamineeDepartment.getName());
        assertEquals(1, updatedRecievedExamineeDepartment.getExamineeList().size());
        assertEquals(updatedRecievedExaminee.getId(), updatedRecievedExamineeDepartment.getExamineeList().get(0).getId());


    }


    public void testAddExamineeAndDepartment() {
        Date date = new Date();
        Examinee examinee = new Examinee();
        examinee.setTextId("textId");
        examinee.setFirstName("firstName");
        examinee.setLastName("lastName");
        examinee.setBirthday(date);


        Researcher researcher = new Researcher();
        researcher.setTextId("researcherTextId");
        researcher.setFirstName("firstName");
        researcher.setSurName("surName");
        researcher.setPassword("password");
        researcher.setHidden(true);

        Department department = new Department();
        department.setName("departmentName");

        Long examineeId = examineeService.insertExaminee(examinee, department, researcher).toBlockingObservable().single();
        assertNotNull(examineeId);
        assertTrue(examineeId > 0);

        Examinee recievedExaminee = examineeService.getExaminee(examineeId).toBlockingObservable().single();
        assertEquals(examinee.getTextId(), recievedExaminee.getTextId());
        assertEquals(examinee.getFirstName(), recievedExaminee.getFirstName());
        assertEquals(examinee.getLastName(), recievedExaminee.getLastName());
        assertEquals(examinee.getBirthday().getTime(), recievedExaminee.getBirthday().getTime());
        assertEquals(1, recievedExaminee.getDepartment_fk());
        assertEquals(0, recievedExaminee.getInstituion_fk());
        assertNotNull(recievedExaminee.getDepartment());
        assertNull(recievedExaminee.getInstitution());

        assertEquals(1, recievedExaminee.getResearcherJoinExamineeList().size());

        Researcher recievedResearcher = recievedExaminee.getResearcherJoinExamineeList().get(0).getResearcher();
        assertTrue(recievedResearcher.getId() != 0);
        assertEquals(researcher.getTextId(), recievedResearcher.getTextId());
        assertEquals(researcher.getFirstName(), recievedResearcher.getFirstName());
        assertEquals(researcher.getSurName(), recievedResearcher.getSurName());
        assertEquals(researcher.getHidden(), recievedResearcher.getHidden());
        assertEquals(researcher.getPassword(), recievedResearcher.getPassword());

        Department recievedDepartment = recievedExaminee.getDepartment();
        assertTrue(recievedDepartment.getId() != 0);
        assertEquals(department.getName(), recievedDepartment.getName());
        assertEquals(1, department.getExamineeList().size());
        assertEquals(recievedExaminee.getId(), department.getExamineeList().get(0).getId());
    }


    public void testUpdateExamineeAndInstitutionAndDepartment() {
        Date date = new Date();
        Examinee examinee = new Examinee();
        examinee.setTextId("textId");
        examinee.setFirstName("firstName");
        examinee.setLastName("lastName");
        examinee.setBirthday(date);

        Institution institution = new Institution();
        institution.setTextId("intstitutionTextId");
        institution.setCity("city");
        institution.setStreet("street");
        institution.setPostalCode("postalCode");
        institution.setLegacyAddress("legacyAdress");
        institution.setProvince("province");

        Researcher researcher = new Researcher();
        researcher.setTextId("researcherTextId");
        researcher.setFirstName("firstName");
        researcher.setSurName("surName");
        researcher.setPassword("password");
        researcher.setHidden(true);

        Department department = new Department();
        department.setName("departmentName");

        Long examineeId = examineeService.insertExaminee(examinee, institution, department, researcher).toBlockingObservable().single();
        assertNotNull(examineeId);
        assertTrue(examineeId > 0);

        Examinee recievedExaminee = examineeService.getExaminee(examineeId).toBlockingObservable().single();
        assertEquals(examinee.getTextId(), recievedExaminee.getTextId());
        assertEquals(examinee.getFirstName(), recievedExaminee.getFirstName());
        assertEquals(examinee.getLastName(), recievedExaminee.getLastName());
        assertEquals(examinee.getBirthday().getTime(), recievedExaminee.getBirthday().getTime());
        assertEquals(1, recievedExaminee.getDepartment_fk());
        assertEquals(1, recievedExaminee.getInstituion_fk());
        assertNotNull(recievedExaminee.getDepartment());
        assertNotNull(recievedExaminee.getInstitution());

        Institution recievedInstitution = recievedExaminee.getInstitution();

        assertEquals(institution.getTextId(), recievedInstitution.getTextId());
        assertEquals(institution.getStreet(), recievedInstitution.getStreet());
        assertEquals(institution.getPostalCode(), recievedInstitution.getPostalCode());
        assertEquals(institution.getCity(), recievedInstitution.getCity());
        assertEquals(institution.getProvince(), recievedInstitution.getProvince());
        assertEquals(institution.getLegacyAddress(), recievedInstitution.getLegacyAddress());


        assertEquals(1, recievedExaminee.getResearcherJoinExamineeList().size());

        Researcher recievedResearcher = recievedExaminee.getResearcherJoinExamineeList().get(0).getResearcher();
        assertTrue(recievedResearcher.getId() != 0);
        assertEquals(researcher.getTextId(), recievedResearcher.getTextId());
        assertEquals(researcher.getFirstName(), recievedResearcher.getFirstName());
        assertEquals(researcher.getSurName(), recievedResearcher.getSurName());
        assertEquals(researcher.getHidden(), recievedResearcher.getHidden());
        assertEquals(researcher.getPassword(), recievedResearcher.getPassword());

        Department recievedDepartment = recievedExaminee.getDepartment();
        assertTrue(recievedDepartment.getId() != 0);
        assertEquals(department.getName(), recievedDepartment.getName());
        assertEquals(1, department.getExamineeList().size());
        assertEquals(recievedExaminee.getId(), department.getExamineeList().get(0).getId());
    }

    public void testUpdateExamineeAndInstitution() {
        Date date = new Date();
        Examinee examinee = new Examinee();
        examinee.setTextId("textId");
        examinee.setFirstName("firstName");
        examinee.setLastName("lastName");
        examinee.setBirthday(date);

        Institution institution = new Institution();
        institution.setTextId("intstitutionTextId");
        institution.setCity("city");
        institution.setStreet("street");
        institution.setPostalCode("postalCode");
        institution.setLegacyAddress("legacyAdress");
        institution.setProvince("province");

        Researcher researcher = new Researcher();
        researcher.setTextId("researcherTextId");
        researcher.setFirstName("firstName");
        researcher.setSurName("surName");
        researcher.setPassword("password");
        researcher.setHidden(true);


        Long examineeId = examineeService.insertExaminee(examinee, institution, researcher).toBlockingObservable().single();
        assertNotNull(examineeId);
        assertTrue(examineeId > 0);

        Examinee recievedExaminee = examineeService.getExaminee(examineeId).toBlockingObservable().single();
        assertEquals(examinee.getTextId(), recievedExaminee.getTextId());
        assertEquals(examinee.getFirstName(), recievedExaminee.getFirstName());
        assertEquals(examinee.getLastName(), recievedExaminee.getLastName());
        assertEquals(examinee.getBirthday().getTime(), recievedExaminee.getBirthday().getTime());
        assertTrue(recievedExaminee.getDepartment_fk() == 0);
        assertNotNull(recievedExaminee.getInstituion_fk());
        assertNull(recievedExaminee.getDepartment());
        assertNotNull(recievedExaminee.getInstitution());

        Institution recievedInstitution = recievedExaminee.getInstitution();

        assertEquals(institution.getTextId(), recievedInstitution.getTextId());
        assertEquals(institution.getStreet(), recievedInstitution.getStreet());
        assertEquals(institution.getPostalCode(), recievedInstitution.getPostalCode());
        assertEquals(institution.getCity(), recievedInstitution.getCity());
        assertEquals(institution.getProvince(), recievedInstitution.getProvince());
        assertEquals(institution.getLegacyAddress(), recievedInstitution.getLegacyAddress());

        assertEquals(1, recievedExaminee.getResearcherJoinExamineeList().size());

        Researcher recievedResearcher = recievedExaminee.getResearcherJoinExamineeList().get(0).getResearcher();
        assertTrue(recievedResearcher.getId() != 0);
        assertEquals(researcher.getTextId(), recievedResearcher.getTextId());
        assertEquals(researcher.getFirstName(), recievedResearcher.getFirstName());
        assertEquals(researcher.getSurName(), recievedResearcher.getSurName());
        assertEquals(researcher.getHidden(), recievedResearcher.getHidden());
        assertEquals(researcher.getPassword(), recievedResearcher.getPassword());


        Date date2 = new Date();
        recievedExaminee.setTextId("NEWtextId");
        recievedExaminee.setFirstName("NEWfirstName");
        recievedExaminee.setLastName("NEWlastName");
        recievedExaminee.setBirthday(date2);

        recievedInstitution.setTextId("NEWintstitutionTextId");
        recievedInstitution.setCity("NEWcity");
        recievedInstitution.setStreet("NEWstreet");
        recievedInstitution.setPostalCode("NEWpostalCode");
        recievedInstitution.setLegacyAddress("NEWlegacyAdress");
        recievedInstitution.setProvince("NEWprovince");


        Long updatedExamineeId = examineeService.insertExaminee(recievedExaminee, recievedInstitution, researcher).toBlockingObservable().single();


        assertNotNull(updatedExamineeId);
        assertTrue(updatedExamineeId > 0);

        Examinee updatedRecievedExaminee = examineeService.getExaminee(examineeId).toBlockingObservable().single();
        assertEquals(recievedExaminee.getTextId(), updatedRecievedExaminee.getTextId());
        assertEquals(recievedExaminee.getFirstName(), updatedRecievedExaminee.getFirstName());
        assertEquals(recievedExaminee.getLastName(), updatedRecievedExaminee.getLastName());
        assertEquals(recievedExaminee.getBirthday().getTime(), updatedRecievedExaminee.getBirthday().getTime());
        assertTrue(updatedRecievedExaminee.getDepartment_fk() == 0);
        assertNotNull(updatedRecievedExaminee.getInstituion_fk());
        assertNull(updatedRecievedExaminee.getDepartment());
        assertNotNull(updatedRecievedExaminee.getInstitution());


        Institution updatedRecievedExamineeInstitutionecievedInstitution = updatedRecievedExaminee.getInstitution();

        assertEquals(recievedInstitution.getTextId(), updatedRecievedExamineeInstitutionecievedInstitution.getTextId());
        assertEquals(recievedInstitution.getStreet(), updatedRecievedExamineeInstitutionecievedInstitution.getStreet());
        assertEquals(recievedInstitution.getPostalCode(), updatedRecievedExamineeInstitutionecievedInstitution.getPostalCode());
        assertEquals(recievedInstitution.getCity(), updatedRecievedExamineeInstitutionecievedInstitution.getCity());
        assertEquals(recievedInstitution.getProvince(), updatedRecievedExamineeInstitutionecievedInstitution.getProvince());
        assertEquals(recievedInstitution.getLegacyAddress(), updatedRecievedExamineeInstitutionecievedInstitution.getLegacyAddress());
    }

}
