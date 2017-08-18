package org.neissa.homescreen;

import android.app.*;
import android.os.*;
import android.content.res.*;
import android.view.*;
import android.hardware.*;
import java.util.*;
import android.widget.AdapterView.*;
import android.content.*;
import android.widget.*;
import android.net.*;
import android.provider.*;
import java.lang.reflect.*;
import android.app.usage.*;

public class MainActivity extends Activity 
{
	public static HashMap<String, String> appMap = new HashMap<String, String>();
	public static String[] appNames = new String[0];
	public static String[] disabledPackages = new String[]{
		"com.android.chrome",
		"com.android.contact",
		"com.android.deskclock",
		"com.android.dialer",
		"com.android.mms",
		"com.android.providers.downloads.ui",
		"com.android.stk",
		"com.android_rsap.irmc",
		"com.cyngn.audiofx",
		"com.cyngn.cameranext",
		"com.cyngn.gallerynext",
		"com.cyngn.themestore",
		"com.google.android.androidforwork",
		"com.google.android.apps.chromecast.app",
		"com.google.android.gms",
		"com.google.android.googlequicksearchbox",
		"org.adaway",
		"org.neissa.homescreen",
	};
	public static int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		buildList();

		android.widget.TextView mTextViewClock = (android.widget.TextView) findViewById(R.id.go_clock);
		mTextViewClock.setOnLongClickListener(new android.view.View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View p1)
				{
					buildList();
					return true;
				}
			});
		android.widget.TextView mTextViewPhoto = (android.widget.TextView) findViewById(R.id.go_photo2);
		mTextViewPhoto.setOnLongClickListener(new android.view.View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View p1)
				{
					gophoto2("1");
					return true;
				}
			});
			
		android.widget.GridView mListView = (android.widget.GridView) findViewById(R.id.list);
		mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener(){
				public void onItemClick(android.widget.AdapterView<?> arg0, View v, int position, long id)
				{
					String appName = appNames[position];
					if (appName.length() == 0)
						return;
					String packageName = appMap.get(appName);
					android.content.Intent obj = getPackageManager().getLaunchIntentForPackage(packageName);
					if (obj == null)
						return;
					startActivity(obj);
				}
			});
		mListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener(){
				public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int position, long id)
				{
					String appName = appNames[position];
					if (appName.length() == 0)
						return false;
					currentPosition = position;
					final String packageName = appMap.get(appName);

					View promptView = LayoutInflater.from(view.getContext()).inflate(R.layout.prompt, null);

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
					alertDialogBuilder.setView(promptView);

					((TextView)promptView.findViewById(R.id.go_appsettings)).setText("> "+packageName);
					final EditText userInput = (EditText) promptView.findViewById(R.id.promptValue);
					userInput.setText(appName);
					alertDialogBuilder.setCancelable(false);
					alertDialogBuilder.setPositiveButton("Appliquer", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id)
							{
								String appName = userInput.getText().toString();
								appMap.remove(appNames[currentPosition]);
								appMap.put(appName, packageName);
								appNames[currentPosition] = appName;
								
								dialog.cancel();
								
								SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = sharedPref.edit();
								editor.putString(packageName, appName);
								editor.commit();
								
								buildList();
							}
						});
					alertDialogBuilder.setNegativeButton("Informations", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id)
							{
								dialog.cancel();
								goapp();
							}
						});
					alertDialogBuilder.create().show();
					return true;
				}
			});
    }
	public void go(View v)
	{
		String intent = "";
		switch (v.getId())
		{
			case R.id.go_clock:
				intent = "com.android.deskclock";
				break;
			case R.id.go_phone:
				intent = "com.android.dialer";
				break;
			case R.id.go_google_chrome:
				intent = "com.android.chrome";
				break;
			case R.id.go_photo:
				intent = "com.cyngn.cameranext";
				break;
		}
		if (intent.length() == 0)
			return;
		android.content.Intent obj = getPackageManager().getLaunchIntentForPackage(intent);
		if (obj == null)
			return;
		startActivity(obj);
	}
	public void goapp()
	{
		String packageName = appMap.get(appNames[currentPosition]);
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
		startActivity(intent);
	}
	public void gophoto2(View view)
	{
		gophoto2("0");
	}
	public void gophoto2(String cameraId)
	{
		String packageName = "org.neissa.camera";
		Intent intent = new Intent(packageName+".START");
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(ComponentName.unflattenFromString("org.neissa.camera/org.neissa.camera.MainActivity"));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("CAMERA_ID",cameraId);
		startActivity(intent);
	}
	public void buildList()
	{
		android.widget.GridView mListView = (android.widget.GridView) findViewById(R.id.list);
		Parcelable state = mListView.onSaveInstanceState();

		UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
		Map<String,UsageStats> queryUsageStats = mUsageStatsManager.queryAndAggregateUsageStats(System.currentTimeMillis()-3*24*60*60*1000,System.currentTimeMillis());
		if (queryUsageStats.size() == 0)
		{
			startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
			return;
		}
			
		java.util.List<android.content.pm.ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
		int nb = 0;
		for (int i=0 ; i < apps.size(); i++)
			if (!java.util.Arrays.asList(disabledPackages).contains(apps.get(i).packageName))
				if (getPackageManager().getLaunchIntentForPackage(apps.get(i).packageName) != null)
					nb++;
		String[] appNamesTmp = new String[nb];
		int n = 0;
		for (int i=0 ; i < apps.size(); i++)
		{
			android.content.pm.ApplicationInfo app = apps.get(i);
			String packageName = app.packageName;
			if (!java.util.Arrays.asList(disabledPackages).contains(packageName))
				if (getPackageManager().getLaunchIntentForPackage(packageName) != null)
				{
					SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
					String appName = sharedPref.getString(packageName, "");
					if (appName.length() == 0)
						appName = (packageName + (app.className != null ?'~' + app.className.replace(packageName, ""): "")).replaceFirst("\\A[a-z]*\\.", "").replace("google.", "").replaceFirst("\\Aandroid\\.", "google.").replace(".apps.", ".").replace(".app.", ".").replace(".engine.", ".").replace(".framework.", ".").replace(".common.", ".").replace(".application.", ".").replaceFirst("Class$", "").replaceFirst("Impl$", "").replaceFirst("App$", "").replaceFirst("Application$", "").replaceFirst("~\\.", "~").replaceFirst("~$", "");
					if(queryUsageStats.containsKey(packageName))
						appName = appName + " *" + ((queryUsageStats.get(packageName).getTotalTimeInForeground()/1000)/60) + "min";
					appMap.put(appName, packageName);
					appNamesTmp[n] = appName;
					n++;
				}
		}
		java.util.Arrays.sort(appNamesTmp);
		
		appNames = new String[nb];
		int nbc = 3;
		int z = (int)Math.ceil((double)nb/(double)nbc);
		int[] maxc = new int[nbc];
		for(int i=0; i<nbc; i++)
			maxc[i] = z;
		if(nb%nbc != 0)
			for(int i=nb%nbc; i<nbc; i++)
				maxc[i]--;
		int[] maxi = new int[nbc];
		for(int i=0; i<nbc; i++)
			maxi[i] = maxc[i];
		for(int i=0; i<nbc-1; i++)
			maxi[i+1] += maxi[i];
		for(int i=0; i<nb; i++)
			appNames[i] = appNamesTmp[0];
		int k=0;
		int j=0;
		for(int i=0; i<nb; i++)
		{
			if(i>=maxi[k])
			{
				k++;
				j=k;
			}
			j = Math.max(0,Math.min(nb-1,j));
			appNames[j] = appNamesTmp[i];
			j += 3;
		}
		
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(MainActivity.this, R.layout.app, appNames);
        mListView.setAdapter(adapter);
		mListView.onRestoreInstanceState(state);
	}
}
