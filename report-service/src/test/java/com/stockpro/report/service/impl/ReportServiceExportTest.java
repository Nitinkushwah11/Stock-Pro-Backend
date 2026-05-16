package com.stockpro.report.service.impl;

import com.stockpro.report.repository.ReportRepository;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ReportServiceExportTest {

    @Test
    void exportsMovementPdfAndExcelFromMovementServiceData() throws Exception {
        String movementsJson = """
                [
                  {
                    "movementId": 1,
                    "productId": 101,
                    "warehouseId": 5,
                    "movementType": "STOCK_IN",
                    "quantity": 500,
                    "referenceId": 1001,
                    "referenceType": "PURCHASE_ORDER",
                    "unitCost": 10.5,
                    "performedBy": 7,
                    "notes": "Initial inventory receipt",
                    "movementDate": "2026-05-11T15:25:30",
                    "balanceAfter": 500
                  }
                ]
                """;

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/movements", exchange -> {
            byte[] body = movementsJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        try {
            ReportServiceImpl service = new ReportServiceImpl(Mockito.mock(ReportRepository.class));
            ReflectionTestUtils.setField(service, "movementServiceUrl", "http://localhost:" + server.getAddress().getPort());

            byte[] pdf = service.exportMovementReportPdf("Bearer token", null);
            byte[] excel = service.exportMovementReportExcel("Bearer token", null);

            assertThat(pdf).startsWith("%PDF".getBytes(StandardCharsets.UTF_8));
            assertThat(excel).startsWith("PK".getBytes(StandardCharsets.UTF_8));
        } finally {
            server.stop(0);
        }
    }
}
