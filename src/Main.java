import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // URL de la API de tasas de cambio
        String apiUrl = "https://api.exchangerate-api.com/v4/latest/USD";

        // Crear cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Crear solicitud GET con HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl)) // Establecer la URI de la API
                .header("Authorization", "dbe0603d0749600f9d53ad58")
                .build(); // Construir la solicitud

        // Bucle para interactuar con el usuario
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            // Mostrar el menú
            System.out.println("\n--- Menú de Conversor de Monedas ---");
            System.out.println("1. Convertir Moneda");
            System.out.println("2. Salir");
            System.out.print("Seleccione una opción: ");
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    // Opción para convertir moneda
                    try {
                        // Enviar la solicitud y recibir la respuesta
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                        // Obtener el código de estado de la respuesta
                        int statusCode = response.statusCode();
                        System.out.println("Código de estado: " + statusCode);

                        // Verificar si el código de estado es 200 (OK)
                        if (statusCode == 200) {
                            // Procesar la respuesta JSON si el código es 200 (OK)
                            String jsonResponse = response.body();

                            // Utilizar Gson para analizar el JSON
                            Gson gson = new Gson();
                            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                            // Extraer el campo "base"
                            String base = jsonObject.get("base").getAsString();
                            System.out.println("Moneda base: " + base);

                            // Extraer las tasas de cambio desde el objeto "rates"
                            JsonObject rates = jsonObject.getAsJsonObject("rates");

                            // Pedir al usuario la cantidad y la moneda de origen
                            System.out.print("Ingrese la cantidad a convertir: ");
                            double amount = scanner.nextDouble();

                            System.out.print("Ingrese la moneda de origen (ejemplo: USD): ");
                            String fromCurrency = scanner.next().toUpperCase();

                            System.out.print("Ingrese la moneda de destino (ejemplo: COP): ");
                            String toCurrency = scanner.next().toUpperCase();

                            // Realizar la conversión
                            double convertedAmount = convertCurrency(amount, fromCurrency, toCurrency, rates);

                            // Mostrar el resultado
                            System.out.printf("%.2f %s es equivalente a %.2f %s\n", amount, fromCurrency,
                                    convertedAmount, toCurrency);
                        } else {
                            // Manejar errores si el código de estado no es 200
                            System.out.println("Error: " + statusCode);
                        }

                    } catch (Exception e) {
                        // Capturar errores en la solicitud HTTP
                        System.out.println("Ocurrió un error durante la solicitud HTTP: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    // Opción para salir
                    System.out.println("¡Gracias por usar el conversor de monedas! Hasta la próxima.");
                    exit = true; // Salir del bucle
                    break;
                default:
                    System.out.println("Opción no válida. Intente nuevamente.");
            }
        }

        scanner.close(); // Cerrar el scanner al final
    }

    // Método para realizar la conversión de moneda
    public static double convertCurrency(double amount, String fromCurrency, String toCurrency, JsonObject rates) {
        // Obtener las tasas de cambio para las monedas origen y destino
        double fromRate = rates.has(fromCurrency) ? rates.get(fromCurrency).getAsDouble() : 1;
        double toRate = rates.has(toCurrency) ? rates.get(toCurrency).getAsDouble() : 1;

        // Si alguna de las monedas no está disponible en la API, devolver -1
        if (fromRate == 0 || toRate == 0) {
            System.out.println("Error: No se pudo encontrar la tasa de cambio para una de las monedas.");
            return -1;
        }

        // Convertir la cantidad
        return amount * (toRate / fromRate);
    }
}