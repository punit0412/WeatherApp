# 🌦️ **WeatherApp**

## 📝 **Summary**

**WeatherApp** is a clean and lightweight Android app that displays
real-time weather based on your current location. It shows temperature,
humidity, wind, visibility, and sunrise/sunset times in a simple,
card-based interface powered by the OpenWeather API.

## 📸 **Screenshot**

`<img src="Screenshot%202025-11-29%20185105.png" width="320">`{=html}

## ✨ **Features**

-   Auto-detects current location
-   Temperature, humidity & visibility
-   Wind speed and direction
-   City & country detection
-   Sunrise and sunset times
-   Manual refresh option
-   Clean card-style UI
-   API + GPS error handling

## 🛠️ Tech Stack

- **Kotlin**
- **Retrofit + Gson**
- **FusedLocationProviderClient** (for location)
- **Material Components**
- **Vector Drawables** for weather icons
- **ConstraintLayout / CardView / XML layouts**


## 🌍 **Weather API**

Powered by the **OpenWeather API**, providing:
- Current weather
- Temperature & humidity
- Wind speed/direction
- City details
- Sunrise/Sunset info

## 📦 **Installation**

### 1. Clone the repo

``` bash
git clone https://github.com/yourusername/WeatherApp.git
```

### 2. Open in Android Studio

### 3. Add your API key

``` xml
<string name="weather_api_key">YOUR_API_KEY</string>
```

### 4. Run the app

Select a device → **Run ▶️**

## 📁 **Project Structure**

    WeatherApp/
     ├── ui/
     ├── network/
     ├── utils/
     └── res/

## 🚀 **Future Enhancements**

-   7-day forecast
-   Dark mode
-   Search city feature
-   Weather animations
-   Widgets

## 🤝 Contributing

Open to pull requests and improvements.

## 📄 License

Free to use and modify.
