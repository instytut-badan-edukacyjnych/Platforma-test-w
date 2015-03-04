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

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.edu.ibe.loremipsum.configuration.Gender;
import pl.edu.ibe.loremipsum.db.schema.Department;
import pl.edu.ibe.loremipsum.db.schema.Examinee;
import pl.edu.ibe.loremipsum.db.schema.Institution;
import pl.edu.ibe.loremipsum.db.schema.Researcher;
import pl.edu.ibe.loremipsum.manager.MappingDependency;
import pl.edu.ibe.loremipsum.tablet.LoremIpsumApp;
import pl.edu.ibe.loremipsum.tablet.base.BaseServiceFragment;
import pl.edu.ibe.loremipsum.tablet.base.LoremIpsumSimpleAdapter;
import pl.edu.ibe.loremipsum.tablet.examinee.data.Field;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.StringUtils;
import pl.edu.ibe.loremipsum.tools.TimeUtils;
import pl.edu.ibe.loremipsum.tools.Tuple;
import pl.edu.ibe.testplatform.R;

/**
 * Created by adam on 23.04.14.
 */
public class ExamineeManagerFragment extends BaseServiceFragment {

    private static final String EXAMINEE_ID = "examineeId";
    private static final String TAG = ExamineeManagerFragment.class.getSimpleName();

    @InjectView(R.id.institution_list)
    Spinner institutionSpinner;

    @InjectView(R.id.institution_container)
    View institutionContainer;
    @InjectView(R.id.add_instistution)
    View addInstistution;
    @InjectView(R.id.institution_id)
    EditText institutionId;
    @InjectView(R.id.institution_name)
    EditText institutionName;
    @InjectView(R.id.institution_street)
    EditText institutionStreet;
    @InjectView(R.id.institution_postal_code)
    EditText institutionPostalCode;
    @InjectView(R.id.institution_city)
    EditText instituitionCity;
    @InjectView(R.id.institution_province)
    EditText institutionProvince;

    @InjectView(R.id.department_container)
    View departmentContainer;
    @InjectView(R.id.department_list)
    Spinner departmentSpinner;
    @InjectView(R.id.add_department)
    View addDepartment;
    @InjectView(R.id.department_name)
    EditText departmentName;

    @InjectView(R.id.examinee_text_id)
    EditText examineeTextId;
    @InjectView(R.id.examinee_first_name)
    EditText examineeFirstName;
    @InjectView(R.id.examinee_last_name)
    EditText examineeLastName;
    @InjectView(R.id.examinee_birthday)
    Button examineeBirthday;
    @InjectView(R.id.examinee_gender)
    RadioGroup examineeGender;
    @InjectView(R.id.examinee_male)
    RadioButton examineeMale;
    @InjectView(R.id.examinee_female)
    RadioButton examineeFemale;

    @InjectView(R.id.list_researchers)
    Spinner researcherSpinner;

    @InjectView(R.id.additional_fields)
    LinearLayout additionalFields;

    private LoremIpsumSimpleAdapter<Institution> institutionsAdapter;
    private LoremIpsumSimpleAdapter<Department> departmentsAdapter;
    private ResearcherAdapter researcherAdapter;

    private Researcher selectedResearcher = null;
    private Department selectedDepartment = null;
    private Institution selectedInstitution = null;
    private Date examineeBirthdayDate;
    private Examinee selectedExaminee;
    private boolean isInEditMode;
    private boolean examineeDuplicateIdError;
    private boolean departmentDuplicateIdError;
    private boolean institutionDuplicateIdError;
    private boolean disableDepartments;
    private boolean disableInstitutions;


    private List<View> additionalFieldsViews;

    private void saveAndExitClicked() {
        boolean emptyIdError = false;
        if (!disableDepartments && selectedDepartment == null && isEditTextEmpty(departmentName)) {
            emptyIdError = true;
        }
        if (!disableInstitutions && selectedInstitution == null && isEditTextEmpty(institutionId)) {
            emptyIdError = true;
        }

        if (selectedDepartment == null) {
            selectedDepartment = new Department();
        }
        selectedDepartment.setName(departmentName.getText().toString());

        if (selectedInstitution == null) {
            selectedInstitution = new Institution();
        }
        selectedInstitution.setTextId(institutionId.getText().toString());
        selectedInstitution.setName(institutionName.getText().toString());
        selectedInstitution.setStreet(institutionStreet.getText().toString());
        selectedInstitution.setPostalCode(institutionPostalCode.getText().toString());
        selectedInstitution.setCity(instituitionCity.getText().toString());
        selectedInstitution.setProvince(institutionProvince.getText().toString());

        if (selectedExaminee == null) {
            selectedExaminee = new Examinee();
        }
        selectedExaminee.setTextId(examineeTextId.getText().toString());
        selectedExaminee.setFirstName(examineeFirstName.getText().toString());
        selectedExaminee.setLastName(examineeLastName.getText().toString());
        selectedExaminee.setBirthday(examineeBirthdayDate);
        selectedExaminee.setGender(examineeGender.getCheckedRadioButtonId() == R.id.examinee_male ? Gender.MALE.toString() :
                examineeGender.getCheckedRadioButtonId() == R.id.examinee_female ? Gender.FEMALE.toString() : Gender.NONE.toString());
        if (validateRequiredFields(selectedExaminee, disableDepartments, disableInstitutions)) {
            insertWhatNeeded(selectedExaminee);
        }
    }

    @OnClick(R.id.examinee_birthday)
    public void onExamineeBirthdayClicked() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        BirthdayPicker dialog = new BirthdayPicker() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                examineeBirthdayDate = new Date(view.getCalendarView().getDate());
                examineeBirthday.setError(null);
                try {
                    examineeBirthday.setText(TimeUtils.dateToString(examineeBirthdayDate, TimeUtils.defaultPatern));
                } catch (ParseException e) {
                    examineeBirthday.setText("");
                }
            }
        };
        dialog.show(fragmentManager, "birthdayPickerDialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_examinee_manager, null, false);
        ButterKnife.inject(this, view);

        disableDepartments = getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableDepartments;
        disableInstitutions = getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableInstitutions;

        AssertionError assertionError;
        Tuple.Two<Examinee, AssertionError> tuple = getServiceProvider().examinee().getExamineeToEdit();
        if (tuple != null) {
            selectedExaminee = tuple.first;
            assertionError = tuple.second;
            isInEditMode = true;
        }

        additionalFieldsViews = getServiceProvider().examinee().getAdditionalFieldsViews(getActivity());
        if (additionalFieldsViews.size() > 0) {
            for (View additionalFieldView : additionalFieldsViews) {
                additionalFields.addView(additionalFieldView);
            }
            additionalFields.setVisibility(View.VISIBLE);
        } else {
            additionalFields.setVisibility(View.GONE);
        }

        if (selectedExaminee != null && isInEditMode) {
            examineeTextId.setText(selectedExaminee.getTextId());
            examineeTextId.setEnabled(false);
            examineeTextId.setFocusable(false);
            examineeFirstName.setText(selectedExaminee.getFirstName());
            examineeLastName.setText(selectedExaminee.getLastName());
            examineeBirthdayDate = selectedExaminee.getBirthday();
            switch (Gender.resolveGender(selectedExaminee.getGender())) {
                case FEMALE:
                    examineeFemale.setChecked(true);
                    examineeMale.setChecked(false);
                    break;
                case MALE:
                    examineeMale.setChecked(true);
                    examineeFemale.setChecked(false);
                    break;
                case NONE:
                    examineeMale.setChecked(false);
                    examineeFemale.setChecked(false);
                    break;
                default:
                    throw new RuntimeException("unimplemented gender: " + selectedExaminee.getGender());
            }
            try {
                examineeBirthday.setText(TimeUtils.dateToString(selectedExaminee.getBirthday(), TimeUtils.defaultPatern));
            } catch (Exception e) {
                e.printStackTrace();
            }
//            validateRequiredFields(selectedExaminee, disableDepartments, disableInstitutions);
        }

        if (getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableDepartments) {
            departmentContainer.setVisibility(View.GONE);
        } else {
            departmentContainer.setVisibility(View.VISIBLE);
            institutionsAdapter = new InstitutionAdapter();
        }
        if (getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableInstitutions) {
            institutionContainer.setVisibility(View.GONE);
        } else {
            institutionContainer.setVisibility(View.VISIBLE);
            departmentsAdapter = new DepartmentAdapter();
        }

        researcherAdapter = new ResearcherAdapter();

        institutionSpinner.setAdapter(institutionsAdapter);
        departmentSpinner.setAdapter(departmentsAdapter);
        researcherSpinner.setAdapter(researcherAdapter);

        examineeGender.setOnCheckedChangeListener((group, checkedId) -> {
            examineeFemale.setError(null);
            examineeMale.setError(null);
        });

        institutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((Institution) institutionSpinner.getSelectedItem()).getId() == -1) {
                    addInstistution.setVisibility(View.VISIBLE);
                    selectedInstitution = null;
                    clearInstitutionFields();
                } else {
                    selectedInstitution = (Institution) institutionSpinner.getSelectedItem();
                    addInstistution.setVisibility(View.GONE);
                    fillInstitutionFields();
                    if (isInEditMode) {
//                        validateRequiredFields(selectedExaminee, disableDepartments, disableInstitutions);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((Department) departmentSpinner.getSelectedItem()).getId() == -1) {
                    addDepartment.setVisibility(View.VISIBLE);
                    selectedDepartment = null;
                    clearDepartmentFields();
                } else {
                    selectedDepartment = (Department) departmentSpinner.getSelectedItem();
                    addDepartment.setVisibility(View.GONE);
                    fillDepartmentFields();
                    if (isInEditMode) {
//                        validateRequiredFields(selectedExaminee, disableDepartments, disableInstitutions);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        researcherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedResearcher = (Researcher) researcherSpinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        examineeTextId.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        if (isEditTextEmpty(examineeTextId)) {
                            examineeTextId.setError(getString(R.string.examinee_id_empty));
                        } else {
                            RxExecutor.runWithUiCallback(
                                    getServiceProvider().examinee().checkIfExamineeExist(examineeTextId.getText().toString())).subscribe((exist) -> {
                                if (exist) {
                                    examineeDuplicateIdError = true;
                                    examineeTextId.setError(getString(R.string.id_exists));
                                } else {
                                    examineeDuplicateIdError = false;
                                    examineeTextId.setError(null);
                                }
                            });
                        }
                    }
                }
        );

        institutionId.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        RxExecutor.runWithUiCallback(
                                getServiceProvider().institution().checkIfInstitutionExist(institutionId.getText().toString())).subscribe((exist) -> {
                            if (exist) {
                                institutionDuplicateIdError = true;
                                institutionId.setError(getString(R.string.id_exists));
                            } else {
                                institutionDuplicateIdError = false;
                                institutionId.setError(null);
                            }
                        });
                    }
                }
        );
        departmentName.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        RxExecutor.runWithUiCallback(
                                getServiceProvider().department().checkIfDepartmentExist(departmentName.getText().toString())).subscribe((exist) -> {
                            if (exist) {
                                departmentDuplicateIdError = true;
                                departmentName.setError(getString(R.string.id_exists));
                            } else {
                                departmentDuplicateIdError = false;
                                departmentName.setError(null);
                            }
                        });
                    }
                }
        );


        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_save) {
            saveAndExitClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void fillInstitutionFields() {
        institutionId.setText(selectedInstitution.getTextId());
        if (isInEditMode) {
            institutionId.setFocusable(false);
            institutionId.setEnabled(false);
        }
        institutionName.setText(selectedInstitution.getName());
        institutionStreet.setText(selectedInstitution.getStreet());
        institutionPostalCode.setText(selectedInstitution.getPostalCode());
        instituitionCity.setText(selectedInstitution.getCity());
        institutionProvince.setText(selectedInstitution.getProvince());
    }

    private void clearInstitutionFields() {
        institutionId.setText("");
        if (isInEditMode) {
            institutionId.setFocusable(true);
            institutionId.setEnabled(true);
        }
        institutionName.setText("");
        institutionStreet.setText("");
        institutionPostalCode.setText("");
        instituitionCity.setText("");
        institutionProvince.setText("");
    }

    private void fillDepartmentFields() {
        if (isInEditMode) {
            departmentName.setEnabled(false);
            departmentName.setFocusable(false);
        }
        departmentName.setText(selectedDepartment.getName());
    }

    private void clearDepartmentFields() {
        if (isInEditMode) {
            departmentName.setEnabled(true);
            departmentName.setFocusable(true);
        }
        departmentName.setText("");
    }

    private boolean validateRequiredFields(Examinee examinee, boolean disableDepartments, boolean disableInstitutions) {
        boolean isValid = true;
        if (isEditTextEmpty(examineeTextId)) {
            examineeTextId.setError(getString(R.string.examinee_id_empty));
            isValid = false;
        } else {
            if (examineeDuplicateIdError) {
                examineeTextId.setError(getString(R.string.id_exists));
                isValid = false;
            } else {
                examineeTextId.setError(null);
            }
        }
        if (isInEditMode) {
            getServiceProvider().examinee().validateFieldsToFill(getActivity(),additionalFieldsViews);
        } else {
            EditText editText;
            for (View view : additionalFieldsViews) {
                if (view instanceof EditText) {
                    editText = (EditText) view;
                    if (StringUtils.isEmpty(editText.getText().toString())) {
                        editText.setError(getString(R.string.field_can_not_be_empty));
                        isValid = false;
                    } else {
                        editText.setError(null);
                    }
                } else if (view instanceof Spinner) {

                } else {
                    throw new RuntimeException("Unknown field type: " + view);
                }
            }
        }

        if (!disableDepartments) {
            if (isEditTextEmpty(departmentName)) {
                departmentName.setError(getActivity().getString(R.string.add_department_id));
                isValid = false;
            } else {
                if (departmentDuplicateIdError) {
                    departmentName.setError(getString(R.string.id_exists));
                    isValid = false;
                } else {
                    departmentName.setError(null);
                }
            }
        }
        if (!disableInstitutions) {
            if (isEditTextEmpty(institutionId)) {
                institutionId.setError(getActivity().getString(R.string.add_institution_id));
                isValid = false;
            } else {
                if (institutionDuplicateIdError) {
                    institutionId.setError(getString(R.string.id_exists));
                    isValid = false;
                } else {
                    institutionId.setError(null);
                }
            }
        }

        MappingDependency mappingDependency = LoremIpsumApp.m_testManager.getMappingDependency();
        if (mappingDependency.isRequired(MappingDependency.EXAMINEE_AGE)) {
            if (examinee.getBirthday() != null) {
                mappingDependency.getLong(MappingDependency.EXAMINEE_AGE).value = examinee.getBirthday().getTime();
                if (!mappingDependency.getLong(MappingDependency.EXAMINEE_AGE).isValid()) {
                    examineeBirthday.setError(getString(R.string.add_birthday));
                    isValid = false;
                } else {
                    examineeBirthday.setError(null);
                }
            } else {
                examineeBirthday.setError(getString(R.string.add_birthday));
                isValid = false;
            }
        }
        if (mappingDependency.isRequired(MappingDependency.EXAMINEE_GENDER)) {
            mappingDependency.getString(MappingDependency.EXAMINEE_GENDER).value = examinee.getGender().toString();
            if (!mappingDependency.getString(MappingDependency.EXAMINEE_GENDER).isValid()) {
                examineeFemale.setError(getString(R.string.choose_gender));
                examineeMale.setError(getString(R.string.choose_gender));
                isValid = false;
            } else {
                examineeFemale.setError(null);
                examineeMale.setError(null);
            }
        }
        if (mappingDependency.isRequired(MappingDependency.INSTITUTION_CITY)) {
            if (selectedInstitution != null) {
                mappingDependency.getString(MappingDependency.INSTITUTION_CITY).value = selectedInstitution.getCity();
                if (!mappingDependency.getString(MappingDependency.INSTITUTION_CITY).isValid()) {
                    instituitionCity.setError(getActivity().getString(R.string.field_must_be_filled));
                    isValid = false;
                } else {
                    instituitionCity.setError(null);
                }
            } else {
                LogUtils.e(TAG, "selectedInstitution is null. ");
            }
        }

        if (mappingDependency.isRequired(MappingDependency.INSTITUTION_POSTAL)) {
            if (selectedDepartment != null) {
                mappingDependency.getString(MappingDependency.INSTITUTION_POSTAL).value = selectedInstitution.getPostalCode();
                if (!mappingDependency.getString(MappingDependency.INSTITUTION_POSTAL).isValid()) {
                    institutionPostalCode.setError(getActivity().getString(R.string.field_must_be_filled));
                    isValid = false;
                } else {
                    institutionPostalCode.setError(null);
                }
            } else {
                LogUtils.e(TAG, "selectedDepartment is null but it shouldn't");
            }
        }
        return isValid;
    }

    private void insertWhatNeeded(Examinee examinee) {
        getServiceProvider().examinee().setExamineeToEdit(examinee,null);
        examinee.setAdditionalData(getServiceProvider().examinee().handleAdditionalFieldsViews(additionalFieldsViews));

        int whatToInsert = 0;
        if (getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableInstitutions
                && getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableDepartments) {
            whatToInsert = 1;
        } else if (getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableInstitutions) {
            whatToInsert = 2;
        } else if (getServiceProvider().currentTaskSuite().getCurrentTaskSuiteConfig().disableDepartments) {
            whatToInsert = 3;
        }
        switch (whatToInsert) {
            case 0:
                RxExecutor.runWithUiCallback(
                        getServiceProvider().examinee()
                                .insertExaminee(examinee, selectedInstitution, selectedDepartment, selectedResearcher)
                ).subscribe((id) -> {
                    handleAddSuccess();
                }, (throwable) -> {
                    handleInsertError(throwable);
                });
                break;
            case 1:
                RxExecutor.runWithUiCallback(
                        getServiceProvider().examinee()
                                .insertExaminee(examinee, selectedResearcher)
                ).subscribe((id) -> {
                    handleAddSuccess();
                }, (throwable) -> {
                    handleInsertError(throwable);
                });
                break;
            case 2:
                RxExecutor.runWithUiCallback(
                        getServiceProvider().examinee()
                                .insertExaminee(examinee, selectedDepartment, selectedResearcher)
                ).subscribe((id) -> {
                    handleAddSuccess();
                }, (throwable) -> {
                    handleInsertError(throwable);
                });
                break;
            case 3:
                RxExecutor.runWithUiCallback(
                        getServiceProvider().examinee()
                                .insertExaminee(examinee, selectedInstitution, selectedResearcher)
                ).subscribe((id) -> {
                    handleAddSuccess();
                }, (throwable) -> {
                    handleInsertError(throwable);
                });
                break;
        }
    }

    private void handleAddSuccess() {
        Toast.makeText(getActivity(), getString(R.string.add_success), Toast.LENGTH_SHORT).show();
        getActivity().finish();
    }

    private void handleInsertError(Throwable throwable) {
        throwable.printStackTrace();
        Toast.makeText(getActivity(), getString(R.string.add_error), Toast.LENGTH_SHORT).show();
    }

    private boolean isEditTextEmpty(EditText editText) {
        return editText.getText().toString() == null || editText.getText().toString().equals("");
    }

    private class InstitutionAdapter extends LoremIpsumSimpleAdapter<Institution> {

        public InstitutionAdapter() {
            super(getActivity(), android.R.layout.simple_list_item_1);

            RxExecutor.runWithUiCallback(
                    getServiceProvider().institution().listInstitutions()).subscribe((list) -> {
                if (list.size() == 0) {
                    institutionSpinner.setVisibility(View.GONE);
                    addInstistution.setVisibility(View.VISIBLE);
                } else {
                    institutionSpinner.setVisibility(View.VISIBLE);
                    addInstistution.setVisibility(View.GONE);
                }
                populate(list);
                Institution institution = new Institution();
                institution.setTextId("+");
                institution.setName(getResources().getString(R.string.add_new));
                institution.setId(-1l);
                addItem(institution);
                if (isInEditMode) {
                    institutionSpinner.setVisibility(View.GONE);
                    int i;
                    for (i = 0; i < list.size(); i++) {
                        if (selectedExaminee.getInstituion_fk() == list.get(i).getId()) {
                            break;
                        }
                    }
                    selectedInstitution = list.get(i);
                    addInstistution.setVisibility(View.VISIBLE);
                    fillInstitutionFields();
                    if (isInEditMode) {
                        validateRequiredFields(selectedExaminee, disableDepartments, disableInstitutions);
                    }

                }
            });
        }

        @Override
        protected long extractId(Institution item) {
            return item.getId();
        }

        @Override
        protected String populateItem(Institution item) {
            return item.getTextId() + " " + item.getName();
        }
    }

    private class DepartmentAdapter extends LoremIpsumSimpleAdapter<Department> {

        public DepartmentAdapter() {
            super(getActivity(), android.R.layout.simple_list_item_1);
            RxExecutor.runWithUiCallback(
                    getServiceProvider().department().listDepartments()).subscribe((result) -> {
                if (result.size() == 0) {
                    departmentSpinner.setVisibility(View.GONE);
                    addDepartment.setVisibility(View.VISIBLE);
                } else {
                    addDepartment.setVisibility(View.GONE);
                    departmentSpinner.setVisibility(View.VISIBLE);
                }

                populate(result);
                Department department = new Department();
                department.setName("+ " + getResources().getString(R.string.add_new));
                department.setId(-1l);
                addItem(department);
                if (isInEditMode) {
                    departmentSpinner.setVisibility(View.GONE);
                    int i;
                    for (i = 0; i < result.size(); i++) {
                        if (selectedExaminee.getDepartment_fk() == result.get(i).getId()) {
                            break;
                        }
                    }
                    selectedDepartment = result.get(i);
                    addDepartment.setVisibility(View.VISIBLE);
                    fillDepartmentFields();
                    if (isInEditMode) {
                        validateRequiredFields(selectedExaminee, disableDepartments, disableInstitutions);
                    }
                }
            });
        }

        @Override
        protected long extractId(Department item) {
            return item.getId();
        }

        @Override
        protected String populateItem(Department item) {
            return item.getName();
        }
    }

    private class ResearcherAdapter extends LoremIpsumSimpleAdapter<Researcher> {

        public ResearcherAdapter() {
            super(getActivity(), android.R.layout.simple_list_item_1);
            RxExecutor.runWithUiCallback(
                    getServiceProvider().researcher().listResearchers()).subscribe((result) -> {
                populate(result);

                int i;
                for (i = 0; i < result.size(); i++) {
                    if (getServiceProvider().login().currentLoggedInUser.getId() == result.get(i).getId()) {
                        break;
                    }
                }
                researcherSpinner.setSelection(i);
            });
        }

        @Override
        protected long extractId(Researcher item) {
            return item.getId();
        }

        @Override
        protected String populateItem(Researcher item) {
            return item.getTextId() + " - " + item.getFirstName() + " " + item.getSurName();
        }
    }
}
