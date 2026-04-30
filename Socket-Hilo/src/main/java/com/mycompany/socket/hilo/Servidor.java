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