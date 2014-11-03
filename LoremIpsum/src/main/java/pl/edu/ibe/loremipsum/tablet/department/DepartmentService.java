/************************************
 * This file is part of Test Platform.
 *
 * Test Platform is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Test Platform is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Test Platform; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Ten plik jest częścią Platformy Testów.
 *
 * Platforma Testów jest wolnym oprogramowaniem; możesz go rozprowadzać dalej
 * i/lub modyfikować na warunkach Powszechnej Licencji Publicznej GNU,
 * wydanej przez Fundację Wolnego Oprogramowania - według wersji 2 tej
 * Licencji lub (według twojego wyboru) którejś z późniejszych wersji.
 *
 * Niniejszy program rozpowszechniany jest z nadzieją, iż będzie on
 * użyteczny - jednak BEZ JAKIEJKOLWIEK GWARANCJI, nawet domyślnej
 * gwarancji PRZYDATNOŚCI HANDLOWEJ albo PRZYDATNOŚCI DO OKREŚLONYCH
 * ZASTOSOWAŃ. W celu uzyskania bliższych informacji sięgnij do
 * Powszechnej Licencji Publicznej GNU.
 *
 * Z pewnością wraz z niniejszym programem otrzymałeś też egzemplarz
 * Powszechnej Licencji Publicznej GNU (GNU General Public License);
 * jeśli nie - napisz do Free Software Foundation, Inc., 59 Temple
 * Place, Fifth Floor, Boston, MA  02110-1301  USA
 ************************************/
package pl.edu.ibe.loremipsum.tablet.department;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.edu.ibe.loremipsum.db.schema.Department;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinExaminee;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import rx.Observable;

/**
 * Created by adam on 17.03.14.
 */
public class DepartmentService extends BaseService {
    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public DepartmentService(ServiceProvider services) {
        super(services);
    }

    public Observable<List<Department>> listDepartments() {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getDepartmentDao().loadAll());
    }

    /**
     * Checks if department with given name exists
     *
     * @param name
     * @return @link{Observable} on result
     */
    public Observable<Boolean> checkIfDepartmentExist(String name) {
        return RxExecutor.run(() -> {
            for (Department department : dbAccess().getDaoSession().getDepartmentDao().loadAll()) {
                if (department.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * @param institution
     * @return @link{Observable} on list of @link{Department} that belongs to @link{Institution}
     */
    public Observable<List<Department>> getDepartmentsListBasedOnInstitution(Institution institution) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().callInTx(() -> {
                    List<Department> departments = new ArrayList<>();
                    HashMap<Long, Department> hashMap = new HashMap<>();

                    for (Examinee examined : institution.getExamineeList()) {
                        if (examined.getDepartment() != null) {
                            hashMap.put(examined.getDepartment().getId(), examined.getDepartment());
                        }
                    }

                    for (Department department : hashMap.values()) {
                        departments.add(department);
                    }
                    return departments;
                }
        ));
    }

    /**
     * @param researcher
     * @return @link{Observable} on list of @link{Department} based on @link{Researcher}
     */
    public Observable<List<Department>> getDepartmentsListBasedOnResearcher(Researcher researcher) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().callInTx(() -> {
                    List<Department> departments = new ArrayList<>();
                    HashMap<Long, Department> hashMap = new HashMap<>();
                    researcher.resetResearcherJoinExamineeList();
                    for (ResearcherJoinExaminee researcherJoinExamined : researcher.getResearcherJoinExamineeList()) {
                        if (researcherJoinExamined.getExaminee().getDepartment() != null) {
                            hashMap.put(researcherJoinExamined.getExaminee().getDepartment().getId(), researcherJoinExamined.getExaminee().getDepartment());
                        }
                    }
                    for (Department department : hashMap.values()) {
                        departments.add(department);
                    }
                    return departments;
                }
        ));
    }

    /**
     * Inserts or updated if needed @link{Department} to database
     *
     * @param department
     * @return @link{Observable} on result
     */
    public Observable<Department> insertDepartment(Department department) {
        return RxExecutor.run(() -> {
            if (department.getId() == null) {
                dbAccess().getDaoSession().insert(department);
            } else {
                dbAccess().getDaoSession().update(department);
            }
            department.resetExamineeList();
            return department;
        });
    }


}
