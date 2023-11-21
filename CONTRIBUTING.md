# CONTRIBUTING
- Do not submit pull requests to update gradle, dependencies or SDK.
- Try not to use any deprecated libraries, dependencies or methods, if other alternatives are available.
- Make sure the characters are properly encoded when translating strings (example: ä as \u00E4, é as \u00E9 etc).
  <br>The strings can be found here:
  - [English](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values/strings.xml)
  - [Dutch](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-nl/strings.xml)
  - [French](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-fr/strings.xml)
  - [German](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-de/strings.xml)
  - [Italian](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-it/strings.xml)
  - [Japanese](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-ja/strings.xml)
  - [Spanish](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-es/strings.xml)
  - Swedish: [Primary](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-sv/strings.xml), [Feedback](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/raw/messages_sv.properties)
  - Turkish: [Primary](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-tr/strings.xml), [Feedback](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/raw/messages_tr.properties)
- When adding a new language:
  - Translate the [primary](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values/strings.xml) strings.
  - Create a new file named `messages_<language code>.properties` in [res/raw](https://github.com/StellarSand/IYPS/tree/main/app/src/main/res/raw) and translate the [feedback strings](https://github.com/nulab/zxcvbn4j/blob/main/src/main/resources/com/nulabinc/zxcvbn/messages.properties).
    <br>Example: for adding `Portuguese (pt)` language, create `messages_pt.properties`.