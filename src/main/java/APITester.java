import org.junit.jupiter.api.*;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GETTest {
    static int initial_todo_count;
    static HashMap<String, HashMap<String, Object>> initialState;

    @BeforeAll
    static void setUp(){
        initial_todo_count = Main2.getTodoCount();
        initialState = Main2.getState();
    }

    @Test
    @Tag("score:1")
    @DisplayName("Block constructor test1")
    void getTest1(){

    }
}