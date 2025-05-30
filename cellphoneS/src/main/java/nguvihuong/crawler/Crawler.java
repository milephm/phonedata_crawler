package nguvihuong.crawler;

import nguvihuong.model.Product;
import nguvihuong.utils.JSONExporter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

public class Crawler {
    private final WebDriver driver;
    public Crawler(WebDriver driver) {
        this.driver = driver;
    }

    public void crawlData(List<Product> productInfo) {
        // String currentUrl = driver.getCurrentUrl();
        Document doc = Jsoup.parse(driver.getPageSource());

        String savePath = "D:\\study\\hust\\20242\\oop_project\\crawler\\cellphoneS\\src\\main\\resources\\assets\\products";

        ArrayList<Element> products = new ArrayList<>(
                doc.select("div[class*=product-info-container product-item]") /* cellphoneS */
                // doc.select("div[class*=grid-cols-2][class*=gap-2] div[class*=ProductCard_brandCard__VQQT8]") /* fpt */
        );

        // Set<String> crawledProductNames = new HashSet<>();
        String rating = "";
        String ratingCount = "";
        List<String> comments = new ArrayList<>();

        for (Element product : products) {
            String name = "";
            Elements titleElements = product.select("div[class*=product__name]"); /* cellphoneS */
            // Elements titleElements = product.select("h3[class*=ProductCard_cardTitle]"); /* fpt */
            if (!titleElements.isEmpty()) {
                name = titleElements.text().replace(" | Chính hãng VN/A", "");
                name = name.trim();
            }

            // get link
            String link ="";
            Elements linkElements = product.select("a[class*=product__link]");
            if (!linkElements.isEmpty()) {
                link = linkElements.attr("href");
            }

            // get price
            int price = 0;
            try {
                Elements priceElements = product.select("p[class*=product__price--show]"); /* cellphoneS */
                // Elements priceElements = product.select("p[class*=Price_currentPrice]"); /* fpt */
                if (!priceElements.isEmpty()) {
                    String priceText = priceElements.text().replaceAll("[.đ\\s]", "");
                    price = Integer.parseInt(priceText);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing price: " + e.getMessage());
            }

            Map<String, String> description = new LinkedHashMap<>();

            // get image
            Elements img_elements = product.select("div.product__image img.product__img");
            String imgURL = img_elements.attr("src").replace("https://cdn2.cellphones.com.vn/insecure/rs:fill:358:358/q:90/plain/","");

//            String imgName = name.replaceAll("[^a-zA-Z0-9.-]", "_") + ".png";
//            String imgPath = savePath + "/" + imgName;
//            try {
//                downloadImage(imgURL, imgPath);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }

            // add info
            productInfo.add(new Product(name, link, imgURL, price, rating, ratingCount, comments, description));
        }
    }

    public static void showData(List<Product> productInfo){
        for (Product product : productInfo) {
            System.out.println(product.getName() + ": " + product.getPrice() + " | " + product.getLink());
        }
    }

    public static void exportJSON(List<Product> productInfo, String filename) throws IOException {
        List<Object> infoList = new ArrayList<>();
        for (Product info : productInfo) {
            infoList.add(Map.of(
                    "name", info.getName(),
                    "price", info.getPrice()
            ));
        }

        List<String> linkList = new ArrayList<>();
        for (Product info : productInfo) {
            linkList.add(info.getLink());
        }

        JSONExporter jsonExporter = new JSONExporter();
        jsonExporter.writeJSON(productInfo, filename + "_info");
        jsonExporter.writeJSON(linkList, filename + "_links");
    }

    public static void downloadImage(String imgURL, String imgPath) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(imgURL);

        // check if connection was successful
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Failed to download image from " + imgURL + ". Response code: " + responseCode);
            return;
        }

        // download the file
        try (InputStream in = connection.getInputStream();
             OutputStream out = new BufferedOutputStream(new FileOutputStream(imgPath))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private static HttpURLConnection getHttpURLConnection(String imgURL) throws IOException {
        URL url = new URL(imgURL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setRequestProperty("Referer", "https://cellphones.com.vn/");
        connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setInstanceFollowRedirects(true);

        return connection;
    }
}