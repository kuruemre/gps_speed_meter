package gps.speed;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private TextView speedometerTextView;
    private LocationManager locationManager;
    Button myButton;
    Button myButton2;
    Button myButton3;
    TextView myTextView;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myButton = findViewById(R.id.button1);
        myButton2 = findViewById(R.id.button2);
        myButton3 = findViewById(R.id.button3);
        myTextView = findViewById(R.id.speed_unit);



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
            }
        });

        myButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the button is clicked
                // For example, you can show a Toast message
                //Toast.makeText(MainActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
                myTextView.setText("h/km");
            }
        });

        myButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Code to execute when the button is clicked
                // For example, you can show a Toast message
                //Toast.makeText(MainActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
                myTextView.setText("m/s");
            }
        });
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
        speedometerTextView.setText(String.format(Locale.getDefault(), "%.1f", speedInKmPerHour));
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
}