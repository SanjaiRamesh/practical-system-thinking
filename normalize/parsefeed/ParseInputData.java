package normalize.parsefeed;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParseInputData {


    static class Record {
        private int id = -1;
        private String name;
        private BigDecimal amount;
        private String currency;

//        public Record(int id, String name, BigDecimal amount, String currency) {
//            this.id = id;
//            this.name = name;
//            this.amount = amount;
//            this.currency = currency;
//        }
        public boolean isRecordIsValid() {
            return id != -1 && name != null && amount != null&&currency != null;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        @Override
        public String toString() {
            return "Record{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", amount=" + amount +
                    ", currency='" + currency + '\'' +
                    '}';
        }
    }
    enum ColumnName {
        ID,
        NAME,
        AMOUNT,
        CURRENCY

    }


    public static Number  parser(String input, Locale local){


        try {
            switch (local.getCountry()){
                case "DE"->{
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                    symbols.setGroupingSeparator('.');
                    symbols.setDecimalSeparator(',');

// Pattern for "€ 1.234,56"
                    DecimalFormat df = new DecimalFormat("€ #,##0.00", symbols);
                    return df.parse(input);
                }
                case"US"->{
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(local);
                    return formatter.parse(input);
                }
            }
            return null;
        } catch (ParseException e) {
            Logger.getLogger(ParseInputData.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    private static String cleanStr(String input){



        return input.replace("€", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();
    }

    public static Optional<Record> pasrserOne(String input, String separator){

        Logger logger = Logger.getLogger(ParseInputData.class.getName());
        String[] strs = input.split(separator);
        Record record = new Record();
        for(String s : strs){
            String[] keyValue = s.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            try{
                switch (ColumnName.valueOf(key.toUpperCase())){
                    case ID->record.setId(Integer.parseInt(value));
                    case NAME ->  record.setName(value);
                    case AMOUNT -> record.setAmount(new BigDecimal(value));
                    case CURRENCY -> record.setCurrency(value);
                }
            } catch(Exception e){
                logger.log(Level.WARNING, e.getMessage());
                return Optional.empty();
            }
        }
        return Optional.of(record);

    }
    public static void main(String[] args) {

        List<String> inputs = List.of("currency=USD;id=101;name=Alice;amount=250.75;",
                "name=ramesh;currency=USD;id=101;amount=250.75;",
                "name=sanjai;currency=USD;amount=twenty;",
                "currency=USD;id=101;name=bob;amount=250.75;");

        for (String input : inputs) {
            Optional<Record> record = pasrserOne(input, ";");
            if (record.isEmpty()) {
                System.out.println("invalid record" + input);
            } else {
                System.out.println("is Record is valid: " + record.get().isRecordIsValid());

                System.out.println(record.get());
            }
        }

        System.out.println("USD:"+parser("$1,234.56", Locale.US));
        System.out.println("EUR:"+parser("€ 1.234,56", Locale.GERMANY));

    }
}
