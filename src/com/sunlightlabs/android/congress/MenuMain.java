package com.sunlightlabs.android.congress;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.sunlightlabs.android.congress.fragments.UpcomingFragment;
import com.sunlightlabs.android.congress.notifications.NotificationService;
import com.sunlightlabs.android.congress.utils.Analytics;
import com.sunlightlabs.android.congress.utils.FragmentUtils;
import com.sunlightlabs.android.congress.utils.Utils;

public class MenuMain extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu_main);

		Analytics.track(this, "/");
		
		setupControls();
		setupFragments();
		
		if (firstTime()) {
			newVersion(); // don't need to see the changelog on first install
			FragmentUtils.alertDialog(this, AlertFragment.FIRST);
			setNotificationState(); // initially, all notifications are stopped
		} else if (newVersion())
			showChangelog();
	}

	public void setupControls() {
		setupDebugBar();
		
		findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { showAbout(); }
		});
		
		findViewById(R.id.donate).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { goDonate(); }
		});
		
		findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { doFeedback(); }
		});
		
		Utils.setTitle(this, R.string.app_name, false);
		
		Utils.setActionButton(this, R.id.action_2, R.drawable.notifications, new View.OnClickListener() {
			public void onClick(View v) { 
				startActivity(new Intent(MenuMain.this, NotificationTabs.class)); 
			}
		});
		
		Utils.setActionButton(this, R.id.action_1, R.drawable.search, new View.OnClickListener() {
			public void onClick(View v) { 
				onSearchRequested();
			}
		});
	}
	
	private void setupFragments() {
		FragmentManager manager = getSupportFragmentManager();
		if (manager.findFragmentById(R.id.main_navigation) == null)
			manager.beginTransaction().add(R.id.main_navigation, MainMenuFragment.newInstance()).commit();
		if (manager.findFragmentById(R.id.upcoming_list) == null)
			manager.beginTransaction().add(R.id.upcoming_list, UpcomingFragment.newInstance()).commit();
	}
	
	private void setupDebugBar() {
		if (getResources().getString(R.string.debug_show_buttons).equals("true")) {
			findViewById(R.id.debug_bar).setVisibility(View.VISIBLE);
			findViewById(R.id.check).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					WakefulIntentService.sendWakefulWork(MenuMain.this, NotificationService.class);
				}
			});
		}
	}

	// destructive function that triggers the first time flag and lets you know if it did so
	public boolean firstTime() {
		if (Utils.getBooleanPreference(this, "first_time", true)) {
			Utils.setBooleanPreference(this, "first_time", false);
			return true;
		}
		return false;
	}
	
	public boolean newVersion() {
		String lastVersionSeen = getVersionSeen();
		String currentVersion = getResources().getString(R.string.app_version);
		if (lastVersionSeen != null && lastVersionSeen.equals(currentVersion))
			return false;
		else {
			setVersionSeen(currentVersion);
			return true;
		}
	}

	public void setNotificationState() {
		Utils.setBooleanPreference(this, NotificationSettings.KEY_NOTIFY_ENABLED,
				NotificationSettings.DEFAULT_NOTIFY_ENABLED);
	}


	public void setVersionSeen(String version) {
		Utils.setStringPreference(this, "last_version_seen", version);
	}

	public String getVersionSeen() {
		return Utils.getStringPreference(this, "last_version_seen");
	}


	@Override 
	public boolean onCreateOptionsMenu(Menu menu) { 
		super.onCreateOptionsMenu(menu); 
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void goDonate() {
		Analytics.page(this, "/donate", false);
		String packageName = getResources().getString(R.string.app_donation_package_name);
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
		} catch(ActivityNotFoundException e) {
			Utils.alert(this, R.string.no_market_installed);
		}
	}
	
	public void showAbout() {
		Analytics.page(this, "/about", false);
		FragmentUtils.alertDialog(this, AlertFragment.ABOUT);
	}
	
	public void showChangelog() {
		Analytics.page(this, "/changelog", false);
		FragmentUtils.alertDialog(this, AlertFragment.CHANGELOG);
	}
	
	public void doFeedback() {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getResources().getString(R.string.contact_email), null));
		intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.contact_subject));
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) { 
		case R.id.settings:
			startActivity(new Intent(this, Settings.class));
			break;
		case R.id.changelog:
			showChangelog();
			break;
		}
		return true;
	}
	
	public static class MainMenuFragment extends Fragment {
		
		public static MainMenuFragment newInstance() {
			MainMenuFragment frag = new MainMenuFragment();
			frag.setRetainInstance(true);
			return frag;
		}
		
		public MainMenuFragment() {}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.main_navigation_frame, container, false);
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			setupControls();
		}
		
		private void setupControls() {
			menuItem(R.id.menu_legislators, R.string.menu_main_legislators, 16,
					new Intent(getActivity(), MenuLegislators.class));
			
			menuItem(R.id.menu_bills, R.string.menu_main_bills, 16,
					new Intent(getActivity(), MenuBills.class));
			
			menuItem(R.id.menu_votes, R.string.menu_main_votes, 16,
					new Intent(getActivity(), MenuVotes.class));
			
			menuItem(R.id.menu_floor, R.string.menu_main_floor_updates, 16,
					new Intent(getActivity(), FloorUpdatePager.class));
			
			menuItem(R.id.menu_hearings, R.string.menu_main_hearings, 16,
					new Intent(getActivity(), HearingList.class)
						.putExtra("chamber", "senate"));
			
			menuItem(R.id.menu_committees, R.string.menu_main_committees, 16,
					new Intent(getActivity(), CommitteePager.class));
		}
		
		private View menuItem(int id, int text, float size, final Intent intent) {
			ViewGroup item = (ViewGroup) getView().findViewById(id);
			TextView textView = (TextView) item.findViewById(R.id.text);
			textView.setText(text);
			textView.setTextSize(size);
			
			item.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(intent);
				}
			});
			
			return item;
		}
		
	}
}