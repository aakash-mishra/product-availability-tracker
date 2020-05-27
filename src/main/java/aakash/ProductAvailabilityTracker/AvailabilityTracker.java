package aakash.ProductAvailabilityTracker;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class AvailabilityTracker {
    private static final Logger LOG = LogManager.getLogger(AvailabilityTracker.class);

    @Autowired
    JavaMailSender javaMailSender;

    private void sendMail(int iterationNumber, String[] subscribers, String productId, String productName) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(subscribers);
        mailMessage.setSubject("BUY NOW! - " + productName + " Reminder #" + iterationNumber);
        mailMessage.setText("An item that you're tracking - " + productName + " - is now available. Place order ASAP: https:amazon.in/dp/" + productId);
        javaMailSender.send(mailMessage);
    }

    @Scheduled(fixedDelay = 300000)
    public void checkProductStatus() {
        try{
            Path path = Paths.get("src/main/resources/product-subscriber.txt");
            List<String> lines = Files.readAllLines(path);
            for(String line : lines) {
                String[] tokens =line.split(" ");
                String productId = tokens[0];
                String[] subscribers = tokens[1].split(",");
                String url = "https://amazon.in/dp/" + productId;
                Document document = Jsoup.connect(url).get();
                String statusString = document.getElementById("availability").text();
                String productName = document.getElementById("productTitle").text();
                LOG.info("Product name is: " + productName);
                LOG.info("Current Status: " + statusString);
                if(!statusString.contains("unavailable")) {
                    LOG.info(productName + " is now available. Sending mail notifs");
                    int i = 1;
                    while(i <= 3) {
                        sendMail(i, subscribers, productId, productName);
                        i++;
                    }
                }
                else {
                    LOG.debug("Not yet boy..");
                }
            }
        }
        catch(IOException e) {
            LOG.error("Exception caught ", e);
        }
    }
}
