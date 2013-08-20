package org.dolphinemu.dolphinemu;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2013 Dolphin Emulator Project
 * Licensed under GPLv2
 * Refer to the license.txt file included.
 */
public final class GameListActivity extends Activity
		implements GameListFragment.OnGameListZeroListener
{
	private int mCurFragmentNum = 0;
	private Fragment mCurFragment;

	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	private SideMenuAdapter mDrawerAdapter;
	private ListView mDrawerList;

	private static GameListActivity mMe;

	// Called from the game list fragment
	public void onZeroFiles()
	{
		mDrawerLayout.openDrawer(mDrawerList);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamelist_activity);
		mMe = this;

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		List<SideMenuItem> dir = new ArrayList<SideMenuItem>();
		dir.add(new SideMenuItem(getString(R.string.game_list), 0));
		dir.add(new SideMenuItem(getString(R.string.browse_folder), 1));
		dir.add(new SideMenuItem(getString(R.string.settings), 2));
		dir.add(new SideMenuItem(getString(R.string.gamepad_config), 3));
		dir.add(new SideMenuItem(getString(R.string.about), 4));

		mDrawerAdapter = new SideMenuAdapter(this, R.layout.sidemenu, dir);
		mDrawerList.setAdapter(mDrawerAdapter);
		mDrawerList.setOnItemClickListener(mMenuItemClickListener);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		recreateFragment();
	}

	private void recreateFragment()
	{
		mCurFragment = new GameListFragment();
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, mCurFragment).commit();
	}
	
	public void SwitchPage(int toPage)
	{
		if (mCurFragmentNum == toPage)
			return;
		
		switch (mCurFragmentNum)
		{
			// Folder browser
			case 1:
				recreateFragment();
				break;
				
			// Settings
			case 2:
			{
			    // Saves the settings that the user has set in the settings menu to the Dolphin ini files.
			    // This is done so that changes can be reflected when the emulator is run next.
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

				NativeLibrary.SetConfig("Dolphin.ini", "Core", "CPUCore", prefs.getString("cpuCorePref", ""));
				NativeLibrary.SetConfig("Dolphin.ini", "Core", "CPUThread", prefs.getBoolean("dualCorePref", true) ? "True" : "False");
				NativeLibrary.SetConfig("Dolphin.ini", "Core", "GFXBackend", prefs.getString("gpuPref", ""));
			}
			break;
			
			case 3: // Gamepad settings
			{
				InputConfigAdapter adapter = ((InputConfigFragment)mCurFragment).getAdapter();
				for (int a = 0; a < adapter.getCount(); ++a)
				{
					InputConfigItem o = adapter.getItem(a);
					String config = o.getConfig();
					String bind = o.getBind();
					String ConfigValues[] = config.split("-");
					String Key = ConfigValues[0];
					String Value = ConfigValues[1];
					NativeLibrary.SetConfig("Dolphin.ini", Key, Value, bind);
				}
			}
			break;
			
			case 0: // Game List
			case 4: // About
	        /* Do Nothing */
				break;
		}
		
		switch(toPage)
		{
			case 0:
			{
				mCurFragmentNum = 0;
				mCurFragment = new GameListFragment();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, mCurFragment).commit();
			}
			break;
			
			case 1:
			{
				Toast.makeText(mMe, getString(R.string.loading_browser), Toast.LENGTH_SHORT).show();
				mCurFragmentNum = 1;
				mCurFragment = new FolderBrowser();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, mCurFragment).commit();
			}
			break;
			
			case 2:
			{
				Toast.makeText(mMe, getString(R.string.loading_settings), Toast.LENGTH_SHORT).show();
				mCurFragmentNum = 2;
				mCurFragment = new PrefsFragment();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, mCurFragment).commit();
			}
			break;
			
			case 3:
			{
				Toast.makeText(mMe, getString(R.string.loading_gamepad), Toast.LENGTH_SHORT).show();
				mCurFragmentNum = 3;
				mCurFragment = new InputConfigFragment();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, mCurFragment).commit();
			}
			break;
			
			case 4:
			{
				Toast.makeText(mMe, getString(R.string.about), Toast.LENGTH_SHORT).show();
				mCurFragmentNum = 4;
				mCurFragment = new AboutFragment();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, mCurFragment).commit();
			}
			break;
			
			default:
				break;
		}
	}
	
	private AdapterView.OnItemClickListener mMenuItemClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			SideMenuItem o = mDrawerAdapter.getItem(position);
			mDrawerLayout.closeDrawer(mDrawerList);
			SwitchPage(o.getID());
		}
	};
	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggle
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void onBackPressed()
	{
		SwitchPage(0);
	}

	public interface OnGameConfigListener
	{
		public boolean onMotionEvent(MotionEvent event);
		public boolean onKeyEvent(KeyEvent event);
	}
	
	// Gets move(triggers, joystick) events
	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent event)
	{
		if (mCurFragmentNum == 3)
		{
			if (((OnGameConfigListener)mCurFragment).onMotionEvent(event))
				return true;
		}
		
		return super.dispatchGenericMotionEvent(event);
	}
	
	// Gets button presses
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (mCurFragmentNum == 3)
		{
			if (((OnGameConfigListener)mCurFragment).onKeyEvent(event))
				return true;
		}
		
		return super.dispatchKeyEvent(event);
	}

}
