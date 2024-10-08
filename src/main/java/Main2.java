import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*
        HashMap<String, HashMap<String, Object>> state = getState();

        // System.out.println(remove("3"));

        for (String key : state.keySet()){
            System.out.println(key);
            System.out.println(state.get(key));
            System.out.println();
        }

        System.out.println("--------------");

        HashMap<String, Object> newItem = new HashMap<>();
        newItem.put("title", "Squirt Butt of the Quarm");
        newItem.put("doneStatus", false);
        post(newItem);

        HashMap<String, Object> newUpdate = new HashMap<>();
        newUpdate.put("title", "Squeek Snack Squiggle Squirm");
        newUpdate.put("doneStatus", true);

        System.out.println(newUpdate);
        System.out.println(put(newUpdate, "4"));

        System.out.println("--------------");

        HashMap<String, HashMap<String, Object>> newState = getState();
        for (String key : newState.keySet()){
            System.out.println(key);
            System.out.println(newState.get(key));
            System.out.println();
        }

        restoreState(state);

        System.out.println("--------------");

        state = getState();
        for (String key : state.keySet()){
            System.out.println(key);
            System.out.println(state.get(key));
            System.out.println();
        }

         */
        //System.out.println(get());
        //System.out.println(get().substring("API Call Success (Response Code 200). API Response: {\"todos\":[".length(), get().length()-2));

        /*

        String trimmedGet = get().substring("API Call Success (Response Code 200). API Response: {\"todos\":[".length(), get().length()-2);
        HashMap<String, Object> tempTest = jsonToHashMap(trimmedGet);
        System.out.println("HERE1" + tempTest);
        tempTest.remove("id");
        System.out.println("HERE2" + tempTest);

        for (String key : tempTest.keySet()) System.out.println("test print" + key + key.getClass() + " -- " + tempTest.get(key) + tempTest.get(key).getClass());


        System.out.println(get("4"));
        HashMap<String, Object> newUpdate = new HashMap<>();
        newUpdate.put("title", "TEST BANDA IDID HJG Squiggle Squirm");
        newUpdate.put("doneStatus", true);
        System.out.println(newUpdate);

        for (String key : newUpdate.keySet()) System.out.println("New print" + key + key.getClass() + " -- " + newUpdate.get(key) + newUpdate.get(key).getClass());

        System.out.println(put(newUpdate, "4"));

        System.out.println("123445" + get("4"));

        System.out.println(put(tempTest, "4"));

        System.out.println(get("4"));


        System.out.println(jsonToHashMap(trimmedGet));

         */
        /*
        HashMap<String, HashMap<String, Object>> state = getState();

        System.out.println("--------");
        for (String key : state.keySet()){
            System.out.println(key);
            System.out.println(state.get(key));
            System.out.println();
        }

        System.out.println("--------------");

        HashMap<String, Object> newItem = new HashMap<>();
        newItem.put("title", "whisper");
        newItem.put("doneStatus", false);
        post(newItem);

        HashMap<String, Object> newUpdate = new HashMap<>();
        newUpdate.put("title", "moan");
        newUpdate.put("doneStatus", true);

        System.out.println(newUpdate);
        System.out.println(put(newUpdate, "4"));

        System.out.println("--------------");

        HashMap<String, HashMap<String, Object>> newState = getState();
        for (String key : newState.keySet()){
            System.out.println(key);
            System.out.println(newState.get(key));
            System.out.println();
        }

        restoreState(state);

        System.out.println("--------------");

        state = getState();
        for (String key : state.keySet()){
            System.out.println(key);
            System.out.println(state.get(key));
            System.out.println();
        }

         */

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

                return "API Call Success (Response Code " + responseCode +"). API Response: " + response.toString();
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

    public static String get(){
        return generalCall("GET", "http://localhost:4567/todos");
    }

    public static String get(String id){
        return generalCall("GET", "http://localhost:4567/todos?id=" + id);
    }

    public static String delete(String id){
        return generalCall("DELETE", "http://localhost:4567/todos/" + id);
    }

    public static String post(HashMap<String, Object> dataAttributes) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(dataAttributes);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create("http://localhost:4567/todos"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        int responseCode = response.statusCode();

        if (responseCode != HttpURLConnection.HTTP_CREATED){
            return "API Call Failed. Response Code: " + responseCode;
        }
        return "API Call Success (Response Code " + responseCode +"). API Response: " + response.toString();
    }

    public static String post(HashMap<String, Object> dataAttributes, String id) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(dataAttributes);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create("http://localhost:4567/todos/" + id))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        int responseCode = response.statusCode();

        if (responseCode != HttpURLConnection.HTTP_CREATED){
            return "API Call Failed. Response Code: " + responseCode;
        }
        return "API Call Success (Response Code " + responseCode +"). API Response: " + response.toString();
    }

    public static String put(HashMap<String, Object> dataAttributes, String id) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(dataAttributes);

        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create("http://localhost:4567/todos/" + id))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        int responseCode = response.statusCode();

        if (responseCode != HttpURLConnection.HTTP_OK){
            return "API Call Failed. Response Code: " + responseCode + " " + response;
        }
        return "(Response Code " + responseCode +") API Response: " + response.toString();
    }

    public static int getTodoCount(){
        return extractEnclosedStrings(get()).size();
    }

    private static List<String> extractEnclosedStrings(String input) {
        input = input.substring("API Call Success (Response Code 200). API Response: {\"todos\":[".length());

        List<String> matches = new ArrayList<>();
        // Updated regex to match text within { } but not break on [ ]
        Pattern pattern = Pattern.compile("\\{([^{}]*|\\{[^}]*\\})*\\}");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            matches.add(matcher.group(0)); // Add the entire match including braces
        }

        return matches;
    }

    public static boolean idIsPresent(String id){
        return get().contains("\"id\":\"" + id + "\"");
    }

    public static boolean entryIsSame(String pastVersion, String id){
        return pastVersion.equals(get(id));
    }

    public static HashMap<String, HashMap<String, Object>> getState(){
        /*
        HashMap<String, HashMap<String, Object>> state = new HashMap<>();

        //System.out.print(get());

        List<String> elements = extractEnclosedStrings(get());

        //elements.set(0, elements.getFirst().strip().substring(1));
        //elements.set(elements.size() - 1, elements.getLast().substring(0, elements.getLast().length() - 1));

        for (String elem : elements){
            elem = elem.substring(1, elem.length() - 1);
            String id_i = "";
            HashMap<String, Object> id_i_Hash = new HashMap<>();
            //System.out.println(elem);

            for (String entry : elem.split(",")){
                //System.out.print("\t");
                //System.out.println(entry);

                int firstColon = entry.indexOf(":");
                String key = entry.substring(0, firstColon);
                switch (key){
                    case "\"id\"" -> id_i = entry.substring(firstColon + 1);
                    case "\"title\"", "\"tasksof\"", "\"categories\"", "\"description\"" -> id_i_Hash.put(key.substring(1, key.length()-1), entry.substring(firstColon + 1 + 1, entry.length() - 1));
                    case "\"doneStatus\"" -> id_i_Hash.put(key.substring(1, key.length()-1), Boolean.parseBoolean(entry.substring(firstColon + 2, entry.length() - 1)));
                }
            }
            state.put(id_i, id_i_Hash);
        }

         */
        String trimmedGet = get().substring("API Call Success (Response Code 200). API Response: {\"todos\":[".length(), get().length()-2);
        HashMap<String, HashMap<String, Object>> state = new HashMap<>();
        for (String s : extractEnclosedStrings(get())) {
            //System.out.println(s);
            HashMap<String, Object> o = jsonToHashMap(s);
            //System.out.println(o);
            //System.out.println();

            state.put((String) o.remove("id"), o);
        }

        //System.out.println("--------");
        //for (String key : state.keySet()){
            //System.out.println(key);
            //System.out.println(state.get(key));
            //System.out.println();
        //}


        return state; //jsonToHashMap(trimmedGet);
    }

    private static HashMap<String, Object> jsonToHashMap(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap resultMap = new HashMap<>();
        try {
            resultMap = objectMapper.readValue(jsonString, HashMap.class);
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions as needed
        }
        resultMap.put("doneStatus", Boolean.parseBoolean((String) resultMap.get("doneStatus")));
        ///System.out.println("HERE --> " + resultMap);
        return resultMap;
    }

    // NOTE: RESTORE STATE WILL NOT RESTORE DELETED NODES
    public static boolean restoreState(HashMap<String, HashMap<String, Object>> prevState) throws IOException, InterruptedException {
        HashMap<String, HashMap<String, Object>> currState = getState();

        for (String key : currState.keySet()){
            if (!prevState.containsKey(key)) {
                // System.out.println("KEY TO BE REMOVED: " + key);
                delete(key);
            }
            else if (!currState.get(key).equals(prevState.get(key))) {
                //System.out.println("--> OLD DICT " + prevState.get(key));
                //System.out.println("--> KEY TO PUT" + key + "  -  " + key);
                //System.out.println("--> PREV ---> " + prevState.get(key));
                //System.out.println("--> CURR ---> " + currState.get(key));
                //System.out.println(put(prevState.get(key), key));
                put(prevState.get(key), key);
            }
        }
        return prevState.isEmpty();
    }
}
