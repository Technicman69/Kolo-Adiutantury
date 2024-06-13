package main.java;

import javax.print.attribute.standard.Media;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class AudioManager extends Component {
    public void playSound(String fileName) {
        try {
            // Open an audio input stream.
            File f = new File("resources/" + fileName).getAbsoluteFile();
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
            // Get a sound clip resource.
            Clip clip = AudioSystem.getClip();
            // Open audio clip and load samples from the audio input stream.
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
