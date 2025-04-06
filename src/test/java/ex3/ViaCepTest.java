package ex3;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ViaCepTest {

    ViaCep viaCep = new ViaCep();

    @Test
    void testCepValido() throws Exception {
        String response = viaCep.buscarPorCep("01001000");
        System.out.println(response);
        assertTrue(response.contains("Praça da Sé"));
    }

    @Test
    void testCepInexistente() throws Exception {
        String response = viaCep.buscarPorCep("00000000");
        System.out.println(response);
        assertTrue(response.contains("erro"));
    }

    @Test
    void testCepComLetras() throws Exception {
        String response = viaCep.buscarPorCep("abc12345");
        System.out.println(response);
        assertTrue(response.contains("Http 400"));
    }

    @Test
    void testCepVazio() throws Exception {
        String response = viaCep.buscarPorCep("");
        System.out.println(response);
        assertTrue(response.contains("Http 400"));
    }

    @Test
    void testCepCurto()throws Exception {
        String response = viaCep.buscarPorCep("123");
        System.out.println(response);
        assertTrue(response.contains("Http 400"));
    }

    @Test
    void testEnderecoValido() throws Exception {
        String response = viaCep.buscarPorEndereco("SP", "Sao Paulo", "Avenida Paulista");
        System.out.println(response);
        assertTrue(response.contains("01311-921")); // CEP da Av. Paulista
    }

    @Test
    void testUfInvalida() throws Exception {
        String response = viaCep.buscarPorEndereco("XX", "Sao Paulo", "Avenida Paulista");
        System.out.println(response);
        assertTrue(response.equals("[]\n"));
    }

    @Test
    void testLogradouroInexistente() throws Exception {
        String response = viaCep.buscarPorEndereco("SP", "São Paulo", "RuaQueNaoExiste");
        assertTrue(response.equals("[]\n"));
    }
}
