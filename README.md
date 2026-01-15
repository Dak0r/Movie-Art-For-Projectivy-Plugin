# Movie Art for Projectivy Plugin

A wallpaper plugin for the [Projectivy](https://play.google.com/store/apps/details?id=com.spocky.projengmenu) AndroidTV launcher which fetches 
movie and tv show backgrounds from [TheMovieDB.org](https://www.themoviedb.org/).

[![Google Play Store badge](https://play.google.com/intl/en_us/badges/images/badge_new.png)](https://play.google.com/store/apps/details?id=com.danielkorgel.projectivy.plugin.cinemaglow)

Note: The project was initially published as `Cinema Glow` hence the bundle identifier and namespace.
The plugin is based on the [Projectivy plugin template](https://github.com/spocky/projectivy-plugin-wallpaper-provider).

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
