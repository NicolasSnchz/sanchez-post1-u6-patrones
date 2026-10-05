package com.tienda.pedidos.service;

import org.springframework.stereotype.Service;

// Implementacion de prueba: imprime el correo en consola en lugar de enviarlo
@Service
public class EmailServiceConsola implements EmailService {

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        System.out.println("=== CORREO ===");
        System.out.println("Para: " + destinatario);
        System.out.println("Asunto: " + asunto);
        System.out.println(cuerpo);
        System.out.println("==============");
    }
}
