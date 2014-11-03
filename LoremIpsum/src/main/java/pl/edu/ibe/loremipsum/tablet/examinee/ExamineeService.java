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
package pl.edu.ibe.loremipsum.tablet.examinee;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pl.edu.ibe.loremipsum.configuration.Gender;
import pl.edu.ibe.loremipsum.db.schema.Department;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.ExamineeDao;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.db.schema.ResearcherJoinExaminee;
import pl.edu.ibe.loremipsum.tablet.base.ServiceProvider;
import pl.edu.ibe.loremipsum.tablet.examinee.data.AdditionalDataItem;
import pl.edu.ibe.loremipsum.tablet.examinee.data.ExamineeAdditionalFieldsManager;
import pl.edu.ibe.loremipsum.tablet.examinee.data.Field;
import pl.edu.ibe.loremipsum.tools.BaseService;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.loremipsum.tools.io.VirtualFile;
import pl.edu.ibe.testplatform.R;
import rx.Observable;

/**
 * Examinee service provides asyncronius operations for examinee table
 * Created by adam on 17.03.14.
 */
public class ExamineeService extends BaseService {
    private static final String TAG = ExamineeService.class.getCanonicalName();
    private AssertionError assertionError;
    private Examinee examinee;
    private ExamineeAdditionalFieldsManager examineeAdditionalFieldsManager;

    /**
     * Creates service with context
     *
     * @param services Services provider
     */
    public ExamineeService(ServiceProvider services) {
        super(services);
    }

    /**
     * @param id Examinee's identifier
     * @return examinee with selected id
     */
    public Observable<Examinee> getExaminee(long id) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getExamineeDao().load(id));
    }

    public Observable<Examinee> getExamineeByTextId(String textId) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getExamineeDao().queryBuilder().where(ExamineeDao.Properties.TextId.eq(textId)).unique());
    }

    /**
     * Checks if @link{Examinee} exist in database
     *
     * @param textId of examinee
     * @return true if examinee exists
     */
    public Observable<Boolean> checkIfExamineeExist(String textId) {
        return RxExecutor.run(() -> {
            for (Examinee examined : dbAccess().getDaoSession().getExamineeDao().loadAll()) {
                if (examined.getTextId().equals(textId)) {
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * checks if examinee exitst
     *
     * @param id Examinee's identifier
     * @return true if examinee exist
     */
    public Observable<Boolean> getExamineeExist(long id) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getExamineeDao().queryBuilder().where(ExamineeDao.Properties.Id.eq(id)).list().size() > 0);
    }

    /**
     * inserts examinee
     *
     * @param examinee Examinee instance
     * @return newly created examinee id
     */
    public Observable<Long> insertExaminee(Examinee examinee) {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getExamineeDao().insert(examinee));
    }

    /**
     * @return list of all examinee
     */
    public Observable<List<Examinee>> listExaminee() {
        return RxExecutor.run(() -> dbAccess().getDaoSession().getExamineeDao().loadAll());
    }

    /**
     * Inserts examinee with proper dependencies
     *
     * @param examinee    Examinee instance
     * @param institution Institution instance
     * @param department  Department instance
     * @param researcher  Researcher instance
     * @return newly created examinee id
     */
    public Observable<Long> insertExaminee(Examinee examinee, Institution institution,
                                           Department department, Researcher researcher) {
        return services.researcher().insertInstitutionWithResearcher(researcher, institution)
                .flatMap(ignore -> services.department().insertDepartment(department))
                .map(ignore -> {
                    examinee.setDepartment_fk(department.getId());
                    examinee.setInstituion_fk(institution.getId());

                    handleExaminee(examinee);

                    ResearcherJoinExaminee researcherJoinExaminee = new ResearcherJoinExaminee();
                    researcherJoinExaminee.setExaminee_fk(examinee.getId());
                    researcherJoinExaminee.setResearcher_fk(researcher.getId());
                    dbAccess().getDaoSession().getResearcherJoinExamineeDao().insert(researcherJoinExaminee);

                    researcher.resetResearcherJoinExamineeList();
                    examinee.resetResearcherJoinExamineeList();
                    researcher.resetResearcherJoinInstitutionList();


                    dbAccess().getDaoSession().clear();
                    return examinee.getId();
                });
    }


    /**
     * Inserts examinee with proper dependencies
     *
     * @param examinee    Examinee instance
     * @param institution Institution instance
     * @param researcher  @return newly created examinee id
     */
    public Observable<Long> insertExaminee(Examinee examinee, Institution institution, Researcher researcher) {
        return services.researcher().insertInstitutionWithResearcher(researcher, institution)
                .map(ignore -> {
                    examinee.setInstituion_fk(institution.getId());

                    handleExaminee(examinee);

                    ResearcherJoinExaminee researcherJoinExaminee = new ResearcherJoinExaminee();
                    researcherJoinExaminee.setExaminee_fk(examinee.getId());
                    researcherJoinExaminee.setResearcher_fk(researcher.getId());
                    dbAccess().getDaoSession().getResearcherJoinExamineeDao().insert(researcherJoinExaminee);

                    researcher.resetResearcherJoinExamineeList();
                    examinee.resetResearcherJoinExamineeList();
                    researcher.resetResearcherJoinInstitutionList();


                    dbAccess().getDaoSession().clear();
                    return examinee.getId();
                });
    }

    /**
     * Inserts examinee with proper dependencies
     *
     * @param examinee   Examinee instance
     * @param department Department instance
     * @param researcher @return newly created examinee id
     */
    public Observable<Long> insertExaminee(Examinee examinee, Department department, Researcher researcher) {
        return services.researcher().insertResearcher(researcher)
                .flatMap(ignore -> services.department().insertDepartment(department))
                .map(ignore -> {
                    examinee.setDepartment_fk(department.getId());


                    handleExaminee(examinee);

                    ResearcherJoinExaminee researcherJoinExaminee = new ResearcherJoinExaminee();
                    researcherJoinExaminee.setExaminee_fk(examinee.getId());
                    researcherJoinExaminee.setResearcher_fk(researcher.getId());
                    dbAccess().getDaoSession().getResearcherJoinExamineeDao().insert(researcherJoinExaminee);

                    researcher.resetResearcherJoinExamineeList();
                    examinee.resetResearcherJoinExamineeList();
                    researcher.resetResearcherJoinInstitutionList();


                    dbAccess().getDaoSession().clear();
                    return examinee.getId();
                });
    }

    /**
     * Updates or inserts @link{Examinee} to database
     *
     * @param examinee
     */
    private void handleExaminee(Examinee examinee) {
        if (examinee.getId() != null && examinee.getId() > 0) {
            dbAccess().getDaoSession().getExamineeDao().update(examinee);
        } else {
            dbAccess().getDaoSession().getExamineeDao().insert(examinee);
        }
    }


    /**
     * Inserts examinee with proper dependencies
     *
     * @param examinee   Examinee instance
     * @param researcher @return newly created examinee id
     */
    public Observable<Long> insertExaminee(Examinee examinee, Researcher researcher) {
        return services.researcher().insertResearcher(researcher)
                .map(ignore -> {
                    dbAccess().getDaoSession().getExamineeDao().insert(examinee);

                    ResearcherJoinExaminee researcherJoinExaminee = new ResearcherJoinExaminee();
                    researcherJoinExaminee.setExaminee_fk(examinee.getId());
                    researcherJoinExaminee.setResearcher_fk(researcher.getId());
                    dbAccess().getDaoSession().getResearcherJoinExamineeDao().insert(researcherJoinExaminee);

                    researcher.resetResearcherJoinExamineeList();
                    examinee.resetResearcherJoinExamineeList();
                    researcher.resetResearcherJoinInstitutionList();


                    dbAccess().getDaoSession().clear();
                    return examinee.getId();
                });
    }

    /**
     * Keeps reference to @link{Examinee} to edit.
     *
     * @param examinee
     * @param assertionError
     */
    public void setExamineeToEdit(Examinee examinee, AssertionError assertionError) {
        this.examinee = examinee;
        this.assertionError = assertionError;
    }

    /**
     * @return @link{Examinee} to edit and description of error
     */
    public Tuple.Two<Examinee, AssertionError> getExamineeToEdit() {
        if (examinee != null && assertionError != null) {
            Tuple.Two<Examinee, AssertionError> tuple = new Tuple.Two<>();
            tuple.first = examinee;
            tuple.second = assertionError;
            return tuple;
        }
        return null;
    }

    public ExamineeAdditionalFieldsManager getExamineeAdditionalFieldsManager() {
        return examineeAdditionalFieldsManager;
    }

    /**
     * Creates and fills @link(ExamineeAdditionalFieldsManager) or creates empty if no config file found.
     *
     * @param rootVirtualFile -root dir of taskSuite
     */
    public void fillExamineeAdditionalFieldsManager(VirtualFile rootVirtualFile) {
        examineeAdditionalFieldsManager = ExamineeAdditionalFieldsManager.createInstance(rootVirtualFile);
    }

    public List<View> getAdditionalFieldsViews(Context activity) {
        return examineeAdditionalFieldsManager.generateViews(activity);
    }

    public String handleAdditionalFieldsViews(List<View> views) {
        return examineeAdditionalFieldsManager.handleFilledViews(views, getSavedAdditionalInfo());
    }

    public ArrayList<AdditionalDataItem> getSavedAdditionalInfo() {
        return getServiceProvider().getGson().fromJson(examinee.getAdditionalData(), new TypeToken<ArrayList<AdditionalDataItem>>() {
        }.getType());
    }

    public void checkIfRequiredFieldsAreFilled() {
        HashMap<String, Tuple.Two<Field, AdditionalDataItem>> hashMap = makeValidationHashMap();
        for (Tuple.Two<Field, AdditionalDataItem> fieldAdditionalDataItemTwo : hashMap.values()) {
            if (fieldAdditionalDataItemTwo.second == null) {
                LogUtils.d(TAG, fieldAdditionalDataItemTwo.first.id + " is empty!");
                throw new AssertionError(fieldAdditionalDataItemTwo.first.id + " is empty!");
            }
        }
    }

    private HashMap<String, Tuple.Two<Field, AdditionalDataItem>> makeValidationHashMap() {
        ArrayList<AdditionalDataItem> additionalDataItems = getSavedAdditionalInfo();
        ArrayList<Field> requiredFields = examineeAdditionalFieldsManager.getRequiredFields();
        HashMap<String, Tuple.Two<Field, AdditionalDataItem>> hashMap = new HashMap<>();
        Tuple.Two<Field, AdditionalDataItem> pair;

        for (Field requiredField : requiredFields) {
            pair = Tuple.Two.create(requiredField, null);
            hashMap.put(requiredField.id, pair);
        }
        if (additionalDataItems != null && additionalDataItems.size() > 0) {
            for (AdditionalDataItem additionalDataItem : additionalDataItems) {
                if (hashMap.get(additionalDataItem.id) != null) {
                    hashMap.get(additionalDataItem.id).second = additionalDataItem;
                }
            }
        }
        return hashMap;
    }

    private ArrayList<Field> getNotFilledItems() {
        ArrayList<Field> fields = new ArrayList<>();
        HashMap<String, Tuple.Two<Field, AdditionalDataItem>> hashMap = makeValidationHashMap();
        for (Tuple.Two<Field, AdditionalDataItem> fieldAdditionalDataItemTwo : hashMap.values()) {
            if (fieldAdditionalDataItemTwo.second == null) {
                fields.add(fieldAdditionalDataItemTwo.first);
            }
        }
        return fields;
    }

    public void validateFieldsToFill(Context context, List<View> additionalFields) {
        HashMap<String, Tuple.Two<View, Field>> hashMap = new HashMap<>();
        Tuple.Two<View, Field> two;
        for (View additionalField : additionalFields) {
            two = new Tuple.Two<>();
            two.first = additionalField;
            hashMap.put(((Field) additionalField.getTag()).id, two);
        }
        for (Field field : getNotFilledItems()) {
            if (hashMap.get(field.id) != null) {
                hashMap.get(field.id).second = field;
            }
        }
        for (Tuple.Two<View, Field> viewFieldTwo : hashMap.values()) {
            if (viewFieldTwo.second != null) {
                if (viewFieldTwo.first instanceof EditText) {
                    ((EditText) viewFieldTwo.first).setError(context.getString(R.string.field_can_not_be_empty));
                }
            }
        }
        // Not filled items are pointed, Now fill with data proper fields.


        ArrayList<AdditionalDataItem> savedInfo = getSavedAdditionalInfo();
        AdditionalDataItem additionalDataItem;
        for (Tuple.Two<View, Field> viewFieldTwo : hashMap.values()) {
            if (viewFieldTwo.second == null) {
                if ((additionalDataItem = findSavedValueById(((Field) viewFieldTwo.first.getTag()).id)) != null) {
                    if (viewFieldTwo.first instanceof EditText) {
                        ((EditText) viewFieldTwo.first).setText(additionalDataItem.value);
                    } else if (viewFieldTwo.first instanceof Spinner) {
                        for (int i = 0; i < ((Spinner) viewFieldTwo.first).getCount(); i++) {
                            if (((Field) ((Spinner) viewFieldTwo.first).getItemAtPosition(i)).id.equals(additionalDataItem.value)) {
                                ((Spinner) viewFieldTwo.first).setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private AdditionalDataItem findSavedValueById(String id) {
        ArrayList<AdditionalDataItem> savedInfo = getSavedAdditionalInfo();
        for (AdditionalDataItem additionalDataItem : savedInfo) {
            if (additionalDataItem.id.equals(id)) {
                return additionalDataItem;
            }
        }
        return null;
    }



}
