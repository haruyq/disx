package xyz.ar06.disx;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.apache.http.client.config.RequestConfig;

import javax.sound.sampled.*;
import java.io.InputStream;

@Deprecated
public class LavaplayerTest {
    private static AudioDataFormat FORMAT = StandardAudioDataFormats.COMMON_PCM_S16_BE;
    private static DefaultAudioPlayerManager PLAYER_MANAGER = new DefaultAudioPlayerManager();
    private static AudioPlayer AUDIO_PLAYER = new DefaultAudioPlayer(PLAYER_MANAGER);

    private static AudioInputStream INPUT_STREAM = AudioPlayerInputStream.createStream(AUDIO_PLAYER, FORMAT, 99999999L, true);

    public static void testTrack(MinecraftServer server){
        DisxLogger.debug("test track called");
        PLAYER_MANAGER.setHttpRequestConfigurator(requestConfig -> RequestConfig.copy(requestConfig)
                .setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .build()
        );
        String URL = "http://disxytsourceapi.ar06.xyz/stream_audio?id=I8Q4vpE9DAs";
        AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
        AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
        PLAYER_MANAGER.getConfiguration().setOutputFormat(FORMAT);
        DisxLogger.debug("calling load item");
        PLAYER_MANAGER.loadItem(URL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                DisxLogger.debug("item loaded");
                AUDIO_PLAYER.addListener(new TrackHandler());
                AUDIO_PLAYER.playTrack(track);
                DisxLogger.debug("called audio player to play track");
                sendAudioData(INPUT_STREAM);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    private static void sendAudioData(InputStream audioStream) {
        try {
            byte[] buffer = new byte[882000]; // A buffer to hold audio data
            int bytesRead;
            while ((bytesRead = audioStream.read(buffer)) >= 0) {
                sendAudioPacket(buffer, bytesRead); // Send the packet to the client
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void streamToLine(){
        try {
            DisxLogger.debug("starting streaming to line");
            SourceDataLine line = AudioSystem.getSourceDataLine(INPUT_STREAM.getFormat());
            DisxLogger.debug("line made");
            // Open the line with the given audio format
            line.open(INPUT_STREAM.getFormat());
            line.start();

            // Buffer for audio data
            byte[] buffer = new byte[1024]; // Adjust the buffer size as needed
            int bytesRead;

            DisxLogger.debug("streaming to line");
            // Read the audio data and write it to the SourceDataLine
            while ((bytesRead = INPUT_STREAM.read(buffer)) >= 0) {
                line.write(buffer, 0, bytesRead); // Write audio data to the line
            }
            DisxLogger.debug("stopping streaming to line");

            // Drain and close the line
            line.drain(); // Ensure all audio data has been played
            line.close(); // Close the line

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendAudioPacket(byte[] audioData, int length) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBytes(audioData, 0, length);

        for (Player p : DisxServerAudioRegistry.getMcPlayers()) {
            DisxServerPacketIndex.ServerPackets.audioData(p, buf);
        }
    }


    public static class TrackHandler extends AudioEventAdapter {
        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            DisxLogger.debug("track started, calling send audio data");
            super.onTrackStart(player, track);
        }
    }
}
