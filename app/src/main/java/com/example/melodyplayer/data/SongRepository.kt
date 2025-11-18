package com.example.melodyplayer.data

import com.example.melodyplayer.model.Song

class SongRepository(
    private val songs: List<Song>
) {
    constructor() : this(DefaultSongCatalog.songs)

    fun searchSongs(keyword: String): List<Song> {
        val query = keyword.trim()
        if (query.isEmpty()) return emptyList()

        return songs.filter { song ->
            song.title.contains(query, ignoreCase = true) ||
                    song.artist.contains(query, ignoreCase = true)
        }
    }

    private object DefaultSongCatalog {
        val songs: List<Song> = listOf(
            Song(
                title = "Shape of You",
                artist = "Ed Sheeran",
                audioUrl = "https://example.com/shape-of-you.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b2735a0b89dba4b0d7f6400c223b"
            ),
            Song(
                title = "Perfect",
                artist = "Ed Sheeran",
                audioUrl = "https://example.com/perfect.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273b94c2a42a4a62db7cbf2e5d2"
            ),
            Song(
                title = "Believer",
                artist = "Imagine Dragons",
                audioUrl = "https://example.com/believer.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273ef1b38061cc872114caf0f6c"
            ),
            Song(
                title = "Counting Stars",
                artist = "OneRepublic",
                audioUrl = "https://example.com/counting-stars.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273bb1e2c0dd3e8b06c8efb937b"
            ),
            Song(
                title = "Thunder",
                artist = "Imagine Dragons",
                audioUrl = "https://example.com/thunder.mp3",
                imageUrl = "https://i.scdn.co/image/ab67616d0000b273b92ec179bdfb67583512d6d9"
            ),
            Song(
                title = "Sunrise Prelude",
                artist = "SampleLib Ensemble",
                audioUrl = "https://samplelib.com/lib/preview/mp3/sample-6s.mp3",
                imageUrl = "https://samplelib.com/sample.jpg"
            ),
            Song(
                title = "Moonlight Echoes",
                artist = "SampleLib Ensemble",
                audioUrl = "https://samplelib.com/lib/preview/mp3/sample-9s.mp3",
                imageUrl = "https://samplelib.com/sample2.jpg"
            )
        )
    }
}