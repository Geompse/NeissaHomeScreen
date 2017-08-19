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

public class MainActivity extends Activity 
{
	public static int COlUMNS = 4;
	public static HashMap<Integer,Integer> groupNameIndexesToAppNameOriginalIndexes = new HashMap<Integer,Integer>();
	public static HashMap<Integer,Integer> appNameSortedIndexesToGroupNameIndexes = new HashMap<Integer,Integer>();
	public static HashMap<String, String> packageNamesToAppNames = new HashMap<String, String>();
	public static HashMap<String, String> appNamesToPackageNames = new HashMap<String, String>();
	public static String[] appNamesOriginal = new String[0];
	public static String[] appNamesSorted = new String[0];
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
	public static Integer currentAppNameOriginalIndex = null;

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
		android.widget.TextView mTextViewPhone = (android.widget.TextView) findViewById(R.id.go_phone);
		mTextViewPhone.setOnLongClickListener(new android.view.View.OnLongClickListener(){
				@Override
				public boolean onLongClick(View p1)
				{
					android.content.Intent obj = getPackageManager().getLaunchIntentForPackage("com.android.mms");
					if (obj == null)
						return false;
					startActivity(obj);
					return true;
				}
			});
		/*android.widget.TextView mTextViewPhoto = (android.widget.TextView) findViewById(R.id.go_photo2);
		 mTextViewPhoto.setOnLongClickListener(new android.view.View.OnLongClickListener(){
		 @Override
		 public boolean onLongClick(View p1)
		 {
		 gophoto2("1");
		 return true;
		 }
		 });*/

		android.widget.GridView mListView = (android.widget.GridView) findViewById(R.id.list);
		mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener(){
				public void onItemClick(android.widget.AdapterView<?> arg0, View v, int appNameSortedIndex, long id)
				{
					String packageName = getPackageNameFromAppNameSortedIndex(appNameSortedIndex);
					android.content.Intent obj = getPackageManager().getLaunchIntentForPackage(packageName);
					if (obj == null)
						return;
					startActivity(obj);
				}
			});
		mListView.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener(){
				public boolean onItemLongClick(android.widget.AdapterView<?> parent, View view, int appNameSortedIndex, long id)
				{
					final String packageName = getPackageNameFromAppNameSortedIndex(appNameSortedIndex);
					if(packageName == null)
						return false;

					View promptView = LayoutInflater.from(view.getContext()).inflate(R.layout.prompt, null);

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
					alertDialogBuilder.setView(promptView);

					((TextView)promptView.findViewById(R.id.go_appsettings)).setText("> " + packageName);
					final EditText userInput = (EditText) promptView.findViewById(R.id.promptValue);
					userInput.setText(packageNamesToAppNames.get(packageName));
					alertDialogBuilder.setCancelable(false);
					alertDialogBuilder.setPositiveButton("Appliquer", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id)
							{
								String appName = userInput.getText().toString();
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
		String packageName = appNamesToPackageNames.get(appNamesOriginal[currentAppNameOriginalIndex]);
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
		Intent intent = new Intent(packageName + ".START");
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(ComponentName.unflattenFromString("org.neissa.camera/org.neissa.camera.MainActivity"));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("CAMERA_ID", cameraId);
		startActivity(intent);
	}
	public void buildList()
	{
		int nbc = Math.max(1,COlUMNS);
		android.widget.GridView mGridView = (android.widget.GridView) findViewById(R.id.list);
		mGridView.setNumColumns(nbc);
		Parcelable state = mGridView.onSaveInstanceState();

		java.util.List<android.content.pm.ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);
		int nb = 0;
		for (int i=0 ; i < apps.size(); i++)
			if (!java.util.Arrays.asList(disabledPackages).contains(apps.get(i).packageName))
				if (getPackageManager().getLaunchIntentForPackage(apps.get(i).packageName) != null)
					nb++;
		packageNamesToAppNames.clear();
		appNamesToPackageNames.clear();
		appNamesOriginal = new String[nb];
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
					packageNamesToAppNames.put(packageName, appName);
					appNamesToPackageNames.put(appName, packageName);
					appNamesOriginal[n] = appName;
					n++;
				}
		}
		java.util.Arrays.sort(appNamesOriginal);

		List<String> groupNames = new ArrayList<String>();
		String last = "";
		for (int i=0; i < nb; i++)
		{
			String cur = appNamesOriginal[i].substring(0, 2);
			if (!cur.equals(last))
			{
				if (groupNames.size() > 0)
					groupNames.add("");
				groupNames.add(cur);
				last = cur;
			}
			groupNameIndexesToAppNameOriginalIndexes.put(groupNames.size(), i);
			groupNames.add(appNamesOriginal[i].substring(2));
		}
		while(groupNames.size()%nbc != 0)
			groupNames.add("");
		nb = groupNames.size();

		appNamesSorted = new String[nb];
		int[] maxi = new int[nbc];
		for (int c=0; c < nbc; c++)
			maxi[c] = (nb/nbc)*(c+1);
		int c=0;
		int j=0;
		for (int i=0; i < nb; i++)
			appNamesSorted[i] = "---";
		for (int i=0; i < nb; i++)
		{
			if (i >= maxi[c])
			{
				c++;
				j = c;
			}
			j = Math.max(0, Math.min(nb - 1, j));
			appNamesSorted[j] = groupNames.get(i);
			appNameSortedIndexesToGroupNameIndexes.put(j, i);
			j += nbc;
		}

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(MainActivity.this, R.layout.app, appNamesSorted);
        mGridView.setAdapter(adapter);
		mGridView.onRestoreInstanceState(state);
	}
	public String getPackageNameFromAppNameSortedIndex(Integer appNameSortedIndex)
	{
		Integer groupNameIndex = appNameSortedIndexesToGroupNameIndexes.get(appNameSortedIndex);
		if (groupNameIndex == null)
			return null;
		currentAppNameOriginalIndex = groupNameIndexesToAppNameOriginalIndexes.get(groupNameIndex);
		if (currentAppNameOriginalIndex == null)
			return null;
		String appName = appNamesOriginal[currentAppNameOriginalIndex];
		if (appName.length() == 0)
			return null;
		return appNamesToPackageNames.get(appName);
	}
}
