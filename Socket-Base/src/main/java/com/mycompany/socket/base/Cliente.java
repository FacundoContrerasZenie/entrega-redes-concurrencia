/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.socket.base;

import java.io.*;
import java.net.*;
import java.util.Scanner;
/**
 *
 * @author facun
 */
public class Cliente {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       try{ 
            Socket socket=new Socket("localhost",7633);
            
            BufferedReader input=new BufferedReader( 
            new InputStreamReader(socket.getInputStream())); //recibe las respuestas
            
            PrintWriter output=new PrintWriter( //manda los textos
             socket.getOutputStream(),true); // el parámetro  
    //true sirve para volcar la salida al  
    //dispositivo de salida (autoflush) 
            Scanner teclado = new Scanner(System.in);
            
            boolean salir=false; 
        do { 
            
            String MensajeCliente = teclado.nextLine();
            output.println(MensajeCliente);
            
            
            if (MensajeCliente.equalsIgnoreCase("SALIR")) {
                salir = true;
                continue; // El "continue" hace que salte directo al final del bucle (while)
            }
            
            
             String RespuestaServidor=input.readLine(); 
             if(RespuestaServidor!=null) System.out.println(RespuestaServidor); 
                else salir=true; 
        }while(!salir); 
        }catch(UnknownHostException une){ 
          System.out.println("No se encuentra el servidor"); 
        }catch(IOException une2){ 
            System.out.println("Error en la comunicación"); 
}
    }
    
}
