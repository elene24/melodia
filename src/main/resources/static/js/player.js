/*
 * Melodia player: every ".play-btn" plays its song in the bottom bar
 * and tells the server about the play (POST /api/songs/{id}/play).
 */
document.addEventListener("DOMContentLoaded", function () {
    var audio = document.getElementById("player");
    if (!audio) {
        return;
    }
    var title = document.getElementById("np-title");
    var artist = document.getElementById("np-artist");
    var cover = document.getElementById("np-cover");
    var currentId = null;

    function markPlaying() {
        document.querySelectorAll(".play-btn").forEach(function (b) {
            var active = b.dataset.id === currentId && !audio.paused;
            b.classList.toggle("playing", active);
            b.textContent = active ? "❚❚" : "▶";
        });
    }

    document.querySelectorAll(".play-btn").forEach(function (button) {
        button.addEventListener("click", function () {
            if (button.dataset.id === currentId) {
                if (audio.paused) {
                    audio.play();
                } else {
                    audio.pause();
                }
                return;
            }
            currentId = button.dataset.id;
            audio.src = button.dataset.src;
            title.textContent = button.dataset.title;
            artist.textContent = button.dataset.artist;
            if (button.dataset.cover) {
                cover.src = button.dataset.cover;
                cover.hidden = false;
            } else {
                cover.hidden = true;
            }
            audio.play();
            fetch("/api/songs/" + currentId + "/play", {method: "POST"});
        });
    });

    audio.addEventListener("play", markPlaying);
    audio.addEventListener("pause", markPlaying);
    audio.addEventListener("ended", markPlaying);
});
