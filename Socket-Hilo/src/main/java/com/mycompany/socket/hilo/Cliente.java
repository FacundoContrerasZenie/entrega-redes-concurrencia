/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.socket.hilo;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 *
 * @author facun
 */
public class Cliente {

    public static void main(String[] args) {
        try { 
            Socket socket = new Socket("localhost", 7633);
            
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true); 
            Scanner teclado = new Scanner(System.in);
            
            // =========================================================
            // HILO DE ESCUCHA (Recibe mensajes asíncronos del servidor)
            // =========================================================
            Thread hiloLectura = new Thread(() -> {
                try {
                    String respuestaServidor;
                    // Bucle infinito que solo lee y muestra en pantalla
                    while ((respuestaServidor = input.readLine()) != null) {
                        System.out.println(respuestaServidor);
                    }
                } catch (IOException e) {
                    // Cuando cerramos la conexión a propósito, este hilo terminará en silencio
                }
            });
            hiloLectura.start(); // Arrancamos el hilo "oreja"

            // =========================================================
            // BUCLE PRINCIPAL (Lee tu teclado y envía al servidor)
            // =========================================================
            boolean salir = false; 
            
            do { 
                String MensajeCliente = teclado.nextLine();
                output.println(MensajeCliente);
                
                if (MensajeCliente.equalsIgnoreCase("SALIR")) {
                    salir = true;
                }
                
            } while (!salir); 
            
            // Cuando escribimos SALIR, salimos del bucle y cerramos todo
            socket.close();
            
        } catch (UnknownHostException une) { 
            System.out.println("No se encuentra el servidor"); 
        } catch (IOException une2) { 
            System.out.println("Error en la comunicación"); 
        }
    }
}