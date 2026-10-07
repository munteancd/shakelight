# ShakeLight

Shake your Android phone to turn the flashlight on or off. That's it. No screen, no settings.

## How it works

- Open the app once after installing. It asks for two permissions (notifications, run in background), then closes itself.
- Shake the phone with 3–4 quick, firm moves: the flashlight toggles and the phone gives a short buzz.
- Works with the screen off and starts again by itself after a reboot.
- Walking, running or a single bump won't trigger it.
- A small notification stays in the status bar: tap it to toggle the flashlight, or press **Oprește** (Stop) to turn ShakeLight off.

**Battery:** the phone has to stay partly awake to catch the shake, which can cost a few percent of battery per day, depending on the phone.

## Build

Android Studio, or from the command line:

```
gradlew assembleDebug
```

The APK ends up in `app/build/outputs/apk/debug/`. Requires Android 8.0+ and a phone with a flashlight.

---

# ShakeLight (RO)

Scuturi telefonul și se aprinde sau se stinge lanterna. Atât. Fără ecran, fără setări.

## Cum funcționează

- După instalare deschizi aplicația o singură dată. Cere două permisiuni (notificări și rulare în fundal), apoi se închide singură.
- Scuturi telefonul scurt și hotărât (3–4 mișcări rapide): lanterna se aprinde sau se stinge, cu o vibrație scurtă.
- Merge și cu ecranul stins și pornește singură după restart.
- Mersul, alergatul sau o lovitură nu o declanșează.
- În bara de notificări rămâne o notificare mică: o atingi ca să aprinzi sau să stingi lanterna, iar cu **Oprește** dezactivezi ShakeLight.

**Baterie:** telefonul trebuie ținut parțial treaz ca să prindă scuturatul, deci poate consuma câteva procente pe zi, în funcție de telefon.

## Build

Din Android Studio sau din linia de comandă:

```
gradlew assembleDebug
```

APK-ul apare în `app/build/outputs/apk/debug/`. Merge pe Android 8.0+, pe telefoane cu lanternă.
