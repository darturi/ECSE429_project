import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws IOException {
        // System.out.println(get());

        ArrayList<String[]> body = new ArrayList<>();
        body.add(new String[] {"title", "\"example1 from java\""});
        //body.add(new String[] {"doneStatus", "true"});

        HashMap<String, String> body2 = new HashMap<>();
        body2.put("title", "example with hash");


        System.out.println(hashMapToJSONString(body2));

        System.out.println(post(body2));
    }

    private static String generalCall(String type, String urlString){
        try {
            // Creating a URL object
            URL url = new URL(urlString);

            // Opening a connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Setting the request method to GET
            connection.setRequestMethod(type);

            // Retrieving the response code
            int responseCode = connection.getResponseCode();

            // Processing the response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return "API Response: " + response.toString();
            } else {
                return "API Call Failed. Response Code: " + responseCode;
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString(); // stack trace as a string
        }
    }

    private static String get(){
        return generalCall("GET", "http://localhost:4567/todos");
    }

    private static String get(String id){
        return generalCall("GET", "http://localhost:4567/todos?id=" + id);
    }

    private static String remove(String id){
        return generalCall("REMOVE", "http://localhost:4567/todos/" + id);
    }

    private static String post(HashMap<String, String> body) throws IOException {
        URL url = new URL ("http://localhost:4567/todos");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        String jsonInputString = hashMapToJSONString(body);

        // System.out.println(con.getResponseCode());
        int responseCode = con.getResponseCode();

        if (responseCode >= 200 && responseCode <= 299) {
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return "API Response: " + response.toString();
            }
        } else {
            return "API Call Failed. Response Code: " + responseCode;
        }
    }

    private static String hashMapToJSONString(HashMap<String, String> body){
        JSONObject jsonObject = new JSONObject(body);
        return jsonObject.toString();
    }

    private static String hashListToBodyString(ArrayList<String[]> bodyList){
        StringBuilder sb = new StringBuilder("{");

        for (int i=0; i<bodyList.size(); i++){
            sb.append("\"").append(bodyList.get(i)[0]).append("\" : ").append(bodyList.get(i)[1]);
            if (i != bodyList.size() - 1) sb.append(",\n");

        }
        sb.append("}");
        return sb.toString();
    }






}