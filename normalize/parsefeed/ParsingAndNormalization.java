package normalize.parsefeed;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;

public class ParsingAndNormalization {
    public static void main(String[] args) {
        runParsingExamples();
        System.out.println("\n====================================\n");
        runNormalizationExamples();
    }

    // =========================================================
    // PART 1: Parsing structured input
    // =========================================================

    static class PaymentRecord {
        private final int id;
        private final String name;
        private final BigDecimal amount;
        private final String currency;

        PaymentRecord(int id, String name, BigDecimal amount, String currency) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.currency = currency;
        }

        @Override
        public String toString() {
            return "PaymentRecord{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", amount=" + amount +
                    ", currency='" + currency + '\'' +
                    '}';
        }
    }

    static class ParseResult {
        private final PaymentRecord record;
        private final String error;

        ParseResult(PaymentRecord record, String error) {
            this.record = record;
            this.error = error;
        }

        boolean isSuccess() {
            return record != null;
        }

        PaymentRecord getRecord() {
            return record;
        }

        String getError() {
            return error;
        }
    }

    private static void runParsingExamples() {
        List<String> inputs = List.of(
                // valid line
                "id=101;name=Alice;amount=250.75;currency=USD",

                // missing required field
                "id=102;name=Bob;currency=EUR",

                // invalid number
                "id=103;name=Charlie;amount=abc;currency=USD",

                // duplicate field
                "id=104;name=David;amount=120.00;currency=USD;currency=EUR",

                // empty line
                "",

                // extra unknown field
                "id=105;name=Eva;amount=99.99;currency=GBP;country=UK",

                // spaces around delimiters
                " id = 106 ; name = Frank ; amount = 300.50 ; currency = INR ",

                // malformed key-value pair
                "id=107;name=Grace;amount;currency=USD"
        );

        System.out.println("PARSING STRUCTURED INPUT EXAMPLES:\n");

        for (String input : inputs) {
            ParseResult result = parsePaymentRecord(input);

            System.out.println("Input: " + "\"" + input + "\"");
            if (result.isSuccess()) {
                System.out.println("Result: SUCCESS -> " + result.getRecord());
            } else {
                System.out.println("Result: ERROR   -> " + result.getError());
            }
            System.out.println();
        }
    }

    public static ParseResult parsePaymentRecord(String line) {
        if (line == null || line.isBlank()) {
            return new ParseResult(null, "Empty or blank input");
        }

        Map<String, String> fields = new HashMap<>();
        Set<String> seenKeys = new HashSet<>();

        String[] pairs = line.split(";");

        for (String pair : pairs) {
            String trimmedPair = pair.trim();

            if (trimmedPair.isEmpty()) {
                continue;
            }

            String[] keyValue = trimmedPair.split("=", 2);
            if (keyValue.length != 2) {
                return new ParseResult(null, "Malformed key-value pair: " + trimmedPair);
            }

            String key = keyValue[0].trim().toLowerCase();
            String value = keyValue[1].trim();

            if (key.isEmpty()) {
                return new ParseResult(null, "Empty key found");
            }

            if (value.isEmpty()) {
                return new ParseResult(null, "Empty value for key: " + key);
            }

            if (!seenKeys.add(key)) {
                return new ParseResult(null, "Duplicate field found: " + key);
            }

            fields.put(key, value);
        }

        List<String> requiredFields = List.of("id", "name", "amount", "currency");
        for (String requiredField : requiredFields) {
            if (!fields.containsKey(requiredField)) {
                return new ParseResult(null, "Missing required field: " + requiredField);
            }
        }

        try {
            int id = Integer.parseInt(fields.get("id"));
            String name = fields.get("name");
            BigDecimal amount = new BigDecimal(fields.get("amount"));
            String currency = fields.get("currency").toUpperCase();

            if (amount.signum() < 0) {
                return new ParseResult(null, "Negative amount is not allowed");
            }

            PaymentRecord record = new PaymentRecord(id, name, amount, currency);
            return new ParseResult(record, null);

        } catch (NumberFormatException e) {
            return new ParseResult(null, "Invalid numeric value");
        }
    }

    // =========================================================
    // PART 2: String normalization
    // =========================================================

    private static final Set<String> COMPANY_SUFFIXES = Set.of(
            "inc", "incorporated", "llc", "ltd", "limited", "corp", "corporation", "company"
    );

    private static final Map<String, String> SYNONYM_MAP = Map.of(
            "intl", "international",
            "tech", "technology",
            "svc", "services",
            "svcs", "services"
    );

    private static void runNormalizationExamples() {
        List<String> inputs = Arrays.asList(
                // uppercase/lowercase
                "STRIPE",
                "stripe",

                // punctuation variation
                "Stripe, Inc.",
                "Stripe-Inc",

                // suffix variation
                "Stripe Incorporated",
                "Stripe LLC",

                // extra spaces
                "   Stripe    Inc   ",

                // null/blank
                null,
                "",
                "   ",

                // unicode input
                "Café Ltd",

                // abbreviation/synonym case
                "Acme Intl Tech",
                "Acme International Technology",

                // over-normalization edge case
                "The Gap Inc",
                "Co-op Bank"
        );

        System.out.println("STRING NORMALIZATION EXAMPLES:\n");

        for (String input : inputs) {
            String normalized = normalizeCompanyName(input);
            System.out.println("Input: " + String.valueOf(input));
            System.out.println("Normalized: " + normalized);
            System.out.println();
        }
    }

    public static String normalizeCompanyName(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalized = input.trim().toLowerCase();

        // Normalize unicode accents: café -> cafe
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        // Map punctuation carefully to spaces rather than removing blindly
        normalized = normalized.replaceAll("[^a-z0-9&\\- ]", " ");

        // Normalize "&" to "and"
        normalized = normalized.replace("&", " and ");

        // Collapse multiple spaces
        normalized = normalized.replaceAll("\\s+", " ").trim();

        String[] tokens = normalized.split(" ");
        List<String> resultTokens = new ArrayList<>();

        for (String token : tokens) {
            String mappedToken = SYNONYM_MAP.getOrDefault(token, token);

            // Avoid over-normalization:
            // remove suffixes like "inc", but don't remove meaningful words like "co-op"
            if (!COMPANY_SUFFIXES.contains(mappedToken)) {
                resultTokens.add(mappedToken);
            }
        }

        return String.join(" ", resultTokens);
    }
}
