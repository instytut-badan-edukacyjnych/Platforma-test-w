/*
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
 */

package pl.edu.ibe.loremipsum.tablet.support;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pl.edu.ibe.loremipsum.support.SupportResponse;
import pl.edu.ibe.loremipsum.tablet.base.ServiceDialogFragment;
import pl.edu.ibe.loremipsum.task.management.OfflineModeEnabledDialog;
import pl.edu.ibe.loremipsum.tools.FileUtils;
import pl.edu.ibe.loremipsum.tools.LogUtils;
import pl.edu.ibe.loremipsum.tools.NetworkUtils;
import pl.edu.ibe.loremipsum.tools.RxExecutor;
import pl.edu.ibe.loremipsum.tools.StringUtils;
import pl.edu.ibe.loremipsum.tools.io.FilesystemFile;
import pl.edu.ibe.testplatform.BuildConfig;
import pl.edu.ibe.testplatform.R;
import rx.android.observables.AndroidObservable;
import rx.observables.ConnectableObservable;

/**
 * Created by adam on 17.04.14.
 */
public class SupportDialog extends ServiceDialogFragment {
    private static final String TAG = SupportDialog.class.toString();
    static private final String dumpLogProgressDialog = "dumpLogProgressDialog";
    static private final String sendProgressDialog = "sendProgressDialog";
    static private ConnectableObservable<Object> dumpLogOperation = null;
    static private String dumpLogTargetName;
    static private ConnectableObservable<SupportResponse> sendOperation = null;
    @InjectView(R.id.email)
    EditText email;
    @InjectView(R.id.phone)
    EditText phone;
    @InjectView(R.id.bug_description)
    EditText bugDescription;
    @InjectView((R.id.attach_log))
    CheckBox attachLogs;
    @InjectView(R.id.button_cancel)
    Button cancel;
    @InjectView(R.id.button_dump_log)
    Button dumpLog;
    @InjectView(R.id.button_send)
    Button send;

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static void show(Activity activity, FragmentManager fragmentManager) {
        if (BuildConfig.OMIT_NETWORK_CHECK) {
            SupportDialog dialog = new SupportDialog();
            dialog.show(fragmentManager, "supportDialog");
        } else {
            if (NetworkUtils.isOnline(activity)) {
                SupportDialog dialog = new SupportDialog();
                dialog.show(fragmentManager, "supportDialog");
            } else {
                OfflineModeEnabledDialog offlineModeEnabledDialog
                        = new OfflineModeEnabledDialog(R.string.disable_offline_mode_to_report_bug) {
                    @Override
                    public void startSettingsActivity() {
                        OfflineModeEnabledDialog.openSettings(activity);
                    }
                };
                offlineModeEnabledDialog.show(fragmentManager, "networkDialog");
            }
        }
    }

    private static void reallyShow(Context context, FragmentManager fragmentManager) {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_support);
        dialog.setTitle(R.string.report_error_issue);

        ButterKnife.inject(this, dialog);
        syncDumpLogOperation();
        syncSendOperation();
        return dialog;
    }

    @OnClick(R.id.button_cancel)
    public void onCancelClick(View v) {
        dismiss();
    }

    @OnClick(R.id.button_dump_log)
    public void onDumpLogClick(View v) {
        FilesystemFile source = new FilesystemFile(LogUtils.getLogsDirectory());
        dumpLogTargetName = getActivity().getString(R.string.app_name);
        FilesystemFile target = new FilesystemFile(new File(Environment.getExternalStorageDirectory(), dumpLogTargetName));
        if (dumpLogOperation == null) {
            dumpLogOperation = RxExecutor.runSingle(() -> {
                target.mkdirs();
                return RxExecutor.EMPTY_OBJECT;
            }).flatMap(ignore -> FileUtils.copyRecursive(source, target, true))
                    .publish();
            dumpLogOperation.connect();
        }
        syncDumpLogOperation();
    }

    private void syncDumpLogOperation() {
        if (dumpLogOperation != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(dumpLogProgressDialog);
            if (prev != null) {
                LogUtils.v(TAG, "Removing existing fragment: " + prev);
                ft.remove(prev);
            }

            ProgressDialog progress = new ProgressDialog();
            progress.setText(getResources().getString(R.string.copying_logs));
            progress.show(ft, dumpLogProgressDialog);

            AndroidObservable.bindFragment(this, dumpLogOperation).subscribe(ignore -> {
                progress.dismiss();

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.logs_copying_done)
                        .setMessage(getActivity().getString(R.string.logs_are_on_sdcard, dumpLogTargetName))
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();
                dumpLogOperation = null;
            });
        }
    }

    @OnClick(R.id.button_send)
    public void onSendClick(View v) {
        String emailString = email.getText().toString();
        if (StringUtils.isEmpty(emailString) || !isValidEmail(emailString)) {
            if (StringUtils.isEmpty(emailString)) {
                email.setError("Email jest wymagany!");
            } else {
                email.setError("Email jest nie poprawny!");
            }
        } else {
            if (sendOperation == null) {
                sendOperation = getServiceProvider().support().reportBug(email.getText().toString(),
                        phone.getText().toString(), bugDescription.getText().toString(),
                        attachLogs.isChecked())
                        .publish();
                sendOperation.connect();
            }
            syncSendOperation();
        }
    }

    private void syncSendOperation() {
        if (sendOperation != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(sendProgressDialog);
            if (prev != null)
                ft.remove(prev);

            ProgressDialog progress = new ProgressDialog();
            progress.setText(getResources().getString(R.string.submitting_report));
            progress.show(ft, sendProgressDialog);

            AndroidObservable.bindFragment(this, sendOperation).subscribe(response -> {
                progress.dismiss();
                dialogDismissed(response);
                sendOperation = null;
            }, throwable -> {
                progress.dismiss();
                sendOperation = null;
                Toast.makeText(getActivity(), getActivity().getString(R.string.send_failure), Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void dialogDismissed(SupportResponse response) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.view_bug_report_sent, null);
        TextView id = (TextView) view.findViewById(R.id.text_report_id);
        TextView timeSpan = (TextView) view.findViewById(R.id.expected_time);
        TextView email = (TextView) view.findViewById(R.id.email);
        TextView phone = (TextView) view.findViewById(R.id.phone);
        TextView note = (TextView) view.findViewById(R.id.note);

        id.setText(Long.toString(response.getReportId()));

        try {
            SupportResponse.SupportInfo info = response.getInfo();
            timeSpan.setText(getResources().getQuantityString(R.plurals.expected_hours, info.getTimeSpan(), info.getTimeSpan()));
            email.setText(info.getEmail());
            phone.setText(info.getPhoneNumber());
            note.setText(info.getAdditionalInfo());
        } catch (SupportResponse.SupportInfoNotFound supportInfoNotFound) {
            timeSpan.setText(R.string.retrieve_failed);
            timeSpan.setTypeface(null, Typeface.BOLD_ITALIC);
            email.setText(R.string.retrieve_failed);
            email.setTypeface(null, Typeface.BOLD_ITALIC);
            phone.setText(R.string.retrieve_failed);
            phone.setTypeface(null, Typeface.BOLD_ITALIC);
            note.setText(R.string.retrieve_failed);
            note.setTypeface(null, Typeface.BOLD_ITALIC);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.bug_report_sent_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (ignore1, ignore2) -> dismiss())
                .create()
                .show();
    }

    public static class ProgressDialog extends ServiceDialogFragment {
        private String text;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);

            Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.dialog_progress);
            dialog.setTitle(R.string.please_wait);
            TextView text = (TextView) dialog.findViewById(R.id.text);
            text.setText(this.text); //R.string.copying_logs);
            return dialog;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
