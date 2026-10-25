package com.shopsphere.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.model.Customer;
import com.shopsphere.model.Order;
import com.shopsphere.model.OrderItem;
import com.shopsphere.model.Product;
import com.shopsphere.repository.CustomerRepository;
import com.shopsphere.repository.OrderRepository;
import com.shopsphere.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        Customer alice = new Customer("user-1", "Alice Vance", "alice@example.com", "STANDARD");
        Customer bob = new Customer("user-2", "Bob Sterling", "bob@prime.com", "PRIME");
        customerRepository.saveAll(List.of(alice, bob));

        Product p1 = new Product("prod-1", "Wireless Noise-Cancelling Headphones", "Electronics", 120.00, 15, true);
        Product p2 = new Product("prod-2", "Mechanical Gaming Keyboard", "Electronics", 80.00, 10, true);
        Product p3 = new Product("prod-3", "Ergonomic Office Chair", "Furniture", 250.00, 5, true);
        Product p4 = new Product("prod-4", "Clean Architecture Book", "Books", 40.00, 20, true);
        Product p5 = new Product("prod-5", "USB-C Hub Adapter", "Accessories", 25.00, 2, true);
        Product p6 = new Product("prod-6", "Legacy Discontinued Monitor", "Electronics", 199.99, 8, false);
        productRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6));

        seedHistoricalOrders();
    }

    private void seedHistoricalOrders() {
        Customer alice = customerRepository.findById("user-1").orElseThrow();
        Customer bob = customerRepository.findById("user-2").orElseThrow();

        OrderItem item101a = new OrderItem(null, "prod-1", "Wireless Noise-Cancelling Headphones", "Electronics", 120.00, 1, 120.00);
        OrderItem item101b = new OrderItem(null, "prod-4", "Clean Architecture Book", "Books", 40.00, 1, 40.00);
        Order ord101 = new Order(null, "ord-101", alice, List.of(item101a, item101b),
                160.00, 0.00, 0.00, 12.80, 172.80, null,
                "STANDARD", "742 Evergreen Terrace, Springfield",
                "Standard delivery to 742 Evergreen Terrace, Springfield",
                "CONFIRMED", Instant.parse("2026-01-10T10:00:00Z"), Instant.parse("2026-01-10T10:00:00Z"));
        orderRepository.save(ord101);

        OrderItem item102 = new OrderItem(null, "prod-3", "Ergonomic Office Chair", "Furniture", 250.00, 1, 250.00);
        Order ord102 = new Order(null, "ord-102", bob, List.of(item102),
                250.00, 0.00, 15.00, 20.00, 285.00, null,
                "EXPRESS", "100 Main St, Seattle", "Express delivery to 100 Main St, Seattle",
                "SHIPPED", Instant.parse("2026-01-12T12:00:00Z"), Instant.parse("2026-01-12T12:00:00Z"));
        orderRepository.save(ord102);

        OrderItem item103 = new OrderItem(null, "prod-2", "Mechanical Gaming Keyboard", "Electronics", 80.00, 2, 160.00);
        Order ord103 = new Order(null, "ord-103", alice, List.of(item103),
                160.00, 0.00, 0.00, 12.80, 172.80, null,
                "STANDARD", "742 Evergreen Terrace, Springfield",
                "Standard delivery to 742 Evergreen Terrace, Springfield",
                "DELIVERED", Instant.parse("2026-01-15T15:30:00Z"), Instant.parse("2026-01-15T15:30:00Z"));
        orderRepository.save(ord103);

        OrderItem item104 = new OrderItem(null, "prod-5", "USB-C Hub Adapter", "Accessories", 25.00, 1, 25.00);
        Order ord104 = new Order(null, "ord-104", bob, List.of(item104),
                25.00, 0.00, 0.00, 2.00, 27.00, null,
                "STANDARD", "500 Pine Ave, Austin", "Standard delivery to 500 Pine Ave, Austin",
                "CONFIRMED", Instant.parse("2026-01-18T09:00:00Z"), Instant.parse("2026-01-18T09:00:00Z"));
        orderRepository.save(ord104);

        OrderItem item105 = new OrderItem(null, "prod-1", "Wireless Noise-Cancelling Headphones", "Electronics", 120.00, 1, 120.00);
        Order ord105 = new Order(null, "ord-105", alice, List.of(item105),
                120.00, 0.00, 0.00, 9.60, 129.60, null,
                "STANDARD", "742 Evergreen Terrace, Springfield",
                "Standard delivery to 742 Evergreen Terrace, Springfield",
                "CANCELLED", Instant.parse("2026-01-20T11:00:00Z"), Instant.parse("2026-01-20T11:00:00Z"));
        orderRepository.save(ord105);
    }

    // ─── Test 1 ────────────────────────────────────────────────────────────────

    @Test
    void testCreateOrder_BasicStandardShippingAndAutoNotes() throws Exception {
        String body = """
                {
                  "items": [{"productId": "prod-4", "quantity": 2}],
                  "shippingAddress": "1 Test Lane",
                  "shippingMethod": "STANDARD"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("status").asText()).isEqualTo("CONFIRMED");
        assertThat(json.get("subtotal").asDouble()).isEqualTo(80.00);
        assertThat(json.get("shippingFee").asDouble()).isEqualTo(7.99);
        assertThat(json.get("taxAmount").asDouble()).isEqualTo(6.40);
        assertThat(json.get("totalAmount").asDouble()).isEqualTo(94.39);
        assertThat(json.get("discountAmount").asDouble()).isEqualTo(0.00);
        assertThat(json.get("notes").asText()).isEqualTo("Standard delivery to 1 Test Lane");
        assertThat(json.get("_id").asText()).isNotBlank();
        assertThat(json.get("orderNumber").asText()).isNotBlank();
    }

    // ─── Test 2 ────────────────────────────────────────────────────────────────

    @Test
    void testCreateOrder_FreeStandardShippingOver100AndStockDeducted() throws Exception {
        String body = """
                {
                  "items": [{"productId": "prod-1", "quantity": 1}],
                  "shippingAddress": "99 Cloud Ave"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("subtotal").asDouble()).isEqualTo(120.00);
        assertThat(json.get("shippingFee").asDouble()).isEqualTo(0.00);
        assertThat(json.get("taxAmount").asDouble()).isEqualTo(9.60);
        assertThat(json.get("totalAmount").asDouble()).isEqualTo(129.60);

        Product updatedProduct = productRepository.findById("prod-1").orElseThrow();
        assertThat(updatedProduct.getStock()).isEqualTo(14);
    }

    // ─── Test 3 ────────────────────────────────────────────────────────────────

    @Test
    void testCreateOrder_PrimeCustomerGetsFreeStandardShippingUnder100() throws Exception {
        String body = """
                {
                  "items": [{"productId": "prod-4", "quantity": 1}],
                  "shippingAddress": "Prime Tower, NYC"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("subtotal").asDouble()).isEqualTo(40.00);
        assertThat(json.get("shippingFee").asDouble()).isEqualTo(0.00);
        assertThat(json.get("taxAmount").asDouble()).isEqualTo(3.20);
        assertThat(json.get("totalAmount").asDouble()).isEqualTo(43.20);
        assertThat(json.get("customer").get("tier").asText()).isEqualTo("PRIME");
    }

    // ─── Test 4 ────────────────────────────────────────────────────────────────

    @Test
    void testCreateOrder_PromoCodeSave10AndVip20AndFlat15() throws Exception {
        // SAVE10 on $160 subtotal → discount=$16, tax on (160-16)=$144 → tax=11.52
        String bodySave10 = """
                {
                  "items": [{"productId": "prod-2", "quantity": 2}],
                  "shippingAddress": "5 Promo Blvd",
                  "promoCode": "save10"
                }
                """;
        MvcResult r1 = mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodySave10))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode j1 = objectMapper.readTree(r1.getResponse().getContentAsString());
        assertThat(j1.get("subtotal").asDouble()).isEqualTo(160.00);
        assertThat(j1.get("discountAmount").asDouble()).isEqualTo(16.00);
        assertThat(j1.get("taxAmount").asDouble()).isEqualTo(11.52);
        assertThat(j1.get("shippingFee").asDouble()).isEqualTo(0.00);
        assertThat(j1.get("totalAmount").asDouble()).isEqualTo(155.52);
        assertThat(j1.get("promoCode").asText()).isEqualTo("SAVE10");

        // VIP20 on $40 subtotal (below $100 threshold) → discount=0
        String bodyVip20Low = """
                {
                  "items": [{"productId": "prod-4", "quantity": 1}],
                  "shippingAddress": "5 Promo Blvd",
                  "promoCode": "VIP20"
                }
                """;
        MvcResult r2 = mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyVip20Low))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode j2 = objectMapper.readTree(r2.getResponse().getContentAsString());
        assertThat(j2.get("discountAmount").asDouble()).isEqualTo(0.00);
        assertThat(j2.get("promoCode").asText()).isEqualTo("VIP20");

        // FLAT15 on $80 subtotal → discount=$15, tax on (80-15)=$65 → tax=5.20, shipping $7.99
        String bodyFlat15 = """
                {
                  "items": [{"productId": "prod-2", "quantity": 1}],
                  "shippingAddress": "5 Promo Blvd",
                  "promoCode": "FLAT15"
                }
                """;
        MvcResult r3 = mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyFlat15))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode j3 = objectMapper.readTree(r3.getResponse().getContentAsString());
        assertThat(j3.get("subtotal").asDouble()).isEqualTo(80.00);
        assertThat(j3.get("discountAmount").asDouble()).isEqualTo(15.00);
        assertThat(j3.get("taxAmount").asDouble()).isEqualTo(5.20);
        assertThat(j3.get("shippingFee").asDouble()).isEqualTo(7.99);
        assertThat(j3.get("totalAmount").asDouble()).isEqualTo(78.19);
    }

    // ─── Test 5 ────────────────────────────────────────────────────────────────

    @Test
    void testCreateOrder_ExpressShippingFee() throws Exception {
        String body = """
                {
                  "items": [{"productId": "prod-3", "quantity": 1}],
                  "shippingAddress": "Fast Lane Rd",
                  "shippingMethod": "express"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("shippingMethod").asText()).isEqualTo("EXPRESS");
        assertThat(json.get("shippingFee").asDouble()).isEqualTo(15.00);
        assertThat(json.get("subtotal").asDouble()).isEqualTo(250.00);
        assertThat(json.get("taxAmount").asDouble()).isEqualTo(20.00);
        assertThat(json.get("totalAmount").asDouble()).isEqualTo(285.00);
        assertThat(json.get("notes").asText()).isEqualTo("Express delivery to Fast Lane Rd");
    }

    // ─── Test 6 ────────────────────────────────────────────────────────────────

    @Test
    void testCreateOrder_ValidationErrors() throws Exception {
        // Empty items list
        mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": [], \"shippingAddress\": \"Anywhere St\"}"))
                .andExpect(status().isBadRequest());

        // Quantity exceeds stock (prod-5 has stock=2)
        mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": [{\"productId\": \"prod-5\", \"quantity\": 10}], \"shippingAddress\": \"Anywhere St\"}"))
                .andExpect(status().isBadRequest());

        // Inactive product
        mockMvc.perform(post("/api/orders")
                        .header("x-user-id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": [{\"productId\": \"prod-6\", \"quantity\": 1}], \"shippingAddress\": \"Anywhere St\"}"))
                .andExpect(status().isBadRequest());

        // Missing x-user-id header
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\": [{\"productId\": \"prod-4\", \"quantity\": 1}], \"shippingAddress\": \"Somewhere\"}"))
                .andExpect(status().isBadRequest());
    }

    // ─── Test 7 ────────────────────────────────────────────────────────────────

    @Test
    void testSearchOrders_DefaultExcludesCancelledAndSortsByTotalDescThenCreatedAtDesc() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orders/search"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(4);

        List<String> orderNumbers = new java.util.ArrayList<>();
        for (JsonNode node : json) {
            orderNumbers.add(node.get("orderNumber").asText());
            assertThat(node.get("status").asText())
                    .isNotIn("CANCELLED", "ARCHIVED");
        }

        assertThat(orderNumbers.get(0)).isEqualTo("ord-102");
        assertThat(orderNumbers.get(1)).isEqualTo("ord-103");
        assertThat(orderNumbers.get(2)).isEqualTo("ord-101");
        assertThat(orderNumbers.get(3)).isEqualTo("ord-104");
    }

    // ─── Test 8 ────────────────────────────────────────────────────────────────

    @Test
    void testSearchOrders_BasicQueryByCustomerNameAndEmail() throws Exception {
        MvcResult r1 = mockMvc.perform(get("/api/orders/search")
                        .param("q", "alice")
                        .param("type", "customer"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode j1 = objectMapper.readTree(r1.getResponse().getContentAsString());
        assertThat(j1.isArray()).isTrue();
        assertThat(j1.size()).isEqualTo(3);
        for (JsonNode n : j1) {
            assertThat(n.get("customer").get("name").asText().toLowerCase()).contains("alice");
        }

        MvcResult r2 = mockMvc.perform(get("/api/orders/search")
                        .param("q", "prime.com")
                        .param("type", "customer"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode j2 = objectMapper.readTree(r2.getResponse().getContentAsString());
        assertThat(j2.isArray()).isTrue();
        assertThat(j2.size()).isEqualTo(2);
        for (JsonNode n : j2) {
            assertThat(n.get("customer").get("email").asText().toLowerCase()).contains("prime.com");
        }
    }

    // ─── Test 9 ────────────────────────────────────────────────────────────────

    @Test
    void testSearchOrders_BasicQueryByProductName() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orders/search")
                        .param("q", "keyboard")
                        .param("type", "product"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(1);
        assertThat(json.get(0).get("orderNumber").asText()).isEqualTo("ord-103");
    }

    // ─── Test 10 ───────────────────────────────────────────────────────────────

    @Test
    void testSearchOrders_BasicQueryTypeAll() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orders/search")
                        .param("q", "evergreen"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(2);
        for (JsonNode node : json) {
            assertThat(node.get("shippingAddress").asText().toLowerCase()).contains("evergreen");
            assertThat(node.get("status").asText()).isNotIn("CANCELLED", "ARCHIVED");
        }
    }

    // ─── Test 11 ───────────────────────────────────────────────────────────────

    @Test
    void testSearchOrders_MultipleStatusesCommaSeparated() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orders/search")
                        .param("status", "CONFIRMED,SHIPPED"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(3);
        List<String> orderNums = new java.util.ArrayList<>();
        for (JsonNode n : json) {
            orderNums.add(n.get("orderNumber").asText());
            assertThat(n.get("status").asText()).isIn("CONFIRMED", "SHIPPED");
        }
        assertThat(orderNums).containsExactlyInAnyOrder("ord-101", "ord-102", "ord-104");
    }

    // ─── Test 12 ───────────────────────────────────────────────────────────────

    @Test
    void testSearchOrders_ExplicitCancelledStatusReturnsCancelled() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orders/search")
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(1);
        assertThat(json.get(0).get("orderNumber").asText()).isEqualTo("ord-105");
        assertThat(json.get(0).get("status").asText()).isEqualTo("CANCELLED");
    }

    // ─── Test 13 ───────────────────────────────────────────────────────────────

    @Test
    void testSearchOrders_TotalAmountRangeWithDecimals() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orders/search")
                        .param("minTotal", "150.0")
                        .param("maxTotal", "200.0"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(2);
        List<String> orderNums = new java.util.ArrayList<>();
        for (JsonNode n : json) {
            orderNums.add(n.get("orderNumber").asText());
            double total = n.get("totalAmount").asDouble();
            assertThat(total).isBetween(150.0, 200.0);
        }
        assertThat(orderNums).containsExactlyInAnyOrder("ord-101", "ord-103");
    }

    // ─── Test 14 ───────────────────────────────────────────────────────────────

    @Test
    void testSearchOrders_CombinedCategoryAndShippingMethodAndMinTotal() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/orders/search")
                        .param("category", "Electronics,Furniture")
                        .param("shippingMethod", "STANDARD")
                        .param("minTotal", "100"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isEqualTo(2);
        List<String> orderNums = new java.util.ArrayList<>();
        for (JsonNode n : json) {
            orderNums.add(n.get("orderNumber").asText());
            assertThat(n.get("shippingMethod").asText()).isEqualTo("STANDARD");
            double total = n.get("totalAmount").asDouble();
            assertThat(total).isGreaterThanOrEqualTo(100.0);
        }
        assertThat(orderNums).containsExactlyInAnyOrder("ord-101", "ord-103");
    }
}
