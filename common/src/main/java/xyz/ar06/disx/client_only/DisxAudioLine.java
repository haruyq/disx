package xyz.ar06.disx.client_only;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import xyz.ar06.disx.DisxLogger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.CompletableFuture;

public class DisxAudioLine {
    private SourceDataLine line;
    AudioFormat audioFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,     // PCM signed encoding
            44100,                              // Sample rate (44.1kHz is common)
            16,                                 // Sample size in bits (16-bit audio)
            2,                                  // Number of channels (stereo in this case)
            4,                                  // Frame size (2 bytes per channel = 4 bytes per frame)
            44100,                              // Frame rate (same as sample rate for PCM)
            true                                // Big-endian byte order
    );
    public DisxAudioLine(){

        try {
            line = AudioSystem.getSourceDataLine(audioFormat);
            line.open(audioFormat);
            line.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeToLine(ByteBuf buf){
        DisxLogger.debug("write to line called");
        byte[] audioData = new byte[882000];
        if (!buf.isReadable()) {
            throw new RuntimeException("Disx Error: No readable data found in packet!");
        }
        buf.readBytes(audioData);
        DisxLogger.debug("audio data read from packet");
        DisxLogger.debug("checking if line open");
        if (!line.isOpen()){
            try {
                DisxLogger.debug("line not open, trying to open");
                line.open(audioFormat);
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }
        DisxLogger.debug("starting line");
        line.start();
        DisxLogger.debug("started line");
        line.write(audioData, 0, audioData.length);
        DisxLogger.debug("wrote data (or called for it)");
    }
}
