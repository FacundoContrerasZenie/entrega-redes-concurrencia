/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.socket.hilo;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor {
    
    // La memoria central donde guardamos a todos los clientes
    public static ConcurrentHashMap<String, PrintWriter> clientesConectados = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket s = new ServerSocket(7633);
            System.out.println("Servidor Multihilo iniciado en el puerto 7633...");

            // El Jefe: Un bucle infinito que SOLO acepta conexiones y crea hilos
            while (true) {
                Socket recepcion = s.accept();
                System.out.println("Nueva conexión entrante...");
                
                // Creamos un hilo para este cliente y lo iniciamos
                HiloCliente trabajador = new HiloCliente(recepcion);
                trabajador.start(); 
            }
            
        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
    
   
}