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

// Parte 2: campanas CORPORATIVO y VOLUMEN (Black Friday inactiva).
// Las expectativas se fijaron con la version de los tres eslabones de cadena y deben
// mantenerse identicas despues de la correccion a Strategy.
@SpringBootTest(properties = "promo.black-friday.activa=false")
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"},
     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CampanasDescuentoTest {

    private static final double DELTA = 0.001;

    @Autowired
    private GestorPedidos gestorPedidos;

    @Test
    void campana1_clienteConNit_aplicaDescuentoCorporativoDel10PorCiento() {
        // ESTANDAR con NIT: subtotal 200.000 -> 10%: base 180.000, impuesto 34.200 -> total 214.200
        ResultadoPedido r = procesar("CORPORATIVO", 4L, new ItemPedido(1L, 2));

        assertTrue(r.isConfirmado());
        assertEquals(214_200.0, r.getTotal(), DELTA);
    }

    @Test
    void campana2_masDe20Unidades_aplicaDescuentoPorVolumenDel12PorCiento() {
        // ESTANDAR sin NIT, 25 unidades: subtotal 125.000 -> 12%: base 110.000, impuesto 20.900 -> total 130.900
        ResultadoPedido r = procesar("VOLUMEN", 5L, new ItemPedido(4L, 25));

        assertTrue(r.isConfirmado());
        assertEquals(130_900.0, r.getTotal(), DELTA);
    }

    @Test
    void campana3_vipConVolumen_ganaElMayorDescuento() {
        // VIP daria 5% (subtotal 125.000), VOLUMEN da 12% -> se aplica 12%: total 130.900
        ResultadoPedido r = procesar("VIP + VOLUMEN", 1L, new ItemPedido(4L, 25));

        assertTrue(r.isConfirmado());
        assertEquals(130_900.0, r.getTotal(), DELTA);
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
