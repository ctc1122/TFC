package com.example.pruebamongodbcss.Modulos.AppChat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioReceiverUDP extends Thread {
    private final int puertoEscucha;
    private volatile boolean enLlamada = true;

    public AudioReceiverUDP(int puertoEscucha) {
        this.puertoEscucha = puertoEscucha;
    }

    public void terminar() {
        enLlamada = false;
    }

    @Override
    public void run() {
        try {
            AudioFormat formato = new AudioFormat(44100.0f, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, formato);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Altavoz no compatible");
                return;
            }

            SourceDataLine altavoz = (SourceDataLine) AudioSystem.getLine(info);
            altavoz.open(formato);
            altavoz.start();

            DatagramSocket socket = new DatagramSocket(puertoEscucha);
            byte[] buffer = new byte[4096];
            DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

            while (enLlamada) {
                socket.receive(paquete);
                altavoz.write(paquete.getData(), 0, paquete.getLength());
            }

            altavoz.stop();
            altavoz.close();
            socket.close();
            System.out.println("Llamada terminada (receptor).");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}