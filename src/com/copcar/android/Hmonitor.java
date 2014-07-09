package com.copcar.android;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Hmonitor extends CordovaPlugin {
	public static final String ACTION_CHECK_BT_NUUBO = "checkBt";
	public static final String ACTION_CHECK_CONNECTION_NUUBO = "checkConnection";

	public static final String LOG_TAG = "hmonitor";
	
	private static final String CTE_TYPE = "type";
	private static final String CTE_STATUS = "status";
	private static final String CTE_TYPE_INIT = "init";
	//private static final String CTE_TYPE_CONNECTION = "connection";
	private static final String CTE_TYPE_BT = "bt";
	private CallbackContext connectionCallbackContext;

	// private Context context;

	// private ConnectivityManager sockMan;
	// private LocationManager locationManager;
	private BroadcastReceiver connectionReceiver;
	private BroadcastReceiver btReceiver;
	
	private boolean connectionRegistered = false;
	private boolean btRegistered = false;
	
	private ConnectivityManager sockMan;

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);

		// context = webView.getContext();

		this.connectionCallbackContext = null;

		this.sockMan = (ConnectivityManager) cordova.getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Log.i(LOG_TAG, "adding connectionrec");
		addConnectionReceiver();
		Log.i(LOG_TAG, "adding b");
		addBTReceiver();
	}

	private void addBTReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		if (this.btReceiver == null) {
			this.btReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					final String action = intent.getAction();
					Log.i(LOG_TAG, "BT Action received: " + action);
					if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
						final int state = intent.getIntExtra(
								BluetoothAdapter.EXTRA_STATE,
								BluetoothAdapter.ERROR);
						switch (state) {
						case BluetoothAdapter.STATE_OFF:
							Log.i(LOG_TAG, "BT status OFF");
							sendPluginResult(PluginResult.Status.OK,
									generateObject(CTE_TYPE_BT, Boolean.FALSE));
							break;
						case BluetoothAdapter.STATE_TURNING_OFF:
							Log.i(LOG_TAG, "BT status turning OFF");
							sendPluginResult(PluginResult.Status.OK,
									generateObject(CTE_TYPE_BT, Boolean.FALSE));
							break;
						case BluetoothAdapter.STATE_ON:
							Log.i(LOG_TAG, "BT status ON");
							sendPluginResult(PluginResult.Status.OK,
									generateObject(CTE_TYPE_BT, Boolean.TRUE));
							break;
						case BluetoothAdapter.STATE_TURNING_ON:
							Log.i(LOG_TAG, "BT status turning ON");
							sendPluginResult(PluginResult.Status.OK,
									generateObject(CTE_TYPE_BT, Boolean.FALSE));
							break;
						}
					}
				}
			};
			cordova.getActivity().registerReceiver(this.btReceiver,
					intentFilter);
			this.btRegistered = true;
		}
	}


	private void addConnectionReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		if (this.connectionReceiver == null) {
			this.connectionReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					if (isNetworkAvailable()) {
						Log.i(LOG_TAG, "CONNECTED");
						// sendPluginResult(PluginResult.Status.OK,generateObject(CTE_TYPE_CONNECTION,Boolean.TRUE));

					} else {
						Log.i(LOG_TAG, "NOT CONNECTED");
						// sendPluginResult(PluginResult.Status.OK,generateObject(CTE_TYPE_CONNECTION,Boolean.FALSE));

					}
				}
			};
			cordova.getActivity().registerReceiver(this.connectionReceiver,
					intentFilter);
			this.connectionRegistered = true;
		}
	}

	/**
	 * Stop network connectionReceiver.
	 **/
	public void onDestroy() {
		if (this.connectionReceiver != null && this.connectionRegistered) {
			try {
				this.cordova.getActivity().unregisterReceiver(
						this.connectionReceiver);
				this.connectionRegistered = false;
			} catch (Exception e) {
				Log.e(LOG_TAG,
						"Error unregistering network connectionReceiver: "
								+ e.getMessage(), e);
			}
		}
		if (this.btReceiver != null && this.btRegistered) {
			try {
				this.cordova.getActivity().unregisterReceiver(this.btReceiver);
				this.btRegistered = false;
			} catch (Exception e) {
				Log.e(LOG_TAG,
						"Error unregistering btReceiver: " + e.getMessage(), e);
			}
		}
		
	}

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		boolean success = Boolean.FALSE;
		connectionCallbackContext = callbackContext;

		try {

			if (ACTION_CHECK_BT_NUUBO.equals(action)) {

				Log.i(LOG_TAG, "Check BT method fired");
				sendPluginResult(PluginResult.Status.OK,
						generateObject(CTE_TYPE_INIT, null));
				success = true;
			} else {
				if (ACTION_CHECK_CONNECTION_NUUBO.equals(action)) {
					Log.i(LOG_TAG, "Check Connection method fired");
					sendPluginResult(PluginResult.Status.OK,
							generateObject(CTE_TYPE_INIT, null));
				}

			}

		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception: " + e.getMessage());

			connectionCallbackContext.error(e.getMessage());

		}
		return success;
	}

	private JSONObject generateObject(String cteTypeInit, Boolean status) {
		JSONObject object = new JSONObject();
		try {
			object.put(CTE_TYPE, cteTypeInit);

			if (status != null) {
				object.put(CTE_STATUS, status);
			}
		} catch (JSONException e) {

			Log.e(LOG_TAG, "JSONException: " + e.getMessage());
		}
		return object;
	}

	private void sendPluginResult(Status status, JSONObject obj) {
		PluginResult pluginResult = new PluginResult(status, obj);
		pluginResult.setKeepCallback(true);
		connectionCallbackContext.sendPluginResult(pluginResult);
	}

	private boolean isNetworkAvailable() {
		NetworkInfo activeNetworkInfo = sockMan.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}