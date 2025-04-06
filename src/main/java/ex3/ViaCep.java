package ex3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ViaCep {
    public String buscarPorCep(String cep) throws Exception {
        String url = "https://viacep.com.br/ws/" + cep + "/json/";
        return fazerRequisicao(url);
    }

    public String buscarPorEndereco(String uf, String cidade, String logradouro) throws Exception {
        String url = "https://viacep.com.br/ws/" +
                uf + "/" +
                cidade.replace(" ", "%20") + "/" +
                logradouro.replace(" ", "%20") + "/json/";
        return fazerRequisicao(url);
    }

    private String fazerRequisicao(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                status >= 200 && status < 400 ? con.getInputStream() : con.getErrorStream()
        ));

        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine).append("\n");
        }

        in.close();
        con.disconnect();

        return content.toString();
    }
}
