# Melodia – Music Streaming Backend

Melodia is a small music streaming platform built with **Spring Boot 3.5,
Spring Data JPA and PostgreSQL**. Artists publish albums, albums contain
songs with uploaded audio files, and listeners like songs, follow artists
and build playlists. It has a REST API and a Thymeleaf website with a
built-in audio player.

The demo songs are 30-second segments of royalty-free tracks from
[Pixabay](https://pixabay.com/playlists/reggae-sunshine-28696853/), used under
the [Pixabay Content License](https://pixabay.com/service/license-summary/)
and credited to their uploaders (see "Music credits" at the end). Album
groupings and release dates in the demo data are made up for the demo.

---

## 1. How to run

### Requirements
- Java 17 or newer
- PostgreSQL 13 or newer (pgAdmin is fine)
- IntelliJ IDEA (or Maven 3.9+)

### Steps
1. Create the database (in pgAdmin: *Databases → Create → Database…*, or in psql):
   ```sql
   CREATE DATABASE melodia;
   ```
2. Set your PostgreSQL username and password. Either edit
   `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_PASSWORD
   ```
   or set the environment variables `DB_USERNAME` / `DB_PASSWORD`.
3. Open the project in IntelliJ and run `MelodiaApplication`
   (or run `mvn spring-boot:run`).
4. Open **http://localhost:8080**.

On the first start, Hibernate creates the tables (`ddl-auto=update`) and the
app inserts demo data: 7 artists, 8 albums, 11 playable songs, 3 users and
2 playlists. Uploaded files are stored in the `uploads/` folder next to the
project. Set `app.seed.enabled=false` to skip the demo data.

### Tests
```bash
mvn test
```
The integration test runs on an in-memory H2 database, so PostgreSQL is not
needed for it.

---

## 2. Data model

```
Artist 1 ──< Album 1 ──< Song >──< Genre
                          ^  ^
                          |  |  (liked songs)
           Playlist >─────┘  └────< User >──< Artist (followed artists)
              v                      1
              └─────────────────────<┘ (owner)
```

| Entity   | Table          | Relationships |
|----------|----------------|---------------|
| Artist   | artists        | `@OneToMany` albums |
| Album    | albums         | `@ManyToOne` artist, `@OneToMany` songs |
| Song     | songs          | `@ManyToOne` album, `@ManyToMany` genres |
| Genre    | genres         | used by Song (`song_genres`) |
| User     | app_users      | `@OneToMany` playlists, `@ManyToMany` likedSongs, `@ManyToMany` followedArtists |
| Playlist | playlists      | `@ManyToOne` owner, `@ManyToMany` songs (`playlist_songs`) |

`User` maps to `app_users` because `user` is a reserved word in PostgreSQL.

---

## 3. Where each requirement is implemented

| Requirement | Where |
|---|---|
| Layered packages | `controller` (`api` = REST, `web` = pages), `service`, `repository`, `entity`, `dto`, `config`, `exception` |
| Stereotype annotations | `@RestController` (controller/api), `@Controller` (controller/web), `@Service`, `@Repository`, `@Configuration` (`WebConfig`, `DataSeedConfig`) |
| Constructor injection only | Every bean receives dependencies through its constructor into `private final` fields. There is no field `@Autowired` in application code. |
| ≥ 3 entities | 6 entities in `entity/` |
| ≥ 2 relationship types | `@OneToMany`/`@ManyToOne` (Artist–Album, Album–Song, User–Playlist) and `@ManyToMany` (Song–Genre, Playlist–Song, User–Song, User–Artist) |
| Repositories extend Spring Data | all extend `JpaRepository` |
| ≥ 2 method-name queries | e.g. `SongRepository.findByGenresNameIgnoreCaseAndDurationSecondsLessThanEqual`, `findTop10ByOrderByPlayCountDesc`, `PlaylistRepository.existsByOwnerIdAndNameIgnoreCase`, `AlbumRepository.findByArtistIdOrderByReleaseDateDesc`, `ArtistRepository.findByNameContainingIgnoreCase` |
| ≥ 2 `@Query` queries | `SongRepository.search` (JOIN + EXISTS, paginated), `findRecommendedForUser` (subqueries), `findMostLiked` (GROUP BY + constructor expression), `incrementPlayCount` (`@Modifying` UPDATE), `PlaylistRepository.sumDurationSeconds` (SUM), `ArtistRepository.findMostFollowed`, `GenreRepository.countSongsPerGenre` (**native SQL**) |
| `@Transactional` | Every service method that changes data. Read methods use `@Transactional(readOnly = true)`. |
| `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping` | all REST controllers |
| `@PathVariable` | e.g. `GET /api/songs/{id}`, `POST /api/playlists/{id}/songs/{songId}` |
| `@RequestParam` | search/filter (`q`, `genre`, `maxDuration`, `limit`) and file parts (`audioFile`, `file`) |
| `@RequestBody` / `@ModelAttribute` | JSON create/update endpoints use `@RequestBody`; the multipart song upload and all HTML forms use `@ModelAttribute` |
| JSR-303 validation + `@Valid` | `dto/*Request` classes (`@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@Min`, `@Max`, `@NotNull`, `@Positive`, `@PastOrPresent`); every incoming object is preceded by `@Valid` |
| File upload | `POST /api/songs` (MP3/WAV/OGG/M4A/FLAC), `POST /api/artists/{id}/image`, `POST /api/albums/{id}/cover`, and the HTML forms with `enctype="multipart/form-data"`. Files are saved to `uploads/` by `FileStorageService` and the file name is stored in the database (`songs.audio_file_name`, `albums.cover_file_name`, `artists.image_file_name`). |
| Pagination & sorting | `Pageable` + `Sort` on `GET /api/songs`, `/api/artists`, `/api/albums`, `/api/playlists`, `/api/users`, `/api/users/{id}/liked-songs` and on the `/songs` page, e.g. `?page=0&size=5&sort=playCount,desc` |

### Why these methods are transactional
- **Adding a song to a playlist:** loads the playlist and the song, checks for a
  duplicate and inserts the join-table row as one unit. If any step fails,
  nothing is saved.
- **Counting a play:** an atomic `UPDATE ... SET play_count = play_count + 1`
  (`@Modifying` queries require a transaction). Because the database does
  the increment itself, two people playing at the same time cannot overwrite
  each other's count (no "lost update").
- **Deleting an artist:** removes the artist's songs from playlists and likes,
  deletes its follows, albums and songs, and only then removes the files.
  A failure rolls everything back.
- **Uploading a song:** the album and genres are checked before the file is
  written. If saving the record fails, the stored file is deleted again.

---

## 4. REST API

| Method | URL | Description |
|---|---|---|
| GET | `/api/songs?q=&genre=&page=&size=&sort=` | search, paginate and sort songs |
| GET | `/api/songs/{id}` | one song |
| POST | `/api/songs` *(multipart)* | upload: `title`, `durationSeconds`, `albumId`, `genreIds`, `audioFile` |
| PUT | `/api/songs/{id}` | update metadata (JSON) |
| DELETE | `/api/songs/{id}` | delete song and its file |
| GET | `/api/songs/{id}/stream` | stream audio (supports seeking) |
| POST | `/api/songs/{id}/play` | count one play |
| GET | `/api/songs/trending` | top 10 by plays |
| GET | `/api/songs/short?genre=Jazz&maxDuration=180` | short songs in a genre |
| GET | `/api/songs/most-liked?genre=&limit=` | like ranking |
| GET/POST/PUT/DELETE | `/api/artists`, `/api/artists/{id}` | artists CRUD (`?name=` search) |
| POST | `/api/artists/{id}/image` *(multipart `file`)* | artist photo |
| GET | `/api/artists/{id}/albums`, `/api/artists/most-followed` | |
| GET/POST/PUT/DELETE | `/api/albums`, `/api/albums/{id}` | albums CRUD |
| POST | `/api/albums/{id}/cover` *(multipart `file`)* | album cover |
| GET | `/api/albums/{id}/songs` | track list |
| GET/POST/PUT/DELETE | `/api/users`, `/api/users/{id}` | users CRUD |
| GET | `/api/users/{id}/liked-songs` | paginated liked songs |
| POST/DELETE | `/api/users/{id}/liked-songs/{songId}` | like / unlike |
| POST/DELETE | `/api/users/{id}/followed-artists/{artistId}` | follow / unfollow |
| GET | `/api/users/{id}/recommendations` | songs from followed artists not liked yet |
| GET/POST | `/api/users/{id}/playlists` | a user's playlists / create one |
| GET/PUT/DELETE | `/api/playlists`, `/api/playlists/{id}` | playlists |
| POST/DELETE | `/api/playlists/{id}/songs/{songId}` | add / remove song |
| GET/POST/DELETE | `/api/genres`, `/api/genres/{id}` | genres |
| GET | `/api/genres/stats` | songs per genre (native SQL) |

Errors are returned as JSON with a status code (400 validation with
`fieldErrors`, 404 not found, 409 duplicate, 413 file too large).

A ready-made **Postman collection** is in `postman/Melodia.postman_collection.json`.

### Example: upload a song with curl
```bash
curl -F title="My Song" -F durationSeconds=200 -F albumId=1 \
     -F genreIds=1 -F audioFile=@my-song.mp3 http://localhost:8080/api/songs
```

---

## 5. Website pages
- `/`: trending songs, new albums, most liked, genres
- `/songs`: search, genre filter, sorting, pagination, play buttons
- `/songs/new`: upload form (multipart, validation messages)
- `/songs/{id}`: details, add to playlist, delete
- `/artists`, `/artists/{id}`: artists, create artist (photo), create album (cover)
- `/albums/{id}`: track list
- `/playlists`, `/playlists/{id}`: playlists, create, remove songs

Forms use the Post/Redirect/Get pattern with flash messages.

---

## Music credits

All tracks come from Pixabay and are used under the Pixabay Content License.
Only short segments are included, as demo data for this project; the audio
files are not meant to be redistributed on their own.

| Track | Uploader on Pixabay |
|---|---|
| Su-Su Riddim | Brotheration_Records |
| Locust | Brotheration_Records |
| Rain Fall | Brotheration_Records |
| Everyday Is A Holiday | Brotheration_Records |
| Love Of My Life | Brotheration_Records |
| Summer | Elijah_K |
| Rasta Never Fall | Kond1 |
| Reggae Town | JuliusH |
| Reggae | AlexGuz |
| Summer Afro Tropical Instrumental | OiBeats |
| Fun Kids Reggae Music | Tunetank |

Artist photos and album covers are abstract images generated for this
project.
