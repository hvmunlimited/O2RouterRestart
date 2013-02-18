package com.jackculhane.RouterRestart;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.net.wifi.*;

public class MainActivity extends Activity {

	final Context context = this;
	private Button restartButton;
	private Button passButton;
	private TextView StatusText;
	
	private Boolean Restarting = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		StatusText = (TextView) findViewById(R.id.textView2);
		restartButton = (Button) findViewById(R.id.button1);
		passButton = (Button) findViewById(R.id.button2);
		
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		final String wifiStatus = info.getSSID();
		
		if (wifiStatus != null)
		{
			StatusText.setText(wifiStatus == null ? "Not Connected" : wifiStatus);
			restartButton.setEnabled(true);
			passButton.setEnabled(true);
		}
		
		restartButton.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v)
			{
				SharedPreferences Prefs = getPreferences(Context.MODE_PRIVATE);
				String WifiPass = Prefs.getString(wifiStatus, null);
				
				if (WifiPass == null)
				{
					Toast.makeText(context, "Please set password first", Toast.LENGTH_SHORT).show();
				}
				else
				{
					if (Restarting)
					{
						Toast.makeText(context, "Already attempting restart!", Toast.LENGTH_LONG).show();
						return;
					}
					
					// Do restart
					Toast.makeText(context, "Restarting Router", Toast.LENGTH_LONG).show();
					Restarting = true;
					new DoRestartTask().execute(WifiPass);
				}
			}
		});
		
		passButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				alert.setTitle("Password");
				alert.setMessage("Enter router serial number");
				
				final EditText input = new EditText(context);
				alert.setView(input);

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Editable value = input.getText();
						SharedPreferences Prefs = getPreferences(Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = Prefs.edit();
						editor.putString(wifiStatus, value.toString());
						editor.commit();
					}
				});

				alert.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	
	private class DoRestartTask extends AsyncTask<String, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(String... args) {
			return Restart.DoReset(args[0]);
		}
		
		
		@Override
		protected void onPostExecute(Boolean result)
		{
			Restarting = false;
			
			if (result)
			{
				Toast.makeText(context, "Router Restarted Successfully", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(context, "Unable to restart router :(", Toast.LENGTH_LONG).show();
			}
		}
		
	}
}
