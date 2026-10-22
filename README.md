# Movie Art for Projectivy Plugin

A wallpaper plugin for the [Projectivy](https://play.google.com/store/apps/details?id=com.spocky.projengmenu) AndroidTV launcher which fetches 
movie and tv show backgrounds from [TheMovieDB.org](https://www.themoviedb.org/).

[![Google Play Store badge](https://play.google.com/intl/en_us/badges/images/badge_new.png)](https://play.google.com/store/apps/details?id=com.danielkorgel.projectivy.plugin.cinemaglow)

Note: The project was initially published as `Cinema Glow` hence the bundle identifier and namespace.
The plugin is based on the [Projectivy plugin template](https://github.com/spocky/projectivy-plugin-wallpaper-provider).

## Fallback Background
For cards that are not a movie or show, the plugin can show *Popular Movies and Shows* (trending on TMDb), *Popular on the App's Service* (for a known streaming app, its most popular titles in that service's catalog, one after another on each visit and, while the app stays focused, at each wallpaper update; other apps get the trending titles), *Dynamic Colors* or a *Custom Background*. The streaming app to TMDb provider mapping is in `helpers/StreamingServices.kt` and was checked against TMDb's US provider list; the region comes from the device locale.

## Contributions
Contributions are welcome, please fork this repo and create a pull request with your changes.
It's expected that the GitHub actions fail in your fork due to missing pipline secrets.

## Local Setup
To build this plugin locally, you need a free [TheMovieDB.org API Key](https://www.themoviedb.org/settings/api).

Obtain one, then create this file: `/movie-art-plugin/apikeys.properties` with the following content,
replacing `abc` with your api key: 
```
TMDB_API_KEY=abc
```
