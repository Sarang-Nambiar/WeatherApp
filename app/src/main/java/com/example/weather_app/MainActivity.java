package com.example.weather_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    Location location;
    RVAdapter rvAdapter;
    private final String sharedPrefFile = "com.example.android.mainsharedprefs";
    public static final String CITY_NAME = "CITY_NAME";
    public static final String WEATHER = "WEATHER";
    public static final String TEMP = "TEMP";
    public static final String FEELS_LIKE = "FEELS_LIKE";
    public static final String TEMP_MAX = "TEMP_MAX";
    public static final String TEMP_MIN = "TEMP_MIN";
    public static final String LAST_UPDATED = "LAST_UPDATED";
    public static final String PRESSURE = "PRESSURE";
    public static final String HUMIDITY = "HUMIDITY";
    private static final String[] LIST_CITIES = {"New York", "Singapore", "Mumbai", "Delhi", "Sydney", "Melbourne"};

    SharedPreferences mPreferences;
    ArrayList<CityModel> arrayList;
    private static final String APIKEY = "10fdb15d1e3decaac4d6139088dfedf9";
    private static final String baseUrl = "https://api.openweathermap.org/data/2.5/weather";
    private static final String iconUrl = "https://openweathermap.org/img/wn/";
    TextView cityNameText, weatherText, feelsLikeText, tempText, tempMaxText, tempMinText, lastUpdatedText, pressureText, humidityText;
    ImageView iconView;
    ConstraintLayout layout;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing all variables and views
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        arrayList = new ArrayList<>();
        cityNameText = findViewById(R.id.cityText);
        weatherText = findViewById(R.id.weatherText);
        feelsLikeText = findViewById(R.id.feelsLikeText);
        tempText = findViewById(R.id.tempText);
        tempMaxText = findViewById(R.id.tempHighText);
        tempMinText = findViewById(R.id.tempLowText);
        iconView = findViewById(R.id.weatherIcon);
        lastUpdatedText = findViewById(R.id.lastUpdatedText);
        layout = (ConstraintLayout) findViewById(R.id.constraintLayout);
        recyclerView = findViewById(R.id.cityRV);
        pressureText = findViewById(R.id.pressureText); //populate for storing values
        humidityText = findViewById(R.id.humidityText);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //setting texts in text view after log in(no network case)
        cityNameText.setText(mPreferences.getString(CITY_NAME, "--"));
        weatherText.setText(mPreferences.getString(WEATHER, "--"));
        feelsLikeText.setText(mPreferences.getString(FEELS_LIKE, "--"));
        tempText.setText(mPreferences.getString(TEMP, "0\u00B0"));
        tempMinText.setText(mPreferences.getString(TEMP_MIN, "0\u00B0"));
        tempMaxText.setText(mPreferences.getString(TEMP_MAX, "0\u00B0"));
        lastUpdatedText.setText(mPreferences.getString(LAST_UPDATED, "Last updated: Never"));
        pressureText.setText(mPreferences.getString(PRESSURE, "--"));
        humidityText.setText(mPreferences.getString(HUMIDITY, "--"));

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            try {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 101);
            } catch (Exception e) {
                startActivity(new Intent(MainActivity.this, noPermissionsPrompt.class));
            }

        } else {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading...");
            progressDialog.setMessage("Fetching your location");
            progressDialog.setCancelable(false);
            progressDialog.show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    getWeather(location.getLatitude(), location.getLongitude());
                    progressDialog.dismiss();
                }
            });
        }
        for (String cityname : LIST_CITIES) {
            getWeatherCity(cityname);
        }


//        if (isGPSEnabled()) {
//            location = getLastKnownLocation(); // getting latitude and longitude
//            getWeather(location.getLatitude(), location.getLongitude()); // getting current location
//            }
//        } else {
//
//        }
    }

    //checking if GPS is enabled
    private boolean isGPSEnabled() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); // checks if the GPS is enabled
    }


    // getting the last known location of the user
    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
//    public void getLocation(String voice2txt) {
//        locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
//        criteria = new Criteria();
//        String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
//        Location location = locationManager.getLastKnownLocation(bestProvider);
//        if (isGPSEnabled()) {
//            Log.e("TAG", "GPS is on");
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//            Toast.makeText(PlacesDecoder.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
//        }
//        else
//        {
//            AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(MainActivity.this);
//            notifyLocationServices.setTitle("Switch on Location Services");
//            notifyLocationServices.setMessage("Location Services must be turned on to complete this action. Also please take note that if on a very weak network connection,  such as 'E' Mobile Data or 'Very weak Wifi-Connections' it may take even 15 mins to load. If on a very weak network connection as stated above, location returned to application may be null or nothing and cause the application to crash.");
//            notifyLocationServices.setPositiveButton("Ok, Open Settings", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent openLocationSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    MainActivity.this.startActivity(openLocationSettings);
//                    finish();
//                }
//            });
//            notifyLocationServices.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    finish();
//                }
//            });
//            notifyLocationServices.show();
//        }
//    }

//    private Location getLocation() {
//        if (ActivityCompat.checkSelfPermission(
//                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
//                    , Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
//        }
//        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
//        List<String> providers = locationManager.getProviders(true);
//        Location bestLocation = null;
//        for (String provider : providers) {
//            Location l = locationManager.getLastKnownLocation(provider);
//            if (l == null) {
//                continue;
//            }
//            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
//                // Found best last known location: %s", l);
//                bestLocation = l;
//            }
//        }
//        return bestLocation;
//    }

    // Calling openweathermap API for getting weather details in the current location
    private void getWeather(double latitude, double longitude) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String tempURL = baseUrl + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + APIKEY;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, tempURL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                            JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                            JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");

                            String description = jsonObjectWeather.getString("description");
                            String iconName = jsonObjectWeather.getString("icon");
                            String cityName = jsonResponse.getString("name");
                            double temp = jsonObjectMain.getDouble("temp") - 273.15; // converting kelvins to celsius
                            double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                            double tempMax = jsonObjectMain.getDouble("temp_max") - 273.15;
                            double tempMin = jsonObjectMain.getDouble("temp_min") - 273.15;
                            int humidity = jsonObjectMain.getInt("humidity");
                            int pressure = jsonObjectMain.getInt("pressure");

                            cityNameText.setText(capitalizeWord(cityName));
                            weatherText.setText(capitalizeWord(description));
                            tempText.setText(Math.round(temp) + "\u00B0");
                            feelsLikeText.setText("Feels like " + Math.round(feelsLike) + "\u00B0");
                            tempMaxText.setText(Math.round(tempMax) + "\u00B0");
                            tempMinText.setText(Math.round(tempMin) + "\u00B0");
                            lastUpdatedText.setText("Last updated: " + Calendar.getInstance().getTime().toString());
                            humidityText.setText("Humidity: " + humidity + "%");
                            pressureText.setText("Pressure: " + pressure + "mb");

                            //downloading weather icons from openweathermap
                            Picasso.get().load(iconUrl + iconName + "@2x.png").into(iconView);
                            if (iconName.contains("d")) {
                                layout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.daytimebg));
                            } else {
                                layout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.nighttimebg));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(stringRequest);
            }
        });

    }

    // calling openWeathermap API for LIST_CITIES
    private void getWeatherCity(String cityname) {
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());
        String tempURL = baseUrl + "?q=" + cityname + "&appid=" + APIKEY;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, tempURL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                            JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                            JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");

                            String iconName = jsonObjectWeather.getString("icon");
                            String cityName = jsonResponse.getString("name");
                            double temp = jsonObjectMain.getDouble("temp") - 273.15; // converting kelvins to celsius

                            arrayList.add(new CityModel(Math.round(temp), cityName, iconName));
                            rvAdapter = new RVAdapter(getApplicationContext(), arrayList, iconUrl);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            recyclerView.setAdapter(rvAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
                requestQueue.add(stringRequest);
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });

    }

    // directing user to no Permissions page
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if ((grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)) {
                startActivity(new Intent(MainActivity.this, noPermissionsPrompt.class));
            }
        }
    }

    //saving the API call values for offline use.
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString(WEATHER, weatherText.getText().toString());
        preferencesEditor.putString(CITY_NAME, cityNameText.getText().toString());
        preferencesEditor.putString(TEMP, tempText.getText().toString());
        preferencesEditor.putString(TEMP_MAX, tempMaxText.getText().toString());
        preferencesEditor.putString(TEMP_MIN, tempMinText.getText().toString());
        preferencesEditor.putString(FEELS_LIKE, feelsLikeText.getText().toString());
        preferencesEditor.putString(LAST_UPDATED, lastUpdatedText.getText().toString());
        preferencesEditor.putString(PRESSURE, pressureText.getText().toString());
        preferencesEditor.putString(HUMIDITY, humidityText.getText().toString());
        preferencesEditor.apply();

    }

    // capitalizing each word in a string
    private String capitalizeWord(String string) {
        String output = "";
        String[] wordList = string.split(" ");
        for (String s : wordList) {
            output += s.substring(0, 1).toUpperCase() + s.substring(1) + " ";
        }
        return output;
    }
}