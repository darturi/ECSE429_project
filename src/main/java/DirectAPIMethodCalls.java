import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.xml.xpath.XPath;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

class Tests{
    private static int getTodoCount(){
        return when().get("/todos")
                .then().statusCode(200).extract().body().jsonPath().getList("todos").size();
    }

    private static int countOccurrences(String str, String subStr) {
        if (str == null || subStr == null || subStr.isEmpty()) {
            return 0; // Handle edge cases
        }

        int count = 0;
        int index = 0;

        while ((index = str.indexOf(subStr, index)) != -1) {
            count++;
            index += subStr.length(); // Move index forward to continue searching
        }

        return count;
    }
    private static String createExampleTodo(String title, boolean doneStatus, String description){
        int beforeNumberOfTodos = getTodoCount();

        final HashMap<String, Object> commandBody = new HashMap<String, Object>();
        commandBody.put("title", title);
        commandBody.put("doneStatus", doneStatus);
        commandBody.put("description", description);


        final Response body = given().body(commandBody).
                when().post("/todos").
                then().
                statusCode(201).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody = body.jsonPath();

        // Check that the number of todos has increased
        Assertions.assertEquals(getTodoCount(), beforeNumberOfTodos + 1);

        // Check that fields of Todo are as expected
        if ((getTodoCount() == beforeNumberOfTodos + 1) &&
                (responseBody.get("title").equals(title)) &&
                (responseBody.get("doneStatus").equals(String.valueOf(doneStatus))) &&
                (responseBody.get("description").equals(description)))
            return responseBody.get("id");
        else return "-1";
    }

    private static void deleteExampleTodo(String id){
        given().body("").
                when().delete("/todos/" + id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();
    }

    private static boolean checkAddWorked(String id, String expectedTitle, String expectedDoneStatus, String expectedDescription){
        final Response body = given().body("").
                when().get("/todos?id=" + id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody = body.jsonPath();

        List<Object> returnOfGet = responseBody.getList("todos");

        HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

        return (expectedTitle.equals(elementOfReturn.get("title"))) &&
                (expectedDoneStatus.equals(elementOfReturn.get("doneStatus"))) &&
                (expectedDescription.equals(elementOfReturn.get("description")));
    }

    private static boolean checkSystemNetZero(HashMap<String, HashMap<String, Object>> id_list){
        final Response body = given().body("").
                when().get("/todos").
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody = body.jsonPath();

        // System.out.println(responseBody.getList("todos"));

        List<Object> returnOfGet = responseBody.getList("todos");

        for (Object o : returnOfGet){
            HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) o;
            HashMap<String, Object> correspondingEntryOfId = id_list.get(elementOfReturn.get("id"));

            if (
                    !(correspondingEntryOfId.get("title").equals(elementOfReturn.get("title"))) ||
                            !(String.valueOf(correspondingEntryOfId.get("doneStatus")).equals(elementOfReturn.get("doneStatus"))) ||
                            !(correspondingEntryOfId.get("description").equals(elementOfReturn.get("description")))
            ) return false;
        }

        return (id_list.size() == returnOfGet.size());
    }

    private boolean verifyUpdateEfficacy(String id, String newTitle, String newDoneStatus, String newDescription){
        final Response body = given().body("").
                when().get("/todos?id=" + id).
                then().
                statusCode(200).
                contentType(ContentType.JSON).
                and().extract().response();

        JsonPath responseBody = body.jsonPath();

        List<Object> returnOfGet = responseBody.getList("todos");

        if (returnOfGet.isEmpty()) return false;

        HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

        return (newTitle.equals(elementOfReturn.get("title"))) &&
                (newDoneStatus.equals(elementOfReturn.get("doneStatus"))) &&
                (newDescription.equals(elementOfReturn.get("description")));
    }

    @Nested
    class PostTests {
        @BeforeAll
        public static void clearDataFromEnv() {
            RestAssured.baseURI = "http://localhost:4567";

            when().post("/admin/data/thingifier")
                    .then().statusCode(200);

            final JsonPath clearedData = when().get("/todos")
                    .then().statusCode(200).extract().body().jsonPath();

            final int newNumberOfTodos = clearedData.getList("todos").size();

            Assertions.assertEquals(0, newNumberOfTodos);
        }

        // Pure Post Tests
        @Test
        public void putMalformedJSON(){
            int beforeTodoCount = getTodoCount();
            final HashMap<String, String> commandBody = new HashMap<String, String>();
            commandBody.put("title", "TEST MALFORMED");

            final JsonPath body = given().body(commandBody.toString().substring(1)).
                    when().post("/todos").
                    then().
                    statusCode(400).
                    contentType("text/html;charset=utf-8").
                    and().extract().body().jsonPath();

            Assertions.assertEquals(beforeTodoCount, getTodoCount());
        }

        @Test
        public void putMalformedXML(){
            int beforeTodoCount = getTodoCount();
            final JsonPath body = given().body("<todo><title>Hello</title></todo").
                    accept("application/xml").
                    when().post("/todos").
                    then().
                    statusCode(400).
                    contentType("text/html;charset=utf-8").
                    and().extract().body().jsonPath();

            Assertions.assertEquals(beforeTodoCount, getTodoCount());
        }

        @Test
        public void canCreateTodoWithJustTitleField() {
            int beforeNumberOfTodos = getTodoCount();

            final HashMap<String, String> commandBody = new HashMap<String, String>();
            commandBody.put("title", "example title");

            final Response body = given().body(commandBody).
                    when().post("/todos").
                    then().
                    statusCode(201).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody = body.jsonPath();

            // Check that the number of todos has increased
            Assertions.assertEquals(getTodoCount(), beforeNumberOfTodos + 1);

            // Check that fields of Todo are as expected
            Assertions.assertEquals(responseBody.get("title"), "example title");
            Assertions.assertEquals(responseBody.get("doneStatus"), "false");
            Assertions.assertEquals(responseBody.get("description"), "");

            // String errorMessages = (String)body.getList("errorMessages").get(0);
            // Assertions.assertTrue(errorMessages.contains("title : can not be empty"),errorMessages);
        }
        @Test
        public void createWithNoBody(){
            int beforeNumberOfTodos = getTodoCount();

            // final HashMap<String, String> commandBody = new HashMap<String, String>();
            // commandBody.put("title", "example title");

            final JsonPath body = given().body("").
                    when().post("/todos").
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();


            Assertions.assertEquals(getTodoCount(), beforeNumberOfTodos);
            Assertions.assertEquals("title : field is mandatory",
                    body.getList("errorMessages").get(0));
        }

        @Test
        public void createWithEmptyBody(){
            int beforeNumberOfTodos = getTodoCount();

            final HashMap<String, String> commandBody = new HashMap<String, String>();
            // commandBody.put("title", "example title");

            final JsonPath body = given().body(commandBody).
                    when().post("/todos").
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();


            Assertions.assertEquals(getTodoCount(), beforeNumberOfTodos);
            Assertions.assertEquals("title : field is mandatory",
                    body.getList("errorMessages").get(0));
        }

        @Test
        public void canCreateTodoWithAllFields() {
            int beforeNumberOfTodos = getTodoCount();

            final HashMap<String, Object> commandBody = new HashMap<String, Object>();
            commandBody.put("title", "example title");
            commandBody.put("doneStatus", true);
            commandBody.put("description", "example description");


            final Response body = given().body(commandBody).
                    when().post("/todos").
                    then().
                    statusCode(201).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody = body.jsonPath();

            // Check that the number of todos has increased
            Assertions.assertEquals(getTodoCount(), beforeNumberOfTodos + 1);

            // Check that fields of Todo are as expected
            Assertions.assertEquals(responseBody.get("title"), "example title");
            Assertions.assertEquals(responseBody.get("doneStatus"), "true");
            Assertions.assertEquals(responseBody.get("description"), "example description");
        }

        @Test
        public void createTodoWithNoTitle() {
            int beforeTodoCount = getTodoCount();
            final HashMap<String, String> commandBody = new HashMap<String, String>();
            commandBody.put("title", "");

            final JsonPath body = given().body(commandBody).
                    when().post("/todos").
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

            String errorMessages = (String)body.getList("errorMessages").get(0);
            Assertions.assertTrue(errorMessages.contains("title : can not be empty"),errorMessages);
            Assertions.assertEquals(beforeTodoCount, getTodoCount());
        }

        @Test
        public void cannotCreateTodoInvalid_doneStatus() {
            int beforeNumberOfTodos = getTodoCount();

            final HashMap<String, Object> commandBody = new HashMap<String, Object>();
            commandBody.put("title", "example title");
            commandBody.put("doneStatus", "hello");

            final JsonPath body = given().body(commandBody).
                    when().post("/todos").
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();


            Assertions.assertEquals(getTodoCount(), beforeNumberOfTodos);

            Assertions.assertEquals("Failed Validation: doneStatus should be BOOLEAN",
                    body.getList("errorMessages").get(0));
        }

        @Test
        public void overrideWithPostValidId() {
            int beforeAddTodoCount = getTodoCount();

            String newId = createExampleTodo("NewTodo", false, "");
            Assertions.assertTrue(checkAddWorked(newId, "NewTodo", "false", ""));

            int afterAddTodoCount = getTodoCount();
            Assertions.assertEquals(beforeAddTodoCount + 1, afterAddTodoCount);

            HashMap<String, Object> updateBody = new HashMap<>();
            updateBody.put("title", "UpdatedWithPost");
            updateBody.put("doneStatus", true);
            updateBody.put("description", "description example");

            Response body = given().body(updateBody).
                    when().post("/todos/" + newId).
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody = body.jsonPath();

            Assertions.assertEquals(responseBody.get("title"), "UpdatedWithPost");
            Assertions.assertEquals(responseBody.get("doneStatus"), "true");
            Assertions.assertEquals(responseBody.get("description"), "description example");

            int afterTodoPostOverrideCount = getTodoCount();
            Assertions.assertEquals(afterTodoPostOverrideCount, afterAddTodoCount);

            deleteExampleTodo(newId);

            int afterTodoDeleteCount = getTodoCount();
            Assertions.assertEquals(afterTodoPostOverrideCount - 1, afterTodoDeleteCount);
        }

        @Test
        public void overrideWithPostInvalidId() {
            int beforeAddTodoCount = getTodoCount();

            Random rand = new Random(1);
            int rand_int = rand.nextInt(1000);

            HashMap<String, Object> updateBody = new HashMap<>();
            updateBody.put("title", "UpdatedWithPost");
            updateBody.put("doneStatus", true);
            updateBody.put("description", "description example");

            Response body = given().body(updateBody).
                    when().post("/todos/" + rand_int).
                    then().
                    statusCode(404).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody = body.jsonPath();

            Assertions.assertEquals("No such todo entity instance with GUID or ID " + rand_int + " found",
                    responseBody.getList("errorMessages").get(0));

            // Ensure number of todos does not change
            int afterTodoPostOverrideCount = getTodoCount();
            Assertions.assertEquals(afterTodoPostOverrideCount, beforeAddTodoCount);
        }
    }

    @Nested
    class GetTests {
        static HashMap<String, HashMap<String, Object>> id_list = new HashMap<>();

        @BeforeAll
        public static void prepareEnv() {
            RestAssured.baseURI = "http://localhost:4567";

            when().post("/admin/data/thingifier")
                    .then().statusCode(200);

            final JsonPath clearedData = when().get("/todos")
                    .then().statusCode(200).extract().body().jsonPath();

            final int newNumberOfTodos = clearedData.getList("todos").size();

            Assertions.assertEquals(0, newNumberOfTodos);

            HashMap<String, Object> title1_hash = new HashMap<>();
            title1_hash.put("title", "TITLE1");
            title1_hash.put("doneStatus", false);
            title1_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE1", false, "")), title1_hash);

            HashMap<String, Object> title2_hash = new HashMap<>();
            title2_hash.put("title", "TITLE2");
            title2_hash.put("doneStatus", true);
            title2_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE2", true, "")), title2_hash);

            HashMap<String, Object> title3_hash = new HashMap<>();
            title3_hash.put("title", "TITLE3");
            title3_hash.put("doneStatus", false);
            title3_hash.put("description", "description3");
            id_list.put((createExampleTodo("TITLE3", false, "description3")), title3_hash);

            HashMap<String, Object> title4_hash = new HashMap<>();
            title4_hash.put("title", "TITLE4");
            title4_hash.put("doneStatus", true);
            title4_hash.put("description", "description4");
            id_list.put((createExampleTodo("TITLE4", true, "description4")), title4_hash);
        }

        // Get Tests
        @Test
        public void getAllTest() {
            int beforeNumberOfTodos = getTodoCount();

            final Response body = given().body("").
                    when().get("/todos").
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody = body.jsonPath();

            List<Object> returnOfGet = responseBody.getList("todos");

            Assertions.assertEquals(id_list.size(), returnOfGet.size());

            for (Object o : returnOfGet){
                HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) o;
                HashMap<String, Object> correspondingEntryOfId = id_list.get(elementOfReturn.get("id"));
                Assertions.assertEquals(correspondingEntryOfId.get("title"), elementOfReturn.get("title"));
                Assertions.assertEquals(String.valueOf(correspondingEntryOfId.get("doneStatus")), elementOfReturn.get("doneStatus"));
                Assertions.assertEquals(correspondingEntryOfId.get("description"), elementOfReturn.get("description"));
            }

            Assertions.assertEquals(beforeNumberOfTodos, getTodoCount());
        }

        @Test
        public void getByValidIdTest() {
            for (String key : id_list.keySet()){
                HashMap<String, Object> associatedHash = id_list.get(key);

                final Response body = given().body("").
                        when().get("/todos?id=" + key).
                        then().
                        statusCode(200).
                        contentType(ContentType.JSON).
                        and().extract().response();

                JsonPath responseBody = body.jsonPath();

                // System.out.println(responseBody.getList("todos"));

                List<Object> returnOfGet = responseBody.getList("todos");

                Assertions.assertEquals(returnOfGet.size(), 1);

                HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

                Assertions.assertEquals(associatedHash.get("title"), elementOfReturn.get("title"));
                Assertions.assertEquals(String.valueOf(associatedHash.get("doneStatus")), elementOfReturn.get("doneStatus"));
                Assertions.assertEquals(associatedHash.get("description"), elementOfReturn.get("description"));

            }
        }

        @Test
        public void getByInvalidIntegerIdTest() {
            Random rand = new Random(1);
            int rand_int = rand.nextInt(1000);
            while (id_list.containsKey(String.valueOf(rand_int))) rand_int = rand.nextInt(1000);

            final Response body = given().body("").
                    when().get("/todos?id=" + rand_int).
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();


            JsonPath responseBody = body.jsonPath();

            List<Object> returnOfGet = responseBody.getList("todos");

            Assertions.assertEquals(returnOfGet.size(), 0);
        }

        // Should have some sort of error message
        @Test
        public void getByInvalidNonIntegerIdTest() {
            final Response body = given().body("").
                    when().get("/todos?id=" + "Hello").
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();


            JsonPath responseBody = body.jsonPath();

            List<Object> returnOfGet = responseBody.getList("todos");

            Assertions.assertEquals(returnOfGet.size(), 0);
        }

        @Test
        public void getBeforeAndAfterUpdateById() {
            String newId = createExampleTodo("Old Value", false, "test");
            Assertions.assertTrue(checkAddWorked(newId, "Old Value", "false", "test"));

            int preUpdateTodoCount = getTodoCount();

            // Get before update
            Response body = given().body("").
                    when().get("/todos?id=" + newId).
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody = body.jsonPath();

            // System.out.println(responseBody.getList("todos"));

            List<Object> returnOfGet = responseBody.getList("todos");

            Assertions.assertEquals(returnOfGet.size(), 1);

            HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

            Assertions.assertEquals("Old Value", elementOfReturn.get("title"));
            Assertions.assertEquals("false", elementOfReturn.get("doneStatus"));
            Assertions.assertEquals("test", elementOfReturn.get("description"));

            // Perform Valid Update
            HashMap<String, String> updateContents = new HashMap<>();
            updateContents.put("title", "New Value");
            updateContents.put("description", "New Test");


            Response response = given().body(updateContents).
                    when().put("/todos/" + newId).
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody2 = response.jsonPath();

            int postUpdateTodoCount = getTodoCount();
            Assertions.assertEquals(preUpdateTodoCount, postUpdateTodoCount);

            // Get after update
            // Get before update
            body = given().body("").
                    when().get("/todos?id=" + newId).
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();

            responseBody = body.jsonPath();

            // System.out.println(responseBody.getList("todos"));

            returnOfGet = responseBody.getList("todos");

            Assertions.assertEquals(returnOfGet.size(), 1);

            elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

            Assertions.assertEquals("New Value", elementOfReturn.get("title"));
            Assertions.assertEquals("false", elementOfReturn.get("doneStatus"));
            Assertions.assertEquals("New Test", elementOfReturn.get("description"));

            deleteExampleTodo(newId);

            Assertions.assertTrue(checkSystemNetZero(id_list));
        }
    }

    @Nested
    class DeleteTests {
        static HashMap<String, HashMap<String, Object>> id_list = new HashMap<>();

        @BeforeAll
        public static void prepareEnv() {

            // avoid the use of Environment.getEnv("/todos") etc. to keep code a little clearer
            RestAssured.baseURI = "http://localhost:4567";

            when().post("/admin/data/thingifier")
                    .then().statusCode(200);

            final JsonPath clearedData = when().get("/todos")
                    .then().statusCode(200).extract().body().jsonPath();

            final int newNumberOfTodos = clearedData.getList("todos").size();

            Assertions.assertEquals(0, newNumberOfTodos);

            HashMap<String, Object> title1_hash = new HashMap<>();
            title1_hash.put("title", "TITLE1");
            title1_hash.put("doneStatus", false);
            title1_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE1", false, "")), title1_hash);

            HashMap<String, Object> title2_hash = new HashMap<>();
            title2_hash.put("title", "TITLE2");
            title2_hash.put("doneStatus", true);
            title2_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE2", true, "")), title2_hash);

            HashMap<String, Object> title3_hash = new HashMap<>();
            title3_hash.put("title", "TITLE3");
            title3_hash.put("doneStatus", false);
            title3_hash.put("description", "description3");
            id_list.put((createExampleTodo("TITLE3", false, "description3")), title3_hash);

            HashMap<String, Object> title4_hash = new HashMap<>();
            title4_hash.put("title", "TITLE4");
            title4_hash.put("doneStatus", true);
            title4_hash.put("description", "description4");
            id_list.put((createExampleTodo("TITLE4", true, "description4")), title4_hash);
        }

        // Delete Tests
        @Test
        public void deleteByValidId() {
            int todoCountPreAdd = getTodoCount();
            // Create new post to delete
            String newId = createExampleTodo("TO BE DELETED", false, "");

            // Check creation went according to plan
            int todoCountPostAdd = getTodoCount();
            Assertions.assertEquals(todoCountPostAdd, todoCountPreAdd + 1);
            Assertions.assertTrue(checkAddWorked(newId, "TO BE DELETED", "false", ""));

            final Response body = given().body("").
                    when().delete("/todos/" + newId).
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();

            int todoCountPostDelete = getTodoCount();

            Assertions.assertEquals(todoCountPostDelete, todoCountPostAdd - 1);
        }

        // OUTPUT NOT IN JSON
        @Test
        public void deleteNoId() {
            int todoCountPreDelete = getTodoCount();

            final Response body = given().body("").
                    when().delete("/todos").
                    then().
                    statusCode(405).
                    contentType("text/html;charset=utf-8").
                    and().extract().response();

            int todoCountPostDelete = getTodoCount();

            Assertions.assertEquals(todoCountPreDelete, todoCountPostDelete);
        }

        @Test
        public void deleteInvalidIntegerId() {
            Random rand = new Random(1);
            int rand_int = rand.nextInt(1000);
            while (id_list.containsKey(String.valueOf(rand_int))) rand_int = rand.nextInt(1000);

            int todoCountPreDelete = getTodoCount();

            final JsonPath body = given().body("").
                    when().delete("/todos/" + rand_int).
                    then().
                    statusCode(404).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

            Assertions.assertEquals("Could not find any instances with todos/" + rand_int,
                    body.getList("errorMessages").get(0));

            //Assertions.assertEquals("No such todo entity instance with GUID or ID " + rand_int + " found",
            //        body.getList("errorMessages").get(0));

            int todoCountPostDelete = getTodoCount();

            Assertions.assertEquals(todoCountPreDelete, todoCountPostDelete);
        }

        // NOT GOOD BEHAVIOR
        @Test
        public void deleteInvalidNonIntegerId() {
            int todoCountPreDelete = getTodoCount();

            final JsonPath body = given().body("").
                    when().delete("/todos/hello").
                    then().
                    statusCode(404).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

            Assertions.assertEquals("Could not find any instances with todos/hello",
                    body.getList("errorMessages").get(0));

            //Assertions.assertEquals("No such todo entity instance with GUID or ID hello found",
            //        body.getList("errorMessages").get(0));

            int todoCountPostDelete = getTodoCount();

            Assertions.assertEquals(todoCountPreDelete, todoCountPostDelete);
        }

        // OUTPUT NOT IN JSON
        @Test
        public void deleteEmptyId() {
            int todoCountPreDelete = getTodoCount();

            final Response body = given().body("").
                    when().delete("/todos/").
                    then().
                    statusCode(404).
                    contentType("text/html;charset=utf-8").
                    and().extract().response();

            int todoCountPostDelete = getTodoCount();

            Assertions.assertEquals(todoCountPreDelete, todoCountPostDelete);
        }
    }

    @Nested
    class HeadTests {
        static HashMap<String, HashMap<String, Object>> id_list = new HashMap<>();

        @BeforeAll
        public static void prepareEnv() {
            RestAssured.baseURI = "http://localhost:4567";

            when().post("/admin/data/thingifier")
                    .then().statusCode(200);

            final JsonPath clearedData = when().get("/todos")
                    .then().statusCode(200).extract().body().jsonPath();

            final int newNumberOfTodos = clearedData.getList("todos").size();

            Assertions.assertEquals(0, newNumberOfTodos);

            HashMap<String, Object> title1_hash = new HashMap<>();
            title1_hash.put("title", "TITLE1");
            title1_hash.put("doneStatus", false);
            title1_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE1", false, "")), title1_hash);

            HashMap<String, Object> title2_hash = new HashMap<>();
            title2_hash.put("title", "TITLE2");
            title2_hash.put("doneStatus", true);
            title2_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE2", true, "")), title2_hash);

            HashMap<String, Object> title3_hash = new HashMap<>();
            title3_hash.put("title", "TITLE3");
            title3_hash.put("doneStatus", false);
            title3_hash.put("description", "description3");
            id_list.put((createExampleTodo("TITLE3", false, "description3")), title3_hash);

            HashMap<String, Object> title4_hash = new HashMap<>();
            title4_hash.put("title", "TITLE4");
            title4_hash.put("doneStatus", true);
            title4_hash.put("description", "description4");
            id_list.put((createExampleTodo("TITLE4", true, "description4")), title4_hash);
        }

        // Head tests

        // SAME BAD HEAD BEHAVIOR
        @Test
        public void headForValidId() {
            for (String key : id_list.keySet()){
                int beforeNumberOfTodos = getTodoCount();

                HashMap<String, Object> associatedHash = id_list.get(key);

                Headers body = given().body("").
                        accept("application/xml").
                        when().head("/todos?id=" + key).
                        then().
                        statusCode(200).
                        contentType(ContentType.XML).
                        and().extract().headers();

                String responseBody = body.asList().toString();

                // Assertions.assertEquals("", responseBody);
                Assertions.assertTrue(responseBody.contains("Date="));
                Assertions.assertTrue(responseBody.contains("Content-Type=application/xml"));
                Assertions.assertTrue(responseBody.contains("Transfer-Encoding=chunked"));
                Assertions.assertTrue(responseBody.contains("Server"));

                Assertions.assertEquals(beforeNumberOfTodos, getTodoCount());

            }
        }

        // ERROR CODE DISAGREES WITH POSTMAN AND IS INAPPROPRIATE
        @Test
        public void headForInvalidIntegerId() {
            int beforeNumberOfTodos = getTodoCount();

            Random rand = new Random(1);
            int rand_int = rand.nextInt(1000);
            while (id_list.containsKey(String.valueOf(rand_int))) rand_int = rand.nextInt(1000);

            final Response body = given().body("").
                    accept("application/xml").
                    when().head("/todos?id=" + rand_int).
                    then().
                    statusCode(200).
                    contentType(ContentType.XML).
                    and().extract().response();

            String responseBody = body.body().asString();

            Assertions.assertEquals("", responseBody);

            Assertions.assertEquals(beforeNumberOfTodos, getTodoCount());
        }

        // ALSO A BAD RESPONSE
        @Test
        public void headForInvalidNonIntegerId() {
            int beforeNumberOfTodos = getTodoCount();

            final Response body = given().body("").
                    accept("application/xml").
                    when().head("/todos?id=" + "hello").
                    then().
                    statusCode(200).
                    contentType(ContentType.XML).
                    and().extract().response();

            String responseBody = body.body().asString();

            Assertions.assertEquals("", responseBody);

            Assertions.assertEquals(beforeNumberOfTodos, getTodoCount());
        }

        // Head Outputs nothing
        @Test
        public void headAll() {
            int beforeNumberOfTodos = getTodoCount();

            Headers body = given().body("").
                    accept("application/xml").
                    when().head("/todos").
                    then().
                    statusCode(200).
                    contentType(ContentType.XML).
                    and().extract().headers();

            String responseBody = body.asList().toString();

            System.out.println(responseBody);

            // Assertions.assertEquals("", responseBody);

            Assertions.assertTrue(responseBody.contains("Date="));
            Assertions.assertTrue(responseBody.contains("Content-Type=application/xml"));
            Assertions.assertTrue(responseBody.contains("Transfer-Encoding=chunked"));
            Assertions.assertTrue(responseBody.contains("Server"));

            Assertions.assertEquals(beforeNumberOfTodos, getTodoCount());
        }
    }

    @Nested
    class PutTests {
        static HashMap<String, HashMap<String, Object>> id_list = new HashMap<>();

        @BeforeAll
        public static void prepareEnv() {

            // avoid the use of Environment.getEnv("/todos") etc. to keep code a little clearer
            RestAssured.baseURI = "http://localhost:4567";

            when().post("/admin/data/thingifier")
                    .then().statusCode(200);

            final JsonPath clearedData = when().get("/todos")
                    .then().statusCode(200).extract().body().jsonPath();

            final int newNumberOfTodos = clearedData.getList("todos").size();

            Assertions.assertEquals(0, newNumberOfTodos);

            HashMap<String, Object> title1_hash = new HashMap<>();
            title1_hash.put("title", "TITLE1");
            title1_hash.put("doneStatus", false);
            title1_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE1", false, "")), title1_hash);

            HashMap<String, Object> title2_hash = new HashMap<>();
            title2_hash.put("title", "TITLE2");
            title2_hash.put("doneStatus", true);
            title2_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE2", true, "")), title2_hash);

            HashMap<String, Object> title3_hash = new HashMap<>();
            title3_hash.put("title", "TITLE3");
            title3_hash.put("doneStatus", false);
            title3_hash.put("description", "description3");
            id_list.put((createExampleTodo("TITLE3", false, "description3")), title3_hash);

            HashMap<String, Object> title4_hash = new HashMap<>();
            title4_hash.put("title", "TITLE4");
            title4_hash.put("doneStatus", true);
            title4_hash.put("description", "description4");
            id_list.put((createExampleTodo("TITLE4", true, "description4")), title4_hash);
        }

        // Put Tests
        @Test
        public void updateToChangeNonDefaultFieldValue() {
            String newId = createExampleTodo("Old Value", false, "");
            Assertions.assertTrue(checkAddWorked(newId, "Old Value", "false", ""));

            int preUpdateTodoCount = getTodoCount();

            // Perform Valid Update
            HashMap<String, String> updateContents = new HashMap<String, String>();
            updateContents.put("title", "New Value");


            Response response = given().body(updateContents).
                    when().put("/todos/" + newId).
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody = response.jsonPath();

            // Verify response
            Assertions.assertEquals(responseBody.get("title"), "New Value");
            Assertions.assertEquals(responseBody.get("doneStatus"), "false");
            Assertions.assertEquals(responseBody.get("description"), "");

            // Verify in get
            Assertions.assertTrue(verifyUpdateEfficacy(newId, "New Value", "false", ""));

            int postUpdateTodoCount = getTodoCount();
            Assertions.assertEquals(preUpdateTodoCount, postUpdateTodoCount);

            deleteExampleTodo(newId);

            // Check that everything is the same as it was
            Assertions.assertTrue(checkSystemNetZero(id_list));
        }

        @Test
        public void updateTodoWithNoTitle() {
            String newId = createExampleTodo("Old Value", false, "");
            Assertions.assertTrue(checkAddWorked(newId, "Old Value", "false", ""));

            int preUpdateTodoCount = getTodoCount();

            // Perform Valid Update
            HashMap<String, Object> updateContents = new HashMap<>();
            updateContents.put("doneStatus", true);

            final JsonPath body = given().body(updateContents).
                    when().put("/todos/" + newId).
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();


            Assertions.assertEquals(getTodoCount(), preUpdateTodoCount);
            Assertions.assertEquals("title : field is mandatory",
                    body.getList("errorMessages").get(0));

            // Verify in get
            Assertions.assertFalse(verifyUpdateEfficacy(newId, "Old Value", "true", ""));

            deleteExampleTodo(newId);

            // Check that everything is the same as it was
            Assertions.assertTrue(checkSystemNetZero(id_list));
        }

        @Test
        public void updateTodoWithNoBody() {
            String newId = createExampleTodo("Old Value", false, "");
            Assertions.assertTrue(checkAddWorked(newId, "Old Value", "false", ""));

            int preUpdateTodoCount = getTodoCount();

            final JsonPath body = given().body("").
                    when().post("/todos").
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();


            Assertions.assertEquals(getTodoCount(), preUpdateTodoCount);
            Assertions.assertEquals("title : field is mandatory",
                    body.getList("errorMessages").get(0));

            // Verify in get
            Assertions.assertTrue(verifyUpdateEfficacy(newId, "Old Value", "false", ""));

            deleteExampleTodo(newId);

            // Check that everything is the same as it was
            Assertions.assertTrue(checkSystemNetZero(id_list));
        }

        @Test
        public void updateToChangeDefaultFieldValue() {
            String newId = createExampleTodo("Old Value", false, "");
            Assertions.assertTrue(checkAddWorked(newId, "Old Value", "false", ""));

            int preUpdateTodoCount = getTodoCount();

            // Perform Valid Update
            HashMap<String, Object> updateContents = new HashMap<>();
            updateContents.put("title", "New Value");
            updateContents.put("doneStatus", true);


            Response response = given().body(updateContents).
                    when().put("/todos/" + newId).
                    then().
                    statusCode(200).
                    contentType(ContentType.JSON).
                    and().extract().response();

            JsonPath responseBody = response.jsonPath();

            // Verify response
            Assertions.assertEquals(responseBody.get("title"), "New Value");
            Assertions.assertEquals(responseBody.get("doneStatus"), "true");
            Assertions.assertEquals(responseBody.get("description"), "");

            // Verify in get
            Assertions.assertTrue(verifyUpdateEfficacy(newId, "New Value", "true", ""));

            int postUpdateTodoCount = getTodoCount();
            Assertions.assertEquals(preUpdateTodoCount, postUpdateTodoCount);

            deleteExampleTodo(newId);

            // Check that everything is the same as it was
            Assertions.assertTrue(checkSystemNetZero(id_list));
        }

        // OUTPUT DISAGREES WITH POSTMAN
        @Test
        public void updateNonExistentTodo() {
            Random rand = new Random(1);
            int rand_int = rand.nextInt(1000);
            while (id_list.containsKey(String.valueOf(rand_int))) rand_int = rand.nextInt(1000);

            // Valid Update
            HashMap<String, String> updateContents = new HashMap<String, String>();
            updateContents.put("title", "New Value");

            final JsonPath body = given().body(updateContents).
                    when().put("/todos/" + rand_int).
                    then().
                    statusCode(404).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

            Assertions.assertEquals("Invalid GUID for " + rand_int + " entity todo",
                    body.getList("errorMessages").get(0));

            Assertions.assertFalse(verifyUpdateEfficacy(String.valueOf(rand_int), "New Value", "false", ""));

            Assertions.assertTrue(checkSystemNetZero(id_list));
        }

        // Again with the weird output type when there's just the error code and no response
        @Test
        public void updateNoSpecifiedId() {
            HashMap<String, String> updateContents = new HashMap<String, String>();
            updateContents.put("title", "New Value");

            final JsonPath body = given().body(updateContents).
                    when().put("/todos/").
                    then().
                    statusCode(404).
                    contentType("text/html;charset=utf-8").
                    and().extract().body().jsonPath();

            Assertions.assertTrue(checkSystemNetZero(id_list));
        }

        @Test
        public void updateToEmptyTitleString() {
            String newId = createExampleTodo("Old Value", false, "");
            Assertions.assertTrue(checkAddWorked(newId, "Old Value", "false", ""));

            int preUpdateTodoCount = getTodoCount();

            HashMap<String, String> updateContents = new HashMap<String, String>();
            updateContents.put("title", "");

            final JsonPath body = given().body(updateContents).
                    when().put("/todos/" + newId).
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

            int postUpdateTodoCount = getTodoCount();
            Assertions.assertEquals(preUpdateTodoCount, postUpdateTodoCount);
            Assertions.assertEquals("Failed Validation: title : can not be empty",
                    body.getList("errorMessages").get(0));

            Assertions.assertFalse(verifyUpdateEfficacy(newId, "", "false", ""));

            deleteExampleTodo(newId);

            Assertions.assertTrue(checkSystemNetZero(id_list));
        }

        @Test
        public void updateToNonBooleanFor_doneStatus() {
            String newId = createExampleTodo("Old Value", false, "");
            Assertions.assertTrue(checkAddWorked(newId, "Old Value", "false", ""));

            int preUpdateTodoCount = getTodoCount();

            HashMap<String, String> updateContents = new HashMap<String, String>();
            updateContents.put("title", "New Value");
            updateContents.put("doneStatus", "Monkey");

            final JsonPath body = given().body(updateContents).
                    when().put("/todos/" + newId).
                    then().
                    statusCode(400).
                    contentType(ContentType.JSON).
                    and().extract().body().jsonPath();

            int postUpdateTodoCount = getTodoCount();
            Assertions.assertEquals(preUpdateTodoCount, postUpdateTodoCount);
            Assertions.assertEquals("Failed Validation: doneStatus should be BOOLEAN",
                    body.getList("errorMessages").get(0));

            Assertions.assertFalse(verifyUpdateEfficacy(newId, "New Value", "Monkey", ""));

            deleteExampleTodo(newId);

            Assertions.assertTrue(checkSystemNetZero(id_list));
        }
    }

    @Nested
    class MiscTests {
        static HashMap<String, HashMap<String, Object>> id_list = new HashMap<>();

        @BeforeAll
        public static void prepareEnv() {
            RestAssured.baseURI = "http://localhost:4567";

            when().post("/admin/data/thingifier")
                    .then().statusCode(200);

            final JsonPath clearedData = when().get("/todos")
                    .then().statusCode(200).extract().body().jsonPath();

            final int newNumberOfTodos = clearedData.getList("todos").size();

            Assertions.assertEquals(0, newNumberOfTodos);

            HashMap<String, Object> title1_hash = new HashMap<>();
            title1_hash.put("title", "TITLE1");
            title1_hash.put("doneStatus", false);
            title1_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE1", false, "")), title1_hash);

            HashMap<String, Object> title2_hash = new HashMap<>();
            title2_hash.put("title", "TITLE2");
            title2_hash.put("doneStatus", true);
            title2_hash.put("description", "");
            id_list.put((createExampleTodo("TITLE2", true, "")), title2_hash);

            HashMap<String, Object> title3_hash = new HashMap<>();
            title3_hash.put("title", "TITLE3");
            title3_hash.put("doneStatus", false);
            title3_hash.put("description", "description3");
            id_list.put((createExampleTodo("TITLE3", false, "description3")), title3_hash);

            HashMap<String, Object> title4_hash = new HashMap<>();
            title4_hash.put("title", "TITLE4");
            title4_hash.put("doneStatus", true);
            title4_hash.put("description", "description4");
            id_list.put((createExampleTodo("TITLE4", true, "description4")), title4_hash);
        }

        // Nothing is outputted
        @Test
        public void optionsCall(){
            int beforeNumberOfTodos = getTodoCount();

            Response body = given().body("").
                    accept("application/xml").
                    when().options("/todos").
                    then().
                    statusCode(200).
                    contentType("text/html;charset=utf-8").
                    and().extract().response();

            String responseBody = body.body().asString();

            // System.out.println(responseBody);

            Assertions.assertEquals("", responseBody);

            Assertions.assertEquals(beforeNumberOfTodos, getTodoCount());
        }

        @Test
        public void manyTodos(){
            int beforeAdd = getTodoCount();
            String[] createdTodoIds = new String[150];
            for (int i=0; i<150; i++){
                createdTodoIds[i] = createExampleTodo("Many #"+i, false, "");
            }
            Assertions.assertEquals(beforeAdd + 150, getTodoCount());

            for (int i=0; i<150; i++){
                deleteExampleTodo(createdTodoIds[i]);
            }

            Assertions.assertEquals(beforeAdd, getTodoCount());
        }

        @Test
        public void largeTitleInput(){
            String potentialTitle =
                    "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                    + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

            int beforeAdd = getTodoCount();

            String newId = createExampleTodo(potentialTitle, false, "");

            Assertions.assertTrue(checkAddWorked(newId, potentialTitle, "false", ""));

            Assertions.assertEquals(beforeAdd + 1, getTodoCount());

            deleteExampleTodo(newId);

            Assertions.assertEquals(beforeAdd, getTodoCount());
        }

        // XML Confirmation
        @Test
        public void getAllXML(){
            int beforeNumberOfTodos = getTodoCount();

            final Response body = given().body("").
                    accept("application/xml").
                    when().get("/todos").
                    then().
                    statusCode(200).
                    contentType(ContentType.XML).
                    and().extract().response();

            String xmlString = body.body().asString();
            xmlString = xmlString.substring(7, xmlString.length() - 8);

            // System.out.println(xmlString);

            Assertions.assertEquals(beforeNumberOfTodos * 2, countOccurrences(xmlString, "todo"));
            Assertions.assertEquals(beforeNumberOfTodos * 2, countOccurrences(xmlString, "title"));
            Assertions.assertEquals(beforeNumberOfTodos * 2, countOccurrences(xmlString, "description"));
            Assertions.assertEquals(beforeNumberOfTodos * 2, countOccurrences(xmlString, "doneStatus"));

            for (String key : id_list.keySet()){
                String title = (String) id_list.get(key).get("title");
                Assertions.assertTrue(xmlString.contains(title));
            }

            Assertions.assertEquals(getTodoCount(), beforeNumberOfTodos);
        }

        @Test
        public void getByIdXML(){
            for (String key : id_list.keySet()){
                HashMap<String, Object> associatedHash = id_list.get(key);

                final Response body = given().body("").
                        accept("application/xml").
                        when().get("/todos?id=" + key).
                        then().
                        statusCode(200).
                        contentType(ContentType.XML).
                        and().extract().response();

                String xmlString = body.body().asString();
                xmlString = xmlString.substring(7, xmlString.length() - 8);

                // System.out.println(xmlString);

                Assertions.assertEquals(1, countOccurrences(xmlString, "<todo"));
                Assertions.assertEquals(1, countOccurrences(xmlString, "<title"));
                Assertions.assertEquals(1, countOccurrences(xmlString, "<description"));
                Assertions.assertEquals(1, countOccurrences(xmlString, "<doneStatus"));
                Assertions.assertEquals(1, countOccurrences(xmlString, "<id"));

                Assertions.assertEquals(
                        associatedHash.get("title"),
                        xmlString.substring(
                                xmlString.indexOf("<title>") + "<title>".length(),
                                xmlString.indexOf("</title>")
                        )
                );

                Assertions.assertEquals(
                        String.valueOf(associatedHash.get("doneStatus")),
                        xmlString.substring(
                                xmlString.indexOf("<doneStatus>") + "<doneStatus>".length(),
                                xmlString.indexOf("</doneStatus>")
                        )
                );

                Assertions.assertEquals(
                        key,
                        xmlString.substring(
                                xmlString.indexOf("<id>") + "<id>".length(),
                                xmlString.indexOf("</id>")
                        )
                );

                // System.out.println(responseBody.getList("todos"));

                // Assertions.assertEquals(returnOfGet.size(), 1);

                // HashMap<String, Object> elementOfReturn = (HashMap<String, Object>) returnOfGet.get(0);

                // Assertions.assertEquals(associatedHash.get("title"), elementOfReturn.get("title"));
                // Assertions.assertEquals(String.valueOf(associatedHash.get("doneStatus")), elementOfReturn.get("doneStatus"));
                // Assertions.assertEquals(associatedHash.get("description"), elementOfReturn.get("description"));

            }
        }
    }
}
