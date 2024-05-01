package com.temnenkov.singlefilebot.channel.impl;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.tx.TransactionStore;

public class Tost {
    void test() {
        try (MVStore store = MVStore.open("database.mv")) {

            MVMap<Integer, String> topMovies = store.openMap("imdbTopMovies");
            topMovies.put(1, "The Shawshank Redemption");
            topMovies.put(2, "The Godfather");
            topMovies.put(3, "The Godfather: Part II");
        }
    }
}
