package com.tienda.pedidos;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.service.GestorPedidos;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Parte 1: pedidos de prueba que fijan el comportamiento observable de GestorPedidos.
// Los valores esperados se definieron contra el GestorPedidos original y no cambian
// tras la refactorizacion: si estas pruebas pasan, la salida es equivalente.
@SpringBootTest
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"},
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GestorPedidosTest {

    private static final double DELTA = 0.001;

    @Autowired
    private GestorPedidos gestorPedidos;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void pedido1_stockInsuficiente_seRechaza() {
        ResultadoPedido r = procesar("Stock insuficiente", 1L, new ItemPedido(3L, 10));

        assertFalse(r.isConfirmado());
        assertEquals("Stock insuficiente: producto 3", r.getMotivoRechazo());
    }

    @Test
    void pedido2_clienteInexistente_seRechaza() {
        ResultadoPedido r = procesar("Cliente inexistente", 99L, new ItemPedido(1L, 1));

        assertFalse(r.isConfirmado());
        assertEquals("Cliente no registrado", r.getMotivoRechazo());
    }

    @Test
    void pedido3_clienteMorosoDentroDelHorarioDeCorte_seRechaza() {
        ResultadoPedido r = procesarALas(LocalTime.of(10, 0), "Moroso 10:00", 3L, new ItemPedido(1L, 1));

        assertFalse(r.isConfirmado());
        assertEquals("Cliente con deuda pendiente: $150000.0", r.getMotivoRechazo());
    }

    @Test
    void pedido4_clienteMorosoFueraDelHorarioDeCorte_seConfirmaSinDescuento() {
        // subtotal 200.000, sin descuento, impuesto 38.000 -> total 238.000
        ResultadoPedido r = procesarALas(LocalTime.of(21, 0), "Moroso 21:00", 3L, new ItemPedido(1L, 2));

        assertTrue(r.isConfirmado());
        assertNotNull(r.getPedidoId());
        assertEquals(238_000.0, r.getTotal(), DELTA);
    }

    @Test
    void pedido5_clienteVip_aplicaDescuentoDel10PorCiento() {
        // subtotal 600.000 (> 500.000) -> 10%: base 540.000, impuesto 102.600 -> total 642.600
        ResultadoPedido r = procesar("VIP", 1L, new ItemPedido(1L, 6));

        assertTrue(r.isConfirmado());
        assertEquals(642_600.0, r.getTotal(), DELTA);
        assertEquals(44, stockDe(1L), "el inventario se descuenta al confirmar");
    }

    @Test
    void pedido6_clienteFrecuente_aplicaDescuentoDel4PorCiento() {
        // 5 pedidos previos -> 4%: subtotal 200.000, base 192.000, impuesto 36.480 -> total 228.480
        ResultadoPedido r = procesar("FRECUENTE", 2L, new ItemPedido(2L, 4));

        assertTrue(r.isConfirmado());
        assertEquals(228_480.0, r.getTotal(), DELTA);
    }

    @Test
    void pedido7_sinItems_seRechaza() {
        ResultadoPedido r = procesar("Sin items", 1L);

        assertFalse(r.isConfirmado());
        assertEquals("El pedido no contiene items", r.getMotivoRechazo());
    }

    private ResultadoPedido procesar(String caso, Long clienteId, ItemPedido... items) {
        PedidoRequest request = new PedidoRequest();
        request.setClienteId(clienteId);
        request.setClienteEmail("cliente" + clienteId + "@correo.com");
        request.setItems(List.of(items));
        ResultadoPedido r = gestorPedidos.procesarPedido(request);
        System.out.printf("[%s] confirmado=%s pedidoId=%s total=%.2f motivo=%s%n",
            caso, r.isConfirmado(), r.getPedidoId(), r.getTotal(), r.getMotivoRechazo());
        return r;
    }

    // Fija la hora devuelta por LocalTime.now() para probar ambos lados del horario de corte (20:00)
    private ResultadoPedido procesarALas(LocalTime hora, String caso, Long clienteId, ItemPedido... items) {
        try (MockedStatic<LocalTime> reloj = Mockito.mockStatic(LocalTime.class, Answers.CALLS_REAL_METHODS)) {
            reloj.when(LocalTime::now).thenReturn(hora);
            return procesar(caso, clienteId, items);
        }
    }

    private int stockDe(Long productoId) {
        return jdbcTemplate.queryForObject(
            "SELECT stock FROM inventario WHERE producto_id = ?", Integer.class, productoId);
    }
}
