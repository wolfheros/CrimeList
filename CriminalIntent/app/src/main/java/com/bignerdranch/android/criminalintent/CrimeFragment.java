package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.UUID;

/**
 * Created by james_huker on 8/5/17.
 * 此类的作用是展示详细试图
 */

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private static final String DIALOG_DATE = "DialogDate";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO=2;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private Callbacks mCallbacks;

    /**
     * Required interface for hostig activities
     */
    interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    // 创建Fragment并添加Bundle键值对。Bundle 对象作为Argument添加进Fragment中。
    // 将对应的CrimeID放入对应选取的fragment中。
    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID , crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return  fragment;
    }
    @Override @Deprecated
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }
    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取一个crime对象。这个crime最初来源是点击列表后由系统传入的。
        // 从Fragment中获取argument然后在获取里面的，UUID数据。再通过UUID获取对应的Crime对象。
        UUID crimeId = (UUID) this.getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = (CrimeLab.get(getActivity())   // 获取一个CrimeLab对象，然后调用它的getCrime方法。
                .getCrime(crimeId));
        //
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container
            , Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime , container ,false);

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        // 获取包含在crime对象里的标题，并把它显示在Title里。
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start ,int count , int after
            ) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start , int before , int count
            ) {
                // Crime 中的getTitle方法
                mCrime.setTitle(s.toString());
                //  对于双布局修改文字，也要刷新CrimeListFragment 视图
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        // 绑定按钮键
        mDateButton = (Button)v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                // 绑定两个fragment，DatePickerFragment , and target Crimefragment.
                dialog.setTargetFragment(CrimeFragment.this , REQUEST_DATE);
                // DIALOG_DATE 是 FragmentManager 用来识别 DatePickerFragment唯一符号！
                dialog.show(ft , DIALOG_DATE);

                Toast.makeText(getActivity(), manager.getFragments().toString(), Toast.LENGTH_LONG).show();
            }
        });

        // 绑定solved的Id值
        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        // 检查包含的Crime里的isSolved值。
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        // 创建监听器，点击后改变isSolved得值。
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView , boolean isChecked) {
                // Set the Crime's solved property
                mCrime.setSolved(isChecked);
                // 修改mSolvedChecks 的值后，平板模式需要刷新CrimeListFragment 视图。
                updateCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT , getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT , getString(R.string.crime_report_subject));
                /*
                 *  可以取消用户的默认选项设置。
                 **/
                i = Intent.createChooser(i , getString(R.string.send_report));
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK , ContactsContract.Contacts.CONTENT_URI);
        // 设置当联系人应用不可用的时候，此处的选择也不可用。
        // pickContact.addCategory(Intent.CATEGORY_HOME);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact , REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        // 检查是否存在联系人应用
        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact , PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断文件储存位置不能为空。并且包含可以执行captureImage intent的activity。
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        //将照片存储到指定路径。
        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT , uri);
        }
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(captureImage , REQUEST_PHOTO);
            }
        });
        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onActivityResult(int requestCode , int resultCode , Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return ;
        }

        if(requestCode == REQUEST_DATE ){   //
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            updateCrime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you wangt your query ti return values for
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform you query - the contactUri is like a "Where"
            // clause here
            Cursor c = getActivity().getContentResolver().query(contactUri , queryFields ,null ,null , null);
            try {
                // Double - check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of date
                // that is your suspect name.
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                updateCrime();
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updateCrime();
            updatePhotoView();
        }
    }
    private void updateCrime(){
        // 添加Crime
        CrimeLab.get(getActivity()).updateCrime(mCrime);

        mCallbacks.onCrimeUpdated(mCrime);
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString ;
        if(mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat , mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if(suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        }  else {
            suspect = getString(R.string.crime_report_suspect , suspect);
        }

         return getString(R.string.crime_report , mCrime.getTitle(), dateString , solvedString , suspect);

    }

    private void updatePhotoView(){
        // 如果mPhotoFile 为空，设置图片的显示也为空。
        if (mPhotoFile == null || ! mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        }else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath() , getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }
}
