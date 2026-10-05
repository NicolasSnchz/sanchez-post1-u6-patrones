package com.tienda.pedidos.service;

// Abstraccion del envio de correo para no depender de un servidor SMTP real
public interface EmailService {
    void enviar(String destinatario, String asunto, String cuerpo);
}
