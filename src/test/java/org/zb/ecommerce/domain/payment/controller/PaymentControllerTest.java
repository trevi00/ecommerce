package org.zb.ecommerce.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.zb.ecommerce.domain.payment.dto.PaymentRequest;
import org.zb.ecommerce.domain.payment.dto.PaymentResponse;
import org.zb.ecommerce.domain.payment.entity.Payment;
import org.zb.ecommerce.domain.payment.entity.PaymentStatus;
import org.zb.ecommerce.domain.payment.service.PaymentService;
import org.zb.ecommerce.global.auth.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentRequest createPaymentRequest() {
        return new PaymentRequest(1L, "CREDIT_CARD", BigDecimal.valueOf(1000.00));
    }

    private PaymentResponse createPaymentResponse() {
        return PaymentResponse.from(createPayment());
    }

    private Payment createPayment() {
        return Payment.builder()
                .orderId(1L)
                .paymentMethod("CREDIT_CARD")
                .amount(BigDecimal.valueOf(1000.00))
                .build();
    }

    @Test
    @DisplayName("결제 생성 - 성공")
    void createPayment_Success() throws Exception {
        PaymentRequest request = createPaymentRequest();
        PaymentResponse response = createPaymentResponse();

        when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.orderId").value(response.orderId()))
                .andExpect(jsonPath("$.paymentMethod").value(response.paymentMethod()))
                .andExpect(jsonPath("$.amount").value(response.amount()))
                .andExpect(jsonPath("$.status").value(response.status().toString()));

        verify(paymentService).createPayment(any(PaymentRequest.class));
    }

    @Test
    @DisplayName("결제 조회 - 성공")
    void getPayment_Success() throws Exception {
        Long paymentId = 1L;
        PaymentResponse response = createPaymentResponse();

        when(paymentService.getPayment(paymentId)).thenReturn(response);

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.orderId").value(response.orderId()))
                .andExpect(jsonPath("$.paymentMethod").value(response.paymentMethod()))
                .andExpect(jsonPath("$.amount").value(response.amount()))
                .andExpect(jsonPath("$.status").value(response.status().toString()));

        verify(paymentService).getPayment(paymentId);
    }

    @Test
    @DisplayName("주문별 결제 조회 - 성공")
    void getPaymentByOrderId_Success() throws Exception {
        Long orderId = 1L;
        PaymentResponse response = createPaymentResponse();

        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/payments/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.orderId").value(response.orderId()))
                .andExpect(jsonPath("$.paymentMethod").value(response.paymentMethod()))
                .andExpect(jsonPath("$.amount").value(response.amount()))
                .andExpect(jsonPath("$.status").value(response.status().toString()));

        verify(paymentService).getPaymentByOrderId(orderId);
    }

    @Test
    @DisplayName("모든 결제 조회 - 성공")
    void getAllPayments_Success() throws Exception {
        List<PaymentResponse> responses = Arrays.asList(
                createPaymentResponse(),
                PaymentResponse.from(Payment.builder()
                        .orderId(2L)
                        .paymentMethod("BANK_TRANSFER")
                        .amount(BigDecimal.valueOf(2000.00))
                        .build())
        );

        when(paymentService.getAllPayments()).thenReturn(responses);

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(responses.get(0).id()))
                .andExpect(jsonPath("$[1].id").value(responses.get(1).id()));

        verify(paymentService).getAllPayments();
    }

    @Test
    @DisplayName("결제 완료 - 성공")
    void completePayment_Success() throws Exception {
        Long paymentId = 1L;
        PaymentResponse completedResponse = PaymentResponse.from(
                Payment.builder()
                        .orderId(1L)
                        .paymentMethod("CREDIT_CARD")
                        .amount(BigDecimal.valueOf(1000.00))
                        .build()
        );

        when(paymentService.completePayment(paymentId)).thenReturn(completedResponse);

        mockMvc.perform(patch("/api/payments/{id}/complete", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(completedResponse.id()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(paymentService).completePayment(paymentId);
    }

    @Test
    @DisplayName("결제 취소 - 성공")
    void cancelPayment_Success() throws Exception {
        Long paymentId = 1L;
        PaymentResponse cancelledResponse = PaymentResponse.from(
                Payment.builder()
                        .orderId(1L)
                        .paymentMethod("CREDIT_CARD")
                        .amount(BigDecimal.valueOf(1000.00))
                        .build()
        );

        when(paymentService.cancelPayment(paymentId)).thenReturn(cancelledResponse);

        mockMvc.perform(patch("/api/payments/{id}/cancel", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cancelledResponse.id()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(paymentService).cancelPayment(paymentId);
    }

    @Test
    @DisplayName("결제 실패 처리 - 성공")
    void failPayment_Success() throws Exception {
        Long paymentId = 1L;
        PaymentResponse failedResponse = PaymentResponse.from(
                Payment.builder()
                        .orderId(1L)
                        .paymentMethod("CREDIT_CARD")
                        .amount(BigDecimal.valueOf(1000.00))
                        .build()
        );

        when(paymentService.failPayment(paymentId)).thenReturn(failedResponse);

        mockMvc.perform(patch("/api/payments/{id}/fail", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(failedResponse.id()))
                .andExpect(jsonPath("$.status").value("FAILED"));

        verify(paymentService).failPayment(paymentId);
    }

    @Test
    @DisplayName("잘못된 요청 데이터로 결제 생성 시 400 에러")
    void createPayment_InvalidRequest_BadRequest() throws Exception {
        PaymentRequest invalidRequest = new PaymentRequest(null, "", BigDecimal.ZERO);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}