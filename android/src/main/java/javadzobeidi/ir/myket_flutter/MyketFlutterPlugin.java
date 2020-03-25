package javadzobeidi.ir.myket_flutter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

import android.provider.Settings.Secure;
import android.util.Log;


import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import javadzobeidi.ir.myket_flutter.util.IabException;
import javadzobeidi.ir.myket_flutter.util.IabHelper;
import javadzobeidi.ir.myket_flutter.util.IabResult;
import javadzobeidi.ir.myket_flutter.util.Inventory;
import javadzobeidi.ir.myket_flutter.util.Purchase;


import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.MyketServerManagedPolicy;
import com.google.android.vending.licensing.Policy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyketFlutterPlugin implements MethodChannel.MethodCallHandler {

    private final  String TAG ="MyketFlutterPlugin";
    private String SKU = "";
    private String payLoad = "";
    private boolean consumption = false;
    private static final int RC_REQUEST = 10001;

    private MethodChannel channel;
    private Activity activity;
    private MethodChannel.Result pendingResult;

    private static String BASE64_PUBLIC_KEY;
    private static final byte[] SALT = new byte[]{
            -46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64,
            89
    };

    private Handler mHandler;

    private IabHelper mHelper;

    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;


    public static void registerWith(PluginRegistry.Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "myket_flutter");
        channel.setMethodCallHandler(new MyketFlutterPlugin(registrar, channel));
    }

    private MyketFlutterPlugin(PluginRegistry.Registrar registrar, MethodChannel channel) {
        this.activity = registrar.activity();
        this.channel = channel;
        this.channel.setMethodCallHandler(this);
        registrar.addActivityResultListener(new PluginRegistry.ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                activityResult(requestCode, resultCode, data);
                return false;
            }
        });
    }


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        pendingResult = result;
        String payload;
        String sku;

        switch (call.method) {
            case "initPay": {
                onDestroy();
                BASE64_PUBLIC_KEY = call.argument("BASE64PUBLICKEY");
                initPay(BASE64_PUBLIC_KEY);
                break;
            }
            case "referralToApplication": {
                String packageName = call.argument("packageName");
                referralToApplication(packageName);
                break;
            }
            case "referralToApplicationVideo": {
                String packageName = call.argument("packageName");
                referralToApplicationVideo(packageName);
                break;
            }
            case "referralToDownloadApplication": {
                String packageName = call.argument("packageName");
                referralToDownloadApplication(packageName);
                break;
            }
            case "referralToComment": {
                String packageName = call.argument("packageName");
                referralToComment(packageName);
                break;
            }
            case "referralToDeveloper":
                String developerId = call.argument("developerId");
                referralToDeveloper(developerId);
                break;
            case "checkLicense":
                checkLicense();
                break;
            case "dispose":
                onDestroy();
                break;
            case "launchPurchaseFlow":
                sku = call.argument("productKey");
                payload = call.argument("payload");
                boolean consumption = call.<Boolean>argument("consumption");
                launchPurchaseFlow(sku, payload, consumption);
                break;
            case "getPurchase":
                sku = call.argument("sku");
                getPurchase(sku);
                break;
            case "queryInventoryAsync":
                sku = call.argument("sku");
                queryInventoryAsync(sku);
                break;
            case "verifyDeveloperPayload":
                verifyDeveloperPayload();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void initPay(String base64PublicKey) {
        mHelper = new IabHelper(activity, base64PublicKey);
        mHelper.enableDebugLogging(true);
        Log.i("mHelper",mHelper.toString());
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.i("Setup finished Error", "Problem setting up In-app Billing: " + result);
                }
            }
        });
    }

    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            if (result.isFailure()) {
                pendingResult.error("Inventory Listener Error", "Failed to query inventory: " + result, null);
                return;
            }
            pendingResult.success(inventory.getSkuDetails(SKU) + "");
        }
    };

    private void queryInventoryAsync(String sku) {
        List<String> additionalSkuList = new ArrayList<>();
        additionalSkuList.add(sku);
        SKU = sku;
        mHelper.queryInventoryAsync(true, additionalSkuList, mGotInventoryListener);
    }


    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (mHelper == null) return;
            JSONObject obj = new JSONObject();
            try {
                if (result.isFailure()) {
                    obj.put("isFailure", result.isFailure());
                    obj.put("response", result.getResponse());
                    obj.put("message", result.getMessage());
                    obj.put("purchase", null);
                    pendingResult.success(obj.toString());
                    return;
                }
                obj.put("isSuccess", result.isSuccess());
                obj.put("response", result.getResponse());
                obj.put("message", result.getMessage());
                obj.put("purchase", purchase.getOriginalJson());
                payLoad = purchase.getDeveloperPayload();
                pendingResult.success(obj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (consumption)
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
        }
    };


    // Called when consumption is complete
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            // Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            if (mHelper == null) return;
            if (result.isSuccess()) {
                  Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
                   Log.d(TAG, "Error while consuming: " + result);
            }
              Log.d(TAG, "End consumption flow.");
        }
    };


    private void getPurchase(String sku) {
        List<String> additionalSkuList = new ArrayList<>();
        additionalSkuList.add(sku);
        try {
            Purchase gasPurchase = mHelper.queryInventory(false, additionalSkuList).getPurchase(sku);
            if (gasPurchase != null) {
                pendingResult.success(gasPurchase.getOriginalJson());
            } else
                pendingResult.success(null);
        } catch (IabException e) {
            e.printStackTrace();
            pendingResult.error("get_purchase_error", e.getMessage(), null);
        }
    }

    private void launchPurchaseFlow(String productKey, String payload, boolean consumption) {
        SKU = productKey;
        this.consumption = consumption;
        if(mHelper != null)
            mHelper.launchPurchaseFlow(activity, productKey, RC_REQUEST,mPurchaseFinishedListener, payload);
    }

    private void referralToDeveloper(String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("myket://developer/" + packageName));
        activity.startActivity(intent);
    }

    private void referralToApplication(String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("myket://details?id=" + packageName));
        activity.startActivity(intent);
    }

    private void referralToApplicationVideo(String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("myket://video?id=" + packageName));
        activity.startActivity(intent);
    }

    private void referralToDownloadApplication(String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("myket://download/" + packageName));
        activity.startActivity(intent);
    }

    private void referralToComment(String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("myket://comment?id=" + packageName));
        activity.startActivity(intent);
    }

    private void checkLicense() {
        String deviceId = Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID);
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        mHandler = new Handler();
        mChecker = new LicenseChecker(
                activity, new MyketServerManagedPolicy(activity,
                new AESObfuscator(SALT, activity.getPackageName(), deviceId)),
                BASE64_PUBLIC_KEY);
        activity.setProgressBarIndeterminateVisibility(true);
        mChecker.checkAccess(mLicenseCheckerCallback);
    }


    private void displayErrorResult(final String errorMessage, final String errorCode) {
        mHandler.post(new Runnable() {
            public void run() {
                pendingResult.error(errorCode, errorMessage, null);
            }
        });
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int policyReason) {
            if (activity.isFinishing()) {
                return;
            }
            activity.setProgressBarIndeterminateVisibility(false);
            mHandler.post(new Runnable() {
                public void run() {
                    pendingResult.success("allow");
                }
            });
        }

        public void dontAllow(int policyReason) {
            if (activity.isFinishing()) {
                return;
            }
            activity.setProgressBarIndeterminateVisibility(false);
            switch (policyReason) {
                case Policy.RETRY:
                    displayErrorResult("RETRY", "dontAllow");
                    break;
                case Policy.MYKET_NOT_INSTALLED:
                    displayErrorResult("MYKET_NOT_INSTALLED", "dontAllow");
                    break;
                case Policy.MYKET_NOT_SUPPORTED:
                    displayErrorResult("MYKET_NOT_SUPPORTED", "dontAllow");
                    break;
                default:
                    displayErrorResult(policyReason + "", "dontAllow");
                    break;
            }
        }

        public void applicationError(int errorCode) {
            if (activity.isFinishing()) {
                return;
            }
            activity.setProgressBarIndeterminateVisibility(false);
            displayErrorResult(errorCode + "", "applicationError");
        }
    }

    private void activityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) return;
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
        } else {
        }
    }

    private void onDestroy() {
        if(mChecker != null)
          mChecker.onDestroy();

        if (mHelper != null){
            mHelper.dispose();
            mHelper = null;
        }

    }

    private void verifyDeveloperPayload() {
        pendingResult.success(payLoad);
    }

}
