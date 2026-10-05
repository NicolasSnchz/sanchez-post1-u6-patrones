package com.tienda.pedidos;

import com.tienda.pedidos.dto.ItemPedido;
import com.tienda.pedidos.dto.PedidoRequest;
import com.tienda.pedidos.dto.ResultadoPedido;
import com.tienda.pedidos.service.GestorPedidos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Parte 2: campana BLACK_FRIDAY activa (25% fijo).
@SpringBootTest(properties = "promo.black-friday.activa=true")
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"},
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CampanaBlackFridayTest {

    private static final double DELTA = 0.001;

    @Autowired
    private GestorPedidos gestorPedidos;

    @Test
    void campana4_blackFridayActiva_aplica25PorCiento() {
        // ESTANDAR: subtotal 100.000 -> 25%: base 75.000, impuesto 14.250 -> total 89.250
        ResultadoPedido r = procesar("BLACK_FRIDAY", 5L, new ItemPedido(1L, 1));

        assertTrue(r.isConfirmado());
        assertEquals(89_250.0, r.getTotal(), DELTA);
    }

    @Test
    void campana5_vipDuranteBlackFriday_ganaElMayorDescuento() {
        // VIP daria 15% (subtotal 1.600.000), BLACK_FRIDAY da 25% -> base 1.200.000, impuesto 228.000 -> total 1.428.000
        ResultadoPedido r = procesar("VIP + BLACK_FRIDAY", 1L, new ItemPedido(3L, 2));

        assertTrue(r.isConfirmado());
        assertEquals(1_428_000.0, r.getTotal(), DELTA);
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
}
