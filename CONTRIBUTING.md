# CONTRIBUTING
- Do not submit pull requests to update gradle, dependencies or SDK.
- Try not to use any deprecated libraries, dependencies or methods, if other alternatives are available.
- Make sure the characters are properly encoded when translating strings (example: `ä` as `\u00E4`, `é` as `\u00E9` etc).
  <br>You can use websites like [Compart](https://www.compart.com/en/unicode), [Symbl](https://symbl.cc/en/unicode/table/) or something else.
  <br>Example: `é` would be shown as `U+00E9` on these websites, so just convert it to `\u00E9` for the android strings.
  <br>The strings can be found here:
  - [English](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values/strings.xml)
  - [Dutch](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-nl/strings.xml)
  - [French](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-fr/strings.xml)
  - [German](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-de/strings.xml)
  - [Italian](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-it/strings.xml)
  - [Japanese](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-ja/strings.xml)
  - [Portuguese](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-pt/strings.xml)
  - [Spanish](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-es/strings.xml)
  - Chinese: [Primary](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-zh/strings.xml), [Feedback](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/raw/messages_zh.properties)
  - Swedish: [Primary](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-sv/strings.xml), [Feedback](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/raw/messages_sv.properties)
  - Turkish: [Primary](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values-tr/strings.xml), [Feedback](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/raw/messages_tr.properties)
- When adding a new language:
  1. Translate the [primary](https://github.com/StellarSand/IYPS/blob/main/app/src/main/res/values/strings.xml) strings.
  2. Create a new file named `messages_<language code>.properties` in [res/raw](https://github.com/StellarSand/IYPS/tree/main/app/src/main/res/raw) and translate the [feedback strings](https://github.com/nulab/zxcvbn4j/blob/main/src/main/resources/com/nulabinc/zxcvbn/messages.properties).
    <br>Example: for adding `Finnish (fi)` language, create `messages_fi.properties`.
