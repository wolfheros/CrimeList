package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by james_huker on 8/6/17.
 */

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.Callbacks , CrimeFragment.Callbacks{
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
    @Override
    protected int getLayoutResId(){
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        // crime来源于点击列表后产生的指定位置Crime，判断此处是不是平板的双排布局。即用detail_fragment_container 这个布局文件判断。
        // 这个布局文件存在与否，在于系统选择布局的时候会根据，屏幕的最小宽度大于sw600dp时，启用另一套资源，呈现出来的就是平板界面，此时这个布局文件就存在。

        if (findViewById(R.id.detail_fragment_container) == null) { // 当是手机布局时
            Intent intent = CrimePagerActivity.newIntent(this , crime.getId());
            startActivity(intent);
        } else {    // 当是平板布局时，创建了一个新的Fragment。
            Fragment newDetail = CrimeFragment.newInstance(crime.getId());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container , newDetail)
                    .commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime){
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }
}
