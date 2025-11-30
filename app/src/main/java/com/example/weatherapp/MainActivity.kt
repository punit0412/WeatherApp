package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone


class MainActivity : AppCompatActivity() {
    
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null

    private var tvMain:TextView? = null
    private var tv_main_Description: TextView? = null
    private var tvTemp: TextView? = null
    private var tv_sunrise_time: TextView? = null
    private var tv_sunset_time:TextView? = null
    private var tv_min_temp: TextView? = null
    private var tv_max_temp: TextView? = null
    private var tv_humidity: TextView? = null
    private var tv_speed: TextView? = null
    private var tv_name: TextView? = null
    private var tv_country: TextView? = null
    private var iv_main: ImageView? = null
    private var iv_humidity: ImageView? = null
    private var iv_min_max: ImageView? = null
    private var iv_wind: ImageView? = null
    private var iv_location: ImageView? = null
    private var iv_sunrise: ImageView? = null
    private var iv_sunset: ImageView? = null

    private lateinit var mSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()




        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setUpUI()

        tvMain = findViewById(R.id.tv_main)
        tv_main_Description = findViewById(R.id.tv_main_description)
        tvTemp = findViewById(R.id.tv_temp)
        tv_sunrise_time = findViewById(R.id.tv_sunrise_time)
        tv_sunset_time = findViewById(R.id.tv_sunset_time)
        tv_min_temp = findViewById(R.id.tv_min)
        tv_max_temp = findViewById(R.id.tv_max)
        tv_humidity = findViewById(R.id.tv_humidity)
        tv_speed = findViewById(R.id.tv_speed)
        tv_name = findViewById(R.id.tv_name)
        tv_country = findViewById(R.id.tv_country)
        iv_main = findViewById(R.id.iv_main)
        iv_humidity = findViewById(R.id.iv_humidity)
        iv_wind = findViewById(R.id.iv_wind)
        iv_min_max = findViewById(R.id.iv_min_max)
        iv_location = findViewById(R.id.iv_location)
        iv_sunrise = findViewById(R.id.iv_sunrise)
        iv_sunset = findViewById(R.id.iv_sunset)




        
        if (!isLocationEnabled()){
            Toast.makeText(
                this,
                "Your location provider is turned off. Please turn it ON in settings.",
                Toast.LENGTH_LONG

            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }else{

            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(
                    object : MultiplePermissionsListener{
                        
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if(report!!.areAllPermissionsGranted()){
                                requestLocationData()
                            }
                            
                            if (report.isAnyPermissionPermanentlyDenied){
                                Toast.makeText(
                                    this@MainActivity,
                                    "You have denied location permission. " +
                                            "Please enable them as it is mandatory for the application",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest?>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermission()
                        }

                    }
                ).onSameThread().check()
        }
    }
    
    
    private fun showRationalDialogForPermission(){
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permission required for location." +
                    "Please turn it ON in the settings.")
            .setPositiveButton("GO TO SETTINGS"){
                _,_ ->
                try{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){
                dialog,_ ->
                dialog.dismiss()
            }.show()
    }

    fun isLocationEnabled(): Boolean{

        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData(){
        
        val mlocationsRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000L
        ).build()

        mFusedLocationProviderClient.requestLocationUpdates(
            mlocationsRequest, mLocationCallback, Looper.getMainLooper()
        )
    }
    
    private val mLocationCallback = object : LocationCallback(){

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            
            val mLastLocation: Location? = locationResult.lastLocation
            
            val latitude = mLastLocation?.latitude

            Log.i("Current Latitude", "$latitude")
            
            val longitude = mLastLocation?.longitude

            Log.i("Current Longitude", "$longitude")

            if (latitude != null && longitude != null) {
                getLocationWeatherDetails(latitude, longitude)
                mFusedLocationProviderClient.removeLocationUpdates(this)
            }
            
        }
    }

    private fun getLocationWeatherDetails(latitude: Double?, longitude: Double?){

        if(Constants.isNetworkAvailable(this)){

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude,
                longitude,
                Constants.METRIC_UNIT,
                Constants.APP_ID
            )

            showCustomProgressDialog()

            listCall.enqueue(

                object : Callback<WeatherResponse> {


                    override fun onResponse(
                        call: Call<WeatherResponse>,
                        response: Response<WeatherResponse>
                    ) {
                        hideProgressDialog()
                        if (response!!.isSuccessful) {
                            val weatherList: WeatherResponse? = response.body()

                            val weatherResponseJsonString = Gson().toJson(weatherList)
                            val editor = mSharedPreferences.edit()
                            editor.putString(Constants.WEATHER_RESPONSE_DATA,weatherResponseJsonString)
                            editor.apply()


                            setUpUI()
                            Log.i("Response Result", "$weatherList")
                        } else {
                            when (response.code()) {
                                400 -> Log.e("API_ERROR", "Bad Request (400)")
                                401 -> Log.e("API_ERROR", "Unauthorized (401)")
                                403 -> Log.e("API_ERROR", "Forbidden (403)")
                                404 -> Log.e("API_ERROR", "Not Found (404)")
                                500 -> Log.e("API_ERROR", "Server Error (500)")
                                else -> Log.e("API_ERROR", "Unknown error: ${response.code()}")
                            }
                        }
                    }
                    override fun onFailure(
                        call: Call<WeatherResponse?>,
                        t: Throwable
                    ) {
                        hideProgressDialog()
                        Log.e("Erorrrrrr", t.message.toString())
                    }

                })

        }else{
            Toast.makeText(
                this@MainActivity,
                "You are not connected to internet, Please connect.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showCustomProgressDialog(){

        mProgressDialog = Dialog(this)

        mProgressDialog!!.setContentView(R.layout.dialog_custom_progres)
        mProgressDialog!!.show()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){

            R.id.action_refresh -> {
                requestLocationData()
                true
            }else ->super.onOptionsItemSelected(item)

        }

    }

    private fun hideProgressDialog(){
        if(mProgressDialog!=null){
            mProgressDialog!!.dismiss()
        }
    }

    private fun setUpUI(){

        val weatherResponseJsonString = mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA,"")
        if(!weatherResponseJsonString.isNullOrEmpty()){
            val weatherList = Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)

            for (i in weatherList?.weather?.indices!!){
                Log.i("Weather Name", weatherList.weather.toString())
                tvMain?.text = weatherList.weather[i].main
                tv_main_Description?.text = weatherList.weather[i].description
                tvTemp?.text = weatherList.main.temp.toString() +
                        getUnit(application.resources.configuration.locales.toString())

                tv_sunrise_time?.text = unixTime(weatherList.sys.sunrise)
                tv_sunset_time?.text = unixTime(weatherList.sys.sunset)

                tv_min_temp?.text = weatherList.main.temp_min.toString() + " min"
                tv_max_temp?.text = weatherList.main.temp_max.toString() + " max"
                tv_humidity?.text = weatherList.main.humidity.toString() + " per cent"
                tv_speed?.text = weatherList.wind.speed.toString()
                tv_name?.text = weatherList.name
                tv_country?.text = weatherList.sys.country

                when(weatherList.weather[i].icon){

                    "01d" -> iv_main?.setImageResource(R.drawable.sunny)
                    "02d" -> iv_main?.setImageResource(R.drawable.cloud)
                    "03d" -> iv_main?.setImageResource(R.drawable.cloud)
                    "04d" -> iv_main?.setImageResource(R.drawable.cloud)
                    "04n" -> iv_main?.setImageResource(R.drawable.cloud)
                    "10d" -> iv_main?.setImageResource(R.drawable.rain)
                    "11d" -> iv_main?.setImageResource(R.drawable.storm)
                    "13d" -> iv_main?.setImageResource(R.drawable.snowflake)
                    "01n" -> iv_main?.setImageResource(R.drawable.cloud)
                    "02n" -> iv_main?.setImageResource(R.drawable.cloud)
                    "03n" -> iv_main?.setImageResource(R.drawable.cloud)
                    "10n" -> iv_main?.setImageResource(R.drawable.cloud)
                    "11n" -> iv_main?.setImageResource(R.drawable.rain)
                    "13n" -> iv_main?.setImageResource(R.drawable.snowflake)
                }


            }

        }


    }

    private fun getUnit(value:String): String? {

        var unit_var = "°C"
        if("US" == value || "LR" == value || "MM" == value){
            unit_var = "°F"
        }
        return unit_var

    }

    private fun unixTime(timex:Long):String?{

        val date = Date(timex*1000)
        val sdf = SimpleDateFormat("HH:mm:ss")
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)

    }
}