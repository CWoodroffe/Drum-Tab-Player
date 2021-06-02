import java.util.ArrayList;

import java.util.Scanner;

import java.io.*;
import javax.sound.sampled.*;

import java.io.File;

import javax.sound.midi.Sequencer;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import javax.swing.Timer;
import javax.swing.JFrame;
import java.awt.EventQueue;

import java.lang.Math;

public class drumPlayer extends JFrame {
    //hex codes for short messages
    public static final int NOTE_ON = 0x90; //144 in dec
    public static final int NOTE_OFF = 0x80; //128 in dec
    public static final int PROGRAM_CHANGE = 192;
    public static final int CONTROL_CHANGE = 176; 
    public static final int PITCH_BEND = 224;
    
    //dec codes for meta messages
    public static final int SET_TEMPO = 81;
    public static final int END_OF_TRACK = 47;
    public static final int TRACK_NAME = 3;
    public static final int TIME_SIGNATURE = 88;
    public static final int KEY_SIGNATURE = 89;

    
    public static float TMSfactor;
    
    public drumPlayer() {
        initUI();
    }
    
    private void initUI() {
        add(new Board());

        setSize(1200, 700);
        
        setTitle("Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }   
    public static String midiInput (String a) {
        Scanner inputScanner = new Scanner(System.in);  // Create a Scanner object
        boolean fileCheck;
        String typeCheck = null;
        String tempMidi = null;
        do{
            System.out.print("Enter midi file:");
            tempMidi = inputScanner.nextLine();  // Read user input
            File tempFile = new File(tempMidi); //check if File exists, loop if not found
            fileCheck = tempFile.exists(); //true or false value whether file exists or not
            if (tempFile.length() > 4){ //prevents StringIndexOutOfBounds exception
                typeCheck = tempMidi.substring(tempMidi.length() - 3, tempMidi.length());
            }
            System.out.println(typeCheck);
        } while (fileCheck == false && typeCheck != "mid");
        return tempMidi;
    }
    
    public static String wavInput (String b) {
        Scanner inputScanner = new Scanner(System.in);
        boolean fileCheck;
        String typeCheck = null;
        String tempWav = null;
        do{
            System.out.print("Enter wav file:");
            tempWav = inputScanner.nextLine();
            File tempFile = new File(tempWav);
            fileCheck = tempFile.exists(); 
            if (tempFile.length() > 4){ 
                typeCheck = tempWav.substring(tempWav.length() - 3, tempWav.length());
            }
            System.out.println(typeCheck);
        } while (fileCheck == false && typeCheck != "wav");
        return tempWav;
    }    
    
    public static float tickToMS(int BPM, int PPQ){ //TMSfactor * event time in ticks = event time in ms
        float temp = BPM * PPQ;
        TMSfactor = 60000 / temp;
        return (TMSfactor);
    }

    public static void main(String[] args) throws Exception {
        //EventQueue.invokeLater(() -> {
            drumPlayer ex = new drumPlayer();
            ex.setVisible(true);
        //});
        String midiFile = null, wavFile = null;
        midiFile = midiInput(midiFile);
        wavFile = wavInput(wavFile);
        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(wavFile));
        Clip sound = AudioSystem.getClip();
        sound.open(ais);
        
        Sequence sequence = MidiSystem.getSequence(new File(midiFile));
        int resolution = sequence.getResolution(); //resolution = # of ticks per quarter note
        int trackNumber = 0;
        
        ArrayList<Integer> noteKey = new ArrayList<>();
        ArrayList<Long> noteKeyTick = new ArrayList<>();
        ArrayList<Float> noteKeyMS = new ArrayList<>();
        ArrayList<Integer> noteBPM = new ArrayList<>();
        ArrayList<Long> noteBPMTick = new ArrayList<>();
        String[] noteName = new String[99];
        noteName[35] = "KICKL";
        noteName[36] = "KICKH";
        noteName[38] = "SNARE";
        noteName[37] = "SS";
        noteName[91] = "RIM";
        noteName[42] = "XH";
        noteName[92] = "HH";
        noteName[46] = "OH";
        noteName[51] = "RIDEM";
        noteName[93] = "RIDEC";
        noteName[53] = "RIDEB";
        noteName[57] = "CRASHA";
        noteName[98] = "CRASHAC";
        noteName[49] = "CRASHB";
        noteName[97] = "CRASHBC";
        noteName[55] = "SPLASH";
        noteName[95] = "SPLASHC";
        noteName[52] = "CHINA";
        noteName[96] = "CHINAC";
        noteName[48] = "TOMA";
        noteName[47] = "TOMB";
        noteName[45] = "TOMC";
        noteName[43] = "TOMD";
        noteName[41] = "TOME";
        for (Track track :  sequence.getTracks()) { //reads through each track of the sequence
            trackNumber++;
            for (int i=0; i < track.size(); i++) { 
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getChannel() == 9){ //if the event is on the drum track
                        if (sm.getCommand() == NOTE_ON) {
                            noteKey.add(sm.getData1());
                            noteKeyTick.add(event.getTick());
                            noteKeyMS.add(event.getTick() * TMSfactor); 
                        }
                    }
                }
                else if (message instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) message;
                    if (mm.getType() == SET_TEMPO) {
                        byte mmData[] = mm.getData();
                        int mspq = ((mmData[0] & 0xff) << 16) | ((mmData[1] & 0xff) << 8) | (mmData[2] & 0xff);
                        int tempo = Math.round(60000001f / mspq);
                        noteBPM.add(tempo);
                        noteBPMTick.add(event.getTick());
                        tickToMS(tempo, resolution);
                    }
                }
            }
        }
        float roundDiff = noteKeyMS.get(0);
        System.out.println(roundDiff);
        sound.start();
        Thread.sleep(Math.round(roundDiff) + 500);
        for (int i = 0; i < noteKey.size(); i++){
            System.out.println(noteName[noteKey.get(i)]);
            if (i != noteKey.size() - 1){
                roundDiff = noteKeyMS.get(i + 1) - noteKeyMS.get(i);
                if (noteKeyMS.get(i) == noteKeyMS.get(i + 1)){
                    roundDiff += 5;
                }
                Thread.sleep(Math.round(roundDiff));
            }
        }
        //try{
            
            

            /*while (!test.isRunning()){
                Thread.sleep(10);
            }
            while (test.isRunning()){
                Thread.sleep(10);
            }
            test.close();*/
        //}catch(Exception ex){
        //    ex.printStackTrace();
        //}
        
    }
}
