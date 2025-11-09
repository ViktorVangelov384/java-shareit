package ru.practicum.shareit.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BaseClientTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final BaseClient baseClient = new BaseClient(restTemplate);

    @Test
    void get_WithoutParameters_ShouldCallMakeAndSendRequest() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.get("/test");

        assertNotNull(result);
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void post_WithBody_ShouldCallMakeAndSendRequest() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.post("/test", "body");

        assertNotNull(result);
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.POST), any(), eq(Object.class));
    }

    @Test
    void patch_WithUserIdAndBody_ShouldCallMakeAndSendRequest() {
        ResponseEntity<Object> responseEntity = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.patch("/test", 1L, "body");

        assertNotNull(result);
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.PATCH), any(), eq(Object.class));
    }

    @Test
    void delete_WithUserId_ShouldCallMakeAndSendRequest() {
        ResponseEntity<Object> responseEntity = ResponseEntity.noContent().build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Object.class)))
                .thenReturn(responseEntity);

        ResponseEntity<Object> result = baseClient.delete("/test", 1L);

        assertNotNull(result);
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.DELETE), any(), eq(Object.class));
    }

    @Test
    void makeAndSendRequest_WithHttpException_ShouldReturnErrorResponse() {
        HttpClientErrorException exception = mock(HttpClientErrorException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn(new byte[0]);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = baseClient.get("/test");

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
}
