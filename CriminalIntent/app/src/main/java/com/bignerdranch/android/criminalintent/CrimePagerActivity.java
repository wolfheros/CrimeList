package com.bignerdranch.android.criminalintent;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

/**
 * Created by james_huker on 8/10/17.
 * 看创建详细视图的CrimeFragment的托管Activit。
 */

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks{
    private static final String EXTRA_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_id";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    // 封装intent的信息，为了不让调用者查看
    public static Intent newIntent(Context packageContext , UUID crimeId) {
        Intent intent = new Intent(packageContext , CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID ,crimeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);
        // 绑定视图
        mViewPager = (ViewPager) findViewById(R.id.activity_crime_pager_view_pager);

        mCrimes = CrimeLab.get(this).getCrimes();
        FragmentManager fragmentManager = this.getSupportFragmentManager();

        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                // 通过传入的position值，确定crime对象，然后返回对应的fragment。
                Crime crime = mCrimes.get(position);
                // 调用的newInstance（）方法传入的UUID参数，并无任何意义，详见P178
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        UUID crimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

        for(int i = 0; i < mCrimes.size() ; i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

    }
    @Override
    public void onCrimeUpdated(Crime crime){

    }
}
