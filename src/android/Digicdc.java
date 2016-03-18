/*
 USB CDC Communication
*/

package com.digistump.digicdc;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.hardware.usb.UsbManager;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.util.Log;
import android.content.Context;
import android.app.PendingIntent;
import android.provider.Settings;

import jp.ksksue.driver.serial.FTDriver;



public class Digicdc extends CordovaPlugin {


  private static final String ACTION_USB_PERMISSION ="com.digistump.digicdc.USB_PERMISSION";
  private static final String ACTION_PERM  ="register";
  private static final String ACTION_CONNECT ="connect";
  private static final String ACTION_WRITE ="write";
  private static final String ACTION_READ ="read";


    FTDriver mSerial;

    public boolean execute(
      String action,
      JSONArray args,
      final CallbackContext callbackContext
      )
      throws JSONException {
        try {

          //
          // get permission
          //
          if (ACTION_PERM.equals(action)) {

            String permission = "android.permission.USB_PERMISSION";
            int res = cordova.getActivity().checkCallingOrSelfPermission(permission);

            if (res != PackageManager.PERMISSION_GRANTED){

                    mSerial = new FTDriver((UsbManager) cordova.getActivity().getSystemService(Context.USB_SERVICE));
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, new Intent(
                          ACTION_USB_PERMISSION), 0);
                    mSerial.setPermissionIntent(permissionIntent);
                    if (!mSerial.begin(FTDriver.BAUD9600)) {
                      callbackContext.error("Fail");
                      return false;
                    }
                    else {
                      callbackContext.success("GOT PERM");
                      return true;
                    }
           }
           callbackContext.success("HAVE PERM");
            return true;
          }


          //
          // connect
          //
          else if (ACTION_CONNECT.equals(action)) {

              String permission = "android.permission.USB_PERMISSION";
              int res = cordova.getActivity().checkCallingOrSelfPermission(permission);
              if (res != PackageManager.PERMISSION_GRANTED){

                      mSerial = new FTDriver((UsbManager) cordova.getActivity().getSystemService(Context.USB_SERVICE));
                      PendingIntent permissionIntent = PendingIntent.getBroadcast(cordova.getActivity(), 0, new Intent(
                            ACTION_USB_PERMISSION), 0);
                      mSerial.setPermissionIntent(permissionIntent);
                      if (!mSerial.begin(FTDriver.BAUD9600)) {
                        callbackContext.error("Fail");
                        return false;
                      }
                      else{
                        callbackContext.success("CONNECT OF");
                        return true;
                      }
             }
             if (!mSerial.begin(FTDriver.BAUD9600)) {
              callbackContext.error("Fail");
              return false;
            }
            else{
              callbackContext.success("GOT PERM");
              return true;
            }
          }


          //
          // write
          //
          else if (ACTION_WRITE.equals(action)) {

            JSONObject arg_object = args.getJSONObject(0);
            final String strWrite = arg_object.getString("text");

            cordova.getThreadPool().execute(new Runnable() {
              public void run() {
                  mSerial.write(strWrite.getBytes(), strWrite.length());
                  callbackContext.success("OK");
              }
            });

            return true;
          }


          //
          // read
          //
          else if(ACTION_READ.equals(action)){

            cordova.getThreadPool().execute(new Runnable() {

              public void run() {
                  String mText;
                  byte[] rbuf = new byte[24];
                  int len;
                  int i;
                  len = mSerial.read(rbuf);
                  mText = "";
                  if (len > 0) {
                    for(i=0;i<len;++i) {
                      mText = mText + "" +(char)rbuf[i];
                    }
                    callbackContext.success(mText);

                  }
                  else {
                    callbackContext.success("");
                  }
              }
            });
            return true;
          }



        } catch(Exception e) {
            System.err.println("Error");
            callbackContext.error("FAIL2");
            return false;
        }
    return true;
  }
}
