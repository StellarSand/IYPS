<img src="fastlane/metadata/android/en-US/images/icon.png" width="80" alt="App icon"/> 

# Is Your Password Secure?

A password strength app that evaluates and rates your password's robustness, estimates crack time, and provides helpful warnings and suggestions for stronger passwords.


<img src="https://img.shields.io/f-droid/v/com.iyps?logo=FDroid&color=green&style=for-the-badge" alt="F-Droid Version"> <img src="https://img.shields.io/endpoint?url=https://play.cuzi.workers.dev/play?i=com.iyps&m=$version&logo=GooglePlay&color=3BCCFF&label=Google%20Play&style=for-the-badge" alt="Google Play Version"> <img src="https://img.shields.io/github/v/release/StellarSand/IYPS?logo=GitHub&color=212121&label=GitHub&style=for-the-badge" alt="GitHub Version">



## Contents
- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Download](#download)
- [Changelog](#changelog)
- [How does it work?](#how-does-it-work)
- [Privacy Policy](#privacy-policy)
- [Issues](#issues)
- [Contributing](#contributing)
- [Credits](#credits)
- [License](#license)



## Overview
Two things that should always be strong: Coffee ☕ and Passwords 🔑.

In our digital age, where data breaches and hacks have skyrocketed, robust passwords are vital for safeguarding our online accounts. Using a password manager like [Bitwarden](https://bitwarden.com/) or [KeePass](https://keepass.info/) is strongly recommended for storing and generating unique, strong passwords.
<br>However, if you choose not to use a password manager (seriously though, consider using one), validating the strength of your passwords is crucial. 
 
 This app analyzes password patterns, predicts potential cracking times, and offers helpful suggestions for maximizing security. Additionally, it features a random password & passphrase generator that is configurable. Being entirely offline ensures that your passwords remain exclusively yours.



## Features
- Fully open source
- Material design 3 & Material You
- Completely offline
- Supports both light and dark theme
- No ads
- No collection of personal data
- Supported languages: 
  - English
  - Chinese
  - Dutch
  - French
  - German
  - Italian
  - Japanese
  - Persian
  - Portuguese & Portuguese (Brazil)
  - Spanish
  - Swedish
  - Turkish



## Screenshots

<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="200"/>

<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="200"/>

<img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="200"/>  <img src="/fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="200"/>



## Download
**Disclaimer**: The Google Play account is not owned by me.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
alt="Get it on F-Droid"
height="80">](https://f-droid.org/packages/com.iyps)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
alt="Get it on Google Play"
height="80">](https://play.google.com/store/apps/details?id=com.iyps)

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png"
alt="Get it on IzzyOnDroid"
height="80">](https://apt.izzysoft.de/fdroid/index/apk/com.iyps?repo=main)
[<img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png"
alt="Get it on GitHub"
height="80">](https://github.com/StellarSand/IYPS/releases/latest)

### Verify integrity if downloaded from GitHub
To verify the integrity of the `.apk`/`.aab` files, if downloaded from GitHub, perform the following steps:

<details>
  <summary><b>Windows</b></summary>
 
1. Open Powershell by searching for it in the `Start menu` OR by pressing `Win + R` and typing `powershell`
2. Change directory to the downloaded path
   ```
   cd "C:\path\to\downloaded\file"
   ```
   Example:
   ```
   cd "C:\Users\JohnDoe\Downloads"
   ```
3. Compute the SHA-256 Hash
   ```
   Get-FileHash -Algorithm SHA256 -Path "filename"
   ```
   Example:
   ```
   Get-FileHash -Algorithm SHA256 -Path "IYPS_v1.5.0.apk"
   ```
4. The computed hash value should be exactly the same as the one provided in the `.sha256` file on GitHub.
</details>

<details>
  <summary><b>Linux & macOS</b></summary>
 
1. Open terminal
2. Change directory to the downloaded path
   ```
   cd /path/to/downloaded/file
   ```
   Example:
   ```
   cd /home/JohnDoe/Downloads/
   ```
3. Compute the SHA-256 Hash
   ```
   sha256sum filename
   ```
   Example:
   ```
   sha256sum IYPS_v1.5.0.apk
   ```
4. The computed hash value should be exactly the same as the one provided in the `.sha256` file on GitHub.
</details>


## Changelog
All notable changes are documented in the [changelog](https://github.com/StellarSand/IYPS/blob/master/CHANGELOG.md).



## How does it work?
For a detailed explanation, refer to the following:
- [Realistic password strength estimation](https://dropbox.tech/security/zxcvbn-realistic-password-strength-estimation)
- [Five algorithms to measure real password strength](https://medium.com/nulab/five-algorithms-to-measure-real-password-strength-bd30126e82cc)



## Privacy Policy
Privacy policy is located [here](https://github.com/StellarSand/IYPS/blob/master/PRIVACY.md).



## Issues
If you find bugs or have suggestions, please report it to the [issue tracker](https://github.com/StellarSand/IYPS/issues). 
- Make sure you're on the latest version before reporting any issues.
- Please search for existing issues before opening a new one. Any duplicates will be closed.



## Contributing
Please read the [contributing guidelines](https://github.com/StellarSand/IYPS/blob/main/CONTRIBUTING.md) before contributing.

New pull requests can be submitted [here](https://github.com/StellarSand/IYPS/pulls).



## Credits
- [parveshnarwal](https://github.com/parveshnarwal) for publishing the app on Google Play and previously co-leading the development.
- [SecLists](https://github.com/danielmiessler/SecLists) for some of the password dictionaries.
- [EFF](https://www.eff.org) for publishing their wordlist to generate random passphrases.
- [Contributors](https://github.com/StellarSand/IYPS/graphs/contributors) for making this app better.



## License
This project is licensed under the terms of [GPL v3.0 license](https://github.com/StellarSand/IYPS/blob/main/LICENSE).
