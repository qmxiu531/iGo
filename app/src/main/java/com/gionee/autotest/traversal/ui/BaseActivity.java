package com.gionee.autotest.traversal.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.gionee.autotest.traversal.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by viking on 9/14/17.
 *
 * base activity for all activity
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Nullable
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private Menu mMenu;
    @LayoutRes
    protected abstract int layoutResId();
    @MenuRes
    protected abstract int menuResId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layoutResId());

        ButterKnife.bind(this);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(isDisplayHomeUpEnabled());
                ab.setDisplayShowTitleEnabled(isDisplayShowTitleEnabled());
            }
        }
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        if (menuResId() != 0) {
            getMenuInflater().inflate(menuResId(), menu);
            mMenu = menu;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Nullable
    public final Menu getMenu() {
        return mMenu;
    }

    protected boolean isDisplayHomeUpEnabled() {
        return true;
    }

    protected boolean isDisplayShowTitleEnabled() {
        return true;
    }

}
