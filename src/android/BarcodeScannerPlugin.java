package org.limitstate.honeywell;

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

import com.honeywell.decodemanager.DecodeManager;
import com.honeywell.decodemanager.SymbologyConfigs;
import com.honeywell.decodemanager.barcode.DecodeResult;
import com.honeywell.decodemanager.barcode.CommonDefine;

public class BarcodeScannerPlugin extends CordovaPlugin {

	private static final String LOG_TAG = "BarcodeScannerPlugin";
	private static final int SCANTIMEOUT = 2000;

	DecodeManager decodeManager = null;
	BroadcastReceiver scannerReceiver = null;
	CallbackContext pluginCallbackContext = null;

	public BarcodeScannerPlugin() {
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("scan")) {
			this.pluginCallbackContext = callbackContext;

			if ((decodeManager == null) && (Build.MODEL.toLowerCase().contains("dolphin 70e".toLowerCase()))) {
				decodeManager = new DecodeManager(((CordovaActivity)this.cordova.getActivity()), ScanResultHandler);
			}
			try{
				// All symbologies enabled
				decodeManager.enableSymbology(CommonDefine.SymbologyID.SYM_ALL);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			try {
				this.doScan();
				return true;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (action.equals("stop")) {
			callbackContext.success("stopped");
			return true;
		}
		return false;
	}

	private Handler ScanResultHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DecodeManager.MESSAGE_DECODER_COMPLETE:
				DecodeResult decodeResult = (DecodeResult) msg.obj;
				JSONObject obj = new JSONObject();
				try{
					obj.put("barcode", decodeResult.barcodeData);
					obj.put("codeID", decodeResult.codeId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				sendUpdate(obj, false);
				pluginCallbackContext.success("done");
				break;
			case DecodeManager.MESSAGE_DECODER_FAIL: 
				break;
			case DecodeManager.MESSAGE_DECODER_READY: 
				ArrayList<java.lang.Integer> arry = decodeManager.getSymConfigActivityOpeartor().getAllSymbologyId();
				boolean b = arry.isEmpty();
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	private void doScan() throws Exception {
		try {
			decodeManager.doDecode(SCANTIMEOUT);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void sendUpdate(JSONObject info, boolean keepCallback) {
	    if (this.pluginCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, info);
			result.setKeepCallback(keepCallback);
			this.pluginCallbackContext.sendPluginResult(result);
		}
	}

}
