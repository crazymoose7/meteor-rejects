package anticope.rejects.utils.accounts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

// Thanks Bento
public class PlayerSkinUtils {
    static Gson gsonReader = new Gson();

    public static UUID getUUID(String playerName) {

        try {
            JsonObject jsonObject = gsonReader.fromJson(
                    getURLContent("https://api.mojang.com/users/profiles/minecraft/" + playerName),
                    JsonObject.class);

            String userIdString = jsonObject.get("id").toString().replace("\"", "")
                    .replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

            return UUID.fromString(userIdString);
        } catch (Exception ignored) {
            return UUID.randomUUID();
        }
    }

    public static String getHeadTexture(UUID playerUUID) {
        try {
            JsonObject jsonObject = gsonReader.fromJson(
                    PlayerSkinUtils.getURLContent(
                            "https://sessionserver.mojang.com/session/minecraft/profile/" + playerUUID.toString()),
                    JsonObject.class);

            String decodedTexture = "";

            for (JsonElement element : jsonObject.getAsJsonArray("properties")) {
                JsonObject object = element.getAsJsonObject();

                if (object.has("name") && object.get("name").getAsString().equals("textures")) {
                    decodedTexture = object.get("value").getAsString();
                    break;
                }
            }

            return decodedTexture;
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String getURLContent(String requestedUrl) {
        String returnValue;

        try {
            URL url = new URL(requestedUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            returnValue = br.lines().collect(Collectors.joining());
            br.close();
        } catch (Exception e) {
            returnValue = "";
        }

        return returnValue;
    }

}
