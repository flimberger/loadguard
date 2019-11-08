# LoadGuard

A (work in progress) battery monitor for Android devices.

## Overview

*The project is in an early stage of development and not yet fit for general use*

LoadGuard monitors your devices battery and alerts you if it reaches 80 % when it is charging.
This prevents shortening the batteries live due to excessive charging.

## Permissions

Currently,
the following permissions are used by LoadGuard:

- `FOREGROUND_SERVICE`:
  TL;DR:
  In order to monitor the batteries state,
  a service must run in the foreground.
  If the notification is annoying,
  hide all notifications from this channel.
  
  Long description:
  Since Android N (7.0),
  the `POWER_CONNECTED` and `POWER_DISCONNECTED` broadcasts are not sent to implicit receivers
  anymore.
  Therefore,
  a service with an explicitly registered receiver must be running at all times.
- `RECEIVE_BOOT_COMPLETED`:
  The aforementioned service should start at boot time,
  or else it would not be able to monitor the battery.
- `VIBRATE`:
  The alarm can use the vibrator.
