# 📷 **Bluromatic**

**Bluromatic** is a modern Android application that applies a blur effect to an image in the background using WorkManager. It demonstrates a clean architecture using MVVM, background processing with CoroutineWorker, and a beautiful Jetpack Compose UI. The image is saved to the device and notifications inform the user of progress.

## ✅ **LAST MAJOR UPDATES**

- 🔄 Enhanced UI State Management:
   - Integration of StateFlow in BlurViewModel to observe WorkManager state in real time.
   - Use of stateIn() to make the flow compatible with the UI component (Compose) lifecycle.
   - Dynamic update of the interface according to state: default, in progress, completed.

- 🔗 Single work string with WorkManager:
   - Replacement of .beginWith() by .beginUniqueWork() with ExistingWorkPolicy.REPLACE.
   - Ensures that only one job chain (cleanup → blur → save) is active at a time, replacing the old one if necessary.
      
## ❌ **NEXT UPDATES**

   - Define task constraints (e.g., network, charging).
   - Write UI tests for the worker sequence.
   - Display final result in UI.
   - cancel tasks and manage states.

## 📋 **Features**

   - 🔮 **Blur Images** :

      - ✅ **DONE** Apply a blur effect in the background.
      - ✅ **DONE** Use WorkManager to run tasks off the UI thread.
      - ✅ **DONE** Chained tasks: cleanup -> blur -> save.
      - ✅ **DONE** Create unique task chains.
      - 🟩 **IN PROGRESS** Cancel tasks and manage states.
      - ❌ **UNDONE** Add constraints to tasks (e.g., network, charging).
   
   - 🎉 **Notifications** :

      - ✅ **DONE** Send Android notifications on start, success, and error.

   - 📷 **Image Management**

      - ✅ **DONE** Load a local image and apply blur.
      - ✅ **DONE** Save blurred image to MediaStore.
      - ✅ **DONE** Clean temporary images before blur.
      - ❌ **UNDONE** Display the final result (open image directly from the UI).

   - 🎨 Modern and Fluid Interface:

      - ✅ **DONE** Follows Material Design 3 guidelines.
      - ✅ **DONE** Responsive layout with adaptive UI.

      - Light/Dark Mode:
         - ✅ **DONE** Supports **light/dark mode**.

   - 🔄 Real-time status management:

      - ✅ **DONE** Use of StateFlow for UI state handling.
      - ✅ **DONE** ViewModel for lifecycle-aware logic.
      - ✅ **DONE** Coroutines for async data operations.
      - ✅ **DONE** Update UI dynamically with task tags and states.

   - 🧠 Architecture & Code Structure:

      - ✅ **DONE** MVVM architecture pattern.
      - ✅ **DONE** Clean separation between UI and business logic.
      - ❌ **UNDONE** Write UI tests for worker chains.

## 🛠️ **Tech Stack**

   - **Kotlin**: Modern, concise language for Android development.
   - **Jetpack Compose**: Declarative UI toolkit for Android.
   - **Material 3**: Modern, accessible user interface.
   - **StateFlow**: Reactive state management for real-time updates.
   - **ViewModel**: MVVM architecture to separate business logic from user interface.
   - **State Management**: Handle states with MutableStateOf and StateFlow.
   - **WorkManager**: Robust background task scheduling.
   - **CoroutineWorker**: Async task processing.
   - **MediaStore**: Save image to system gallery.
   
## 🚀 **How to Use**
1. **Launch the app**:
   - Download the "app-release.apk" file find in \app\release\ .
   - Install the file in your smartphone or in an emulator. (Good performance because in Release Build Variant)
   - If you want to use android studio, download the code and launch the app on an Android device or emulator. (Bad performance because in Debug Build Variant)
2. **Start a Blur:**:
   - Tap the Start button to apply a fixed blur (you can also cancel the task).
   - A notification appears when the task starts.
3. **Blur in Progress:**:
   - WorkManager executes background blur + save steps.
4. **Image Saved**:
   - Image is saved to your gallery.
   - You get a notification of the saved image.

## 📸 **Screenshots**

- **Initial screen**:

   ![Initial screen](screenshots/initial_screen.png)

- **Cleaing notif screen**:

   ![Cleaning notif screen](screenshots/cleaning_notif_screen.png)

- **Blur image notif screen**:

   ![Blur image notif screen](screenshots/blur_image_notif_screen.png)

- **Save image notif screen**:

   ![Save image notif screen](screenshots/save_image_notif_screen.png)



## 🤝 **Contributions**
Contributions are welcome! Feel free to fork the repository and submit a pull request for new features or bug fixes✅🟩❌.