package com.fuzzy;


import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class CrptApi {

    private final static Logger log = Logger.getLogger(CrptApi.class.getName());
    private final ReentrantLock lock = new ReentrantLock();
    private final int requestLimit;
    private final long interval;
    private int count = 0;
    private long lastUpdate = System.currentTimeMillis();

    public static void main(String[] args) {
        String json = """
                {
                  "description": {
                    "participantInn": "string"
                  },
                  "doc_id": "string",
                  "doc_status": "string",
                  "doc_type": "LP_INTRODUCE_GOODS",
                  "importRequest": true,
                  "owner_inn": "string",
                  "participant_inn": "string",
                  "producer_inn": "string",
                  "production_date": "2020-01-23",
                  "production_type": "string",
                  "products": [
                    {
                      "certificate_document": "string",
                      "certificate_document_date": "2020-01-23",
                      "certificate_document_number": "string",
                      "owner_inn": "string",
                      "producer_inn": "string",
                      "production_date": "2020-01-23",
                      "tnved_code": "string",
                      "uit_code": "string",
                      "uitu_code": "string"
                    }
                  ],
                  "reg_date": "2020-01-23",
                  "reg_number": "string"
                }
                """;

        String signature = "sample_signature";

        Gson gson = new Gson();
        Document document = gson.fromJson(json, Document.class);


        CrptApi api = new CrptApi(TimeUnit.SECONDS, 3);
        for (int i = 0; i < 10; i++) {
            api.callApi(document, signature);
        }
    }

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.interval = timeUnit.toMillis(1);
    }

    public void callApi(Document document, String signature) {
        try {
            lock.lock();
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdate > interval) {
                count = 0;
                lastUpdate = currentTime;
            }

            if (count < requestLimit) {
                sendRequest(document, signature);
                count++;
            } else {
                long waitTime = interval - (currentTime - lastUpdate);
                if (waitTime > 0) {
                    Thread.sleep(waitTime);
                }

                lastUpdate = System.currentTimeMillis();
                count = 1;
                sendRequest(document, signature);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }


    private void sendRequest(Document document, String signature) throws IOException {

        URL url = new URL("https://ismp.crpt.ru/api/v3/lk/documents/create");
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String requestBody = "{\"document\": " + new Gson().toJson(document) + ", \"signature\": \"" + signature + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.info("API call successful. Response Code: " + responseCode);
            } else {
                log.warning("API call failed. Response Code: " + responseCode);
            }
        } catch (IOException e) {
            log.warning("Error occurred during API call: " + e.getMessage());
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    public static class Document {

        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;

        public Document() {
        }

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDoc_id() {
            return doc_id;
        }

        public void setDoc_id(String doc_id) {
            this.doc_id = doc_id;
        }

        public String getDoc_status() {
            return doc_status;
        }

        public void setDoc_status(String doc_status) {
            this.doc_status = doc_status;
        }

        public String getDoc_type() {
            return doc_type;
        }

        public void setDoc_type(String doc_type) {
            this.doc_type = doc_type;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getParticipant_inn() {
            return participant_inn;
        }

        public void setParticipant_inn(String participant_inn) {
            this.participant_inn = participant_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getProduction_type() {
            return production_type;
        }

        public void setProduction_type(String production_type) {
            this.production_type = production_type;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public String getReg_date() {
            return reg_date;
        }

        public void setReg_date(String reg_date) {
            this.reg_date = reg_date;
        }

        public String getReg_number() {
            return reg_number;
        }

        public void setReg_number(String reg_number) {
            this.reg_number = reg_number;
        }
    }

    public static class Product {

        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public Product() {
        }

        public String getCertificate_document() {
            return certificate_document;
        }

        public void setCertificate_document(String certificate_document) {
            this.certificate_document = certificate_document;
        }

        public String getCertificate_document_date() {
            return certificate_document_date;
        }

        public void setCertificate_document_date(String certificate_document_date) {
            this.certificate_document_date = certificate_document_date;
        }

        public String getCertificate_document_number() {
            return certificate_document_number;
        }

        public void setCertificate_document_number(String certificate_document_number) {
            this.certificate_document_number = certificate_document_number;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getTnved_code() {
            return tnved_code;
        }

        public void setTnved_code(String tnved_code) {
            this.tnved_code = tnved_code;
        }

        public String getUit_code() {
            return uit_code;
        }

        public void setUit_code(String uit_code) {
            this.uit_code = uit_code;
        }

        public String getUitu_code() {
            return uitu_code;
        }

        public void setUitu_code(String uitu_code) {
            this.uitu_code = uitu_code;
        }
    }

    public static class Description {

        private String participantInn;

        public Description() {
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

    }

}
