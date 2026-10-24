# AlarmGap

AlarmGap is a modern Android application built with Jetpack Compose that helps you calculate the exact time difference between two times, or determine an end time based on a start time and duration. It's especially useful for figuring out exactly how much sleep you'll get before your alarm goes off!

## Features

- **Time Difference Calculation:** Easily calculate the time gap between a start time and an end time.
- **Duration Addition/Subtraction:** Calculate what time it will be after a specific duration passes.
- **Live Calculation:** See the results update instantly as you change the inputs.
- **History:** Keep track of your past calculations.
- **Customization & Settings:**
  - Support for Dark and Light themes (Dynamic colors supported).
  - Customizable time formats (12-hour / 24-hour).
  - Option to show/hide seconds, decimal hours, and total minutes.
- **Modern UI:** Built entirely with Jetpack Compose and Material Design 3 guidelines.

## Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System:** [Material Design 3](https://m3.material.io/)
- **Architecture:** MVVM (Model-View-ViewModel) using `ViewModel` and `StateFlow`.
- **Navigation:** Jetpack Navigation Compose

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Koala or newer recommended)
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 37

### Building the App

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/AlarmGap.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync and download the required dependencies.
4. Build and run the app on an emulator or a physical device.

## License

This project is open-source and available under the [MIT License](LICENSE).
