/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.socket.hilo;


import java.io.*;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class HiloCliente extends Thread {
    
    private Socket socket;
    private String miNombre; // El nombre que le asignaremos al usuario
    private PrintWriter out;
    private BufferedReader input;

    // Constructor: recibe el socket que le mandó el Jefe (Servidor)
    public HiloCliente(Socket socket) {
        this.socket = socket;
    }

    // Aquí es donde ocurre la magia paralela
    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // =========================================================
            // FASE 1: REGISTRO OBLIGATORIO (HELLO)
            // =========================================================
            out.println("Servidor: Conectado. Por favor, identifícate usando el comando: HELLO \"TuNombre\"");
            
            boolean registrado = false;
            while (!registrado) {
                String primeraLinea = input.readLine();
                if (primeraLinea == null) return; // Si el cliente cierra de golpe, salimos del hilo
                
                if (primeraLinea.startsWith("HELLO ")) {
                    // Extraemos el nombre limpiando el comando y las comillas
                    String nombreDeseado = primeraLinea.substring(6).replace("\"", "").trim();
                    if (!nombreDeseado.isEmpty()) {
                        // Llamamos a la función para verificar duplicados y asignarlo
                        miNombre = registrarNombreUnico(nombreDeseado, out);
                        registrado = true;
                    } else {
                        out.println("Servidor Error: El nombre no puede estar vacío. Usa: HELLO \"TuNombre\"");
                    }
                } else {
                    out.println("Servidor: Debes registrarte primero. Usa: HELLO \"TuNombre\"");
                }
            }

            // =========================================================
            // FASE 2: BUCLE PRINCIPAL DE CHAT Y COMANDOS
            // =========================================================
            boolean done = false;
            
            while (!done) {
                String linea = input.readLine();

                if (linea == null) {
                    done = true;
                } else {
                    // 1. El servidor loguea TODO en su propia consola
                    System.out.println("LOG [" + miNombre + "]: " + linea);

                    // 2. Procesamiento del Cerebro (Enrutamiento)
                    if (linea.equals("DATETIME")) {
                        out.println("Servidor: " + obtenerFechaHora());

                    } else if (linea.startsWith("RESOLVE") || linea.startsWith("*RESOLVE")) {
                        String ecuacion = linea.replace("*RESOLVE", "").replace("RESOLVE", "").replace("\"", "").trim();
                        try {
                            // Llama al método estático que está en tu clase Servidor principal
                            double resultado = Servidor.evaluarMatematica(ecuacion); 
                            out.println("Servidor: El resultado es " + resultado);
                        } catch (Exception e) {
                            out.println("Servidor Error: Expresión matemática inválida.");
                        }

                    } else if (linea.equals("LIST")) {
                        out.println("Servidor: Clientes conectados: " + String.join(", ", Servidor.clientesConectados.keySet()));

                    } else if (linea.startsWith("*ALL ")) {
                        String mensaje = linea.substring(5).replace("\"", "").trim();
                        transmitirATodos(miNombre, mensaje);

                    } else if (linea.startsWith("*")) {
                        // Lógica para mensajes privados a 1 o varios clientes (Ej: *C1 "Hola")
                        manejarMensajePrivado(miNombre, linea, out);

                    } else if (linea.equals("HELP")) {
                        mostrarMenuAyuda(out);

                    } else if (linea.equalsIgnoreCase("SALIR")) {
                        out.println("Servidor: Desconectando... ¡Adiós!");
                        done = true; // Rompe el ciclo while para ir a la limpieza

                    } else {
                        out.println("Servidor: Comando no reconocido. Escribe HELP para ver la lista de comandos.");
                    }
                }
            }
            
            // =========================================================
            // FASE 3: LIMPIEZA AL DESCONECTARSE
            // =========================================================
            desconectarUsuario();

        } catch (IOException e) {
            System.out.println("Error con un cliente (" + miNombre + "): " + e.getMessage());
            desconectarUsuario();
        }
    }
    
    // =========================================================
    // FASE 4: FUNCIONES DE SOPORTE DEL HILO
    // =========================================================

    private void desconectarUsuario() {
        if (miNombre != null && Servidor.clientesConectados.containsKey(miNombre)) {
            Servidor.clientesConectados.remove(miNombre);
            System.out.println("Log: Usuario " + miNombre + " se ha desconectado.");
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar el socket: " + e.getMessage());
        }
    }

    private String registrarNombreUnico(String nombreDeseado, PrintWriter outCliente) {
        String nombreFinal = nombreDeseado;
        int contador = 1;
        
        // Mientras el nombre ya exista en el mapa global del Servidor, le sumamos un número
        while (Servidor.clientesConectados.containsKey(nombreFinal)) {
            nombreFinal = nombreDeseado + contador;
            contador++;
        }
        
        // Lo guardamos en el diccionario global de la clase Servidor
        Servidor.clientesConectados.put(nombreFinal, outCliente);
        
        // Le avisamos al cliente cómo quedó bautizado
        outCliente.println("Servidor: ¡Bienvenido! Tu nombre de usuario asignado es: " + nombreFinal);
        mostrarMenuAyuda(outCliente);
        
        return nombreFinal;
    }

    private void manejarMensajePrivado(String remitente, String linea, PrintWriter outRemitente) {
        try {
            // Separamos por el primer espacio. Ej: ["*Facu,C1", "\"Hola como estas\""]
            int primerEspacio = linea.indexOf(" ");
            if (primerEspacio == -1) throw new Exception();

            // Limpiamos los destinatarios (quitamos el asterisco inicial)
            String destinosCrudos = linea.substring(1, primerEspacio); 
            String[] destinos = destinosCrudos.split(","); // Soporta 1, 2 o más separados por coma
            
            // Limpiamos el mensaje
            String mensaje = linea.substring(primerEspacio + 1).replace("\"", "").trim();

            for (String destinatario : destinos) {
                destinatario = destinatario.trim();
                PrintWriter outDestino = Servidor.clientesConectados.get(destinatario);
                
                if (outDestino != null) {
                    outDestino.println("(Privado) " + remitente + ": " + mensaje);
                } else {
                    outRemitente.println("Servidor Error: El usuario '" + destinatario + "' no existe o no está conectado.");
                }
            }
        } catch (Exception e) {
            outRemitente.println("Servidor Error: Formato incorrecto. Usa: *Usuario \"Mensaje\" o *Usu1,Usu2 \"Mensaje\"");
        }
    }

    private void transmitirATodos(String remitente, String mensaje) {
        // Iteramos sobre todos los conectados en la memoria central
        for (Map.Entry<String, PrintWriter> entrada : Servidor.clientesConectados.entrySet()) {
            String nombre = entrada.getKey();
            PrintWriter outDestino = entrada.getValue();
            
            // Enviamos a todos MENOS al que originó el mensaje
            if (!nombre.equals(remitente)) {
                outDestino.println("(Global) " + remitente + ": " + mensaje);
            }
        }
    }

    private String obtenerFechaHora() {
        ZonedDateTime ahora = ZonedDateTime.now(ZoneId.of("America/Argentina/Mendoza"));
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return ahora.format(formato);
    }

    private void mostrarMenuAyuda(PrintWriter out) {
        out.println("--- COMANDOS DISPONIBLES ---");
        out.println("1. DATETIME : Muestra fecha y hora actual");
        out.println("2. RESOLVE \"ecuacion\" : Resuelve matemática (Ej: RESOLVE \"2+2\")");
        out.println("3. LIST : Lista usuarios conectados");
        out.println("4. *ALL \"mensaje\" : Envia mensaje a todos");
        out.println("5. *Nombre \"mensaje\" : Mensaje privado a un usuario");
        out.println("6. *Nom1,Nom2 \"mensaje\" : Mensaje privado a varios");
        out.println("7. SALIR : Desconecta del servidor");
        out.println("----------------------------");
    }
}