package com.example.weather;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class MyController {

    @GetMapping
    public String homeMethod() {
        return "home";
    }


    @GetMapping("search")
    public String searchMethod(@RequestParam("city") String city, Model model) {

        try {
            String json = getJson(city);
            String[] nameAvrMax = parseJson(json);

            model.addAttribute("name", nameAvrMax[0]);
            model.addAttribute("avr", nameAvrMax[1]);
            model.addAttribute("max", nameAvrMax[2]);
        } catch (Exception e) {
            model.addAttribute("city", "Такой город не был найден");
            return "home";
        }

        return "search";
    }

    private String[] parseJson(String json) {

        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();

        JsonObject city = jo.getAsJsonObject("city");
        String name = city.get("name").getAsString();

        List<JsonObject> list = new ArrayList<>();
        for(JsonElement element : jo.getAsJsonArray("list")) {
            list.add(element.getAsJsonObject());
        }

        double temp = 0;
        int tempMax = 0;
        int count = 0;
        for(JsonObject el : list) {
            if (el.get("dt_txt").getAsString().split(" ")[1].equals("06:00:00")) {
                JsonObject mainObj = el.getAsJsonObject("main");
                temp += mainObj.get("temp").getAsInt();
                count++;
                int temp_max_today = mainObj.get("temp_max").getAsInt();
                if(temp_max_today > tempMax) {
                    tempMax = temp_max_today;
                }
            }
        }
        double avrTemp = temp / count;

        String[] strings = new String[3];
        strings[0] = name;
        strings[1] =  String.valueOf(avrTemp);
        strings[2] =  String.valueOf(tempMax);

        return strings;
    }

    private String getJson(String city) {

        String[] latAndLong = city.trim().split("(\\++|\\s+)");
        String uri;
        if(latAndLong.length > 1) {
            uri = "http://api.openweathermap.org/data/2.5/forecast?lat=" + latAndLong[0] + "&lon=" + latAndLong[1] +
                    "&appid=c8a9f34addb3265f8fffb17c81877267&units=metric";
        } else {
            uri = "http://api.openweathermap.org/data/2.5/forecast?q=" + city +
                    "&appid=c8a9f34addb3265f8fffb17c81877267&units=metric";
        }

        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();

        String json = "";
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            json = response.body();
        } catch (Exception e) {
            return "response error";
        }

        return json;
    }
}
