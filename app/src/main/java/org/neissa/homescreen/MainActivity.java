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

		android.widget.TextView mTextView = (android.widget.TextView) findViewById(R.id.go_clock);
		mTextView.setOnLongClickListener(new android.view.View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View p1)
				{
					buildList();
					return true;
				}
			});
		
		android.widget.ListView mListView = (android.widget.ListView) findViewById(R.id.list);
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
	public void buildList()
	{
		android.widget.ListView mListView = (android.widget.ListView) findViewById(R.id.list);
		Parcelable state = mListView.onSaveInstanceState();
		
		java.util.List<android.content.pm.ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
		int nb = 0;
		for (int i=0 ; i < apps.size(); i++)
			if (!java.util.Arrays.asList(disabledPackages).contains(apps.get(i).packageName))
				if (getPackageManager().getLaunchIntentForPackage(apps.get(i).packageName) != null)
					nb++;
		appNames = new String[nb];
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
					appMap.put(appName, packageName);
					appNames[n] = appName;
					n++;
				}
		}
		java.util.Arrays.sort(appNames);

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(MainActivity.this, R.layout.app, appNames);
        mListView.setAdapter(adapter);
		mListView.onRestoreInstanceState(state);
	}
}
