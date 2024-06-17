package main.java;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class AudioManager extends Component {
    public void playSound(String fileName) {
        try {
            // Open an audio input stream.
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(Objects.requireNonNull(getClass().getResource("/" + fileName)));
            // Get a sound clip resource.
            Clip clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
