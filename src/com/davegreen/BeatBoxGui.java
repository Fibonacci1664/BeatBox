package com.davegreen;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class BeatBoxGui
{
    private JFrame mainFrame;
    private BorderLayout layout;
    private JPanel mainPanel;
    private JPanel background;
    private JButton buttonStart;
    private JButton buttonStop;
    private JButton upTempo;
    private JButton downTempo;
    private JButton saveMe;
    private JButton loadMe;
    private ArrayList<JCheckBox> checkBoxArrayList;
    //private JCheckBox c;
    private Box buttonBox;
    private Box nameBox;
    //private GridLayout grid;
    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;
    private String[] instrumentNames;
    private int[] instruments;
    
    public BeatBoxGui()
    {
        this.mainFrame = new JFrame("Cyber BeatBox");
        this.layout = new BorderLayout();
        //this.mainPanel = new JPanel(grid);
        this.background = new JPanel(layout);
        this.buttonStart = new JButton("Start");
        this.buttonStop = new JButton("Stop");
        this.upTempo = new JButton("Increase Tempo");
        this.downTempo = new JButton("Decrease Tempo");
        this.saveMe = new JButton("Save Me");
        this.loadMe = new JButton("Load Me");
        this.checkBoxArrayList = new ArrayList<>();
        //this.c = new JCheckBox();
        this.buttonBox = new Box(BoxLayout.Y_AXIS);
        this.nameBox = new Box(BoxLayout.Y_AXIS);
        //this.grid = new GridLayout(16, 16);
        this.instrumentNames = new String[] {"Base Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas",
                                         "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open High Conga"};
        this.instruments =  new int[] {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};
    }
    
    public void buildGUI()
    {
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // START BUTTON
        buttonStart.addActionListener(new MyStartListener());
        buttonBox.add(buttonStart);
        
        // STOP BUTTON
        buttonStop.addActionListener(new MyStopListener());
        buttonBox.add(buttonStop);
        
        // INCREASE TEMPO BUTTON
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);
        
        // DECREASE TEMPO BUTTON
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);
        
        // SAVE PATTERN BUTTON
        saveMe.addActionListener(new MySendListener());
        buttonBox.add(saveMe);
        
        // LOAD PATTERN BUTTON
        loadMe.addActionListener(new MyReadListener());
        buttonBox.add(loadMe);
        
        for(int i = 0; i < 16; i ++)
        {
            nameBox.add(new Label(instrumentNames[i]));
        }
        
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        
        mainFrame.getContentPane().add(background);
        
        GridLayout grid = new GridLayout(16, 16);
        
        grid.setVgap(1);
        grid.setHgap(2);
        
        mainPanel = new JPanel(grid);
        
        background.add(BorderLayout.CENTER, mainPanel);
        
        for(int i = 0; i < 256; i ++)
        {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxArrayList.add(c);
            mainPanel.add(c);
        }
        
        setUpMidi();
        
        mainFrame.setBounds(50, 50, 300, 300);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
    
    public void setUpMidi()
    {
        try
        {
            this.sequencer = MidiSystem.getSequencer();
            sequencer.open();
            this.sequence = new Sequence(Sequence.PPQ, 4);
            this.track = sequence.createTrack();
            this.sequencer.setTempoInBPM(120);
        
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void buildTrackAndStart()
    {
        int[] trackList = null;
        
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        
        for(int i = 0; i < 16; i ++)
        {
            trackList = new int[16];
            
            int key = instruments[i];
            
            for(int j = 0; j < 16; j ++)
            {
                JCheckBox jCheckBox = checkBoxArrayList.get(j + 16 * i);
                
                if(jCheckBox.isSelected())
                {
                    trackList[j] = key;
                }
                else
                {
                    trackList[j] = 0;
                }
            }
            
            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }
        
        track.add(makeEvent(192, 9, 1, 0, 15));
        
        try
        {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick)
    {
        MidiEvent event = null;
        
        try
        {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return event;
    }
    
    public void makeTracks(int[] list)
    {
        for(int i = 0; i < 16; i ++)
        {
            int key = list[i];
            
            if(key != 0)
            {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }
    
    public class MyStartListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            buildTrackAndStart();
        }
    }
    
    public class MyStopListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            sequencer.stop();
        }
    }
    
    public class MyUpTempoListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }
    }
    
    public class MyDownTempoListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * .97));
        }
    }
    
    // This class writes/saves the checkbox pattern
    public class MySendListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            boolean[] checkboxState = new boolean[256];
            
            for(int i = 0; i < 256; i ++)
            {
                JCheckBox check = (JCheckBox) checkBoxArrayList.get(i);
                if(check.isSelected())
                {
                    checkboxState[i] = true;
                }
            }
    
            ObjectOutputStream oos;
            
            try (FileOutputStream fileStream = new FileOutputStream(new File("Checkbox.ser")))
            {
                oos = new ObjectOutputStream(fileStream);
                oos.writeObject(checkboxState);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    // This class reads/loads the checkbox pattern
    public class MyReadListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            boolean[] checkboxState = null;
            ObjectInputStream ois;
            
            try (FileInputStream fileIn = new FileInputStream(new File("Checkbox.ser")))
            {
                ois = new ObjectInputStream(fileIn);
                checkboxState = (boolean[]) ois.readObject();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            
            
            for(int i = 0; i < 256; i ++)
            {
                JCheckBox check = (JCheckBox) checkBoxArrayList.get(i);
                
                if(checkboxState[i])
                {
                    check.setSelected(true);
                }
                else
                {
                    check.setSelected(false);
                }
            }
            
            sequencer.stop();
            buildTrackAndStart();
        }
    }
}
