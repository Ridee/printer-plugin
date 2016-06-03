package com.jinn.plugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Set;
import java.util.*;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.content.Intent;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.zj.btsdk.BluetoothService;
import com.zj.btsdk.PrintPic;

import android.util.Log;


public class PrinterPlugin extends CordovaPlugin {
  private BluetoothService mService = null;
  private BluetoothService mServiceSafe = null;   
  private CallbackContext temp_callbackContext = null;
  private Context context = null;
  private static final int REQUEST_ENABLE_BT = 2;

  public boolean execute(String action, JSONArray jsonArgs, CallbackContext callbackContext) throws JSONException {
    temp_callbackContext = callbackContext;
    if(mService == null){
      context = this.cordova.getActivity().getApplicationContext();
      mService = new BluetoothService(context, mHandler);
      mServiceSafe = mService;
    }
    try{
      System.out.println("HELLO!");
      if (action.equals("printMessage")) {
        JSONObject args = jsonArgs.getJSONObject(0);
        JSONArray a = args.getJSONArray("message");
        byte [] messageBytes = new byte[a.length()];
        if (a != null) {
          int len = a.length();
          for (int i=0;i<len;i++){
            messageBytes[i]=(byte)(Integer.parseInt(a.get(i).toString()));
          }
        }
        mService.write(messageBytes);

        PluginResult.Status status = PluginResult.Status.NO_RESULT;
        PluginResult pluginResult = new PluginResult(status);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);

      }else if(action.equals("scan")){
        mService.startDiscovery();
        Set<BluetoothDevice> pairedDevices = mService.getPairedDev();
        if (pairedDevices.size() > 0) {
          JSONArray devices= new JSONArray();
          devices.put(new JSONObject().put("Type", "Scan"));
          for (BluetoothDevice device : pairedDevices) {
            JSONObject obj = new JSONObject();
            obj.put("name", device.getName());
            obj.put("address", device.getAddress());
            devices.put(obj);
          }
          callbackContext.success(devices);
        } else {
          String noDevices = "No devices have been paired";
          //mPairedDevicesArrayAdapter.add(noDevices);
          callbackContext.error("no paired devices");
        }
        mService.cancelDiscovery();

      }else if(action.equals("connect")) {
        JSONObject args = jsonArgs.getJSONObject(0);
        String macAddress = args.getString("address");
        PluginResult.Status status = PluginResult.Status.NO_RESULT;
        PluginResult pluginResult = new PluginResult(status);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
        mService.connect(mService.getDevByMac(macAddress));

      }else if (action.equals("isBTOpen")) {
        if( mService.isBTopen() == false) {
          Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          this.cordova.startActivityForResult((CordovaPlugin) this, enableIntent, REQUEST_ENABLE_BT);
          PluginResult.Status status = PluginResult.Status.NO_RESULT;
          PluginResult pluginResult = new PluginResult(status);
          pluginResult.setKeepCallback(true);
          callbackContext.sendPluginResult(pluginResult);
        }else{
          JSONArray response= new JSONArray();
          response.put(new JSONObject().put("Type", "BluetoothOpen"));
          PluginResult result = new PluginResult(PluginResult.Status.OK, response);
          result.setKeepCallback(false);
          callbackContext.sendPluginResult(result);
        }
      }else if (action.equals("printImg")) {
        JSONObject args = jsonArgs.getJSONObject(0);
        String src = args.getString("src");
        int x = Integer.parseInt(args.getString("x"));
        byte[] sendData = null;
        PrintPic pg = new PrintPic();
        pg.initCanvas(684);
        pg.initPaint();
        pg.drawImageFromAssets(context, x, 0, src);
        sendData = pg.printDraw();

        mService.write(sendData);
        mService.write(new byte[]{(byte)13, (byte)10});
        callbackContext.success();

      }else if (action.equals("stopBT")) {
        mService.stop();
        callbackContext.success();
      }else if (action.equals("disconnect")) {
        BluetoothAdapter blueAdap = BluetoothAdapter.getDefaultAdapter();
        try{
          if(!blueAdap.disable()) {
            if(!blueAdap.isEnabled()) {
              Log.d("printer", "Bluetooth is already off");
              throw new Exception("Bluetooth is already off.");
            } else {
              Log.d("printer", "Error disabling Bluetooth.");
              throw new Exception("Error disabling Bluetooth.");
            }
          }
        }
        catch(Exception e) {
          throw e;
        }
      }else if (action.equals("forceEnable")) {
        mService = mServiceSafe;
        // BluetoothAdapter blueAdap = BluetoothAdapter.getDefaultAdapter();
        // try {
        //   if(!blueAdap.enable()) {
        //     if(blueAdap.isEnabled()) {
        //       throw new Exception("Bluetooth is already on.");
        //     }else {
        //       throw new Exception("Error enabling Bluetooth.");
        //     }
        //   }
        // } catch(Exception e) {
        //   throw e;
        // }
      }
      return true;
    }catch (Exception e){
      callbackContext.error(e.getMessage());
      return false;
    }
  }

  private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            PluginResult result;
            JSONArray response;

            switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                  Toast.makeText(context, "Connect successful",
                            Toast.LENGTH_SHORT).show();
                  response = new JSONArray();
                  try {
                  response.put(new JSONObject().put("Type", "Connected"));
                  }catch(Exception e){
                    Log.d("printer", "error");
                  }
                  result = new PluginResult(PluginResult.Status.OK,
                        response);
                  result.setKeepCallback(true);
                  temp_callbackContext.sendPluginResult(result);
                  break;
                case BluetoothService.STATE_CONNECTING:
                  response = new JSONArray();
                  try {
                  response.put(new JSONObject().put("Type", "Connecting"));
                  }catch(Exception e){
                    Log.d("printer", "error");
                  }
                  result = new PluginResult(PluginResult.Status.OK,
                        response);
                  result.setKeepCallback(true);
                  temp_callbackContext.sendPluginResult(result);
                  break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                /*result = new PluginResult(PluginResult.Status.OK,
                        "none");
                  result.setKeepCallback(true);
                  temp_callbackContext.sendPluginResult(result);*/
                  break;
                }
              break;
            case BluetoothService.MESSAGE_CONNECTION_LOST:
              Toast.makeText(context, "Device connection was lost",
                               Toast.LENGTH_SHORT).show();
              response = new JSONArray();
              try {
                response.put(new JSONObject().put("Type", "ConnectionLost"));
              }catch(Exception e){
                Log.d("printer", "error");
              }
              result = new PluginResult(PluginResult.Status.ERROR,
                        response);
              result.setKeepCallback(false);
              temp_callbackContext.sendPluginResult(result);
              break;
            case BluetoothService.MESSAGE_UNABLE_CONNECT:
              Toast.makeText(context, "Unable to connect device",
                        Toast.LENGTH_SHORT).show();
              response = new JSONArray();
              try {
                response.put(new JSONObject().put("Type", "UnableToConnect"));
              }catch(Exception e){
                Log.d("printer", "error");
              }
              result = new PluginResult(PluginResult.Status.ERROR,
                        response);
              result.setKeepCallback(false);
              temp_callbackContext.sendPluginResult(result);
              break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      PluginResult result;
      JSONArray response;
      switch (requestCode) {
      case REQUEST_ENABLE_BT:
        if (resultCode == Activity.RESULT_OK) {
          Toast.makeText(context, "Bluetooth open successful", Toast.LENGTH_LONG).show();
          response = new JSONArray();
          try {
            response.put(new JSONObject().put("Type", "BluetoothOpen"));
          }catch(Exception e){
            Log.d("printer", "error");
          }
          result = new PluginResult(PluginResult.Status.OK,
                    response);
          result.setKeepCallback(false);
          temp_callbackContext.sendPluginResult(result);
        } else {
          Toast.makeText(context, "Bluetooth Disconnected", Toast.LENGTH_LONG).show();
          response = new JSONArray();
          try {
            response.put(new JSONObject().put("Type", "BluetoothClose"));
          }catch(Exception e){
            Log.d("printer", "error");
          }
          result = new PluginResult(PluginResult.Status.ERROR,response);
          result.setKeepCallback(false);
          temp_callbackContext.sendPluginResult(result);
        }
        break;
      }
  }
}
