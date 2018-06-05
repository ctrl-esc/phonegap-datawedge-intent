package org.limitstate.intent;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

public class BroadcastIntentPlugin extends CordovaPlugin {
	
	protected void onCreate(Context context, CallbackContext callbackContext) {
		IntentFilter filter = new IntentFilter();
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		filter.addAction("org.limitstate.datawedge.ACTION");
		context.registerReceiver(myBroadcastReceiver, filter);
	}
	
	private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
		    String action = intent.getAction();
		    Bundle b = intent.getExtras();
		    //  This is useful for debugging to verify the format of received intents from DataWedge
		    //for (String key : b.keySet())
		    //{
		    //    Log.v(LOG_TAG, key);
		    //}
		    if (action.equals("org.limitstate.datawedge.ACTION")) {
			//  Received a barcode scan
			try {
			    sendScanResult(intent, "via Broadcast");
			} catch (Exception e) {
			    //  Catch if the UI does not exist when we receive the broadcast... this is not designed to be a production app
			}
		    }
		}
	};
	
	private void sendScanResult(Intent initiatingIntent, String howDataReceived)
	{
		String decodedSource = initiatingIntent.getStringExtra("com.symbol.datawedge.source");
		String decodedData = initiatingIntent.getStringExtra("com.symbol.datawedge.data_string");
		String decodedLabelType = initiatingIntent.getStringExtra("com.symbol.datawedge.label_type");

		if (null == decodedSource)
		{
		    decodedSource = initiatingIntent.getStringExtra("com.motorolasolutions.emdk.datawedge.source");
		    decodedData = initiatingIntent.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string");
		    decodedLabelType = initiatingIntent.getStringExtra("com.motorolasolutions.emdk.datawedge.label_type");
		}

		//lblScanSource.setText(decodedSource + " " + howDataReceived);
		//lblScanData.setText(decodedData);
		//lblScanLabelType.setText(decodedLabelType);
		
		JSONObject obj = new JSONObject();
		try{
			obj.put("barcode", decodedData);
			obj.put("codeType", decodedLabelType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sendUpdate(obj);
	}
	

	private void sendUpdate(JSONObject info) {
	   
	}

}
