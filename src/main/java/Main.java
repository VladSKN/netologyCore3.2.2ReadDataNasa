import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final CloseableHttpClient httpClient;

    static {
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();
    }

    public static void main(String[] args) throws IOException {
        jsonToObject("https://api.nasa.gov/planetary/apod?api_key=SdF2r9RoBQ1zRNfmyF58kLelb7Xm6KT4qZvsECBB");

    }

    public static void jsonToObject(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(request);
        String responseJSON = EntityUtils.toString(response.getEntity());
        ObjectMapper mapper = new ObjectMapper();
        List<NasaData> list = new ArrayList<>();
        NasaData nasaData = mapper.readValue(responseJSON, NasaData.class);
        list.add(nasaData);
        list.forEach(System.out::println);
        writeFile(nasaData);

    }

    public static void createFile(File file) {
        try {
            if (file.createNewFile()) {
                System.out.println(file + " Файл был создан");
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void writeFile(NasaData nasaData) throws IOException {
        HttpGet request = new HttpGet(nasaData.getUrl());
        CloseableHttpResponse response = httpClient.execute(request);
        String[] mas = nasaData.getUrl().split("/");
        File file = new File(mas[6]);
        createFile(file);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            BufferedImage bufferedImage = ImageIO.read(response.getEntity().getContent());
            ImageIO.write(bufferedImage, "jpg", baos);
            baos.flush();
            ImageIO.write(bufferedImage, "jpg", file);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
