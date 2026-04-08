# 🔐 Password Manager App

A fully developed Android password manager focused on **client-side encryption and secure credential storage**.  
Built with modern Android tools and Firebase, this app ensures that sensitive user data remains encrypted and accessible only with the correct master password.

---

## 🚀 Features

- 🔑 **User Authentication**
  - Secure login & signup using Firebase Authentication

- 🔐 **Strong Encryption (AES-256 GCM)**
  - All vault data is encrypted locally before being stored
  - Uses **AES/GCM/NoPadding** for confidentiality + integrity

- 🔑 **Secure Key Derivation**
  - Master password is converted into an encryption key using:
    - PBKDF2WithHmacSHA256  
    - 200,000+ iterations  
    - Unique per-user salt

- 🗂 **Encrypted Vault**
  - Vault stored as encrypted JSON in Firebase
  - Supports add, edit, and delete operations

- 🔒 **Master Password Protection**
  - Password is never stored directly
  - Incorrect password → decryption fails → vault remains locked

- ⚡ **Session-Based Access**
  - Encryption key generated only after entering master password
  - Key is stored temporarily during session

- 📱 **Modern UI**
  - Built using Jetpack Compose

- 🌙 **Dark Mode Support**

- 🛡 **Screen Security**
  - Screenshots & screen recording disabled

---

## 🛠 Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **Firebase Authentication**
- **Firebase Realtime Database / Firestore**
- **AES-256 (GCM Mode)**
- **PBKDF2 Key Derivation**
- **MVVM Architecture**

---

## 🔐 Security Overview

1. User logs in via Firebase  
2. User enters **master password**  
3. App:
   - Retrieves user-specific **salt**
   - Derives encryption key using PBKDF2  
4. Vault (stored as encrypted JSON) is decrypted locally  
5. If password is incorrect:
   - Decryption fails due to GCM authentication
   - Vault remains inaccessible  

> ⚠️ If the master password is lost, the vault cannot be recovered.

---

## 📸 Screenshots

### 🗂 Vault (Light & Dark Mode)
<p align="center">
  <img src="screenshots/vault_light.png" width="250"/>
  <img src="screenshots/vault_dark.png" width="250"/>
</p>

---

### ➕ Add Entry
<p align="center">
  <img src="screenshots/add_vault_entry.png" width="250"/>
</p>

---

### 🔐 Vault Dialog
<p align="center">
  <img src="screenshots/vault_dialog_light.png" width="250"/>
  <img src="screenshots/vault_dialog_dark.png" width="250"/>
</p>

---

### 🌙 Dark Mode Toggle
<p align="center">
  <img src="screenshots/toggle_dark_mode.png" width="250"/>
</p>

---

### 📭 Empty State
<p align="center">
  <img src="screenshots/empty_vault.png" width="250"/>
</p>

---

### 🔑 Authentication
<p align="center">
  <img src="screenshots/google_login.jpg" width="250"/>
  <img src="screenshots/set_master_password.png" width="250"/>
  <img src="screenshots/enter_master_password.png" width="250"/>
</p>

---

## 🎯 Status

✅ **Completed & Functional**

---

## 📦 Installation

```bash
git clone https://github.com/sukhmmeet/Password-Manager.git
```

1. Open in Android Studio  
2. Add your `google-services.json`  
3. Run the app 🚀  

---

## 🤝 Contributing

Contributions are welcome.  
Feel free to fork and submit pull requests for improvements.

---

## 📄 License

This project is open-source and free to use.

---
