> Android trivia game demonstrating hybrid persistence with Firebase + Room.
# Trivia Masters
Trivia Masters is an Android trivia game featuring persistent player progression, an in-game shop, and hybrid local/remote data storage. The application was designed as a submission for a university module on mobile app development..

## Features
- Multiple game modes (Standard and Blitz)
- Persistent player stats including scores, combos, and performance tracking
- In-game currency and shop system
- Hybrid persistence with Firebase Realtime Database for authenticated players and Room (SQLite) for offline/local gameplay

## Screenshots
<p align="center">
  <strong>Main Menu & Stats</strong><br><br>
<img width="230" alt="main menu" src="https://github.com/user-attachments/assets/aa3554e7-93f0-4e89-86ac-4550b81db571" />
<img width="230" alt="stats" src="https://github.com/user-attachments/assets/ac0c1771-bfea-441d-bc7c-a1bb4dfdcf10" />
 </p> 

<p align="center">
  <strong>Gameplay</strong><br><br>
<img width="230" alt="gameplay1" src="https://github.com/user-attachments/assets/ebb7c846-cbf6-4db5-84d4-14cecb6fa404" />
<img width="230" alt="gameplay2" src="https://github.com/user-attachments/assets/f13a11b9-d85f-48ed-bb4a-a1d1158f0680" />
</p>

<p align="center">
  <strong>Shop System</strong><br><br>
  <img width="230" alt="shop" src="https://github.com/user-attachments/assets/861d5f9d-c938-4e60-a6cf-d6a79a706d93" />
</p>

## Running the project
1. Clone the repository

2. Open in Android Studio

3. Sync Gradle

4. Run on an emulator or physical device

*Firebase configuration is not included.
To enable cloud features, provide your own google-services.json.
The app will still run locally using Room without Firebase.
