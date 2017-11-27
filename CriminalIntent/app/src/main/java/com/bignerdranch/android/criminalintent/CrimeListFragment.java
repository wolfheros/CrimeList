package com.bignerdranch.android.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by james_huker on 8/6/17.
 */

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private boolean mSubtitleVisible;
    private static final String SAVED_SUBTITLE_VISIBLE_COUNT = "visible_count";
    private Callbacks mCallbacks;

    /**
     * 内部callbacks 借口，用来实现fragment回调接口。
     * 此接口被接管这个fragment 的 activity 负责实现。然后在被调用
     *
     * Required interface for hosting activities.
     **/
    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }


    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 通知FragmentManager ，此可以接fragment菜单调用请求。
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container
            , Bundle savedInstanceState) {

        // 绑定layout视图
        View view = inflater.inflate(R.layout.fragment_crime_list , container , false);

        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycle_view );
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // get mSubtitleVisible data before remote.
        if(savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE_COUNT);
        }

        updateUI();
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
    // rotate and save Bundle object , mSubtitleVisible filed;
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(SAVED_SUBTITLE_VISIBLE_COUNT , mSubtitleVisible);
    }

        // create a OptionsBar
    @Override
    public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {
        super.onCreateOptionsMenu(menu , inflater);
        inflater.inflate(R.menu.fragment_crime_list , menu);

        MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }
    }

    // this method when user click on ,used by system auto;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // choose which one subtitle been clicked. choose by id.
        switch (item.getItemId()) {
            case R.id.menu_item_new_crime:

                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                // 调用的是实现Callbacks接口的CrimeListActivity 类里的onCrimeSelected() 方法。
                updateUI();
                mCallbacks.onCrimeSelected(crime);
                return true;
            case R.id.menu_item_show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;   // false to true
                // 声明菜单选项已经改变，需要重新创建新的选项菜单
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        // get size if Crimes() Arrays
        int crimeCount = crimeLab.getCrimes().size();
        //  exchange the String word in the String.xml. Using int count .
        String subtitle = getString(R.string.subtitle_format , crimeCount);
        if(!mSubtitleVisible) { // false
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        // setting a sub actionbar for title.
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    // 抽取的更新fragment的界面的方法。
    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if(mAdapter == null){
            // 创建mAdapter进行关联crimes。创建出来新的CrimeAdapter
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        updateSubtitle();
    }

    // ViewHolder内部类
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;
        private Crime mCrime;
        public CrimeHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            // 绑定每个结构的布局
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_date_text_view);
            mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_check_box);
        }
        @Override
        public void onClick(View v) {
            // 直接调用的是CrimeListActivity 实现CrimeListFragment.Callbacks接口后的重写的方法
           mCallbacks.onCrimeSelected(mCrime);
        }

        // 这里用CrimeHolder的bindCrime方法来替换，
        // CrimeAdapter里的onBindViewHolder()绑定方法
        public void bindCrime(Crime crime) {
            // 来源于Adapter的crime实例。
            mCrime = crime;
            // 设定每个组件的显示名称
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedCheckBox.setChecked(mCrime.isSolved());
        }

    }
    // adapter inner class

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        // 当RecyclerView需要新的View视图时，会调用这个方法
        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent , int viewType ) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            // 绑定自己的全新布局
            View view = layoutInflater
                    .inflate(R.layout.list_item_crime ,parent , false);
            return new CrimeHolder(view);
        }
        // 这个方法是用来绑定视图和数据（模型层）
        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bindCrime(crime);
        }
        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

    }
    @Override
    public void onDetach(){
        super.onDetach();
        mCallbacks = null;
    }


}
