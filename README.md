# Melodia

A music streaming backend built with Spring Boot 3.5, Spring Data JPA and
PostgreSQL. Artists publish albums, albums contain songs with uploaded audio
files, and users like songs, follow artists and build playlists. It has a REST
API and a Thymeleaf website with an audio player.

## Run

1. Create the database: `CREATE DATABASE melodia;`
2. Set your PostgreSQL password in `src/main/resources/application.properties`
   (default: `postgres` / `postgres`).
3. Run `MelodiaApplication` and open http://localhost:8080.

On first start, Hibernate creates the tables and demo data is inserted
(7 artists, 11 songs, 3 users, 2 playlists). Uploaded files are saved in
`uploads/`. Tests (`mvn test`) run on an in-memory H2 database.

## Data model

| Entity | Relationships |
|---|---|
| Artist | `@OneToMany` albums |
| Album | `@ManyToOne` artist, `@OneToMany` songs |
| Song | `@ManyToOne` album, `@ManyToMany` genres |
| Genre | linked to songs through `song_genres` |
| User | `@OneToMany` playlists, `@ManyToMany` liked songs and followed artists |
| Playlist | `@ManyToOne` owner, `@ManyToMany` songs |

## Requirements

| Requirement | Where |
|---|---|
| Layered packages | `controller` (`api`, `web`), `service`, `repository`, `entity`, `dto`, `config`, `exception` |
| Constructor injection | all beans, no field `@Autowired` |
| Method-name queries | e.g. `findByGenresNameIgnoreCaseAndDurationSecondsLessThanEqual`, `findTop10ByOrderByPlayCountDesc` |
| `@Query` | `SongRepository.search`, `incrementPlayCount`, `findMostLiked`, `findRecommendedForUser`; native: `GenreRepository.countSongsPerGenre` |
| `@Transactional` | every service method that changes data |
| Validation | `dto/*Request` classes + `@Valid` in controllers |
| File upload | `POST /api/songs` (multipart), artist photos, album covers; stored in `uploads/`, file name saved in the database |
| Pagination & sorting | e.g. `GET /api/songs?page=0&size=5&sort=playCount,desc` |

## Main endpoints

| Method | URL | Description |
|---|---|---|
| GET | `/api/songs?q=&genre=&page=&size=&sort=` | search songs |
| POST | `/api/songs` | upload a song (multipart: `title`, `durationSeconds`, `albumId`, `genreIds`, `audioFile`) |
| GET | `/api/songs/{id}/stream` | stream audio |
| POST | `/api/songs/{id}/play` | count a play |
| GET | `/api/songs/trending` | top 10 songs |
| POST | `/api/playlists/{id}/songs/{songId}` | add a song to a playlist |
| GET | `/api/users/{id}/recommendations` | recommended songs |

All endpoints are in the Postman collection: `postman/Melodia.postman_collection.json`.

## Music credits

The demo songs are 30-second segments of royalty-free tracks from
[Pixabay](https://pixabay.com), used under the
[Pixabay Content License](https://pixabay.com/service/license-summary/):
tracks by Brotheration_Records, Elijah_K, Kond1, JuliusH, AlexGuz, OiBeats
and Tunetank. Album groupings and dates are demo values. Cover images were
generated for this project.
