package com.example.pruebamongodbcss.Modulos.AppChat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioSenderUDP extends Thread {
    private final String ipDestino;
    private final int puertoDestino;
    private volatile boolean enLlamada = true;

    public AudioSenderUDP(String ipDestino, int puertoDestino) {
        this.ipDestino = ipDestino;
        this.puertoDestino = puertoDestino;
    }

    public void terminar() {
        enLlamada = false;
    }

    @Override
    public void run() {
        try {
            AudioFormat formato = new AudioFormat(44100.0f, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, formato);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Micrófono no compatible");
                return;
            }

            TargetDataLine microfono = (TargetDataLine) AudioSystem.getLine(info);
            microfono.open(formato);
            microfono.start();

            byte[] buffer = new byte[4096];
            DatagramSocket socket = new DatagramSocket();
            InetAddress destino = InetAddress.getByName(ipDestino);

            while (enLlamada) {
                int bytesLeidos = microfono.read(buffer, 0, buffer.length);
                DatagramPacket paquete = new DatagramPacket(buffer, bytesLeidos, destino, puertoDestino);
                socket.send(paquete);
            }

            microfono.stop();
            microfono.close();
            socket.close();
            System.out.println("Llamada terminada (emisor).");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
