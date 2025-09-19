# 🌦️ WeatherTalk

WeatherTalk is a smart Android application that delivers **real-time weather forecasts** with interactive charts, notifications, and an **AI-powered assistant**.  
It helps users decide whether the weather is good for **camping, trucking, or other activities** based on their current location.

---

## 🚀 Features

### 🔍 Weather Search
- Enter any city name to get current weather data (temperature, wind speed, condition).
- Uses the **Open-Meteo Weather API** + **Geocoding API**.

### 📊 Dashboard (Thermal Representation)
- **Line Chart** → 7-day forecast trends for one or more cities.
- **Bar Chart** → Compare daily max temperatures across cities.
- **Pie Chart** → Average temperatures of selected cities.
- Supports **multiple cities** (2…15 or more).
- Thermal representation: see temperature highs/lows visually.

### 🗂️ City Management
- Add or remove cities dynamically.
- Save cities in local storage (SharedPreferences).
- Compare **multiple cities at once**.

### 🖱️ Interactive Charts
- Tap any point → popup shows **temperature, humidity, windspeed**.
- Zoom & pan for deeper analysis.

### 📡 AI Assistant
- Floating button in **MainActivity**.
- Uses **GPS location** (FusedLocationProviderClient) to fetch **live weather**.
- Gives activity recommendations:
    - ✅ “Perfect for camping ⛺ or trucking 🚚!”
    - ⚠️ “Too hot ☀️ — better to stay indoors.”
    - 🌬️ “Windy today — avoid long trucking.”

### 🔔 Smart Notifications
- Background **WeatherWorker** checks periodically.
- Sends **push notifications** when:
    - Sudden weather changes.
    - Storm warnings / high wind alerts.
    - Extreme heat or cold.
- User can snooze/resume alerts.

### ⚙️ Settings
- Customize update intervals.
- Enable/disable notifications.
- Change default units (Celsius, Fahrenheit).

---

## 🛠️ Tech Stack

- **Language**: Java
- **IDE**: Android Studio
- **UI**: XML + [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **APIs**:
    - Open-Meteo Weather API
    - Open-Meteo Geocoding API
- **Location**: Google Play Services (Fused Location Provider)
- **Notifications**: Android Notification Manager + WorkManager
- **Persistence**: SharedPreferences
- **Architecture**: Activity-based (extendable to MVVM)

---

## 📱 Screenshots

| Main Activity | Weather Dashboard | Multi-City Chart | AI Assistant |
|---------------|------------------|------------------|--------------|
| ![Main](screenshots/main.png) | ![Dashboard](screenshots/dashboard.png) | ![Cities](screenshots/multi.png) | ![AI](screenshots/ai.png) |

---

## ⚡ Installation

1. Clone the repo:
   ```bash
   git clone https://github.com/yourusername/weathertalk.git
   cd weathertalk
