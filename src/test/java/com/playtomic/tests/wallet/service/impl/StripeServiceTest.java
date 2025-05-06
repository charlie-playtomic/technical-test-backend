package com.playtomic.tests.wallet.service.impl;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.playtomic.tests.wallet.service.Payment;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeServiceException;
import com.playtomic.tests.wallet.service.StripeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test is failing with the current implementation.
 *
 * How would you test this?
 */
public class StripeServiceTest {

    private WireMockServer wireMockServer;
    private StripeService stripeService;
    private static final int WIRE_MOCK_PORT = 9999;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(WIRE_MOCK_PORT));
        wireMockServer.start();
        WireMock.configureFor("localhost", WIRE_MOCK_PORT);

        URI testUri = URI.create("http://localhost:" + WIRE_MOCK_PORT + "/stripe/charges");
        URI refundUri = URI.create("http://localhost:" + WIRE_MOCK_PORT + "/stripe/refunds");
        stripeService = new StripeService(testUri, refundUri, new RestTemplateBuilder());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void test_exception() {
        // Setup mock for amount too small
        stubFor(post(urlEqualTo("/stripe/charges"))
                .withRequestBody(containing("\"amount\":5"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"error\": \"Amount too small\"}")));

        assertThrows(StripeAmountTooSmallException.class, () -> {
            stripeService.charge("4242 4242 4242 4242", new BigDecimal(5));
        });
    }

    @Test
    void test_ok() throws StripeServiceException {
        // Setup mock for successful charge
        stubFor(post(urlEqualTo("/stripe/charges"))
                .withRequestBody(containing("\"amount\":15"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"id\": \"ch_123456789\"}")));

        Payment payment = stripeService.charge("4242 4242 4242 4242", new BigDecimal(15));
        assertNotNull(payment);
        assertEquals("ch_123456789", payment.getId());
    }

    @Test
    void test_server_error() {
        // Setup mock for server error
        stubFor(post(urlEqualTo("/stripe/charges"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"error\": \"Internal server error\"}")));

        assertThrows(StripeServiceException.class, () -> {
            stripeService.charge("4242 4242 4242 4242", new BigDecimal(20));
        });
    }
}
