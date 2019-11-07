package com.jurestanic.quotestore;

public class ApiQuote {

    private Success success;
    private Contents contents;

    public Success getSuccess() {
        return success;
    }

    public Contents getContents() {
        return contents;
    }

    public class Success {
        private String total;
        public String getTotal() {
            return total;
        }
    }

    public class Contents {
        private Quotes[] quotes;
        private String copyright;

        public Quotes[] getQuotes() {
            return quotes;
        }

        public String getCopyright() {
            return copyright;
        }
    }

    public class Quotes {
        private String quote;
        private String author;
        private String length;
        private String[] tags;
        private String category;
        private String title;
        private String date;
        private int id;

        public String getQuote() {
            return quote;
        }

        public String getAuthor() {
            return author;
        }

        public String getLength() {
            return length;
        }

        public String[] getTags() {
            return tags;
        }

        public String getCategory() {
            return category;
        }

        public String getTitle() {
            return title;
        }

        public String getDate() {
            return date;
        }

        public int getId() {
            return id;
        }
    }


}
