# Movie Art for Projectivy Plugin

A wallpaper plugin for the Projectivy GoogleTV launcher which fetches 
movie and tv show backgrounds from [TheMovieDB.org](https://www.themoviedb.org/).

Plugin is based on the [Projectivy plugin template](https://github.com/spocky/projectivy-plugin-wallpaper-provider).

The project was initially published as `Cinema Glow` hence the bundle identifier and namespce.

## Contributions
Contributions are welcome, please fork this repo and create a pull request with your changes.
The actions will fail in your fork due to missing secrets.

## Local Setup
To build this plugin locally, you need a free [TheMovieDB.org API Key](https://www.themoviedb.org/settings/api).

Obtain one, then create a file  `/movie-art-plugin/apikeys.properties` with the following content,
replacing `abc` with your api key: 
```
TMDB_API_KEY=abc
```
