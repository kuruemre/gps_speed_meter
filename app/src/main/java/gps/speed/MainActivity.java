package gps.speed;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
public class MainActivity extends AppCompatActivity implements LocationListener,SensorEventListener, Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private TextView speedometerTextView;
    private LocationManager locationManager;
    Button myButton;
    Button myButton2;
    Button myButton3;
    TextView myTextView;
    private ImageView compassImage;
    private float currentDegree = 0f;
    private SensorManager sensorManager;
    private Activity currentActivity;
    TextView tvHeading;
    int mode;

    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }
    private class AppOpenAdManager {
        private static final String LOG_TAG = "AppOpenAdManager";
        private static final String AD_UNIT_ID = "ca-app-pub-8962230903240284/2620605853";

        private AppOpenAd appOpenAd = null;
        private boolean isLoadingAd = false;
        private boolean isShowingAd = false;

        private long loadTime = 0;

        /** Constructor.
         * @param mainActivity*/
        public AppOpenAdManager(MainActivity mainActivity) {}

        public AppOpenAdManager() {

        }

        /** Request an ad. */
        public void loadAd(Context context) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return;
            }

            isLoadingAd = true;
            AdRequest request = new AdRequest.Builder().build();
            AppOpenAd.load(
                    context, AD_UNIT_ID, request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(AppOpenAd ad) {
                            // Called when an app open ad has loaded.
                            Log.d(LOG_TAG, "Ad was loaded.");
                            appOpenAd = ad;
                            isLoadingAd = false;
                            loadTime = (new Date()).getTime();
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            // Called when an app open ad has failed to load.
                            Log.d(LOG_TAG, loadAdError.getMessage());
                            isLoadingAd = false;
                        }
                    });
        }
        /** Shows the ad if one isn't already showing. */
        public void showAdIfAvailable(
                @NonNull final Activity activity,
                @NonNull OnShowAdCompleteListener onShowAdCompleteListener){
            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                Log.d(LOG_TAG, "The app open ad is already showing.");
                return;
            }

            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                Log.d(LOG_TAG, "The app open ad is not ready yet.");
                onShowAdCompleteListener.onShowAdComplete();
                loadAd(activity);
                return;
            }

            appOpenAd.setFullScreenContentCallback(
                    new FullScreenContentCallback() {

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when fullscreen content is dismissed.
                    // Set the reference to null so isAdAvailable() returns false.
                    Log.d(LOG_TAG, "Ad dismissed fullscreen content.");
                    appOpenAd = null;
                    isShowingAd = false;

                    onShowAdCompleteListener.onShowAdComplete();
                    loadAd(activity);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when fullscreen content failed to show.
                    // Set the reference to null so isAdAvailable() returns false.
                    Log.d(LOG_TAG, adError.getMessage());
                    appOpenAd = null;
                    isShowingAd = false;

                    onShowAdCompleteListener.onShowAdComplete();
                    loadAd(activity);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    Log.d(LOG_TAG, "Ad showed fullscreen content.");
                }
            });
            isShowingAd = true;
            appOpenAd.show(activity);
        }

        /** Show the ad if one isn't already showing. */
        private void showAdIfAvailable(@NonNull final Activity activity) {
            showAdIfAvailable(
                    activity,
                    new OnShowAdCompleteListener() {
                        @Override
                        public void onShowAdComplete() {
                            // Empty because the user will go back to the activity that shows the ad.
                        }
                    });
        }

        /** Utility method to check if ad was loaded more than n hours ago. */
        private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
            long dateDifference = (new Date()).getTime() - this.loadTime;
            long numMilliSecondsPerHour = 3600000;
            return (dateDifference < (numMilliSecondsPerHour * numHours));
        }
        /** Check if ad exists and can be shown. */
        public boolean isAdAvailable() {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
        }


    }
    private AdView mAdView;

    private AppOpenAdManager appOpenAdManager;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.registerActivityLifecycleCallbacks(this);
        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        appOpenAdManager = new AppOpenAdManager();


        mode = 0;
        compassImage = (ImageView) findViewById(R.id.compass);
        tvHeading = (TextView) findViewById(R.id.direction);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        myButton = findViewById(R.id.button1);
        myButton2 = findViewById(R.id.button2);
        myButton3 = findViewById(R.id.button3);
        myTextView = findViewById(R.id.speed_unit);
        //Intent intent = new Intent(MainActivity.this, CompassActivity.class);
        //startActivity(intent);

        // Get reference to the TextView that will display the speedometer reading
        speedometerTextView = findViewById(R.id.speedometer_text_view);

        // Get reference to the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Request location updates from the location manager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the button is clicked
                // For example, you can show a Toast message
                //Toast.makeText(MainActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
                myTextView.setText("km/h");
                mode = 0;
            }
        });

        myButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the button is clicked
                // For example, you can show a Toast message
                //Toast.makeText(MainActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
                myTextView.setText("h/km");
                mode = 1;
            }
        });

        myButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the button is clicked
                // For example, you can show a Toast message
                //Toast.makeText(MainActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
                myTextView.setText("m/h");
                mode = 2;
            }
        });
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        appOpenAdManager.showAdIfAvailable(currentActivity);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location updates from the location manager
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {
                // Permission denied, display an error message
                Toast.makeText(this, "Location permission required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Calculate the speed in km/h using the location's speed property
        float speedInMetersPerSecond = location.getSpeed();
        float speedInKmPerHour = speedInMetersPerSecond * 3.6f;

        // Update the speedometer TextView with the new speed reading
        if(mode == 0)
        {

            speedometerTextView.setText(String.format(Locale.getDefault(), "%.1f", speedInKmPerHour));
        }
        else if(mode == 1)
        {

            speedometerTextView.setText(String.format(Locale.getDefault(), "%.1f", speedInKmPerHour));

        }
        else
        {
            speedInKmPerHour = speedInKmPerHour*1000f;
            speedometerTextView.setText(String.format(Locale.getDefault(), "%.1f", speedInKmPerHour));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Not needed for this example
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Not needed for this example
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Not needed for this example
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister listener
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        tvHeading.setText("" + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation rotateAnimation = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        // set the duration of the animation (in milliseconds)
        rotateAnimation.setDuration(210);

        // set the animation after the end of the reservation status
        rotateAnimation.setFillAfter(true);

        // Start the animation
        compassImage.startAnimation(rotateAnimation);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    /** ActivityLifecycleCallback methods. */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        // Updating the currentActivity only when an ad is not showing.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}


}