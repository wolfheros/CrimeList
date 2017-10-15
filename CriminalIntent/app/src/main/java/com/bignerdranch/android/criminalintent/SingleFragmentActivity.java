package com.bignerdranch.android.criminalintent;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by james_huker on 8/6/17.
 * 此类为抽象类：第一，目的产生一个activity 。
 *             第二，目的继承此类的新类会实现里面的 createfragment()抽象方法对应的产生一个fragment.
 *             第三，在设置了getLayoutResId() 方法后，可以用来给基础的activity
 *                  绑定不同的资源 ID，实现手机和平板的布局文件更换。
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {
    // 需要继承的抽象方法。
    protected abstract Fragment createFragment();
    // 重写此方法后调用的将会是子类的相对应的此方法。
    @LayoutRes
    protected int getLayoutResId(){
        return R.layout.activity_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResId());     // 注意此处的R.layout.activity_fragment,采用的是灵活布局。
        FragmentManager fm =getSupportFragmentManager();    //创建一个兼容的FragmentManager对象。
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);   //注意此处的是R.layout.fragment_container

        // 判断fragment是不是为null。
        if(fragment == null) {
            // 继承得到的新类需要实现的方法。
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container , fragment)
                    .commit();
        }
    }
}
