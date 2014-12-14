package se.kolefors.handroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParserException;

public class MainActivity extends Activity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    public Context context;

    private List fragmentList;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        this.fragmentList = new ArrayList();

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        } else if (id == R.id.action_update) {
            redrawAllFragments();
        }
        return super.onOptionsItemSelected(item);
    }

    private Timer autoUpdate;

    @Override
    public void onResume() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ApplicationContextProvider.getContext());
        Log.d("onResume", "Setting update interval to " + settings.getString("update_interval", null) + ".");
        int interval = Integer.parseInt(settings.getString("update_interval", "0"));
        super.onResume();
        autoUpdate = new Timer();
        if (interval > 0) {
            autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        refreshAllFragments();
                    }
                });
                }
            }, 0, interval);
        }
    }

    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }

    private void redrawAllFragments() {
        Iterator it = fragmentList.iterator();
        while(it.hasNext())
        {
            MainFragment currFragment  = (MainFragment)it.next();
            currFragment.redraw();
        }
    }

    private void refreshAllFragments() {
        Log.d("refreshAllFragments", "Updating status of everything!");
        Iterator it = fragmentList.iterator();
        while(it.hasNext())
        {
            MainFragment currFragment  = (MainFragment)it.next();
            currFragment.refresh();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a MainFragment (defined as a static inner class below).
            Fragment fragment = MainFragment.newInstance(position + 1);
            fragmentList.add(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.devices);
                case 1:
                    return getString(R.string.groups);
                case 2:
                    return getString(R.string.scenarios);
                case 3:
                    return getString(R.string.sensors);
            }
            return null;
        }
    }

    public static class MainFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static MainFragment newInstance(int sectionNumber) {
            MainFragment fragment = new MainFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private View rootView;
        private boolean hasBeenDrawn;
        private PopulateFragment populater;

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d("MainFragment.onSharedPreferenceChanged()", "Notifying fragment " + getArguments().getInt(ARG_SECTION_NUMBER) + " about preference changes.");
            redraw();
        }

        public MainFragment() {
            hasBeenDrawn = false;
        }

        private View refresh() {
            Log.d("MainFragment.redraw()", "Drawing fragment " + getArguments().getInt(ARG_SECTION_NUMBER) + ".");
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ApplicationContextProvider.getContext());
            settings.registerOnSharedPreferenceChangeListener(this);
            String url = settings.getString("ha_url", null)+"/api.php?";
            String username = settings.getString("username", null);
            String password = settings.getString("password", null);
            if(username != null && !username.equals("") && password != null && !password.equals(""))
                url = url + "requireslogin=1&login_username="+username+"&login_password="+password+"&";

            try {
                populater.downloadAndUpdateStatuses(url);
            } catch (IOException ioe){
                ioe.printStackTrace();
            } catch (XmlPullParserException xpe) {
                xpe.printStackTrace();
            }

            return rootView;
        }

        private View redraw() {
            Log.d("MainFragment.redraw()", "Drawing fragment " + getArguments().getInt(ARG_SECTION_NUMBER) + ".");
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ApplicationContextProvider.getContext());
            settings.registerOnSharedPreferenceChangeListener(this);
            String url = settings.getString("ha_url", null)+"/api.php?";
            String username = settings.getString("username", null);
            String password = settings.getString("password", null);
            if(username != null && !username.equals("") && password != null && !password.equals(""))
                url = url + "requireslogin=1&login_username="+username+"&login_password="+password+"&";

            if (populater == null) {
                if(getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                    populater = new PopulateDeviceFragment(rootView);
                } else if(getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                    populater = new PopulateGroupFragment(rootView);
                } else if(getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
                    populater = new PopulateScenarioFragment(rootView);
                } else if(getArguments().getInt(ARG_SECTION_NUMBER) == 4) {
                    populater = new PopulateSensorFragment(rootView);
                }
            }

            try {
                populater.downloadAndCompleteRedraw(url);
            } catch (IOException ioe){
                ioe.printStackTrace();
            } catch (XmlPullParserException xpe) {
                xpe.printStackTrace();
            }

            return rootView;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            if (!hasBeenDrawn) {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                hasBeenDrawn = true;
            }
            return redraw();
        }
    }

}
