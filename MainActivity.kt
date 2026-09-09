package com.example.onlinemusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

data class Track(val title: String, val artist: String, val url: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val player = ExoPlayer.Builder(this).build()
        setContent { MusicApp(player) }
    }
}

@Composable
fun MusicApp(player: ExoPlayer) {
    val tracks = remember {
        listOf(
            Track("Song 01", "Artist 01", "https://YOUR-DOMAIN.com/audio/song01.mp3"),
            Track("Song 02", "Artist 02", "https://YOUR-DOMAIN.com/audio/song02.mp3"),
            Track("Song 03", "Artist 03", "https://YOUR-DOMAIN.com/audio/song03.mp3"),
            Track("Song 04", "Artist 04", "https://YOUR-DOMAIN.com/audio/song04.mp3")
        )
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val favorites = remember { mutableStateListOf<Int>() }

    val filtered = tracks.filter {
        it.title.contains(query, true) || it.artist.contains(query, true)
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    Scaffold(
        containerColor = Color(0xFF09090B),
        bottomBar = {
            Column {
                if (playing) {
                    NowPlayingBar(
                        track = tracks[selected],
                        onPlayPause = {
                            if (playing) player.pause() else player.play()
                            playing = !playing
                        },
                        onNext = {
                            selected = (selected + 1) % tracks.size
                            player.setMediaItem(MediaItem.fromUri(tracks[selected].url))
                            player.prepare()
                            player.play()
                            playing = true
                        }
                    )
                }
                NavigationBar(containerColor = Color(0xFF111113)) {
                    listOf("⌂" to "Home", "⌕" to "Search", "♥" to "Library").forEachIndexed { i, item ->
                        NavigationBarItem(
                            selected = selectedTab == i,
                            onClick = { selectedTab = i },
                            icon = { Text(item.first, fontSize = 20.sp) },
                            label = { Text(item.second) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { pad ->
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.padding(pad),
                tracks = tracks,
                selected = selected,
                onSelect = { index ->
                    selected = index
                    player.setMediaItem(MediaItem.fromUri(tracks[index].url))
                    player.prepare()
                    player.play()
                    playing = true
                },
                favorites = favorites,
                onFavorite = { i ->
                    if (favorites.contains(i)) favorites.remove(i) else favorites.add(i)
                }
            )
            1 -> SearchScreen(
                modifier = Modifier.padding(pad),
                query = query,
                onQuery = { query = it },
                tracks = filtered,
                onSelect = { track ->
                    selected = tracks.indexOf(track)
                    player.setMediaItem(MediaItem.fromUri(track.url))
                    player.prepare()
                    player.play()
                    playing = true
                }
            )
            else -> LibraryScreen(
                modifier = Modifier.padding(pad),
                tracks = favorites.map { tracks[it] },
                onSelect = { track ->
                    selected = tracks.indexOf(track)
                    player.setMediaItem(MediaItem.fromUri(track.url))
                    player.prepare()
                    player.play()
                    playing = true
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier,
    tracks: List<Track>,
    selected: Int,
    onSelect: (Int) -> Unit,
    favorites: List<Int>,
    onFavorite: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 20.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("My Music", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Listen online", color = Color.Gray, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Quick picks", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickCard("Made for you")
                QuickCard("Daily Mix")
            }
        }
        item {
            Text("Recently played", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
        items(tracks.indices.toList()) { i ->
            TrackRow(
                tracks[i], selected == i, favorites.contains(i),
                onClick = { onSelect(i) },
                onFavorite = { onFavorite(i) }
            )
        }
    }
}

@Composable
fun SearchScreen(
    modifier: Modifier,
    query: String,
    onQuery: (String) -> Unit,
    tracks: List<Track>,
    onSelect: (Track) -> Unit
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Search", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("What do you want to listen to?") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFFE8E8E8),
                unfocusedContainerColor = Color(0xFFE8E8E8),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(10.dp)
        )
        Spacer(Modifier.height(18.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tracks) { track ->
                TrackRow(track, false, false, { onSelect(track) }, {})
            }
        }
    }
}

@Composable
fun LibraryScreen(modifier: Modifier, tracks: List<Track>, onSelect: (Track) -> Unit) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Library", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))
        if (tracks.isEmpty()) {
            Text("Your liked songs will appear here.", color = Color.LightGray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tracks) { track -> TrackRow(track, false, true, { onSelect(track) }, {}) }
            }
        }
    }
}

@Composable
fun QuickCard(title: String) {
    Box(
        modifier = Modifier.width(160.dp).height(92.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1D1D22))
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TrackRow(
    track: Track,
    selected: Boolean,
    favorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF222228) else Color.Transparent)
            .clickable { onClick() }.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF303038)),
            contentAlignment = Alignment.Center
        ) { Text("♪", color = Color.White, fontSize = 26.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(track.artist, color = Color.Gray, fontSize = 13.sp)
        }
        Text(if (favorite) "♥" else "♡", color = Color.White, fontSize = 22.sp,
            modifier = Modifier.clickable { onFavorite() }.padding(8.dp))
    }
}

@Composable
fun NowPlayingBar(track: Track, onPlayPause: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Color(0xFF18181B)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF303038)),
            contentAlignment = Alignment.Center
        ) { Text("♪", color = Color.White) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(track.artist, color = Color.Gray, fontSize = 11.sp)
        }
        IconButton(onClick = onPlayPause) { Text("▶", color = Color.White) }
        IconButton(onClick = onNext) { Text("⏭", color = Color.White) }
    }
}
