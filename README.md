HAndroid
========
This is my first Android app ever, so please expect bugs. Hopefully the basic functionality works (it does for me) but bug reports are always welcome.

The app is for controlling HomeAutomation (http://karpero.mine.nu/ha). The intention is not to make an app that does everything HomeAutomation does, it's to make an app that does the stuff that you need to quickly do when your computer isn't within reach. Like a HomeAutomation remote control.

Please note that HomeAutomation 3.0 is needed. This app does NOT work with anything earlier than that!

Maybe some day this app will reach Google Play. For the time being it lives together with this readme at https://kolefors.se/handroid/

Oh, and HAndroid might not be the final name. We'll see about that...

/Daniel Malmgren
daniel@kolefors.se

Notes about app settings:<br>
URL - Needs to be full url including http/https<br>
username/password - If any of those are empty, local login is used instead (works well at home).<br>

Todo:<br>
More error checking<br>
Page for macros?<br>
Maybe a widget?<br>
Show upcoming schedule stuff and maybe also latest stuff in log<br>
Maybe houseplan? Not very useful on phone, but maybe on tablet.<br>

Changelog<br>
0.6 (2014-02-07)
Added page with sensor readings
Try avoiding white text on white background, it's hard to read
Fixed a case where it kept hanging on "please wait..."

0.5 (2014-01-24)
Auto update of device statuses, with user adjustable interval
Nice glow on names of active devices

0.4 (2014-01-18)
Statuses of devices are now displayed (they're yellow if they're on).
Fixed unnecessary redraws

0.3 (2014-01-17)
Added page for changing scenario
Added sliders for dimmers

0.2 (2014-01-13)
Hopefully fixed button sizes so they're equally big on all devices
Changed api calls so it's more in line with rest of the api

0.1 (2014-01-12)
App starts and displays devices and groups along with buttons to turn on or off
Settings for url, username and password
