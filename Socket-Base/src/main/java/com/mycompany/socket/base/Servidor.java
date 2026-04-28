/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.socket.base;

import java.io.*;
import java.net.*;

/**
 *
 * 
 */

public class Servidor {

    public static void main(String[] args) {
        try {
            ServerSocket s = new ServerSocket(7633);
            Socket recepcion = s.accept();

            BufferedReader input = new BufferedReader(new InputStreamReader(recepcion.getInputStream()));
            PrintWriter output = new PrintWriter(recepcion.getOutputStream(), true);

            boolean done = false;
            
            while (!done) {
                String linea = input.readLine();

                if (linea == null) {
                    done = true;
                } else {
                    // 1. Mostrar el log en el servidor
                    System.out.println("Recibido del cliente: " + linea);

                    // 2. Verificar si el mensaje pide resolver una ecuación
                    if (linea.startsWith("RESOLVE") || linea.startsWith("*RESOLVE")) {

                        // Limpiamos todo el texto extra para que quede solo la matemática
                        String ecuacionLimpia = linea.replace("*RESOLVE", "")
                                                     .replace("RESOLVE", "")
                                                     .replace("\"", "")
                                                     .trim();

                        try {
                            
                            double resultado = evaluarMatematica(ecuacionLimpia);

                            // Enviamos el resultado al cliente
                            output.println("El resultado es: " + resultado);

                        } catch (Exception e) {
                           
                            output.println("Error: La ecuación enviada no es válida.");
                        }

                    }
                    
                    else if (linea.trim().equalsIgnoreCase("SALIR")) {
                        done = true;
                        output.println("Conexión cerrada.");
                    }
                   
                    else {
                        output.println("Servidor recibió: " + linea);
                    }
                }
            }
            recepcion.close();
            
        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
    
    // Método para evaluar strings matemáticos 
public static double evaluarMatematica(final String str) {
    return new Object() {
        int pos = -1, ch;

        void nextChar() {
            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
        }

        boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        double parse() {
            nextChar();
            double x = parseExpression();
            if (pos < str.length()) throw new RuntimeException("Inesperado: " + (char)ch);
            return x;
        }

        double parseExpression() {
            double x = parseTerm();
            for (;;) {
                if      (eat('+')) x += parseTerm(); // Suma
                else if (eat('-')) x -= parseTerm(); // Resta
                else return x;
            }
        }

        double parseTerm() {
            double x = parseFactor();
            for (;;) {
                if      (eat('*')) x *= parseFactor(); // Multiplicación
                else if (eat('/')) x /= parseFactor(); // División
                else return x;
            }
        }

        double parseFactor() {
            if (eat('+')) return parseFactor(); // Unario más
            if (eat('-')) return -parseFactor(); // Unario menos

            double x;
            int startPos = this.pos;
            if (eat('(')) { // Paréntesis
                x = parseExpression();
                eat(')');
            } else if ((ch >= '0' && ch <= '9') || ch == '.') { // Números
                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                x = Double.parseDouble(str.substring(startPos, this.pos));
            } else {
                throw new RuntimeException("Carácter inesperado: " + (char)ch);
            }
            return x;
        }
    }.parse();
}
}
