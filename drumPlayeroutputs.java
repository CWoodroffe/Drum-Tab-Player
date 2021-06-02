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

public class drumPlayeroutputs {
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
    
    //MIDI codes for drums
    public static final int KICKL = 35;
    public static final int KICKH = 36;
    public static final int SNARE = 38;
    public static final int SS = 37;
    public static final int RIM = 91;
    public static final int XH = 42;
    public static final int HH = 92;
    public static final int OH = 46;
    public static final int RIDEM = 51;
    public static final int RIDEC = 93;
    public static final int RIDEB = 53;
    public static final int CRASHA = 57;
    public static final int CRASHAC = 98;
    public static final int CRASHB = 49;
    public static final int CRASHBC = 97;
    public static final int SPLASH = 55;
    public static final int SPLASHC = 95;
    public static final int CHINA = 52;
    public static final int CHINAC = 96;
    public static final int TOMA = 48;
    public static final int TOMB = 47;
    public static final int TOMC = 45;
    public static final int TOMD = 43;
    public static final int TOME = 41;
    
    public static float TMSfactor;    
    
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
        //System.out.println("temp =" + temp);
        TMSfactor = 60000 / temp;
        //System.out.println("factor =" + TMSfactor);
        //System.out.println("one tick per " + TMSfactor + " milliseconds");
        return (TMSfactor);
    }

    public static void main(String[] args) throws Exception {
        String midiFile = null, wavFile = null;
        midiFile = midiInput(midiFile);
        wavFile = wavInput(wavFile);
        Sequence sequence = MidiSystem.getSequence(new File(midiFile));
        int resolution = sequence.getResolution(); //resolution = # of ticks per quarter note
        System.out.println("ppq is " + resolution);
        int trackNumber = 0;
        
        ArrayList<Integer> noteKey = new ArrayList<>();
        ArrayList<Long> noteKeyTick = new ArrayList<>();
        ArrayList<Float> noteKeyMS = new ArrayList<>();
        ArrayList<Integer> noteBPM = new ArrayList<>();
        ArrayList<Long> noteBPMTick = new ArrayList<>();
        for (Track track :  sequence.getTracks()) { //reads through each track of the sequence
            trackNumber++;
            //System.out.println("Track " + trackNumber + ": size = " + track.size());
            //System.out.println();
            for (int i=0; i < track.size(); i++) { 
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    //System.out.print ("the type of short message is " + sm.getCommand() + " ");
                    if (sm.getChannel() == 9){ //if the event is on the drum track
                        if (sm.getCommand() == NOTE_ON) {
                            //System.out.print("Channel: " + sm.getChannel() + " ");
                            //System.out.print("@" + event.getTick() + " ");
                            //System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                            
                            noteKey.add(sm.getData1());
                            noteKeyTick.add(event.getTick());
                            //System.out.println("getTick * TMSfactor = " + event.getTick() + " * " + TMSfactor + " = " + (event.getTick() * TMSfactor));
                            noteKeyMS.add(event.getTick() * TMSfactor); 
                        } else {
                            //System.out.println("***Command:" + sm.getCommand());
                        }
                    }
                }
                else if (message instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) message;
                    //System.out.print ("the meta message is " + mm + ", ");
                    //System.out.print ("the type of meta message is " + mm.getType());
                    //System.out.println(" and the data is " + mmData);
                    if (mm.getType() == SET_TEMPO) {
                        byte mmData[] = mm.getData();
                        int mspq = ((mmData[0] & 0xff) << 16) | ((mmData[1] & 0xff) << 8) | (mmData[2] & 0xff);
                        int tempo = Math.round(60000001f / mspq);
                        //System.out.println("the tempo at @" + event.getTick() + " is " + tempo);
                        noteBPM.add(tempo);
                        noteBPMTick.add(event.getTick());
                        tickToMS(tempo, resolution);
                    }
                }
                else {
                    //System.out.println("###Other message: " + message.getClass());
                }
            }
            System.out.println();
        }
        for (int i = 0; i < noteBPM.size(); i++){
            //System.out.println(noteBPM.get(i) + " BPM at " + noteBPMTick.get(i) + " or " + (noteBPMTick.get(i) * TMSfactor) + " milliseconds");
        }
        for (int i = 0; i < noteKey.size(); i++){
            //System.out.println(noteKey.get(i) + " at " + noteKeyTick.get(i) + " or " + noteKeyMS.get(i) + " milliseconds");
        }
        
    
        

        //try{
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(wavFile));
            Clip test = AudioSystem.getClip();  

            test.open(ais);
            test.start();

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
